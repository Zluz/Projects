package jmr.rpclient.tiles;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import jmr.data.WeatherSymbol;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.tables.Page;
import jmr.util.transform.DateFormatting;

public class WeatherForecastTile extends TileBase {


	public final static int NUMBER_OF_DAYS = 1;
	
	public final static long MAX_IMPORT_AGE = TimeUnit.HOURS.toMillis( 2 );
	
	@SuppressWarnings("serial")
	final class PageData extends HashMap<String,String> {};

	private Thread threadUpdater;
	
	final private Map<String,EnumMap<Value,String>> mapDays = new HashMap<>();
	final private PageData pagedata = new PageData();
	
	
	
	private void updatePages() {

		final String strPath = "/External/Ingest/Import_WeatherGov/data";
		
		final Map<String, String> mapRaw = Client.get().loadPage( strPath );
		
		final Map<String,EnumMap<Value,String>> mapLoading = new HashMap<>();
		
		for ( int iPeriod = 0; iPeriod<20; iPeriod++ ) {

			boolean bValid = false;
			
			final Map<String,String> mapPeriod = new HashMap<>();
			
			final String strNamePrefix = 
								String.format( "+period_%02d.", iPeriod ); 
			

			final String strUpdateTime = mapRaw.get( "+update_time" );

			for ( final Entry<String, String> entry : mapRaw.entrySet() ) {
				final String strKey = entry.getKey();
				
				if ( strKey.startsWith( strNamePrefix ) ) {
					
					final String strNewName = strKey.substring( 
												strNamePrefix.length() );
					final String strValue = entry.getValue();
					
					mapPeriod.put( strNewName, strValue );
					
					bValid = true;
				}
			}
			
			if ( bValid ) {
				
				final String strTimeStart = mapPeriod.get( "time_start" );
				final String strDate = strTimeStart.substring( 0, 10 );
				
				final EnumMap<Value,String> mapValues;
				if ( mapLoading.containsKey( strDate ) ) {
					mapValues = mapLoading.get( strDate );
				} else {
					mapValues = new EnumMap<>( Value.class );
					mapLoading.put( strDate, mapValues );
				}
				
				final boolean bDaytime = 
								"true".equals( mapPeriod.get( "daytime" ) );
				
				if ( bDaytime ) {
					mapValues.put( Value.UPDATE_TIME, strUpdateTime );
					mapValues.put( Value.DAY_NAME_LONG, 
										mapPeriod.get( "name" ) );
					mapValues.put( Value.FORECAST_DAY, 
										mapPeriod.get( "forecast_short" ) );
					mapValues.put( Value.TEMP_DAY, 
										mapPeriod.get( "temperature" ) );
					mapValues.put( Value.WIND_DIRECTION_DAY, 
										mapPeriod.get( "wind_direction" ) );
					mapValues.put( Value.WIND_SPEED, 
										mapPeriod.get( "wind_speed" ) );
				} else {
					mapValues.put( Value.UPDATE_TIME, strUpdateTime );
					mapValues.put( Value.FORECAST_NIGHT, 
										mapPeriod.get( "forecast_short" ) );
					mapValues.put( Value.TEMP_NIGHT, 
										mapPeriod.get( "temperature" ) );
					mapValues.put( Value.WIND_DIRECTION_NIGHT, 
										mapPeriod.get( "wind_direction" ) );
					
					if ( ! mapValues.containsKey( Value.DAY_NAME_LONG ) ) {
						mapValues.put( Value.DAY_NAME_LONG, 
											mapPeriod.get( "name" ) );
					}
				}
			}
		}
		
		for ( final EnumMap<Value, String> map : mapLoading.values() ) {
			
			final String strDayName = map.get( Value.DAY_NAME_LONG );
			final String strShortDay =
							DateFormatting.getShortDayOfWeek( strDayName );
			if ( ! StringUtils.isBlank( strShortDay ) ) {
				map.put( Value.DAY_NAME_SHORT, strShortDay );
			} else {
				map.put( Value.DAY_NAME_SHORT, strDayName );
			}
			
			final String strWDD = map.get( Value.WIND_DIRECTION_DAY );
			final String strWDN = map.get( Value.WIND_DIRECTION_NIGHT );
			final String strWDC;
			
			final boolean bWDDBlank = StringUtils.isBlank( strWDD ); 
			final boolean bWDNBlank = StringUtils.isBlank( strWDN ); 
			
			if ( ! bWDDBlank ) {
				if ( ! bWDNBlank ) {
					if ( strWDD.equals( strWDN ) ) {
						strWDC = strWDD;
					} else {
						strWDC = strWDD + ", " + strWDN;
					}
				} else {
					strWDC = strWDD;
				}
			} else {
				if ( ! bWDNBlank ) {
					strWDC = strWDN;
				} else {
					strWDC = "-";
				}
			}
			
			map.put( Value.WIND_DIRECTION_COMBINED, strWDC );
		}
		

		synchronized ( mapDays ) {
			mapDays.clear();
			mapDays.putAll( mapLoading );
		}
		synchronized ( pagedata ) {
			pagedata.clear();
			pagedata.putAll( mapRaw );
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

	
	public enum Value {
		DAY_NAME_SHORT,
		DAY_NAME_LONG,
		TEMP_DAY,
		TEMP_NIGHT,
		WIND_SPEED,
		WIND_DIRECTION_DAY,
		WIND_DIRECTION_NIGHT,
		WIND_DIRECTION_COMBINED,
		FORECAST_DAY,
		FORECAST_NIGHT,
		UPDATE_TIME, // repeated with current import. meh.
		;
	}
	
	
	public void paintDay( 	final GC gc,
							final Rectangle rect,
							final EnumMap<Value,String> map,
							final List<String> listWarning ) {
		final int iX = rect.x;
		final int iDayWidth = rect.width;
		final boolean bNarrow = iDayWidth < 140;

		int iY = 2;
		gc.setFont( Theme.get().getFont( 18 ) );
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

		final String strDay;
		if ( bNarrow ) {
			strDay = map.get( Value.DAY_NAME_SHORT );
		} else {
			strDay = map.get( Value.DAY_NAME_LONG );
		}
		
		if ( strDay.length() < 4 ) {
			gc.drawText( strDay, iX + 20, iY );
		} else {
			gc.drawText( strDay, iX + 6, iY );
		}

		iY = 26;
		final String strTextDay = map.get( Value.FORECAST_DAY );
		final String strText; 
		final Image imageIconDay;
		final WeatherSymbol symbolDay;
		if ( ! StringUtils.isBlank( strTextDay ) ) {
			strText = "   " + strTextDay;
			symbolDay = WeatherSymbol.getSymbol( strTextDay );
			imageIconDay = symbolDay.getIcon();
		} else {
			strText = " Night: " + map.get( Value.FORECAST_NIGHT );
			imageIconDay = WeatherSymbol.getIconUnknown();
			symbolDay = null;
		}
		if ( null != imageIconDay && WeatherSymbol.UNKNOWN != symbolDay ) {
			gc.drawImage( imageIconDay, iX + 2 , iY );
			iY = 86;
			gc.setFont( Theme.get().getFont( 10 ) );
		} else {
			iY = 60;
			gc.setFont( Theme.get().getFont( 7 ) );
			listWarning.add( "Not found: " + strTextDay );
//			System.out.println( 
//					"Weather symbol not found for: " + strText );
		}

		if ( iDayWidth > 120 ) {
			final String strNight = map.get( Value.FORECAST_NIGHT );
			final WeatherSymbol symbolNight = 
										WeatherSymbol.getSymbol( strNight );
			final Image imageIconNight = symbolNight.getIcon();
			if ( null != imageIconNight 
							&& WeatherSymbol.UNKNOWN != symbolNight ) {
				gc.drawImage( imageIconNight, iX + 60, 26 );
			} else {
				listWarning.add( "Not found: " + strNight );
			}
		}

		
		if ( gc.textExtent( strText ).x + 10 < iDayWidth ) {
			gc.drawText( strText, iX, iY );
		} else {
			String strAbbr = strText;
			int iWidth;
			do {
				int iLen = strAbbr.length() - 3;
				strAbbr = strAbbr.substring( 0, iLen );
				iWidth = gc.textExtent( strAbbr ).x;
			} while ( iWidth + 10 > iDayWidth );
			gc.drawText( strAbbr + "...", iX, iY );
		}

		iY = 114;
		gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		final String strLow = ""+ map.get( Value.TEMP_NIGHT );
		if ( iDayWidth < 120 ) {
			gc.setFont( Theme.get().getFont( 14 ) );
			gc.drawText( strLow + "°", iX + iDayWidth - 25, 114 );
		} else {
			gc.setFont( Theme.get().getFont( 18 ) );
			gc.drawText( strLow + "°", iX + 80, 110 );
		}
		
		final String strWindRaw = map.get( Value.WIND_SPEED );
		if ( ! StringUtils.isEmpty( strWindRaw ) && iDayWidth > 110 ) {
			
			String strWindAbbr = strWindRaw;
			strWindAbbr = strWindAbbr.replaceAll( " mph", "" );
			strWindAbbr = strWindAbbr.replaceAll( " to ", " - " );

			final String strWindDir = ""+
									map.get( Value.WIND_DIRECTION_COMBINED );

			iY = 16;
//			final int iXW = iX + 68;
			final int iXW = iX + iDayWidth - 40;
			gc.setFont( Theme.get().getFont( 9 ) );
//			gc.drawText( "wind", iXW, iY );			
			gc.setFont( Theme.get().getFont( 12 ) );	iY += 14;
			gc.drawText( strWindAbbr, iXW, iY );	
			gc.setFont( Theme.get().getFont( 9 ) );		iY += 16;
			gc.drawText( "mph", iXW + 8, iY );			
			gc.setFont( Theme.get().getFont( 11 ) );	iY += 17;
			gc.drawText( strWindDir, iXW, iY );
		}
		

		iY = 100;
		gc.setFont( Theme.get().getFont( 26 ) );
		final String strHigh = map.get( Value.TEMP_DAY );
		if ( StringUtils.isNotBlank( strHigh ) ) {
			gc.drawText( strHigh + "°", iX + 14, iY + 1 );
			gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
			gc.drawText( strHigh, iX + 14, iY );
		} else {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
			gc.drawText( "(-)", iX + 14, iY + 1 );
		}
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
	}
	
	
	public List<Integer> partitionWidth( final int iAvailableSpace ) {
		final List<Integer> list = new LinkedList<>();
		int iRemaining = iAvailableSpace;
		// 300, 450, 600, 650, 800
		if ( iAvailableSpace > 600 ) {
			list.add( 170 ); iRemaining -= 170; 
			list.add( 170 ); iRemaining -= 170;
//			list.add( 160 ); iRemaining -= 160;
		}
		final int iNarrowDays = iRemaining / 80;
		final int iWidth = iRemaining / iNarrowDays;
		for ( int iDay = 0; iDay < iNarrowDays - 1; iDay++ ) {
			list.add( iWidth );
			iRemaining -= iWidth;
		}
		list.add( iRemaining );
		return list;
	}
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
		WeatherSymbol.getIconUnknown(); // to initialize this icon

		final Rectangle rectOriginal = gc.getClipping();
		
		final long lTimeNow = System.currentTimeMillis();
		
		synchronized ( mapDays ) {

			final List<String> listWarning = new LinkedList<>();
			
			final String strUpdateTime;
			
			if ( null != mapDays && ! mapDays.isEmpty() ) {
				strUpdateTime = mapDays.values()
								.iterator().next().get( Value.UPDATE_TIME );
				
				final Long lTimeImport = 
								DateFormatting.getDateTime( strUpdateTime );
				if ( null != lTimeImport ) {
					final Long lAgeImport = lTimeNow - lTimeImport;
					if ( lAgeImport > MAX_IMPORT_AGE ) {
						listWarning.add( "Import appears to be outdated: " 
											+ strUpdateTime );
					}
				} else {
					listWarning.add( "Missing update time" );
				}
				
			} else {
				strUpdateTime = "<import not found>";
				listWarning.add( "Weather import data not found" );
			}
			
			final List<String> listDays;
			if ( null != mapDays ) {
				listDays = new LinkedList<>( mapDays.keySet() );
				Collections.sort( listDays );
			} else {
				listDays = Collections.emptyList();
			}

			int iX = rect.x;
			
			final List<Integer> listWidths = 
										partitionWidth( rectOriginal.width );
			
			for ( int iDay = 0; iDay < listWidths.size(); iDay++ ) {
				if ( listDays.size() > iDay && listWidths.size() > iDay ) {
					
					final String strKey = listDays.get( iDay );
					final int iWidth = listWidths.get( iDay );
					
					final EnumMap<Value,String> mapPeriod = mapDays.get( strKey );
					
					final Rectangle rectDay = new Rectangle( 
										iX, 0, iWidth, rectOriginal.height );
					
					gc.setClipping( rectDay );
					
					paintDay( gc, rectDay, mapPeriod, listWarning );
					
					iX += iWidth;
				}
			}
			
			gc.setClipping( rectOriginal );

			if ( ! listWarning.isEmpty() ) {
				gc.setBackground( Theme.get().getColor( Colors.BACK_ALERT ) );
				gc.setFont( Theme.get().getFont( 12 ) );
				gc.drawText( "WARNING", 26, 30 );
				int iY = 46;
				for ( final String strText : listWarning ) {
					gc.drawText( strText, 30, iY );
					iY += 16;
				}
			}
			
			if ( null != pagedata ) {
				gc.setFont( Theme.get().getFont( 7 ) );
				
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
				final String strText = "page " + strSeqPage + strTab 
										+ strElapsed + strTab 
										+ "Update time: " + strUpdateTime;
				
				final int iXLen = gc.textExtent( strText ).x + 20;
				gc.drawText( strText, rect.width - iXLen, rect.height - 10 );
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
