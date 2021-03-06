package jmr.pr121.servlets;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.appengine.api.utils.SystemProperty;

import jmr.pr121.config.Configuration;
import jmr.pr121.storage.ClientData;
import jmr.pr121.storage.ClientData.Key;
import jmr.pr122.DocMetadataKey;

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
			writer.print("\r\n\tgetRequestURI(): " + req.getRequestURI() );
			writer.print("\r\n\tgetQueryString(): " + req.getQueryString() );
			writer.print("\r\n\tgetProtocol(): " + req.getProtocol() );
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
			if ( null!=prinicipal ) {
				writer.print( "\t\tPrincipal.class: " + prinicipal.getClass().getName() + "\r\n" );
			}
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
			writer.print("Registered Clients:\r\n");
			for ( final Entry<String, ClientData> entry : 
										ClientData.CLIENTS.entrySet() ) {
				final String strSession = entry.getKey();
				final ClientData client = entry.getValue();

				writer.print("\tSession: " + strSession + "\r\n" );

				writer.print("\t\tUser-Agent: " + client.getUserAgent() + "\r\n" );
//				writer.print("\t\tClient info: " + client.getClientInfo() + "\r\n" );
				
				
				final Map<Key, String> mapInfo = client.getClientInfoMap();
				writer.print("\t\tClient info map: " + mapInfo.size() + " items\r\n" );
				for ( final Entry<Key, String> entryInfo : mapInfo.entrySet() ) {
					final String strKey = entryInfo.getKey().getKey();
					final String strValue = entryInfo.getValue();
					writer.print("\t\t\t" + strKey + " = " + strValue + "\r\n" );
				}
				
				final List<String> listInfo = client.getClientInfoList();
				if ( listInfo.size() > 4 ) {
					client.clearClientInfo();
				}
				writer.print("\t\tClient info list: " + listInfo.size() + " items\r\n" );
				for ( final String strInfo : listInfo ) {
					writer.print("\t\t\t" + strInfo + "\r\n" );
				}
				
				
			}

			
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
//			writer.print( "\tSystemProperty.applicationId.key(): " 
//						+ SystemProperty.applicationId.key() + "\r\n" );
//			writer.print( "\tSystemProperty.applicationId.get(): " 
//						+ SystemProperty.applicationId.get() + "\r\n" );
			writer.print( "\tSystemProperty.version.get(): " 
						+ SystemProperty.version.get() + "\r\n" );
			
//			java.lang.NoClassDefFoundError: Could not initialize class com.google.appengine.spi.ServiceFactoryFactory$RuntimeRegistry
//			final OAuthService oauth = com.google.appengine.api.oauth
//					.OAuthServiceFactory.getOAuthService();
//			writer.print( "\tOAuthService.toString(): " 
//						+ oauth.toString() + "\r\n" );
//			writer.print( "\tOAuthService.getCurrentUser(): " 
//						+ oauth.getCurrentUser() + "\r\n" );
//			writer.print( "\tOAuthService.getOAuthConsumerKey(): " 
//					+ oauth.getOAuthConsumerKey() + "\r\n" );

//			writer.print( "\tCapabilities\r\n" ); 
//			final CapabilitiesService cs = com.google.appengine.api.capabilities
//					.CapabilitiesServiceFactory.getCapabilitiesService();
//			writer.print( "\t\tCapabilitiesService: " + cs.toString() + "\r\n" );
//			cs.
//			com.google.appengine.api.capabilities.
//			for ( final CapabilityStatus status : 
//					com.google.appengine.api.capabilities.CapabilityStatus.values() ) {
//				final int value = cs.getStatus( status. );
//				writer.print( "\t\t" + status.name() + ": " +  
//			}

			
//			if ( SystemProperty.environment.value() == 
//							SystemProperty.Environment.Value.Production ) {
//				   // do something that's production-only
//				 }

			writer.print("\tInstance start : " + ldtStart.toString() + "\r\n");
			final long lElapsed = ldtNow.toEpochSecond( ZoneOffset.UTC ) 
						- ldtStart.toEpochSecond( ZoneOffset.UTC );
			writer.print("\tTime now       : " + ldtNow.toString() + "\r\n");
			writer.print("\tTime elapsed   : " + lElapsed + "s\r\n");
			

			
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
			writer.print("Cloud Storage:\r\n");


			writer.print("\r\n");
			writer.print("Java Runtime Information:\r\n");

			writer.print( "\tRuntime.toString(): " 
						+ Runtime.getRuntime().toString() + "\r\n" );
			
			writer.print( "\tSystem properties:\r\n" );
			final Properties properties = System.getProperties();
			for ( final Entry<Object, Object> entry : properties.entrySet() ) {
				writer.print( "\t\t" + entry.getKey() + " = " + entry.getValue() + "\r\n" ); 
			}
			
			final ClassLoader cl = Status.class.getClassLoader();

			writer.print("\r\n");
			writer.print( "\tAvailable Classes:\r\n" );
			final List<String> listClasses = getAllClasses( cl );
//			Collections.sort( listClasses );
			for ( final String strClass : listClasses ) {
				writer.print( "\t\t" + strClass + "\r\n" );
			}
			
			writer.print("\r\n");
			writer.print( "\tLoaded JARs (discovered):\r\n" );
			if ( cl instanceof URLClassLoader ) {
				final URL[] urls = ((URLClassLoader) cl).getURLs();
				for ( final URL url : urls ) {
					writer.print( "\t\t" + url.toString() + "\r\n" );
				}
			} else {
				writer.print( "\t\tClassLoader is not URLClassLoader.\r\n" ); 
			}
			
			

			writer.print("\r\n");
			writer.print("History\r\n");
			
			for ( final String line : listLog ) {
				writer.print( line +"\r\n" );
			}
			
			listLog.add( req.getRequestURL().toString() );
			listLog.add( "\t\t" + req.getQueryString() );
			
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
			writer.print("\r\n" + ExceptionUtils.getStackTrace( e ) + "\r\n");
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
	
	
	public static List<String> getAllClasses( final ClassLoader cl ) {

		final List<String> list = new LinkedList<>();
		try {
			Class classCL = cl.getClass();
			while ( classCL != java.lang.ClassLoader.class ) {
				classCL = classCL.getSuperclass();
			}
			final Field fieldClasses = classCL.getDeclaredField( "classes" );
			fieldClasses.setAccessible( true );
			final Vector vClasses = (Vector) fieldClasses.get( cl );
			for ( final Object item : vClasses ) {
				list.add( item.toString() );
			}
		} catch ( final Throwable t ) {
//			writer.print( "\tERROR: " + t.toString() + "\r\n" );
			list.add( "*** ERROR: " + t.toString() );
		}

		return list;
	}
	
	
}