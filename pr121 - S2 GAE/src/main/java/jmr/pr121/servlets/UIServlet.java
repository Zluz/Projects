package jmr.pr121.servlets;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.geronimo.mail.util.Base64;

import com.google.appengine.api.utils.SystemProperty;

import jmr.pr121.config.Configuration;
import jmr.pr121.storage.ClientData;
import jmr.pr121.storage.GCSHelper;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileWriter;
import jmr.util.http.ContentType;

//import com.google.apphosting.runtime.jetty9.AppEngineAuthentication;

/*
	http://localhost:8080/hello?name=value&name_2=value_2

	https://pr121-s2gae.appspot.com/hello
 */


@SuppressWarnings("serial")
@WebServlet(
    name = "User Interface",
    urlPatterns = {	"/ui",
					"/ui/map",
					"/ui/gcs",
					"/ui/tesla",
					"/ui/input",
    				"/ui/log" }
)
public class UIServlet extends HttpServlet {

	final static LocalDateTime ldtStart;
	private Input pageInput;
	private TeslaUIServlet pageTesla;
	private GCSListingServlet pageGCS;
	private DocumentMapServlet pageMap;
	
	static {
		ldtStart = LocalDateTime.now();
	}
	
	
	
	@Override
	public void doGet(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  							throws IOException, ServletException {
		Log.add( this.getClass().getName() + ".doGet()" );		
		Log.add( req );

		final LocalDateTime ldtNow = LocalDateTime.now();

		// authenticate, authorize
		final UserAuth user = new UserAuth( req, resp );
		if ( !user.require( 2 ) ) return;
		if ( user.isAborted() ) return;
		
		final ClientData client = ClientData.register( req );
		
		
//		for ( final Entry<String, String[]> 
//					entry : req.getParameterMap().entrySet() ) {
//		}
		
		final Map<ParameterName, String> params = 
				ParameterName.getEnumMapOf( req.getParameterMap().entrySet() );
		final String strURL = req.getRequestURL().toString();
		params.put( ParameterName.REQUEST_URL, strURL );
		params.put( ParameterName.REQUEST_BASE, 
				strURL.split( "/" )[0] + "//" + strURL.split( "/" )[2] );
		
		
		

		
		

		final String strURI = req.getRequestURI().trim();
		
		Log.add( "Handling UI request.." );
		Log.add( "\tstrURI = " + strURI );
		Log.add( "\tparameters:" );
		for ( final Entry<ParameterName, String> entry : params.entrySet() ) {
			Log.add( "\t\t" + entry.getKey() + " = " + entry.getValue() );
		}
		
		
		if ( strURI.startsWith( "/ui/map" ) ) {
			
			if ( null==pageMap ) {
				pageMap = new DocumentMapServlet();
			}
			pageMap.doGet( params, resp, client );
			
			return;
		} else if ( strURI.startsWith( "/ui/gcs" ) ) {

			if ( null==pageGCS ) {
				pageGCS = new GCSListingServlet();
			}
			pageGCS.doGet( params, resp, client );
			
			return;
		} else if ( strURI.startsWith( "/ui/tesla" ) ) {

			if ( null==pageTesla ) {
				pageTesla = new TeslaUIServlet();
			}
			pageTesla.doGet( params, resp, client );
			
			return;
		} else if ( strURI.startsWith( "/ui/input" ) ) {
			
			if ( null==pageInput ) {
				pageInput = new Input();
			}
			pageInput.doGet( params, resp, client );
			
			return;
		}
		
		
		
		
		
		
		
		
		
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
    
		try {
			writer.print( UIServlet.class.getName() + "\r\n\r\n");

			writer.print("Request Details:");
			writer.print("\r\n\tgetQueryString(): " + req.getQueryString() );
			writer.print("\r\n\tgetRequestURI(): " + req.getRequestURI() );
			writer.print("\r\n\tgetPathInfo(): " + req.getPathInfo() );
			writer.print("\r\n\tgetLocalName(): " + req.getLocalName() );
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
			final Principal pu = req.getUserPrincipal();
			writer.print( "\tHttpServletRequest.getUserPrincipal(): " + pu + "\r\n" );
			if ( null!=pu ) {
				writer.print( "\t\tPrincipal.class: " + pu.getClass().getName() + "\r\n" );
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
					writer.print( "\t" + name + " = " + value + "\r\n" );
				}
			}
			writer.print("\r\n");
			final boolean bAccepted = Configuration.get().isBrowserAccepted( req );
			writer.print( "\tConfiguration.isBrowserAccepted(): " + bAccepted + "\r\n" );


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
				        
						final String strType = req.getParameter( "type" );
						final ContentType type = ContentType.getContentType( strType );
						if ( null!=type ) {
							Log.add( "Saving to GCS: " + strName );
							
							//TODO assume base-64 encoded ..
							//     in the future the request should specify.
//							final String strDataEnc = new String( data );
//							final String strDataTrunc = 
//									strDataEnc.substring( strDataEnc.indexOf( ',' ) + 1 );

//							final byte[] arrDecoded = Base64.decode( strDataTrunc.getBytes() );

							int i=0;
							while ( i<data.length && data[i]!=',' ) {
								i++;
							}
							
							final int iOffs = i;
							final byte[] arrTrunc = new byte[ data.length - i ];
//							System.arraycopy( data, i+1, arrTrunc, 0, 
//													arrTrunc.length - i );
							for ( i=0; i<data.length - iOffs; i++ ) {
								arrTrunc[ i ] = data[ i+iOffs ];
							}

							final byte[] arrDecoded = Base64.decode( arrTrunc );

							final GCSFactory factory = GCSHelper.GCS_FACTORY;
							final GCSFileWriter file = factory.create( strName, type );
							file.upload( arrDecoded );

							Log.add( "Saved PNG to GCS (" + arrDecoded.length + " bytes)." );

							final String strFileRaw = strName + ".raw";
							final GCSFileWriter fileRaw = factory.create( 
										strFileRaw, ContentType.BINARY );
							fileRaw.upload( data );
							
							Log.add( "Saved RAW to GCS (" + data.length + " bytes)." );
						}
				        
						if ( data.length < 1024 * 2 ) {
							Configuration.get().put( strName, strConfig );
							Log.add( "Saved to config (" + data.length + " bytes)." );
						}
				        
				    } catch ( final Exception e ) {
						Log.add( "Failed to retrieve POST data." );
						Log.add( ExceptionUtils.getStackTrace( e ));
				    }
				    
				    
				} else {
					Log.add( "Data not stored because name is empty." );
				}
			}

			
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
		}

		writer.close();
	}
	
	
}