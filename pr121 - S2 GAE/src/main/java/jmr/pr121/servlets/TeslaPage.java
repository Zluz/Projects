package jmr.pr121.servlets;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jmr.pr121.storage.ClientData;

//import com.google.appengine.repackaged.com.google.gson.JsonElement;
//import com.google.appengine.repackaged.com.google.gson.JsonObject;

import jmr.pr121.storage.GCSHelper;
import jmr.pr122.DocMetadataKey;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileReader;

public class TeslaPage {


	// window reports 1200px wide
	public static final int NAV_WIDTH = 220;
	public static final int BODY_WIDTH = 930;

	/** HTML IMG tooltip (title attribute) line feed */
	public static final String TTCR = "&#10";

	enum NavItem {
		INFORMATION( "Information" ),
		STILL_CAP( "Still Captures" ),
		SCREENSHOTS( "Screenshots" ),
		TESLA_JSON( "Tesla JSON" ),
		NEST_JSON( "Nest JSON" ),
//		SPOTIFY( "Spotify" ),
//		S2_DEVICES( "S2 Devices" ),
//		S2_EVENTS( "S2 Events" ),
//		GAE_UTIL( "App Engine" ),
		CONFIG( "Configuration" ),
		;
		
		public final String strCaption;
		
		NavItem( final String strCaption ) {
			this.strCaption = strCaption;
		}
		

		public static NavItem getNavItem( final String text ) {
			if ( StringUtils.isEmpty( text ) ) {
				return null;
			}
			
			final String strNorm = text.trim().toUpperCase();
			for ( final NavItem param : NavItem.values() ) {
				if ( strNorm.equals( param.name() ) ) {
					return param;
				}
			}
			
			return null;
		}
		
	};
	
	enum Option {
		
		STILL_CAPTIONS( ClientData.Key.OPTION_STILL_CAPTIONS, 
				"STILL CAPTURE CAPTIONS", 180, "SOURCE","AGE","IP","RESOLUTION" ),
		
		SCREENSHOT_CAPTIONS( ClientData.Key.OPTION_SCREENSHOT_CAPTIONS,
				"SCREENSHOT CAPTIONS", 180, "MAC","AGE","IP","RESOLUTION" ),
		
		DISPLAY_MODE( ClientData.Key.OPTION_DISPLAY_MODE,
				"DISPLAY MODE", 140, "DAY","NIGHT","AUTO" ),
		
		ADVANCED( ClientData.Key.OPTION_ADVANCED,
				"ADVANCED OPTIONS", 140, "OFF","ON" ),
		
		REFRESH_FREQUENCY( ClientData.Key.OPTION_REFRESH_FREQUENCY,
				"REFRESH FREQUENCY", 110, "30s", "60s", "2m", "20m", "60m" ),
		
		REFRESH_DURATION( ClientData.Key.OPTION_REFRESH_DURATION,
				"REFRESH DURATION", 110, "10m", "60m", "2h", "4h" ),
		;
		
		public final String strTitle;
		public final ClientData.Key key;
		public final String[] arrOptions;
		public final int iWidth;
		
		Option(	final ClientData.Key key,
					final String strTitle,
					final int iWidth,
					final String... arrOptions ) {
			this.strTitle = strTitle;
			this.key = key;
			this.iWidth = iWidth;
			this.arrOptions = arrOptions;
		}
		
		public String getValue(	final ClientData client ) {
			if ( null==client ) return null;
			
			final ClientData.Key key = this.key;
			final String strValue = client.get( key );
			return strValue;
		}
	}


	public static class Marker_20180628_2208 {};
	
	

	public static boolean generateTableOfJsonObject(	
										final PrintWriter writer,
										final JsonObject jo ) {
		if ( null==writer ) return false;
		if ( null==jo ) return false;
		
//		final int iTableWidth = BODY_WIDTH;
		final int iTableWidth = 500;
		final String strNameWidth = "200px";
		final String strValueWidth = "300px";
		
//	    writer.println( "<table class='blueTable' width='100%'>" );
		writer.println( "<div id='context-4' class='content'>" );
	    writer.println( "<table class='blueTable' width='" + iTableWidth + "px'>" );
	    
	    //TODO sort?
	    for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {

	    	final String strKey = entry.getKey();
	    	final JsonElement je = entry.getValue();

		    writer.println( "<tr width='" + iTableWidth + "px'>" );
		    writer.println( "<td width='" + strNameWidth + "'>" + strKey + "</td>" );
		    writer.println( "<td width='" + strValueWidth + "'>" + je.toString() + "</td>" );
		    writer.println( "</tr>" );
	    }

	    writer.println( "</table>" );
		writer.println( "</div>" );
		return false;
	}

	
	public static boolean generateTableOfJson(	final PrintWriter writer,
												final String strContent ) {
		if ( null==writer ) return false;
		if ( null==strContent ) return false;
		
		Log.add( "--> TeslaPage.generateTableOfJson()" );

		JsonElement je = null;
		try {
			je = ServletConstants.JSON_PARSER.parse( strContent );
		} catch ( final Exception e ) {
			je = null;
		}
		
		if ( null!=je ) {
			
			if ( je.isJsonObject() ) {
				final JsonObject jo = je.getAsJsonObject();
		
				final boolean bResult = generateTableOfJsonObject( writer, jo );
				return bResult;
			}
		}
		
		
		for ( final String strLine : strContent.split( "\n" ) ) {
			writer.println( "<tt>" + strLine + "</tt><br>" );
		}
		Log.add( "<-- TeslaPage.generateTableOfJson()" );
		return true;
	}
	
	

