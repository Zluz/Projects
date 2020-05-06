package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.data.WeatherSymbol;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.tables.Page;
import jmr.util.transform.DateFormatting;

public class WeatherForecastTile extends TileBase {


	public final static int NUMBER_OF_DAYS = 1;
	
	@SuppressWarnings("serial")
	final class PageData extends HashMap<String,String> {};

//	final Map<String,String>[] maps = new Map<String,String>[] { null, null };
	
	final PageData[] pages = new PageData[ NUMBER_OF_DAYS + 1 ];
//	private Date dateYahooUpdate = null;
	private String strWeatherImport = null;
	private Thread threadUpdater;
	
	
	private void updatePages() {
		final PageData[] newpages = new PageData[ NUMBER_OF_DAYS + 1 ];

		final String strPath = "/External/Ingest/Import_WeatherGov/data";
		
		final Map<String, String> map = Client.get().loadPage( strPath );

		newpages[ 0 ] = new PageData();
		newpages[ 0 ].putAll( map );

		synchronized ( pages ) {
			pages[ 0 ] = newpages[ 0 ];
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

//	public enum Values {
//		LOW,
//		HIGH,
//		DAY,
//		TEXT
//	}
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
		synchronized ( pages ) {

//			final EnumMap< Values, String > 
//					em = new EnumMap< WeatherForecastTile.Values, String >( 
//								Values.class );

			// 5 tiles (750) = 9 days
			// 3 tiles (450) = 5 days 
			final int iTileWidth = rect.width / 150;
			final int iNumberOfDays = 
//					( rect.width >= 750 ) ? NUMBER_OF_DAYS : 5;
					0 + (int)Math.floor( iTileWidth * 8 / 5 );
			
			final List<String> listNotFound = new LinkedList<>();
			
			int iDayIndex = 0;
			
			for ( int iPeriod = 0; iPeriod<20; iPeriod++ ) {
//			{
//				final int iDay = 0;
				
//				em.clear();
				
				final Map<String,String> mapAll = pages[ 0 ];
				final Map<String,String> mapPeriod = new HashMap<>();
				
				boolean bValid = false;

				//TODO inefficient; rework

				if ( null!=mapAll && !mapAll.isEmpty() ) {

					final String strNamePrefix = 
							String.format( "+period_%02d.", iPeriod ); 

					for ( final Entry<String, String> 
												entry : mapAll.entrySet() ) {
						final String strKey = entry.getKey();
						
						if ( strKey.startsWith( strNamePrefix ) ) {
							
							final String strNewName = strKey.substring( 
											strNamePrefix.length() );
							final String strValue = entry.getValue();
							
							mapPeriod.put( strNewName, strValue );
							bValid = true;
						}
					}
					
					if ( bValid && mapPeriod.containsKey( "daytime" ) ) {
						if ( "false".equals( mapPeriod.get( "daytime" ) ) ) {
							bValid = false;
						}
					}
					
				} else {
					bValid = false;
				}
				
				final int iDayWidth = ( rect.width - 20 ) / iNumberOfDays + 15;
				
				if ( bValid ) {
					
					final int iX = ( rect.width - 20 ) * iDayIndex 
										/ iNumberOfDays + 15;
					int iY = 2;
					gc.setFont( Theme.get().getFont( 18 ) );
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

					//gc.drawText( em.get( Values.DAY ), iX + 20, iY );
					final String strDay = DateFormatting.getShortDayOfWeek( 
											mapPeriod.get( "name" ) );
					
					gc.drawText( strDay, iX + 20, iY );

					iY = 26;
//					final String strText = "  "+em.get( Values.TEXT );
					final String strText = "  " + mapPeriod.get( "forecast_short" );
					final WeatherSymbol symbol = WeatherSymbol.getSymbol( strText );
					final Image imageIcon = symbol.getIcon();
					if ( null!=imageIcon ) {
						gc.drawImage( imageIcon, iX + 5 , iY );
						iY = 86;
						gc.setFont( Theme.get().getFont( 10 ) );
					} else {
						iY = 60;
						gc.setFont( Theme.get().getFont( 7 ) );
						listNotFound.add( strText );
//						System.out.println( 
//								"Weather symbol not found for: " + strText );
					}
					if ( gc.textExtent( strText ).x < iDayWidth ) {
						gc.drawText( strText, iX, iY );
					} else {
						gc.drawText( 
								StringUtils.abbreviate( strText, 16 ), iX, iY );
					}

					iY = 118;
					gc.setFont( Theme.get().getFont( 10 ) );
					gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
//					gc.drawText( em.get( Values.LOW ) + "°", iX + 10, iY );
					gc.drawText( "??" + "°", iX + 10, iY );

					iY = 100;
					gc.setFont( Theme.get().getFont( 24 ) );
//					final String strHigh = em.get( Values.HIGH );
					final String strHigh = ""+ mapPeriod.get( "temperature" );
					gc.drawText( strHigh + "°", iX + 35, iY );
					gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
					gc.drawText( strHigh, iX + 34, iY );
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

				
					iDayIndex++;
				}
			}
			
			if ( ! listNotFound.isEmpty() ) {
				gc.setBackground( Theme.get().getColor( Colors.BACK_ALERT ) );
				gc.setFont( Theme.get().getFont( 12 ) );
				gc.drawText( "Forecast text not matched:", 26, 30 );
				int iY = 46;
				for ( final String strText : listNotFound ) {
					gc.drawText( strText, 30, iY );
					iY += 16;
				}
			}
			
//			if ( null!=strWeatherImport && null!=pages[ iNumberOfDays ] ) {
			if ( null != pages[ 0 ] ) {
				gc.setFont( Theme.get().getFont( 7 ) );
				
//				final PageData pagedata = pages[ iNumberOfDays ];
				final PageData pagedata = pages[ 0 ];
				
//				final String strTitle = pagedata.get( "title" );
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
				final String strText = 
//											strTitle + strTab 
											"page " + strSeqPage + strTab 
											+ strElapsed; 
//											+ strTab 
//											+ strWeatherImport;
				
				final int iXLen = gc.textExtent( strText ).x + 20;
				gc.drawText( strText, rect.width - iXLen, rect.height - 10 );

//				final String strURL = pages[ NUMBER_OF_DAYS ].get( "URL" );
//				final String strURL = pages[ NUMBER_OF_DAYS ].get( "title" );
//				gc.drawText( strURL, 50, rect.height - 10 );
			}
		}
		
	}
	
	@Override
	protected void activateButton( final S2Button button ) {}
	



	
	public static void main( final String[] args ) {
		final long lDays = TimeUnit.SECONDS.toDays( 2147483647 );
		System.out.println( "days: " + lDays );
	}
	
}
