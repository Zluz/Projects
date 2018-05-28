package jmr.pr121.servlets;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.utils.SystemProperty;

import jmr.pr121.config.Configuration;

//import com.google.apphosting.runtime.jetty9.AppEngineAuthentication;

/*
	http://localhost:8080/hello?name=value&name_2=value_2

	https://pr121-s2gae.appspot.com/hello
 */


@SuppressWarnings("serial")
@WebServlet(
    name = "Status",
    urlPatterns = {"/status"}
)
public class Status extends HttpServlet {

	final static LocalDateTime ldtStart;
	
	static {
		ldtStart = LocalDateTime.now();
	}
	
	
	final static List<String> listLog = new LinkedList<>();

	public static void add( final String strText ) {
		listLog.add( strText );
	}

	
	
	
	@Override
	public void doGet(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  							throws IOException, ServletException {
		Log.add( req );

		final LocalDateTime ldtNow = LocalDateTime.now();
    
		final UserAuth user = new UserAuth( req, resp );
		if ( !user.require( 2 ) ) return;
		if ( user.isAborted() ) return;

		

	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
    
		try {
			writer.print( Status.class.getName() + "\r\n\r\n");

			writer.print("Request Details:");
			writer.print("\r\n\tgetQueryString(): " + req.getQueryString() );
			writer.print("\r\n\tgetRequestURI(): " + req.getRequestURI() );
			writer.print("\r\n\tgetPathInfo(): " + req.getPathInfo() );
			writer.print("\r\n\tgetLocalName(): " + req.getLocalName() );
			writer.print("\r\n");

			writer.print("\r\n");
			writer.print("Request Headers:\r\n");
			{
				final Enumeration<String> names = req.getHeaderNames();
				while ( names.hasMoreElements() ) {
					final String name = names.nextElement();
					final String value = req.getHeader( name );
					writer.print("\t" + name + " = " + value + "\r\n" );
				}
			}


			writer.print("\r\n");
			writer.print("Request Attributes:\r\n");
			{
				final Enumeration<String> names = req.getAttributeNames();
				while ( names.hasMoreElements() ) {
					final String name = names.nextElement();
					final Object value = req.getAttribute( name );
					writer.print( "\t" + name + " = "
							+ "(" + value.getClass().getName() + ") "
							+ value + "\r\n" );
				}
			}


			writer.print("\r\n");
			writer.print("Additional Request Information:\r\n");
			final Principal prinicipal = req.getUserPrincipal();
			writer.print( "\tHttpServletRequest.getUserPrincipal(): " + prinicipal + "\r\n" );
			writer.print( "\t\tPrincipal.class: " + prinicipal.getClass().getName() + "\r\n" );
			writer.print( "\tHttpServletRequest.getLocale(): " + req.getLocale() + "\r\n" );
			try {
				writer.print( "\tHttpServletRequest.getParts().size(): " + req.getParts().size() + "\r\n" );
			} catch ( final ServletException e ) {
				writer.print( "\tHttpServletRequest.getParts().size() raised " + e.toString() + "\r\n" );
			}
			final HttpSession session = req.getSession();
			writer.print( "\treq.getSession(): " + session + "\r\n" );
			writer.print( "\t\tHttpSession.getId(): " + session.getId() + "\r\n" );
//			writer.print( "\t\tHttpSession.getSessionContext(): " + session.getSessionContext() + "\r\n" );
			writer.print( "\t\tHttpSession.getServletContext(): " + session.getServletContext() + "\r\n" );
//			final ServletContext context = session.getServletContext();
//			writer.print( "\t\t\tServletContext: " + context + "\r\n" );
//			writer.print( "\t\t\tServletContext: " + context.get + "\r\n" );
//			writer.print( "\t\tHttpSession.getServletContext(): " + req.au + "\r\n" );
			

			
			writer.print("\r\n");
			writer.print("Object Classes:\r\n");

			final Principal up = req.getUserPrincipal();
			final Locale l = req.getLocale();

			writer.print( "\tHttpServletRequest: " + req.getClass().getName() + "\r\n" );
			if ( null!=up ) {
				writer.print( "\tHttpServletRequest.getUserPrincipal(): " + up.getClass().getName() + "\r\n" );
			} else {
				writer.print( "\tHttpServletRequest.getUserPrincipal() is null\r\n" );
			}
			if ( null!=l ) {
				writer.print( "\tHttpServletRequest.getLocale(): " + l.getClass().getName() + "\r\n" );
			} else {
				writer.print( "\tHttpServletRequest.getLocale() is null\r\n" );
			}

			
			writer.print("\r\n");
			writer.print("App Engine Information:\r\n");

			writer.print( "\tSystemProperty.environment.value(): " 
						+ SystemProperty.environment.value() + "\r\n" );
			writer.print( "\tSystemProperty.version.get(): " 
					+ SystemProperty.version.get() + "\r\n" );
			writer.print("\tInstance time: " + ldtStart.toString() + "\r\n");
			final long lElapsed = ldtNow.toEpochSecond( ZoneOffset.UTC ) 
						- ldtStart.toEpochSecond( ZoneOffset.UTC );
			writer.print("\tTime elapsed: " + lElapsed + "s\r\n");
			
//			if ( SystemProperty.environment.value() == 
//							SystemProperty.Environment.Value.Production ) {
//				   // do something that's production-only
//				 }

			
			writer.print("\r\n");
			writer.print("Loaded Configuration:\r\n");
			{
				for ( final Entry<String, String> 
								entry : Configuration.get().entrySet() ) {
					final String name = entry.getKey();
					final Object value = entry.getValue();
					writer.print( "\t" + name + " = " );
					if ( value instanceof String 
							&& value.toString().contains( "\n" ) ) {
						final String[] items = value.toString().split( "\n" );
						writer.print( "(" + items.length + " items)\r\n" );
						for ( final String item : items ) {
							writer.print( "\t\t\t" + item + "\r\n" );
						}
					} else {
						writer.print( value + "\r\n" );
					}
				}
			}
			writer.print("\r\n");
			final boolean bAccepted = Configuration.get().isBrowserAccepted( req );
			writer.print( "\tConfiguration.isBrowserAccepted(): " + bAccepted + "\r\n" );


			writer.print("\r\n");
			writer.print("History\r\n");
			
			for ( final String line : listLog ) {
				writer.print( line +"\r\n" );
			}
			
			listLog.add( req.getRequestURL().toString() );
			listLog.add( "\t\t" + req.getQueryString() );
			
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
		}

	}
	


	@Override
	public void doPost(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) throws IOException {
		Log.add( this.getClass().getName() + ".doPost()" );
		
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
				        
				        final ByteBuffer bb = ByteBuffer.wrap( data );
				        
				        
				        final String strConfig = UTF_8.decode( bb ).toString();
				        
				        Configuration.get().put( strName, strConfig );
						
						Log.add( "Storing data (" + data.length + " bytes)." );
				        
				    } catch ( final Exception e ) {
						Log.add( "Failed to retrieve POST data." );
				    }
				    
				    
				} else {
					Log.add( "Data not stored because name is empty." );
				}
			}

			
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
		}


	}
	
	
}