	public static boolean generateImageList(	final PrintWriter writer,
												final ClientData client, 
												final long lNow,
												final NavItem item, 
												final String strURLBase,
												final Option option,
												final String strFilter, 
												final boolean bTight ) {


		final GCSFactory factory = GCSHelper.GCS_FACTORY;

		final Map<String, GCSFileReader> listing = factory.getListing();

	    final List<String> listOrdered = new LinkedList<>( listing.keySet() );
	    Collections.sort( listOrdered );
	    
	    int iRow = 1;
	    int iCol = 0;
	    int iColsPerRow = 3;
	    
	    int iPrefixLen = "B8-27-EB-".length();
	    
//	    writer.println( "<table width='100%'><tr>" );
	    
	    writer.print( "<script>\n"
	    		+ "function doGoTo( url ) {\n"
	    		+ "    top.location.href = url;\n"
	    		+ "}\n"
	    		+ "</script>\n" );
	    		
	    final int iHeader;
	    final int iRowPad;
	    if ( bTight ) {
	    	iHeader = 0;
	    	iRowPad = 0;
	    } else {
	    	iHeader = 14;
	    	iRowPad = 28;
	    }
	    
	    writer.println( "<table width='" + BODY_WIDTH + "px'>" );
	    writer.println( "<tr><td height='" + iHeader + "px'></td></tr>\n<tr>" );

		for ( final String key : listOrdered ) { 


			final GCSFileReader file = listing.get( key );
			
			
			// TODO update
			final String strImageTime = "0";
			
			
			if ( key.contains( "thumb" ) && key.contains( strFilter ) ) {
				iCol++;
				
				if ( iCol > iColsPerRow ) {
				    writer.println( "</tr>" );
				    iRow++;
				    iCol = 1;
				    if ( 2==iRow ) {
				    	iColsPerRow = 4;
				    	
					    writer.println( "</table>" );
					    writer.println( "<table width='" + BODY_WIDTH + "px'>" );
				    }
				    writer.println( "<tr><td height='" + iRowPad + "px'></td></tr>" );
				    writer.println( "<tr>" );
				}
				
				writer.print( "<td align='center'>" );
				
//				final GCSFileReader file = listing.get( key );

				final String strThumbURL = strURLBase + "/ui/gcs?name=" + key;
				
//				final String strFullURL = strThumbURL.replace( "-thumb.j", ".j");
				final String strFullImage = key.replace( "-thumb.", ".");
				final String strFullURL = strURLBase + "/ui/tesla"
							+ "?page=" + item.name()
							+ "&" + ParameterName.IMAGE_TIME.name() + "=" + strImageTime 
							+ "&" + ParameterName.FULL_IMAGE.name() + "=" + strFullImage 
							+ "&" + ParameterName.FAST_IMAGE.name() + "=" + key; 


				// strAge does not need to be evaluated if on the 
				// Tesla browser and age is not the display option.
				String strAge;
				
				final String strModified = 
						file.get( DocMetadataKey.LAST_MODIFIED_MS );
				try {
					final long lModified = Long.parseLong( strModified );
					final long lAgeMS = lNow - lModified;
					
					String strFormat = DurationFormatUtils.formatDuration( 
							lAgeMS, " HH:mm:ss" );
					strFormat = strFormat.replace( " 00:", " " );
					strFormat = strFormat.replace( " 0", " " );
					strAge = strFormat;
					
				} catch ( NumberFormatException e ) {
					final String strDate = file.get( DocMetadataKey.FILE_DATE );
					if ( StringUtils.isNotEmpty( strDate ) ) {
						strAge = strDate;
					} else {
						strAge = "(no time data)";
					}
				}
				
				
				
//				final String strOption = Option.STILL_CAPTIONS.getValue( client );
				final String strOption = option.getValue( client );

				final String strCaption;

				if ( "IP".equals( strOption ) ) {

					strCaption = file.get( DocMetadataKey.DEVICE_IP );

				} else if ( "MAC".equals( strOption ) ) {

					strCaption = file.get( DocMetadataKey.DEVICE_MAC );

				} else if ( "AGE".equals( strOption ) ) {
					
					strCaption = strAge;
					
				} else {
					String strSource = strFullImage;
					strSource = strSource.replace( "CAPTURE_", "" );
					strSource = strSource.replace( "_capture_", "/" );
					strSource = strSource.replace( "SCREENSHOT_", "" );
					strSource = strSource.replace( "_screenshot.png", "" );
					strSource = "x-" + strSource.substring( iPrefixLen );
					strCaption = strSource;
				}

				final String strTooltip = 
						"Filename: " + strFullImage + TTCR
						+ "Image age: " + strAge + TTCR
						+ "Device IP: " + file.get( DocMetadataKey.DEVICE_IP ) + TTCR
						+ "Device MAC: " + file.get( DocMetadataKey.DEVICE_MAC );
				
//				writer.println( "<div class='div-thumbnail' style='max-width:320px;'>" );
				writer.print( "<div class='div-thumbnail-" + iColsPerRow + "'>" );
//				writer.println( "<a href='" + strFullURL + "'>" );
				writer.print( "<img class='image-thumbnail' "
							+ "src='" + strThumbURL + "' "
							+ "title='" + strTooltip + "' "
							+ "onclick='doGoTo(\"" + strFullURL + "\");'>" );
//				writer.println( "</a>" );
				writer.print( "<div class='text-image-caption'>" + strCaption + "</div>" );
//				writer.print( "<div class='text-image-caption'>" 
//									+ strCaption + TTCR 
//									+ strTooltip + "</div>" );
				writer.print( "</div>" );
				writer.println( "</td>" );
			}
		}
	    writer.println( "</tr></table>" );
		return true;
	}

	
	
