package jmr.pr131.web;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import jmr.pr122.DocMetadataKey;
import jmr.pr123.storage.GCSFileReader;
import jmr.pr131.data.FileCache;
import jmr.pr131.data.FileCache.FileRecord;
import jmr.pr131.data.Logger;

@SuppressWarnings("serial")
@WebServlet(
    name = "FileEditor",
    urlPatterns = {	"/file",
    				"/file/reload",
    				"/file/store" }
)
public class FileEditor extends HttpServlet {

	static {
		Logger.info( "FileEditor class loaded." );
	}
	
	final static List<String> BROWSERS = new LinkedList<>();
	
	
//	final GCSFactory factory = new GCSFactory( Constants.BUCKET_NAME );
	final FileCache filecache = new FileCache( Constants.BUCKET_NAME );
	
	
	public FileEditor() {
		Logger.info( "FileEditor instantiated." );
	}
	
	
	
	private void doGetPage( final HttpServletRequest request,
							final HttpServletResponse response ) 
											throws IOException {

	    response.setContentType( ContentType.TEXT_HTML.getMimeType() );
	    response.setCharacterEncoding( "UTF-8" );

	    final String strFile = getFilenameFromRequest( request );
	    final FileRecord record = filecache.getFileRecord( strFile );
	    final String strContent = record.getContent();
	    
	    final StringBuilder sb = new StringBuilder();
	    sb.append( "<!DOCTYPE html>\n" + 
		    		"<html>\n" + 
		    		"<head> <title>File: " + strFile + "</title>\n" + 
		    		"<style>\n"
//		    		+ "#wrap { position:absolute; "
//							+ "top:10%; left:2%; right: 2%; bottom:10% }\n"
		    		+ "textarea { "
	//	    				+ "position:absolute; "
	//	    				+ "top:10; left:10; right:10; bottom:10; padding:1em; "
		    				+ "width: -moz-available; "
//		    				+ "height: -moz-available; "
		    				+ "}\n"
					+ "box-sizing: border-box;\n"
		    		+ "</style>\n"
		    		+ ""
		    		+ "</head>\n" 
		    		+ "\n"
		    		+ "<body>\n" );
	
	    
	    sb.append( "\n"
//   	    		+ "<form action='/file/store' name='formSave' method='post'>\n"
   	    		+ "<form action='/file' name='formSave' method='post'>\n"
   	    		+ "<table width='100%' height='100%'><tr><td>\n"
   	    		
//   	    		+ "<font size='+1'>\n"
	    		+ "Bucket: "
	    		+ "<input type='text' name='bucket' size='80' readonly "
	    					+ "value='" + filecache.getBucketName() + "'><BR>\n"
	    					
	    		+ "Filename: "
	    		+ "<input type='text' name='filename' size='80' readonly "
	    					+ "value='" + strFile + "'><BR>\n"

	    		+ "Generation: "
	    		+ "<input type='text' name='generation' size='80' readonly "
	    					+ "value='" + record.getGeneration() + "'><BR>\n"
	    					
	    					
//	    		+ "<input type='text' name='text_1'>"
	    					
//	    		+ "</font>\n"
	    		+ "</td><td>\n"
	    		+ "<div id='controls' align='right'>\n" 
//	    		+ "<input type='button' "
//	    				+ "id='reload' value='Reload' onClick='do_reload();'>\n" 
//	    		+ "<input type='button' "
//	    				+ "id='save1' value='Save 1' onClick='do_save();'>\n" 
   	    		+ "<input type='submit' "
	    				+ "id='save' value='Save'>\n" 
	    		+ "</div>\n"
	    		+ "</td></tr>\n<tr><td colspan='2'>\n"
	    		+ "<div id='wrap'>\n"
//	    		+ "<textarea width='100%' height='100%' box-sizing='border-box'"
//				    		+ "width: -moz-available;\n" + 
//				    		"height: -moz-available;>\n"
	    		+ "<textarea name='textarea' "
	    					+ "rows='20' width='100%' box-sizing='border-box'"
				    		+ "width: -moz-available;>\n"
//	    		+ "Test text.\nSecond line.\n"
	    		+ strContent
	    		+ "</textarea>\n"
	    		+ "</div>\n"
	    		+ "</td></tr>\n</table>\n"
	    		+ "</form>\n"
	    		+ "\n" );

	    sb.append( "Past Browsers:<UL>\n" );
	    for ( final String strLine : BROWSERS ) {
		    sb.append( "<LI><TT>" + strLine + "</TT></LI>\n" );
	    }
	    sb.append( "</UL>\n" );
	    
	    sb.append( "Static Log:\n<BR>" );
	    for ( final String strLine : Logger.getLines() ) {
	    	sb.append( "<TT>" + strLine + "</TT><BR>\n" );
	    }
	    
	    sb.append( "</body>\n" + 
	    		"</html>\n" );
	
	    response.getWriter().print( sb.toString() );
	}
	
	
	private String getFilenameFromRequest( final HttpServletRequest request ) {
	    final String strFile;
	    final String strFileParam = request.getParameter( "file" );
	    if ( null!=strFileParam ) {
	    	strFile = strFileParam;
	    } else {
	    	strFile = Constants.FILE_CONTROL;
	    }
	    return strFile;
	}
	

	
	private void doGetFileReload( 	final HttpServletRequest request,
									final HttpServletResponse response ) 
											throws IOException {

	    response.setContentType( ContentType.TEXT_PLAIN.getMimeType() );
	    response.setCharacterEncoding( "UTF-8" );
	    
	    final String strFile = getFilenameFromRequest( request );
	    
//		final GCSFileReader file = factory.getFile( strFile );
//		final byte[] data = file.getContent();
//		final String strContent = new String( data );
	    
//	    final String strContent = filecache.getFileContent( strFile );
	    final FileRecord record = filecache.getFileRecord( strFile );
	    final GCSFileReader file = record.getFile();
	    final String strContent = record.getContent();
		
		final Map<DocMetadataKey, String> map = file.getMap();
		for ( final Entry<DocMetadataKey, String> entry : map.entrySet() ) {
			final String strValue = entry.getValue();
			if ( strValue.length() < 256 ) {
				final String strKey = entry.getKey().name();
				response.setHeader( strKey, strValue );
			}
		}
		
		response.setHeader( "Generation", "" + file.getGeneration() );
		response.setHeader( "UpdateTime", "" + file.getUpdateTime() );
		response.setHeader( "CreateTime", "" + file.getCreateTime() );
	    
	    response.getWriter().print( strContent );
	}
	
	
	@Override
	public void doGet( 	final HttpServletRequest request, 
		  			 	final HttpServletResponse response ) 
		  					 		throws IOException {
		Logger.info( "--- FileEditor.doGet()" );

		final String strURI = request.getRequestURI().trim();
		
//		final Enumeration<?> enAttrs = request.getAttributeNames();
//		while ( enAttrs.hasMoreElements() ) {
//			final String strName = enAttrs.nextElement().toString();
//			final String strValue = request.getAttribute( strName ).toString();
//			System.out.println( "\t" + strName + " = " + strValue );
//		}

		final Enumeration<?> enHeaders = request.getHeaderNames();
		while ( enHeaders.hasMoreElements() ) {
			final String strName = enHeaders.nextElement().toString();
			final String strValue = request.getHeader( strName ).toString();
			System.out.println( "\t" + strName + " = " + strValue );
		}

		final Principal user = request.getUserPrincipal();
		if ( null!=user ) {
			Logger.info( "User: " + user.toString() );
		}
		
		final String strUserAgent = request.getHeader( "User-Agent" );
//		Logger.info( "User-Agent: " + strUserAgent );
		
		if ( ! BROWSERS.contains( strUserAgent ) ) {
			BROWSERS.add( strUserAgent );
			Logger.info( "New User-Agent: " + strUserAgent );
		}
		
//		System.out.println( "getRemoteUser: " + request.getRemoteUser() );
//		System.out.println( "getRemoteHost: " + request.getRemoteHost() );
//		System.out.println( "getRemoteUser: " + request.getRemoteUser() );
		
		if ( strURI.startsWith( "/file/reload" ) ) {
			doGetFileReload( request, response );
		} else {
			doGetPage( request, response );
		}
	}
	
	
	@Override
	protected void doPost(  final HttpServletRequest request, 
							final HttpServletResponse response )
									throws ServletException, IOException {
		Logger.info( "--- FileEditor.doPost()" );
		
		System.out.println( 
				"" + request.getParameterMap().size() + " parameters" );
		for ( final Entry<String, String[]> 
							entry : request.getParameterMap().entrySet() ) {
			final String strValue = Arrays.toString( entry.getValue() )
							.replace( '\n', '/' )
							.replace( '\r', '/' );
			System.out.println( "\t" + entry.getKey() + " = " + strValue );
		}
		
		final String strTextarea = request.getParameter( "textarea" );
		final String strFilename = request.getParameter( "filename" );
		System.out.println( "--- textarea - start ---" );
		System.out.println( strTextarea );
		System.out.println( "--- textarea - end ---" );
		
	
		filecache.saveFileContent( strFilename, strTextarea );
		
//		super.doPost( request, response );
		doGetPage( request, response );
	}
	
}
