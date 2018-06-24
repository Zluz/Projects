package jmr.pr121.servlets;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

//import com.google.appengine.repackaged.com.google.gson.JsonElement;
//import com.google.appengine.repackaged.com.google.gson.JsonObject;

import jmr.pr121.storage.GCSHelper;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileReader;

public class TeslaPage {


	// window reports 1200px wide
	public final static int NAV_WIDTH = 220;
	public final static int BODY_WIDTH = 930;
	
	enum NavItem {
		INFORMATION( "Information" ),
		STILL_CAP( "Still Captures" ),
		SCREENSHOTS( "Screenshots" ),
		TESLA_JSON( "Tesla JSON" ),
		NEST_JSON( "Nest JSON" ),
//		SPOTIFY( "Spotify" ),
//		S2_DEVICES( "S2 Devices" ),
//		S2_EVENTS( "S2 Events" ),
		GAE_UTIL( "App Engine" ),
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
												final NavItem item, 
												final String strURLBase,
												final String strFilter ) {


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
	    		
	    
	    
	    writer.println( "<table width='" + BODY_WIDTH + "px'>" );
	    writer.println( "<tr><td height='14px'></td></tr>\n<tr>" );

		for ( final String key : listOrdered ) { 
			
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
				    writer.println( "<tr><td height='28px'></td></tr>" );
				    writer.println( "<tr>" );
				}
				
				writer.print( "<td align='center'>" );
				
//				final GCSFileReader file = listing.get( key );

				final String strThumbURL = strURLBase + "/ui/gcs?name=" + key;
				
//				final String strFullURL = strThumbURL.replace( "-thumb.j", ".j");
				final String strFullImage = key.replace( "-thumb.", ".");
				final String strFullURL = strURLBase + "/ui/tesla?"
							+ "full_image=" + strFullImage
							+ "&fast_image=" + key
							+ "&page=" + item.name();

				String strCaption = strFullImage;
				strCaption = strCaption.replace( "CAPTURE_", "" );
				strCaption = strCaption.replace( "_capture_", "/" );
				strCaption = strCaption.replace( "SCREENSHOT_", "" );
				strCaption = strCaption.replace( "_screenshot.png", "" );
				strCaption = "x-" + strCaption.substring( iPrefixLen );

//				writer.println( "<div class='div-thumbnail' style='max-width:320px;'>" );
				writer.print( "<div class='div-thumbnail-" + iColsPerRow + "'>" );
//				writer.println( "<a href='" + strFullURL + "'>" );
				writer.print( "<img class='image-thumbnail' "
							+ "src='" + strThumbURL + "' "
							+ "onclick='doGoTo(\"" + strFullURL + "\");'>" );
//				writer.println( "</a>" );
				writer.print( "<div class='text-image-caption'>" + strCaption + "</div>" );
				writer.print( "</div>" );
				writer.println( "</td>" );
			}
		}
	    writer.println( "</tr></table>" );
		return true;
	}

	
	
	public static boolean generatePageOutput(	final NavItem item, 
												final PrintWriter writer,
												final String strURLBase ) {
		if ( null==item ) return false;
		if ( null==writer ) return false;
		
		Log.add( "--- TeslaPage.generatePageOutput()" );
		
		switch ( item ) {
		
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
				
				return generateImageList( writer, item, strURLBase, "capture" );
				
			}
			case SCREENSHOTS: {
				
				return generateImageList( writer, item, strURLBase, "screenshot" );
				
			}
			case GAE_UTIL: {
				
				final String strContent = ""
						+ "{\n"
						+ "    \"name_01\": \"value_01\",\n"
						+ "    \"color\": \"blue\"\n"
						+ "}";
				final boolean bResult = 
						generateTableOfJson( writer, strContent );
				return bResult;
			}

		}
		return false;
	}
	
	
}