	public static boolean generatePageOutput(	final NavItem item, 
												final PrintWriter writer,
												final ClientData client,
												final String strURLBase ) {
		if ( null==item ) return false;
		if ( null==writer ) return false;
		
		Log.add( "--- TeslaPage.generatePageOutput()" );
		
		final long lNow = System.currentTimeMillis();
		
		switch ( item ) {
		
			case INFORMATION: {

				writer.println( "<p><a class='text-title'>GOOGLE APP ENGINE</a></p>" );
				writer.println( "<a class='text-normal'>File: </a><br>" );

				testColor( writer, "#1066AE" );
				testColor( writer, "#1470AE" );
				testColor( writer, "#1C7CAE" );

				testColor( writer, "#1066A0" );
				testColor( writer, "#1470A0" );
				testColor( writer, "#1C7CA0" );

				break;
			}
		
			case TESLA_JSON: {

				final String strFilename = "TESLA_Combined.json";

//				writer.println( "<p><a class='text-title'>Tesla Combined JSON</a></p>" );
				writer.println( "<p><a class='text-title'>TESLA COMBINED JSON</a></p>" );
				writer.println( "<a class='text-normal'>File: " + strFilename + "</a><br>" );
				
				final GCSFileReader file = 
						GCSHelper.GCS_FACTORY.getFile( strFilename );
				if ( null!=file ) {
					final String strContent = new String( file.getContent() );
					writer.println( "<a class='text-normal'>Size: " + strContent.length() + "</a><br><br>" );
					final boolean bResult = 
							generateTableOfJson( writer, strContent );
					return bResult;
				}
				break;
			}

			case NEST_JSON: {

				final String strSharedFilename = "NEST_SHARED_DETAIL.json";
				final String strDeviceFilename = "NEST_DEVICE_DETAIL.json";

//				writer.println( "<p><a class='text-title'>Nest JSON: Shared and Device Detail</a></p>" );
				writer.println( "<p><a class='text-title'>NEST JSON (SHARED AND DEVICE DETAIL)</a></p>" );
				writer.println( "<a class='text-normal'>File: " + strSharedFilename + "</a><br><br>" );

				final GCSFileReader fileShared = 
						GCSHelper.GCS_FACTORY.getFile( "NEST_SHARED_DETAIL.json" );
				
				if ( null!=fileShared ) {
					final String strContent = new String( fileShared.getContent() );
//					final boolean bResult = 
							generateTableOfJson( writer, strContent );
//					return bResult;
				}

				writer.println( "<br><br>" );
				writer.println( "<a class='text-normal'>File: " + strDeviceFilename + "</a><br><br>" );

				final GCSFileReader fileDevice = 
						GCSHelper.GCS_FACTORY.getFile( "NEST_DEVICE_DETAIL.json" );
				
				if ( null!=fileDevice ) {
					final String strContent = new String( fileDevice.getContent() );
//					final boolean bResult = 
							generateTableOfJson( writer, strContent );
//					return bResult;
				}
				
				return true;
			}
			
			case STILL_CAP: {
				
				return generateImageList( writer, client, lNow, item, strURLBase, 
						Option.STILL_CAPTIONS, "capture", true );
				
			}
			case SCREENSHOTS: {
				
				return generateImageList( writer, client, lNow, item, strURLBase, 
						Option.SCREENSHOT_CAPTIONS, "screenshot", false );
				
			}
			case CONFIG: {
				
				writer.println( "<table><tr><td width='60px'><br></td><td>" );

				writer.println( "<p></p>" );
				
				drawOptionTable( writer, Option.STILL_CAPTIONS, client );
				drawOptionTable( writer, Option.SCREENSHOT_CAPTIONS, client );
				drawOptionTable( writer, Option.DISPLAY_MODE, client );
//				drawOptionTable( writer, Option.ADVANCED, client );
				drawOptionTable( writer, Option.REFRESH_FREQUENCY, client );
				
				
				
//				final String strContent = ""
//						+ "{\n"
//						+ "    \"name_01\": \"value_01\",\n"
//						+ "    \"color\": \"blue\"\n"
//						+ "}";
//				final boolean bResult = 
//						generateTableOfJson( writer, strContent );

				writer.println( "</td></tr></table>" );

//				return bResult;
//				break;
				return true;
			}

		}
		return false;
	}
	
	
	private static void testColor( 	final PrintWriter writer,
									final String strColor ) {
		writer.println( 
//				"<a class='text-title'>TEST: " + strColor + " </a></p>\r\n" + 
				"<table class='tableRadio tableRadio-110' style='border-collapse: collapse;'><tr>\r\n" + 
				"<td >" + strColor +  "</td>\r\n" + 
				"<td style='background-color: " + strColor +  "; color: #FFFFFF; -webkit-border-radius: 6px;'>AGE</td>\r\n" + 
				"<td >IP</td>\r\n" + 
				"<td >RESOLUTION</td>\r\n" + 
				"</tr></table>\n" +
				"<br>" +
				""
				);
	}
	
	
	private static void drawOptionTable(	final PrintWriter writer,
//											final String strTitle,
//											final String[] arrOptions ) {
											final Option option,
											final ClientData client ) {

		final String strTitle = option.strTitle;
		final String[] arrOptions = option.arrOptions;
		
		final String strOption = option.getValue( client );
		
		writer.println( "<br>" );
		writer.println( "<a class='text-title'>" + strTitle + "</a>\n<p></p>" );
//		writer.println( "<p></p>" );

//		writer.println( "<a class='text-normal'>STILL CAPTURES</a><br>" );
//		final String strTDWidth = "width: " + option.iWidth + "px; ";
		final String strBorderCollapse = "border-collapse: collapse; ";
		final String strTDClass = "tableRadio-" + option.iWidth;
		writer.println( "<table class='tableRadio " + strTDClass + "' "
//				+ "id='" + option.name() + "'><tr>" );
//				+ "style='border-collapse: collapse;' id='" + option.name() + "'><tr>" );
//				+ "style='border: 2px solid #707070;' id='" + option.name() + "'><tr>" );
//				+ "style='border-collapse: collapse; border: 2px solid #A0A0A0; border-radius: 5px;' id='" + option.name() + "'><tr>" );
//				+ "style='border-collapse: collapse; border: 1px solid #A0A0A0; border-radius: 5px; -webkit-border-radius: 10px;' id='" + option.name() + "'><tr>" );
//				+ "id='" + option.name() + "'><tr>" );
//				+ "style='border-collapse: collapse;' id='" + option.name() + "'><tr>" );
				+ "style='" 
//								+ strTDWidth 
								+ strBorderCollapse 
						+ "' id='" + option.name() + "'><tr>" );
		for ( final String strItem : arrOptions ) {
			final String strInitClass;
			if ( strItem.equals( strOption ) ) {
//				strOnLoad = " onload='doSelectOption(this);'";
				strInitClass = " init-option";
			} else {
				strInitClass = "";
			}
			writer.println( 
//					"<td style='" + strTDWidth + "' onclick='doSelectOption(this,true);'" + strOnLoad + ">" 
					"<td class='" + strTDClass + strInitClass + "' onclick='doSelectOption(this,true);'>" 
					+ strItem + "</td>" );
		}
		writer.println( "</tr></table>" );
		writer.println( "<br>" );
	}
	
	
}
