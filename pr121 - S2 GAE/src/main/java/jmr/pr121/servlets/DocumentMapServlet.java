package jmr.pr121.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import jmr.p121.comm.GAEEmail;
import jmr.pr121.storage.ClientData;
import jmr.pr121.storage.DocumentData;
import jmr.pr121.storage.DocumentMap;
import jmr.pr121.storage.GCSHelper;
import jmr.pr122.DocMetadataKey;
import jmr.pr123.storage.GCSFileWriter;
import jmr.util.http.ContentType;

/*
	http://localhost:8080/hello?name=value&name_2=value_2

	https://pr121-s2gae.appspot.com/hello
 */


@SuppressWarnings("serial")
//@WebServlet(
//    name = "DocumentMap",
//    urlPatterns = {"/map"}
//)
public class DocumentMapServlet extends HttpServlet implements IPage {


	public void writeImageCell( final PrintWriter writer,
								final String strDocName,
								final String strURL,
								final DocumentData item ) {

		if ( ! strDocName.contains( "-thumb." ) ) return;
		
//		final String strURL = strRequestURL + "?name=" + strDocName;
		final String strLink = "<a href=\"" + strURL + "\">" + strDocName + "</a>";
		final String strImage = "<img width='300' src='" + strURL + "'>";
		
		writer.print( "\t<tr>\n" );
		writer.print( "\t\t<td width='0%'>" + strImage + "</td>\n" );
		
		writer.print( "\t\t<td width='100%'>" );
		writer.print( strLink + "<br>\n" );
		writer.print( item.time.toString() + "<br>\n" );
		final float fSize = 1.0f / 1024 * item.data.length;
		final String strSize = String.format( "%.3f", fSize );
		writer.print( "Size: " + strSize + " KB<br>\n" );
		writer.print( "<br>\n" );
		
		for ( final Entry<DocMetadataKey, String> md : 
								item.mapMetadata.entrySet() ) {
			final DocMetadataKey key = md.getKey();
			final String value = md.getValue();
			
			writer.print( key.name() + " = " + value + "<br>\n" );
		}

		writer.print( "<br>\n" );

//		writer.print( item.get( DocMetadataKey.SENSOR_DESC ) + "<br>\n" );
		writer.print( "</td>\n" );
		
		writer.print( "\t</tr>\n" );
		writer.print( "\t<tr>\n" );
		writer.print( "\t\t<td colspan='2'>" + item.strSource + "<br><hr></td>\n" );
		writer.print( "\t</tr>\n" );
	}
	
	
	@Override
	public boolean doGet(	final Map<ParameterName,String> map,
							final HttpServletResponse resp,
							final ClientData client ) throws IOException {
		Log.add( this.getClass().getName() + ".doGet()" );		
		
		
		final String strName = map.get( ParameterName.NAME );

		DocumentData doc = null;
		
		final DocumentMap docs = DocumentMap.get();
		
		if ( null!=strName && !strName.isEmpty() ) {
			doc = docs.get( strName );
		}

		if ( null!=doc ) {

			Log.add( "Showing document: " + strName );
			
			
			{
				if ( ContentType.TEXT_PLAIN.equals( doc.type ) ) {
					GAEEmail.sendTestEmail();
				}
			}

			resp.setContentType( doc.type.getMimeType() );
			final ServletOutputStream out = resp.getOutputStream();
			out.write( doc.data );
			
		} else {
		
			Log.add( "Showing document listing." );

			final String strRequestURL = map.get( ParameterName.REQUEST_URL );

		    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );
		    resp.setCharacterEncoding("UTF-8");

		    final PrintWriter writer = resp.getWriter();
		    
		    writer.print( "<!DOCTYPE html>\n"
		    		+ "<html><head>\n"
		    		+ "<title>Internal Document Map</title>\n"
		    		+ "\n\n"
		    		+ ServletConstants.strJS
		    		+ "\n"
		    		+ "<script src=\"https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.3.1.min.js\"></script>"
		    		+ "\n\n\n"
		    		+ ""
		    		+ "</head>\n"
		    		+ "<body>\n" );

		    writer.print( "<h1 style=\"color: #5e9ca0;\">Documents</h1>\n" );
		    
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

//			for ( final Entry<String, DocumentData> 
//								entry : DocumentMap.get().entrySet()) {
//		    final List<String> listDocs = DocumentMap.get().entrySet().
			for ( final String key : docs.getOrderedKeys() ) { 
//				final String key = entry.getKey();
//				final DocumentData item = entry.getValue();
				final DocumentData item = docs.get( key );
//				final String strDocName = entry.getKey();

				final String strURL = strRequestURL + "?name=" + key;
				
//				writeImageCell( writer, strDocName, strURL, item );

				final String strLink = "<a href=\"" + strURL + "\">" + key + "</a>";
				
				writer.print( "\t<tr>\n" );
				writer.print( "\t\t<td>" + item.time.toString() + "</td>\n" );
				writer.print( "\t\t<td>" + strLink + "</td>\n" );
				writer.print( "\t\t<td>" + item.type.name() + "</td>\n" );
//				writer.print( "\t\t<td>" + item.strClass + "</td>\n" );
				writer.print( "\t\t<td>" + item.data.length + "</td>\n" );
//				writer.print( "\t\t<td>" + item.asString() + "</td>\n" );
				writer.print( "\t\t<td>" + item.get( DocMetadataKey.FILENAME ) + "</td>\n" );
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
		    
//			for ( final Entry<String, DocumentData> 
//								entry : DocumentMap.get().entrySet()) {
			for ( final String strDocName : docs.getOrderedKeys() ) { 

//				final String strDocName = entry.getKey();
				
				if ( strDocName.startsWith( "DEVICE_SCREENSHOT" ) ) {
					
//					final DocumentData item = entry.getValue();
					final DocumentData item = docs.get( strDocName );
	
					final String strURL = strRequestURL + "?name=" + strDocName;
					
					writeImageCell( writer, strDocName, strURL, item );
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
		    
//			for ( final Entry<String, DocumentData> 
//								entry : docs.entrySet()) {
			for ( final String strDocName : docs.getOrderedKeys() ) { 
//				final String strDocName = entry.getKey();
				
				if ( strDocName.startsWith( "DEVICE_STILL" ) ) {
					
//					final DocumentData item = entry.getValue();
					final DocumentData item = docs.get( strDocName );

					final String strURL = strRequestURL + "?name=" + strDocName;
					
					writeImageCell( writer, strDocName, strURL, item );

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
		    
		    
		    
//			writer.print("DataMap\r\n");
		
			Log.add( "DocumentMap content complete." );
		}
		return true;
	}
	
	
	@Override
	public void doPost(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) throws IOException {
		Log.add( this.getClass().getName() + ".doPost()" );
		
		final LocalDateTime now = LocalDateTime.now();
      
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
		writer.print("DataMap\r\n");
    
		try {

			final EnumMap<DocMetadataKey, String> map = 
							new EnumMap<>( DocMetadataKey.class );

			writer.print("\r\nParameters:\r\n");
			for ( final Entry<String, String[]> entry : 
						req.getParameterMap().entrySet() ) {
				
				final String strName = entry.getKey();
				final String strValue = req.getParameter( strName );
				writer.print( "\t" + strName + " = " + strValue + "\r\n" );
				
				final DocMetadataKey key = DocMetadataKey.getKeyFor( strName );
				if ( null!=key ) {
					map.put( key, strValue );
				}
			}
		
			final String strName = req.getParameter( "name" );
			final String strContentType = req.getParameter( "type" );
//			final String strClass = req.getParameter( "class" );
			
			final String strURL = req.getRequestURL().toString() 
									+ "?" + req.getQueryString();
			
			final ContentType type = ContentType.getContentType( strContentType );
			
			writer.print( "\r\nPOST data:\r\n" );

			if ( null!=strName && !strName.isEmpty() ) {
				
			    try {
			    	
			    	final byte[] data = new byte[ req.getContentLength() ];
			        
			        final ServletInputStream in = req.getInputStream();
			        byte[] buf = new byte[1024];
			        int r;
			        int pos = 0;
			        while ((r = in.read(buf)) != -1) {
			        	System.arraycopy( buf, 0, data, pos, r );
			        	pos = pos + r;
			        }
			        
					final DocumentData doc = new DocumentData( 
								now, type, strURL, map, data );
					
					DocumentMap.get().put( strName, doc );
					
					Log.add( "Storing data (" + data.length + " bytes)." );

					if ( ContentType.TEXT_PLAIN.equals( type ) ) {
						final String strKey = "TEXT_" + strName;
						final GCSFileWriter gcsfile = 
								GCSHelper.GCS_FACTORY.create( strKey, type );
						final InputStream stream = new ByteArrayInputStream( data );
						gcsfile.upload( stream );
					}
			        
			    } catch ( final Exception e ) {
					Log.add( "Failed to retrieve POST data." );
					Log.add( ExceptionUtils.getStackTrace( e ) );
					resp.setStatus( 500 );
			    }
			    
			    
			} else {
				Log.add( "Data not stored because name is empty." );
			}
			
			resp.setStatus( 200 );

		} catch ( final Throwable e ) {
			Log.add( "Error: " + e.toString() );
			Log.add( ExceptionUtils.getStackTrace( e ) );
			resp.setStatus( 500 );
		}

	}
	
}