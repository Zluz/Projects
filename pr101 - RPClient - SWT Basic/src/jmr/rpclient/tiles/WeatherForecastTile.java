package jmr.rpclient.tiles;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.data.WeatherSymbol;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.tables.Page;

public class WeatherForecastTile extends TileBase {


	public final static int NUMBER_OF_DAYS = 9;
	
	@SuppressWarnings("serial")
	final class PageData extends HashMap<String,String> {};

//	final Map<String,String>[] maps = new Map<String,String>[] { null, null };
	
	final PageData[] pages = new PageData[ NUMBER_OF_DAYS + 1 ];
//	private Date dateYahooUpdate = null;
	private String strWeatherImport = null;
	private Thread threadUpdater;
	
	
	private void updatePages() {
		synchronized ( pages ) { 

			for ( int iDay = 0; iDay<NUMBER_OF_DAYS; iDay++ ) {
				
				final String strPath = "/External/Ingest/Weather_Forecast_Yahoo"
						+ "/data/query/results/channel/item/forecast/0" + iDay;
				
				final Map<String, String> map = 
							Client.get().loadPage( strPath );
				
				if ( null==pages[ iDay ] ) {
					pages[ iDay ] = new PageData();
				}
				pages[ iDay ].clear();
				pages[ iDay ].putAll( map );
				
				strWeatherImport = map.get( Page.ATTR_LAST_MODIFIED );
			}
			
			final String strPath = "/External/Ingest/Weather_Forecast_Yahoo";
			
			final Map<String, String> map = 
						Client.get().loadPage( strPath );
			
			if ( null==pages[ NUMBER_OF_DAYS ] ) {
				pages[ NUMBER_OF_DAYS ] = new PageData();
			}
			pages[ NUMBER_OF_DAYS ].clear();
			pages[ NUMBER_OF_DAYS ].putAll( map );
		}
	}
	
	
	public WeatherForecastTile() {
		threadUpdater = new Thread( "WeatherForecastTile Updater" ) {
			@Override
			public void run() {
				try {
		
					Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
		
					for (;;) {
						
						try {
							updatePages();
						} catch ( final Exception e ) {
							// ignore.. 
							// JDBC connection may have been dropped..
						}
		
						Thread.sleep( TimeUnit.MINUTES.toMillis( 2 ) );
		//						Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
//		threadUpdater.start();
	}

	public enum Values {
		LOW,
		HIGH,
		DAY,
		TEXT
	}
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
		synchronized ( pages ) {

			final EnumMap< Values, String > 
					em = new EnumMap< WeatherForecastTile.Values, String >( 
								Values.class );

			// 5 tiles (750) = 9 days
			// 3 tiles (450) = 5 days 
			final int iTileWidth = rect.width / 150;
			final int iNumberOfDays = 
//					( rect.width >= 750 ) ? NUMBER_OF_DAYS : 5;
					0 + (int)Math.floor( iTileWidth * 8 / 5 );
			
			for ( int iDay = 0; iDay<iNumberOfDays; iDay++ ) {
				em.clear();
				
				final Map<String, String> map = pages[ iDay ];
				
				boolean bValid = true;
				
				if ( null!=map && !map.isEmpty() ) {
					
					for ( final Values value : Values.values() ) {
						final String strKey = value.name().toLowerCase();
						final String strValue = map.get( strKey );
						if ( null!=strValue ) {
							em.put( value, strValue);
						} else {
							bValid = false;
						}
					}
				} else {
					bValid = false;
				}
				
				if ( bValid ) {
					
					final int iX = ( rect.width - 20 ) * iDay 
										/ iNumberOfDays + 15;
					int iY = 2;
					gc.setFont( Theme.get().getFont( 18 ) );
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
					gc.drawText( em.get( Values.DAY ), iX + 20, iY );

					iY = 26;
					final String strText = "  "+em.get( Values.TEXT );
					final WeatherSymbol symbol = WeatherSymbol.getSymbol( strText );
					final Image imageIcon = symbol.getIcon();
					if ( null!=imageIcon ) {
						gc.drawImage( imageIcon, iX + 5 , iY );
					}

					iY = 86;
					gc.setFont( Theme.get().getFont( 10 ) );
					gc.drawText( strText, iX, iY );

					iY = 118;
					gc.setFont( Theme.get().getFont( 10 ) );
					gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
					gc.drawText( em.get( Values.LOW ) + "°", iX + 10, iY );

					iY = 100;
					gc.setFont( Theme.get().getFont( 24 ) );
					gc.drawText( em.get( Values.HIGH ) + "°", iX + 35, iY );
					gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
					gc.drawText( em.get( Values.HIGH ), iX + 34, iY );
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				}
			}
			
			if ( null!=strWeatherImport ) {
				gc.setFont( Theme.get().getFont( 6 ) );
				
				final PageData pagedata = pages[ iNumberOfDays ];
				final String strTitle = pagedata.get( "title" );
				final String strSeqPage = pagedata.get( Page.ATTR_SEQ_PAGE );
				
				final String strTime = pagedata.get( ".last_modified_uxt" );
				final String strElapsed;
				if ( null!=strTime ) {
					final long lTime = Long.parseLong( strTime );
					final long lElapsed = System.currentTimeMillis() - lTime;
					final long lMinutes = TimeUnit.MILLISECONDS.toMinutes( lElapsed );
					strElapsed = "" + lMinutes + " minutes old";
				} else {
					strElapsed = "(age: unknown)";
				}
				
				final String strTab = "     ";
				final String strText = strTitle + strTab 
											+ "page " + strSeqPage + strTab 
											+ strElapsed + strTab 
											+ strWeatherImport;
				
				final int iXLen = gc.textExtent( strText ).x + 20;
				gc.drawText( strText, rect.width - iXLen, rect.height - 10 );

//				final String strURL = pages[ NUMBER_OF_DAYS ].get( "URL" );
//				final String strURL = pages[ NUMBER_OF_DAYS ].get( "title" );
//				gc.drawText( strURL, 50, rect.height - 10 );
			}
		}
		
	}
	
	@Override
	protected void activateButton( final int iIndex ) {}
	



	
	public static void main( final String[] args ) {
		final long lDays = TimeUnit.SECONDS.toDays( 2147483647 );
		System.out.println( "days: " + lDays );
	}
	
}
