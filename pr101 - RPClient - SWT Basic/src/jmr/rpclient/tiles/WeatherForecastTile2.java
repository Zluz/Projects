package jmr.rpclient.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.fasterxml.jackson.databind.JsonNode;

import jmr.S2Properties;
import jmr.data.WeatherSymbol;
import jmr.pr110.ToDo;
import jmr.pr151.S2ES;
import jmr.rpclient.ModalMessage;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.util.transform.DateFormatting;
import jmr.util.transform.JsonUtils;
import jmr.util.transform.Temperature;

public class WeatherForecastTile2 extends TileBase {

	public final static int NUMBER_OF_DAYS = 1;
	
	public final static long MAX_IMPORT_AGE = TimeUnit.HOURS.toMillis( 8 );
	
	@SuppressWarnings("serial")
	final class PageData extends HashMap<String,String> {};

	private Thread threadUpdater;
	
//	final private Map<String,EnumMap<Value,String>> mapDays = new HashMap<>();
//	final private PageData pagedata = new PageData();

//	final private S2ES client; // = new S2ES();
//	final private S2ES client = S2ES.get();
	private JsonNode jnCurrent = null;
	private Map<String,JsonNode[]> mapCurrent = null;
	private List<String> listKeysCurrent = null;

	
	
	
	public WeatherForecastTile2() {
//		final S2Properties properties = S2Properties.get();
//		final String strESKey = "home.elasticsearch.url";
//		final String strESBase = properties.getProperty( strESKey );
//		client = new S2ES( strESBase );
//		client = new S2ES();
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

	
//	public enum Value {
//		DAY_NAME_SHORT,
//		DAY_NAME_LONG,
//		TEMP_DAY,
//		TEMP_NIGHT,
//		WIND_SPEED,
//		WIND_DIRECTION_DAY,
//		WIND_DIRECTION_NIGHT,
//		WIND_DIRECTION_COMBINED,
//		FORECAST_DAY,
//		FORECAST_NIGHT,
//		UPDATE_TIME, // repeated with current import. meh.
//		;
//	}

	private void updatePages() {
		final JsonNode jnNew = S2ES.get().retrieveLatestWeatherForecast();
		if ( null != jnNew && jnNew.has( "periods" ) ) {
			
			final Map<String,JsonNode[]> mapNew = new HashMap<>();
			
			jnNew.at( "/periods" ).spliterator().forEachRemaining( jn-> {
				final String strTime = jn.at( "/startTime" ).asText();
				if ( strTime.contains( "T" ) ) {
					
					final String strKey = strTime.split( "T" )[0];
					final JsonNode[] arr;
					if ( ! mapNew.containsKey( strKey ) ) {
						arr = new JsonNode[]{ null, null };
						mapNew.put( strKey, arr );
					} else {
						arr = mapNew.get( strKey );
					}
					
					if ( jn.at( "/isDaytime" ).asBoolean() ) {
						arr[ 0 ] = jn;
					} else {
						arr[ 1 ] = jn;
					}
				}
			});

			final List<String> listKeysNew = new ArrayList<>( mapNew.keySet() );
			Collections.sort( listKeysNew );
			
			for ( final JsonNode[] arr : mapNew.values() ) {
				if ( null == arr[0] ) arr[0] = S2ES.MAPPER.createObjectNode();
				if ( null == arr[1] ) arr[1] = S2ES.MAPPER.createObjectNode();
			}
			
//			final List<JsonNode> list = 
//					Arrays.asList( jnNew.at( "/periods" ).elements() );
//			for ( final JsonNode jn : jnNew.at( "/periods" ). ) {
//				
//			}
			
			this.jnCurrent = jnNew;
			this.mapCurrent = mapNew;
			this.listKeysCurrent = listKeysNew;
		}
	}

	
	public void paintDay( 	final GC gc,
							final Rectangle rect,
//							final EnumMap<Value,String> map,
//							final JsonNode jn,
							final JsonNode[] arr,
							final List<String> listWarning ) {
		final int iX = rect.x;
		final int iDayWidth = rect.width;
		final boolean bNarrow = iDayWidth < 140;

		int iY = 2;
		gc.setFont( Theme.get().getFont( 18 ) );
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

		final String strDay;
//		final String strDayLong0 = arr[0].at( "/name" ).asText();
//		final String strDayLong = StringUtils.isEmpty( strDayLong0 ) 
//				? arr[1].at( "/name" ).asText() : strDayLong0;
		final String strDayLong = getValueDayFirst( arr, "/name" );
		if ( bNarrow ) {
//			strDay = map.get( Value.DAY_NAME_SHORT );
			strDay = DateFormatting.getShortDayOfWeek( strDayLong );
		} else {
//			strDay = map.get( Value.DAY_NAME_LONG );
			strDay = strDayLong;
		}
		
		if ( StringUtils.isEmpty( strDay ) ) {
			gc.setFont( Theme.get().getFont( 10 ) );
			gc.drawText( strDayLong, iX + 2, iY + 8 );
			gc.setFont( Theme.get().getFont( 18 ) );
		} else if ( strDay.length() < 4 ) {
			gc.drawText( strDay, iX + 20, iY );
		} else {
			gc.drawText( strDay, iX + 6, iY );
		}
		
		iY = 26;
//		final String strTextDay = map.get( Value.FORECAST_DAY );
		final String strTextDay = arr[0].at( "/shortForecast" ).asText();
		final String strIconDay = arr[0].at( "/icon" ).asText();
		final String strTextNight = arr[1].at( "/shortForecast" ).asText();
		final String strIconNight = arr[1].at( "/icon" ).asText();
		
		final boolean bNoDay = StringUtils.isEmpty( strIconDay );
		
		final String strText; 
		final Image imageIconDay;
		final WeatherSymbol symbolDay;
		if ( ! StringUtils.isBlank( strTextDay ) ) {
			strText = "   " + strTextDay;
//			symbolDay = WeatherSymbol.getSymbol( strTextDay, strIconDay );
			symbolDay = WeatherSymbol.getSymbol( null, strIconDay );
			imageIconDay = symbolDay.getIcon();
		} else {
//			final String strTextNight = arr[1].at( "/shortForecast" ).asText();
//			strText = " Night: " + map.get( Value.FORECAST_NIGHT );
			strText = " Night: " + strTextNight;
			imageIconDay = WeatherSymbol.getIconUnknown();
			symbolDay = null;
		}

		if ( bNoDay ) {
			iY = 86;
			// skip for now
		} else if ( null != imageIconDay && WeatherSymbol.UNKNOWN != symbolDay ) {
			gc.drawImage( imageIconDay, iX + 2 , iY );
			iY = 86;
//			gc.setFont( Theme.get().getFont( 10 ) );
			
			if ( symbolDay.isOutdated() ) {
				ToDo.add( "Using outdated weather icon "
						+ "(" + symbolDay.name() + ") for: " + strIconDay );
			}
			
		} else {
			iY = 60;
//			gc.setFont( Theme.get().getFont( 7 ) );
			listWarning.add( "Not found (text): " + strTextDay );
			listWarning.add( "Not found (icon): " + strIconDay );
			ToDo.add( "Weather icon not found for: " + strIconDay );
//			System.out.println( 
//					"Weather symbol not found for: " + strText );
		}
		gc.setFont( Theme.get().getFont( 10 ) );

		final WeatherSymbol symbolNight;
		if ( iDayWidth > 120 && StringUtils.isNotBlank( strTextNight ) ) {
//			final String strNight = map.get( Value.FORECAST_NIGHT );
			symbolNight = 
//						WeatherSymbol.getSymbol( strTextNight, strIconNight );
						WeatherSymbol.getSymbol( null, strIconNight );
			final Image imageIconNight = symbolNight.getIcon();
			if ( null != imageIconNight 
							&& WeatherSymbol.UNKNOWN != symbolNight ) {
				gc.drawImage( imageIconNight, iX + 60, 26 );
				if ( symbolNight.isOutdated() ) {
					ToDo.add( "Using outdated weather icon "
						+ "(" + symbolNight.name() + ") for: " + strIconNight );
				}
			} else {
//				listWarning.add( "Not found: " + strTextNight );
				listWarning.add( "Not found (text): " + strTextNight );
				listWarning.add( "Not found (icon): " + strIconNight );
				ToDo.add( "Weather icon not found for: " + strIconNight );
			}
		} else {
			symbolNight = null;
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
//		final String strLow = ""+ map.get( Value.TEMP_NIGHT );
		final String strLow = arr[1].at( "/temperature" ).asText();
		if ( iDayWidth < 120 ) {
			gc.setFont( Theme.get().getFont( 14 ) );
			gc.drawText( strLow + "°", iX + iDayWidth - 24, 120 );
		} else {
			gc.setFont( Theme.get().getFont( 18 ) );
			gc.drawText( strLow + "°", iX + 70, 110 );
		}
		
//		final String strWindRaw = map.get( Value.WIND_SPEED );
		final String strWindRaw = getValueDayFirst( arr, "/windSpeed" );
		if ( ! StringUtils.isEmpty( strWindRaw ) && iDayWidth > 110 ) {
			
			String strWindAbbr = strWindRaw;
			strWindAbbr = strWindAbbr.replaceAll( " mph", "" );
			strWindAbbr = strWindAbbr.replaceAll( " to ", " - " );
			strWindAbbr = strWindAbbr.trim();
			if ( strWindAbbr.length() < 5 ) {
				strWindAbbr = "  " + strWindAbbr;
			}

//			final String strWindDir = ""+
//									map.get( Value.WIND_DIRECTION_COMBINED );
//			final String strWindDir = arr[0].at( "/windDirection" ).asText();
			final String strWindDir = getValueDayFirst( arr, "/windDirection" );

			final String strWindDirLong = 
					Temperature.getLongCardinalDirection( strWindDir );
			
			iY = 16;
//			final int iXW = iX + 68;
			final int iXW = iX + iDayWidth - 44;
			gc.setFont( Theme.get().getFont( 9 ) );
//			gc.drawText( "wind", iXW, iY );			
			gc.setFont( Theme.get().getFont( 14 ) );	iY += 14;
			gc.drawText( strWindAbbr, iXW, iY );	
			gc.setFont( Theme.get().getFont( 9 ) );		iY += 18;
			gc.drawText( "mph", iXW + 10, iY );			
			gc.setFont( Theme.get().getFont( 11 ) );	iY += 16;
			gc.drawText( strWindDirLong, iXW + 2, iY );
		}
		

		iY = 100;
		gc.setFont( Theme.get().getFont( 30 ) );
		
//		final String strHigh = map.get( Value.TEMP_DAY );
		final String strHigh = arr[0].at( "/temperature" ).asText();
		
		if ( StringUtils.isNotBlank( strHigh ) ) {
			gc.drawText( strHigh + "°", iX + 16, iY + 1, true );
			gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
			gc.drawText( strHigh, iX + 15, iY, true );
		} else { // also when bNoDay 
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
			gc.drawText( "..", iX + 16, iY + 1 );

			gc.drawText( "..", iX + 16, 26 );
		}
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		
		

		super.addInfoRegion( rect, 10, 
				"Weather Details for " + strDayLong,
				()-> {
					final StringBuilder sb = new StringBuilder();
					sb.append( "[Day]" + CR );
					sb.append( strTextDay + CR );
					sb.append( strIconDay + CR );
					if ( null != symbolDay ) {
						sb.append( "WeatherSymbol: " + symbolDay.name() + CR );
					} else {
						sb.append( "(symbolDay is null)" + CR );
					}
//					sb.append( "  Temp: " + strHigh + CR );
					sb.append( CR );
					sb.append( "[Night]" + CR );
					sb.append( strTextNight + CR );
					sb.append( strIconNight + CR );
					if ( null != symbolNight ) {
						sb.append( "WeatherSymbol: " + symbolNight.name() + CR );
					} else {
						sb.append( "(symbolNight is null)" + CR );
					}
//					sb.append( "  Temp: " + strLow + CR );
					
//					final String strJsonMessy = arr[0].toString();
//					final String strJsonPretty = JsonUtils.getPretty( strJsonMessy );
//					sb.append( strJsonPretty );
					return sb.toString().trim();
		} );

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
	
	
	private String getValueDayFirst( 	final JsonNode[] arr,
										final String strField ) {
		if ( null == arr || arr.length < 2 ) return "";
		final String strDay = arr[0].at( strField ).asText();
		if ( StringUtils.isNotEmpty( strDay ) ) {
			return strDay;
		} else {
			final String strNight = arr[1].at( strField ).asText();
			return strNight;
		}
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
		
		final JsonNode jn = this.jnCurrent;
//		synchronized ( mapDays ) {

		final List<String> listWarning = new LinkedList<>();
		
		final String strUpdateTime;
		
//		if ( null != mapDays && ! mapDays.isEmpty() ) {
		if ( null != jn ) {
//			strUpdateTime = mapDays.values()
//							.iterator().next().get( Value.UPDATE_TIME );
			strUpdateTime = jn.at( "/updated" ).asText();
			
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
		
		
		// special case: 2 tiles high
		final boolean bBigTile = 300 == rectOriginal.height;
		
//		final List<String> listDays;
//		final Map<String,>
//		if ( null != mapDays ) {
//		if ( null != jn )
//			jn.at( "periods" ).
//			listDays = new LinkedList<>( mapDays.keySet() );
//			Collections.sort( listDays );
//		} else {
//			listDays = Collections.emptyList();
//		}

		if ( bBigTile ) { 

//			if ( ! listDays.isEmpty() ) {
			if ( null != jn ) {
				
//				final String strKey = listDays.get( 0 );
//				final JsonNode jnNow = jn.at( "/periods/0" ); 
				final JsonNode[] arr = mapCurrent.values().iterator().next();
						
//				final EnumMap<Value,String> 
//									mapPeriod = mapDays.get( strKey );
				
				final Image imageSmall = 
								new Image( gc.getDevice(), 150, 150 );
				final GC gcSmall = 
								new GC( imageSmall );
				final Rectangle rect = new Rectangle( 0, 0, 150, 150 );
				gcSmall.setBackground( 
							Theme.get().getColor( Colors.BACKGROUND ) );
				gcSmall.fillRectangle( 0, 0, 150, 150 );

//				paintDay( gcSmall, rect, mapPeriod, listWarning );
//				paintDay( gcSmall, rect, jnNow, listWarning );
				paintDay( gcSmall, rect, arr, listWarning );
				
				gc.drawImage( imageSmall, 0, 0, 150, 150, 0, 0, 400, 300 );
			}

		} else if ( null != mapCurrent ) {
			
			int iX = rect.x;
			
			final List<Integer> listWidths = 
									partitionWidth( rectOriginal.width );
			
			final List<String> list = this.listKeysCurrent;
			
			for ( int iDay = 0; iDay < listWidths.size(); iDay++ ) {
//				if ( listDays.size() > iDay && listWidths.size() > iDay ) {
				if ( list.size() > iDay && listWidths.size() > iDay ) {
					
					final JsonNode[] arr = mapCurrent.get( list.get( iDay ) );
					
//					final String strKey = listDays.get( iDay );
					
					final int iWidth = listWidths.get( iDay );
					
//					final EnumMap<Value,String> 
//									mapPeriod = mapDays.get( strKey );
					
					final Rectangle rectDay = new Rectangle( 
									iX, 0, iWidth, rectOriginal.height );
					
					gc.setClipping( rectDay );
					
//					paintDay( gc, rectDay, mapPeriod, listWarning );
					paintDay( gc, rectDay, arr, listWarning );
					
					iX += iWidth;
				}
			}
		}
		
		gc.setClipping( rectOriginal );

		if ( ! listWarning.isEmpty() ) {
			gc.setBackground( Theme.get().getColor( 
										Colors.BACKGROUND_FLASH_ALERT ) );
			gc.setFont( Theme.get().getFont( 12 ) );
			gc.drawText( "WARNING", 26, 30 );
			int iY = 46;
			for ( final String strText : listWarning ) {
				gc.drawText( strText, 30, iY );
				iY += 16;
			}
		}

		gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );

//		if ( null != pagedata ) {
		if ( null != this.jnCurrent ) {
			gc.setFont( Theme.get().getFont( 7 ) );
			
//			final String strSeqPage = pagedata.get( Page.ATTR_SEQ_PAGE );
			
//			final String strTime = pagedata.get( ".last_modified_uxt" );
			final String strTime = jnCurrent.at( "/updated" ).asText();
			final String strElapsed;
			final Long lEpochMS = DateFormatting.getDateTime( strTime );
//			if ( null!=strTime ) {
			if ( null != lEpochMS ) {
				
				final long lElapsed = lTimeNow - lEpochMS; 
				
//				final long lTime = Long.parseLong( strTime );
//				final long lElapsed = System.currentTimeMillis() - lTime;
				final long lMinutes = TimeUnit.MILLISECONDS.toMinutes( lElapsed );
				strElapsed = "" + lMinutes + " minutes old";
			} else {
				strElapsed = "(age: unknown)";
			}
			
			final String strTab = "     ";
			final String strText = ""
//									+ "page " + strSeqPage + strTab 
//									+ strElapsed + strTab 
									+ strElapsed; 
//									+ "Update time: " + strUpdateTime;
			
			final int iXLen = gc.textExtent( strText ).x + 20;
			gc.drawText( strText, rect.width - iXLen, rect.height - 10 );
		}
//		}
		
	}
	
	@Override
	protected void activateButton( final S2Button button ) {}

//	@Override
//	public boolean clickCanvas( final Point point ) {
////		final String strBody = String.format( 
////				"Point clicked on %s: ( %d, %d )\n"
////				+ "(this message will disappear in 3s)",
////				WeatherForecastTile2.class.getName(),
////				point.x,
////				point.y );
////		final ModalMessage message = 
////				new ModalMessage( "Click", strBody, 10000 );
////		ModalMessage.add( message );
//		return true;
//	}
	



	
	public static void main( final String[] args ) {
		final long lDays = TimeUnit.SECONDS.toDays( 2147483647 );
		System.out.println( "days: " + lDays );
	}
	
}
