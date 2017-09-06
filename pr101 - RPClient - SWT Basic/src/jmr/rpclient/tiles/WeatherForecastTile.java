package jmr.rpclient.tiles;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.data.WeatherSymbol;
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
		
						Thread.sleep( TimeUnit.MINUTES.toMillis( 10 ) );
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

			for ( int iDay = 0; iDay<NUMBER_OF_DAYS; iDay++ ) {
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
										/ NUMBER_OF_DAYS + 15;
					
					final String strRange = 
//							map.get("low") + "-" + map.get("high");
							em.get( Values.LOW ) + "-" + em.get( Values.HIGH );
					
					gc.setFont( Theme.get().getFont( 18 ) );
//					gc.drawText( map.get("day"), iX + 18, 0 );
					gc.drawText( em.get( Values.DAY ), iX + 18, 0 );
					gc.drawText( strRange, iX + 10, 100 );
					
//					final String strText = "  "+map.get("text");
					final String strText = "  "+em.get( Values.TEXT );
					gc.setFont( Theme.get().getFont( 10 ) );
					gc.drawText( strText, iX, 82 );
					
					final WeatherSymbol symbol = WeatherSymbol.getSymbol( strText );
					final Image imageIcon = symbol.getIcon();
					if ( null!=imageIcon ) {
						gc.drawImage( imageIcon, iX + 5 , 22 );
					}
				}
			}
			
			if ( null!=strWeatherImport ) {
				gc.setFont( Theme.get().getFont( 6 ) );
				
				final PageData pagedata = pages[ NUMBER_OF_DAYS ];
				final String strTitle = pagedata.get( "title" );
				final String strSeqPage = pagedata.get( Page.ATTR_SEQ_PAGE );
				
				final String strTime = pagedata.get( "timestamp" );
				final long lTime = Long.parseLong( strTime );
				final long lElapsed = System.currentTimeMillis() - lTime;
				final long lMinutes = TimeUnit.MILLISECONDS.toMinutes( lElapsed );
				final String strElapsed = "" + lMinutes + " minutes old";
				
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
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static void main( final String[] args ) {
		final long lDays = TimeUnit.SECONDS.toDays( 2147483647 );
		System.out.println( "days: " + lDays );
	}
	
}
