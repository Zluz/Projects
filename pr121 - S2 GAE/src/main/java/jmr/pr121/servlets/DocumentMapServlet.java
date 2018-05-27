package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import jmr.pr121.doc.DocumentData;
import jmr.pr121.doc.DocumentMap;

/*

	http://localhost:8080/hello?name=value&name_2=value_2

	https://pr121-s2gae.appspot.com/hello

 */


@SuppressWarnings("serial")
@WebServlet(
    name = "DocumentMap",
    urlPatterns = {"/map"}
)
public class DocumentMapServlet extends HttpServlet implements IPage {

//	final List<String> listHistory = new LinkedList<>();
	
//	final static Map<String,DocumentData> mapDoc = new HashMap<>();

//	public DocumentMapServlet() {};

	@Override
	public boolean doGet(	final EnumMap<ParameterName,String> map,
							final HttpServletResponse resp ) throws IOException {

		Log.add( "DocumentMapServlet.doGet()" );		
		
//		final String strName = req.getParameter( "name" );

//		mapParams.put( ParameterName.NAME, strName );
		
		final String strName = map.get( ParameterName.NAME );

		DocumentData doc = null;
		
//		final PrintWriter sbWriter = resp.getWriter();

		final String strData;
		if ( null!=strName && !strName.isEmpty() ) {
//			sbWriter.append( "\r\nData (name=" + strName + "):\r\n");
			doc = DocumentMap.get().get( strName );
			if ( null!=doc ) {
				strData = doc.asString();
//				sbWriter.append( strData + "\r\n" );
			} else {
//				sbWriter.append( "(null data)\r\n" );
				strData = null;
			}
		} else {
//			sbWriter.append( "(no name given)\r\n" );
			strData = null;
		}

		if ( null!=doc ) {

			Log.add( "Showing document: " + strName );

			resp.setContentType( doc.strContentType );
			final ServletOutputStream out = resp.getOutputStream();
			out.write( doc.data );
			
		} else {
		
			Log.add( "Showing document listing." );

			final String strRequestURL = map.get( ParameterName.REQUEST_URL );

//		    resp.setContentType("text/plain");
//		    resp.setCharacterEncoding("UTF-8");

		    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );
//		    resp.setCharacterEncoding("UTF-8");

		    final PrintWriter writer = resp.getWriter();
		    

		    writer.print( "<h1 style=\"color: #5e9ca0;\">Documents</h1>\n" );
		    
		    
//		    try {
//		    	Log.add( "Getting request URI.." );
////		    	final String strURIRequest = req.getRequestURI();
//		    	final String strURLRedir = "http://localhost:8080/map";
//		    	Log.add( "\tRedirect URL: " + strURLRedir );
//		    	
//		    	Log.add( "service.isUserLoggedIn() = " 
//		    				+ service.isUserLoggedIn() );
//		    	
//		    	Log.add( "Building logout link.." );
//		    	final String strURLLogout = 
//		    				service.createLogoutURL( strURLRedir );
////		    				service.
//		    	Log.add( "\tLogout URI: " + strURLLogout );
//			    
//				writer.print( "<a href='" + strURLLogout + "'> Log out </a>" );
//				
//		    } catch ( final Throwable e ) {
//		    	Log.add( "Exception encountered: " + e.toString() );
//		    }
		    
//		    writer.print( "<p>" + req.getRequestURL() + "</p>\n" );
		    
		    writer.print( "<table>\n" );
		    writer.print( "<tbody>\n" );
		    writer.print( "<tr>\n" );
		    writer.print( "<td><strong>Time</strong></td>\n" );
		    writer.print( "<td><strong>Key</strong></td>\n" );
		    writer.print( "<td><strong>ContentType</strong></td>\n" );
		    writer.print( "<td><strong>Class</strong></td>\n" );
		    writer.print( "<td><strong>Length</strong></td>\n" );
		    writer.print( "<td><strong>Textual</strong></td>\n" );
		    writer.print( "</tr>\n" );

			for ( final Entry<String, DocumentData> 
								entry : DocumentMap.get().entrySet()) {
				final String key = entry.getKey();
				final DocumentData item = entry.getValue();

//				writer.print("\t" + item.time.toString() 
//						+ "\t" + key + "\t" + item.strClass + "\t"
//						+ item.asString(60) + "\r\n");
				
				// <a href="host/get?name=my_image">my_image</a>
				
//				final String strURL = req.getRequestURL() + "?name=" + key;
				final String strURL = strRequestURL + "?name=" + key;
				final String strLink = "<a href=\"" + strURL + "\">" + key + "</a>";
				
				writer.print( "\t<tr>\n" );
				writer.print( "\t\t<td>" + item.time.toString() + "</td>\n" );
				writer.print( "\t\t<td>" + strLink + "</td>\n" );
				writer.print( "\t\t<td>" + item.strContentType + "</td>\n" );
				writer.print( "\t\t<td>" + item.strClass + "</td>\n" );
				writer.print( "\t\t<td>" + item.data.length + "</td>\n" );
				writer.print( "\t\t<td>" + item.asString() + "</td>\n" );
				writer.print( "\t</tr>\n" );
				
			}
		    


		    writer.print( "</tbody>\n" );
		    writer.print( "</table>\n" );
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
		    
		    
		    
		    
			writer.print("DataMap\r\n");
		
//			writer.print( sbWriter.toString() );
		
			
//			
////			writer.print("\r\nDocuments:\r\n");
//			for ( final Entry<String, DocumentData> 
//								entry : DocumentMap.get().entrySet() ) {
//				final String key = entry.getKey();
//				final DocumentData item = entry.getValue();
//				
//				writer.print( "\t" + item.time.toString()
//						+ "\t" + key
//						+ "\t" + item.strClass 
//						+ "\t" + item.asString( 60 )
//						+ "\r\n" );
//			}
//			
//			writer.print("\r\nHistory:\r\n");
//			for ( final String line : listHistory ) {
//				writer.print("\t" + line +"\r\n");
//			}
			
//			listHistory.add( req.getRequestURL().toString() );
//			listHistory.add( "\t\t" + req.getQueryString() );
//			if ( null!=strData ) {
//				listHistory.add( "\t\tData: " + strData );
//			}
			
			Log.add( "DocumentMap content complete." );
		}
		return true;
	}
	
	
	@Override
	public void doGet(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  									throws IOException {
		final UserAuth ua = new UserAuth( req, resp );
		if ( !ua.require( 99 ) ) return;
		if ( ua.isAborted() ) return;
		
		
		Log.add( this.getClass().getName() + ".doGet()" );
		
		final UserService service = UserServiceFactory.getUserService();
		final User user = service.getCurrentUser();

		Log.add( "UserService: " + service );
		Log.add( "User: " + user );

		
		
		if ( null==user ) {

		    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );

			Log.add( "User is null, showing log in screen." );

			final PrintWriter out = resp.getWriter();
			out.println( "Please <a href='"
					+ service.createLoginURL(req.getRequestURI())
					+ "'> Log In </a>" );
			out.flush();
			return;
		}
		
		Log.add( "User is " + user.toString() + ", showing log out link." );
		
		
		
		
		Log.add( "\tclass: " + user.getClass().getName() );
		Log.add( "\temail: " + user.getEmail() );
		Log.add( "\tuser id: " + user.getUserId() );
		Log.add( "\tdomain: " + user.getAuthDomain() );

		// later versions of gae api
		Log.add( "\tnickname: " + user.getNickname() );
//		Log.add( "\tfed id: " + user.getFederatedIdentity() );

//		SecurityUtils.getSubject();
		

		
//		final UserAuth auth = new UserAuth( req, resp );
//		if ( auth.isAborted() ) return;

//		SessionRegistry.register( req );

//		DocumentData doc = null;
//		
//		final StringBuilder sbWriter = new StringBuilder();
    
		try {
			
			Log.add( "Examining parameters.." );
			
			final EnumMap<ParameterName,String> 
					mapParams = new EnumMap<>( ParameterName.class );
			
//			writer.print("\r\nParameters:\r\n");
//			for ( final Entry<String, String[]> entry : 
//						req.getParameterMap().entrySet() ) {
//				
//				final String strName = entry.getKey();
//				final String strValue = req.getParameter( strName );
//				writer.print( "\t" + strName + " = " + strValue + "\r\n" );
//			}

			final String strName = req.getParameter( "name" );

			mapParams.put( ParameterName.NAME, strName );
			
			this.doGet( mapParams, resp );
//			
//			final String strData;
//			if ( null!=strName && !strName.isEmpty() ) {
//				sbWriter.append( "\r\nData (name=" + strName + "):\r\n");
//				doc = DocumentMap.get().get( strName );
//				if ( null!=doc ) {
//					strData = doc.asString();
//					sbWriter.append( strData + "\r\n" );
//				} else {
//					sbWriter.append( "(null data)\r\n" );
//					strData = null;
//				}
//			} else {
//				sbWriter.append( "(no name given)\r\n" );
//				strData = null;
//			}
//
//			if ( null!=doc ) {
//
//				Log.add( "Showing document: " + strName );
//
//				resp.setContentType( doc.strContentType );
//				final ServletOutputStream out = resp.getOutputStream();
//				out.write( doc.data );
//				
//			} else {
//			
//				Log.add( "Showing document listing." );
//
//
////			    resp.setContentType("text/plain");
////			    resp.setCharacterEncoding("UTF-8");
//
//			    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );
////			    resp.setCharacterEncoding("UTF-8");
//
//			    final PrintWriter writer = resp.getWriter();
//			    
//
//			    writer.print( "<h1 style=\"color: #5e9ca0;\">Documents</h1>\n" );
//			    
//			    
//			    try {
//			    	Log.add( "Getting request URI.." );
////			    	final String strURIRequest = req.getRequestURI();
//			    	final String strURLRedir = "http://localhost:8080/map";
//			    	Log.add( "\tRedirect URL: " + strURLRedir );
//			    	
//			    	Log.add( "service.isUserLoggedIn() = " 
//			    				+ service.isUserLoggedIn() );
//			    	
//			    	Log.add( "Building logout link.." );
//			    	final String strURLLogout = 
//			    				service.createLogoutURL( strURLRedir );
////			    				service.
//			    	Log.add( "\tLogout URI: " + strURLLogout );
//				    
//					writer.print( "<a href='" + strURLLogout + "'> Log out </a>" );
//					
//			    } catch ( final Throwable e ) {
//			    	Log.add( "Exception encountered: " + e.toString() );
//			    }
//			    
//			    writer.print( "<p>" + req.getRequestURL() + "</p>\n" );
//			    
//			    writer.print( "<table>\n" );
//			    writer.print( "<tbody>\n" );
//			    writer.print( "<tr>\n" );
//			    writer.print( "<td><strong>Time</strong></td>\n" );
//			    writer.print( "<td><strong>Key</strong></td>\n" );
//			    writer.print( "<td><strong>ContentType</strong></td>\n" );
//			    writer.print( "<td><strong>Class</strong></td>\n" );
//			    writer.print( "<td><strong>Length</strong></td>\n" );
//			    writer.print( "<td><strong>Textual</strong></td>\n" );
//			    writer.print( "</tr>\n" );
//
//				for ( final Entry<String, DocumentData> 
//									entry : DocumentMap.get().entrySet()) {
//					final String key = entry.getKey();
//					final DocumentData item = entry.getValue();
//
////					writer.print("\t" + item.time.toString() 
////							+ "\t" + key + "\t" + item.strClass + "\t"
////							+ item.asString(60) + "\r\n");
//					
//					// <a href="host/get?name=my_image">my_image</a>
//					
//					final String strURL = req.getRequestURL() + "?name=" + key;
//					final String strLink = "<a href=\"" + strURL + "\">" + key + "</a>";
//					
//					writer.print( "\t<tr>\n" );
//					writer.print( "\t\t<td>" + item.time.toString() + "</td>\n" );
//					writer.print( "\t\t<td>" + strLink + "</td>\n" );
//					writer.print( "\t\t<td>" + item.strContentType + "</td>\n" );
//					writer.print( "\t\t<td>" + item.strClass + "</td>\n" );
//					writer.print( "\t\t<td>" + item.data.length + "</td>\n" );
//					writer.print( "\t\t<td>" + item.asString() + "</td>\n" );
//					writer.print( "\t</tr>\n" );
//					
//				}
//			    
//
//
//			    writer.print( "</tbody>\n" );
//			    writer.print( "</table>\n" );
//			    writer.print( "<h1 style=\"color: #5e9ca0;\">History</h1>\n" );
//			    writer.print( "<table>\n" );
//			    writer.print( "<tbody>\n" );
//			    writer.print( "<tr>\n" );
//			    writer.print( "<td><strong>Time</strong></td>\n" );
//			    writer.print( "<td><strong>URL</strong></td>\n" );
//			    writer.print( "<td><strong>Parameters</strong></td>\n" );
//			    writer.print( "</tr>\n" );
//			    writer.print( "<tr>\n" );
//			    writer.print( "<td>&nbsp;</td>\n" );
//			    writer.print( "<td>&nbsp;</td>\n" );
//			    writer.print( "<td>&nbsp;</td>\n" );
//			    writer.print( "</tr>\n" );
//			    writer.print( "</tbody>\n" );
//			    writer.print( "</table>\n" );
//			    writer.print( "<p>&nbsp;</p>\n" );
//			    
//			    
//			    
//			    
//				writer.print("DataMap\r\n");
//			
//				writer.print( sbWriter.toString() );
//			
//				
////				
//////				writer.print("\r\nDocuments:\r\n");
////				for ( final Entry<String, DocumentData> 
////									entry : DocumentMap.get().entrySet() ) {
////					final String key = entry.getKey();
////					final DocumentData item = entry.getValue();
////					
////					writer.print( "\t" + item.time.toString()
////							+ "\t" + key
////							+ "\t" + item.strClass 
////							+ "\t" + item.asString( 60 )
////							+ "\r\n" );
////				}
////				
////				writer.print("\r\nHistory:\r\n");
////				for ( final String line : listHistory ) {
////					writer.print("\t" + line +"\r\n");
////				}
////				
////				listHistory.add( req.getRequestURL().toString() );
////				listHistory.add( "\t\t" + req.getQueryString() );
////				if ( null!=strData ) {
////					listHistory.add( "\t\tData: " + strData );
////				}
//				
//				Log.add( "DocumentMap content complete." );
//			}
		} catch ( final Throwable e ) {
//			writer.print("\r\nError: " + e.toString() + "\r\n");
			Log.add( "\r\nError: " + e.toString() );
		}

	
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
			
			writer.print("\r\nParameters:\r\n");
			for ( final Entry<String, String[]> entry : 
						req.getParameterMap().entrySet() ) {
				
				final String strName = entry.getKey();
				final String strValue = req.getParameter( strName );
				writer.print( "\t" + strName + " = " + strValue + "\r\n" );
			}
		
			final String strName = req.getParameter( "name" );
			final String strContentType = req.getParameter( "type" );
			final String strClass = req.getParameter( "class" );
			
			writer.print( "\r\nPOST data:\r\n" );

			{

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
								now, strClass, strContentType, data );
						
						DocumentMap.get().put( strName, doc );
						
						Log.add( "Storing data (" + data.length + " bytes)." );

				        
				    } catch ( final Exception e ) {
						Log.add( "Failed to retrieve POST data." );
				    }
				    
				    
				} else {
					Log.add( "Data not stored because name is empty." );
				}
			}

			
//			listHistory.add( req.getRequestURL().toString() );
//			listHistory.add( "\t\t" + req.getQueryString() );
			
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
		}

	}
	
}