package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import jmr.pr121.storage.ClientData;
import jmr.pr121.storage.GCSHelper;
import jmr.pr122.DocMetadataKey;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileReader;
import jmr.util.http.ContentType;


@SuppressWarnings("serial")
//@WebServlet(
//    name = "GCSListing",
//    urlPatterns = {"/ui/gcs"}
//)
public class GCSListingServlet extends HttpServlet implements IPage {


	public void writeImageCell( final PrintWriter writer,
								final String strDocName,
								final String strURL,
								final GCSFileReader item ) {

		if ( ! strDocName.contains( "-thumb." ) ) return;
		
//		final String strURL = strRequestURL + "?name=" + strDocName;
		final String strLink = "<a href=\"" + strURL + "\">" + strDocName + "</a>";
		final String strImage = "<img width='300' src='" + strURL + "'>";
		
		writer.print( "\t<tr>\n" );
		writer.print( "\t\t<td width='0%'>" + strImage + "</td>\n" );
		
		writer.print( "\t\t<td width='100%'>" );
		writer.print( strLink + "<br>\n" );
//		writer.print( item.time.toString() + "<br>\n" );
		writer.print( item.get( DocMetadataKey.FILE_DATE ) + "<br>\n" );
//		final float fSize = 1.0f / 1024 * item.data.length;
		final float fSize = 1.0f / 1024 * item.getSize();
		final String strSize = String.format( "%.3f", fSize );
		writer.print( "Size: " + strSize + " KB<br>\n" );
		writer.print( "<br>\n" );
		
		for ( final Entry<DocMetadataKey, String> md : 
//								item.mapMetadata.entrySet() ) {
								item.getMap().entrySet() ) {
			final DocMetadataKey key = md.getKey();
			final String value = md.getValue();
			
			writer.print( key.name() + " = " + value + "<br>\n" );
		}

		writer.print( "<br>\n" );

//		writer.print( item.get( DocMetadataKey.SENSOR_DESC ) + "<br>\n" );
		writer.print( "</td>\n" );
		
		writer.print( "\t</tr>\n" );
		writer.print( "\t<tr>\n" );
//		writer.print( "\t\t<td colspan='2'>" + item.strSource + "<br><hr></td>\n" );
//		writer.print( "\t\t<td colspan='2'>" + item. + "<br><hr></td>\n" );
		writer.print( "\t</tr>\n" );
	}
	
	
	@Override
	public boolean doGet(	final Map<ParameterName,String> map,
							final HttpServletResponse resp,
							final ClientData client ) throws IOException {
		Log.add( this.getClass().getName() + ".doGet()" );		
		
		final String strName = map.get( ParameterName.NAME );

		GCSFileReader reader = null;
		
		final GCSFactory factory = GCSHelper.GCS_FACTORY;
		
		if ( null!=strName && !strName.isEmpty() ) {
			reader = factory.getFile( strName );
		}

		if ( null!=reader ) {

			Log.add( "Showing file: " + strName );

			resp.setContentType( reader.getContentType() );
			final ServletOutputStream out = resp.getOutputStream();
			out.write( reader.getContent() );
			
		} else {
		
			Log.add( "Showing GCS file listing." );

			final Map<String, GCSFileReader> listing = factory.getListing();
			Log.add( "Listing loaded. " + listing.size() + " files." );

			final String strRequestURL = map.get( ParameterName.REQUEST_URL );

		    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );
		    resp.setCharacterEncoding("UTF-8");

		    final PrintWriter writer = resp.getWriter();
		    
			writer.print( "<!DOCTYPE html>\n"
		    		+ "<html><head>\n"
		    		+ "<title>Google Cloud Storage Listing</title>\n"
		    		+ "\n\n"
		    		+ ServletConstants.strJS
		    		+ "\n"
		    		+ "<script src=\"https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.3.1.min.js\"></script>"
		    		+ "\n\n\n"
		    		+ ""
		    		+ "</head>\n"
		    		+ "<body>\n" );

		    writer.print( "<h1 style=\"color: #5e9ca0;\">Files</h1>\n" );
		    
//		    writer.print( "<table width='100%'>\n" );
		    writer.print( "<table>\n" );
		    writer.print( "<tbody>\n" );
		    writer.print( "<tr>\n" );
		    writer.print( "<td><strong>Time</strong></td>\n" );
		    writer.print( "<td><strong>Key</strong></td>\n" );
		    writer.print( "<td><strong>ContentType</strong></td>\n" );
//		    writer.print( "<td><strong>Class</strong></td>\n" );
		    writer.print( "<td><strong>Length</strong></td>\n" );
//		    writer.print( "<td><strong>Textual</strong></td>\n" );
		    writer.print( "<td><strong>Filename</strong></td>\n" );
//		    writer.print( "<td><strong>Sensor</strong></td>\n" );
		    writer.print( "</tr>\n" );


		    final List<String> listOrdered = new LinkedList<>( listing.keySet() );
		    Collections.sort( listOrdered );
		    
			for ( final String key : listOrdered ) { 
				
//				final String key = entry.getKey();
//				final DocumentData item = entry.getValue();
//				final DocumentData item = docs.get( key );
				final GCSFileReader item = listing.get( key );
//				final String strDocName = entry.getKey();
				
//				final Map<String, String> mapMeta = item.getMap();
//				final Map<DocMetadataKey, String> mapMeta = item.getMap();
				
				final String strType = item.getContentType();
				final String strFilename = item.get( DocMetadataKey.FILENAME );
				final String strFileDate = item.get( DocMetadataKey.FILE_DATE );
//				final String strDeviceIP = item.get( DocMetadataKey.DEVICE_IP );
//				final String strDeviceMAC = item.get( DocMetadataKey.DEVICE_MAC );
//				final String strSensorDesc = item.get( DocMetadataKey.SENSOR_DESC );

				final String strURL = strRequestURL + "?name=" + key;
				
//				writeImageCell( writer, strDocName, strURL, item );

				final String strLink = "<a href=\"" + strURL + "\">" + key + "</a>";
				
				writer.print( "\t<tr>\n" );
				writer.print( "\t\t<td>" + strFileDate + "</td>\n" );
				writer.print( "\t\t<td>" + strLink + "</td>\n" );
				writer.print( "\t\t<td>" + strType + "</td>\n" );
//				writer.print( "\t\t<td>" + item.strClass + "</td>\n" );
				writer.print( "\t\t<td align='right'>" + item.getSize() + "</td>\n" );
//				writer.print( "\t\t<td>" + item.asString() + "</td>\n" );
				writer.print( "\t\t<td>" + strFilename + "</td>\n" );
//				writer.print( "\t\t<td>" + item.get( DocMetadataKey.SENSOR_DESC ) + "</td>\n" );
				writer.print( "\t</tr>\n" );
				
			}
		    


		    writer.print( "</tbody>\n" );
		    writer.print( "</table>\n" );
		    
		    writer.print( "<form action='/status' method='post'>\n"
		    		+ "(form)<input type='submit' value='Submit-normal'>\n"
		    		+ "(form)<input type='submit' value='Submit-onclick' "
		    				+ "onclick=\"httpGetAsync( '/status' )\" >\n"
		    		+ "(form)<input type='submit' value='Submit-jQuery' \n"
    						+ "onclick=\" $.get( '/status', null ); \" >\n"
		    		+ "</form><br>\n" );

//		    writer.print( "<form action='/status' method='post'>\n" );

		    writer.print( "\n\n" );
		    
//		    writer.print( "<button type='button' "
//	    		+ "onclick=\" $.get( '/status', "
//	    				+ "function(data, status){\n" + 
//	    				"  alert(\"Data: \" + data + \"\\nStatus: \" + status);\n" + 
//	    		"    } ); \">"
//		    		+ "jQuery-button 01</button>\n" );

		    writer.print( "\n\n" );

		    writer.print( "<button type='button' "
		    		+ "onclick=\"doUpdate_Test();\">"
			    		+ "jQuery-button 02</button> <br>\n" );

		    writer.print( "\n\n" );

		    writer.print( "<img id='img-status' src='/images/info-outline-512.png' alt=\"\" height='18'>\n" );
//		    writer.print( "<img src='images/status-loading.gif' alt=\"\" height='18'>" );
		    writer.print( "<button type='button' "
		    		+ "onclick=\"doUpdate_Test03();\">"
			    		+ "jQuery-button 03</button> <br>\n" );

		    writer.print( "\n\n" );

		    writer.print( "<img id='img-email-stills' src='/images/info-outline-512.png' alt=\"\" height='18'>\n" );
//		    writer.print( "<img src='images/status-loading.gif' alt=\"\" height='18'>" );
		    writer.print( "<button type='button' "
//		    		+ "onclick=\"doEmailRequest( $.get('#img-email-stills'), 'GET_CAPTURE_STILLS' );\">"
		    		+ "onclick=\"doEmailRequest( 'img-email-stills', 'GET_CAPTURE_STILLS' );\">"
			    		+ "GET_CAPTURE_STILLS</button> <br>\n" );

		    writer.print( "\n\n" );
			    
		    writer.print( "<button type='button' "
		    		+ "onclick=\"alert('test');\">"
			    		+ "alert-test</button> <br>\n" );

		    writer.print( "\n\n" );

		    writer.print( "<button type='button' "
		    		+ "onclick=''>"
			    		+ "do nothing</button>\n" );

		    
		    
		    writer.print( "\n\n" );

		    writer.print( "<h1 style=\"color: #5e9ca0;\">Screenshots</h1>\n" );
		    writer.print( "<table width='100%'>\n" );
		    writer.print( "<tbody>\n" );
			writer.print( "\t<tr>\n" );
			writer.print( "\t\t<td colspan='2'><hr></td>\n" );
			writer.print( "\t</tr>\n" );
		    writer.print( "<tr>\n" );
		    
			for ( final String key : listOrdered ) { 

				if ( key.startsWith( "SCREENSHOT" ) ) {
					
					final GCSFileReader item = listing.get( key );
	
					final String strURL = strRequestURL + "?name=" + key;
					
					writeImageCell( writer, key, strURL, item );
				}
			}
		    
		    writer.print( "</tr>\n" );
		    writer.print( "<tr>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "</tr>\n" );
		    writer.print( "</tbody>\n" );
		    writer.print( "</table>\n" );
		    writer.print( "<p>&nbsp;</p>\n" );


		    
		    writer.print( "\n\n" );

		    writer.print( "<h1 style=\"color: #5e9ca0;\">Still Captures</h1>\n" );
		    writer.print( "<table width='100%'>\n" );
		    writer.print( "<tbody>\n" );
			writer.print( "\t<tr>\n" );
			writer.print( "\t\t<td colspan='2'><hr></td>\n" );
			writer.print( "\t</tr>\n" );
		    writer.print( "<tr>\n" );
		    
			for ( final String key : listOrdered ) { 
				
				if ( key.startsWith( "CAPTURE_" ) ) {
					
					final GCSFileReader item = listing.get( key );

					final String strURL = strRequestURL + "?name=" + key;
					
					writeImageCell( writer, key, strURL, item );

				}
			}
		    
		    writer.print( "</tr>\n" );
		    writer.print( "<tr>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "</tr>\n" );
		    writer.print( "</tbody>\n" );
		    writer.print( "</table>\n" );
		    writer.print( "<p>&nbsp;</p>\n" );

		    
		    
		    
		    writer.print( "\n\n" );

		    writer.print( "<h1 style=\"color: #5e9ca0;\">History</h1>\n" );
		    writer.print( "<table>\n" );
		    writer.print( "<tbody>\n" );
		    writer.print( "<tr>\n" );
		    writer.print( "<td><strong>Time</strong></td>\n" );
		    writer.print( "<td><strong>URL</strong></td>\n" );
		    writer.print( "<td><strong>Parameters</strong></td>\n" );
		    writer.print( "</tr>\n" );
		    writer.print( "<tr>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "<td>&nbsp;</td>\n" );
		    writer.print( "</tr>\n" );
		    writer.print( "</tbody>\n" );
		    writer.print( "</table>\n" );
		    writer.print( "<p>&nbsp;</p>\n" );
		    

		    
		    writer.print( "</body>\n</html>" );
		    
		    
		
			Log.add( "GCS listing complete." );
		}
		return true;
	}
	
	
//	@Override
//	public void doGet(	final HttpServletRequest req, 
//		  				final HttpServletResponse resp ) 
//		  									throws IOException {
//		Log.add( this.getClass().getName() + ".doGet()" );
//
//		Log.add( req );
//		final UserAuth ua = new UserAuth( req, resp );
//		if ( !ua.require( 99 ) ) return;
//		if ( ua.isAborted() ) return;
//		
//		
//		
//		final UserService service = UserServiceFactory.getUserService();
//		final User user = service.getCurrentUser();
//
//		Log.add( "UserService: " + service );
//		Log.add( "User: " + user );
//
//		
//		
//		if ( null==user ) {
//
//		    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );
//
//			Log.add( "User is null, showing log in screen." );
//
//			final PrintWriter out = resp.getWriter();
//			out.println( "Please <a href='"
//					+ service.createLoginURL(req.getRequestURI())
//					+ "'> Log In </a>" );
//			out.flush();
//			return;
//		}
//		
//		Log.add( "User is " + user.toString() + ", showing log out link." );
//		
//		
//		
//		
//		Log.add( "\tclass: " + user.getClass().getName() );
//		Log.add( "\temail: " + user.getEmail() );
//		Log.add( "\tuser id: " + user.getUserId() );
//		Log.add( "\tdomain: " + user.getAuthDomain() );
//
//		// later versions of gae api
//		Log.add( "\tnickname: " + user.getNickname() );
////		Log.add( "\tfed id: " + user.getFederatedIdentity() );
//
////		SecurityUtils.getSubject();
//		
//
//		try {
//			
//			Log.add( "Examining parameters.." );
//			
//			final EnumMap<ParameterName,String> 
//					mapParams = new EnumMap<>( ParameterName.class );
//			
//
//			final String strName = req.getParameter( "name" );
//
//			mapParams.put( ParameterName.NAME, strName );
//			
//			this.doGet( mapParams, resp );
//		} catch ( final Throwable e ) {
////			writer.print("\r\nError: " + e.toString() + "\r\n");
//			Log.add( "\r\nError: " + e.toString() );
//		}
//	}
		
}