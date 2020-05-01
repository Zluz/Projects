package jmr.pr126.comm.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.json.JsonException;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


//from
//StarHost:jmr/home/comm/http/HttpAtomConsumer.java
@SuppressWarnings("restriction")
public class HttpListener implements HttpCommConstants {
	
	

	private final static Logger 
				LOGGER = Logger.getLogger( HttpListener.class.getName() );
	
	
	public interface Listener {
		public void received( final Map<String,Object> map );
	}
	
	private final static List<Listener> LISTENERS = new LinkedList<>();
	
	
	private static HttpListener instance;
	

	private final HttpServer server;
	private String strIP;
	private final int iPort;
	private final String strEndpoint;
	

	
	private HttpListener( final int iPort, 
			              final String strEndpoint,
			              final boolean bQuiet ) {
		this.iPort = iPort;
		this.strEndpoint = strEndpoint;
		HttpServer serverCand = null;
		try {
			System.out.println( "Hosting port " + iPort );
			final InetSocketAddress port = new InetSocketAddress(iPort);
			serverCand = HttpServer.create(port, 0);

			final com.sun.net.httpserver.HttpContext context = 
					serverCand.createContext( strEndpoint, new MessageHandler() );
			serverCand.setExecutor( null ); // creates a default executor
			serverCand.start();
			
			System.out.println( "getPath(): " + context.getPath() );
			final HttpHandler handler = context.getHandler();
			System.out.println( "getHandler(): " + handler );

			this.addHostRegistryListener();
			
		} catch ( final IOException e ) {
			
			// if ( ! bQuiet ) // ? 
			if ( e.toString().contains( "already in use: bind" ) ) {
				System.err.println( "Port " + iPort + " appears to already "
						+ "in use. Cannot attach listener." );
			} else {
				e.printStackTrace();
			}
			
			serverCand = null;
		}
		this.server = serverCand;
	}
	
	
	public boolean isListening() {
		return ( null != this.server );
	}
	
	
	private void addHostRegistryListener() {
		this.registerListener( new Listener() {
			@Override
			public void received( final Map<String, Object> map ) {
				
//				LOGGER.info( ()-> "HttpListener-Listener.received()" );
				
				if ( null==map ) return;
				if ( ! map.containsKey( KEY_EVENT_SUBJECT ) ) return;
				
				final String strSubject = 
									map.get( KEY_EVENT_SUBJECT ).toString();

				LOGGER.info( ()-> "    Listener.received() - "
										+ "subject: " + strSubject );

				if ( VALUE_LISTENER_ACTIVATED.equals( strSubject ) ) {
					
					final String strHostAlias = 
									map.get( KEY_HOST_ALIAS ).toString();
					final String strHostURL = 
									map.get( KEY_HOST_URL ).toString();

					LOGGER.info( ()-> "    Listener.received() - "
										+ "Alias: " + strHostAlias + ", "
										+ "URL: " + strHostURL );

					try {
						final URL url = new URL( strHostURL );
					
						HostRegistry.getInstance().register( 
														strHostAlias, url );
						
					} catch ( final MalformedURLException url ) {
						LOGGER.warning( ()-> 
								"Malformed URL in LISTENER_ACTIVATED event.");
					}
				}
			}
		});
	}


	private HttpListener( final int iPort ) {
		this( iPort, ENDPOINT, false );
	}
	
	
	public synchronized static HttpListener getInstance( final int iPort ) {
		if ( null==instance ) {
			final HttpListener listener = new HttpListener( iPort );
			if ( listener.isListening() ) {
				instance = listener;
			} else {
				return null;
			}
		}
		return instance;
	}
	
	
	
	public void registerListener( final Listener listener ) {
		LISTENERS.add( listener );
	}
	
	
	public static void process( final String strData ) {

		final Map<String,Object> map;
		
		try {
			
			final String strParsable;
			if ( strData.contains( PARAMETER + "=" ) ) {
				strParsable = StringUtils.substring( 
									strData, strData.indexOf( "=" ) + 1 );
			} else {
				strParsable = strData;
			}
			
			final JsonParser parser = new JsonParser();
			final JsonElement je = parser.parse( strParsable );
			if ( null!=je && je.isJsonObject() ) {
				final JsonObject jo = je.getAsJsonObject();
				final Gson gson = new Gson();
				map = gson.fromJson( jo, Map.class );
			} else {
				map = null;
			}
		} catch ( final JsonException e ) {
			LOGGER.warning( ()-> "Encountered " + e.toString() 
							+ " while trying to process received data: " 
							+ StringUtils.abbreviate( strData, 40 ) );
			return;
		}
		
		if ( null!=map ) {
			
//			LOGGER.info( ()-> "Processing data (" + map.size() + " entries)" );
			
			for ( final Listener listener : LISTENERS ) {
				listener.received( map );
			}
		}
	}
	

	static class MessageHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (null == exchange)
				return;

//			final InetAddress addrRemote = exchange.getRemoteAddress().getAddress();
			final URI uri = exchange.getRequestURI();
			final String strURI = exchange.getRequestURI().toString();

			final String strMessage = uri.getQuery();
			
			HttpListener.process( strMessage );

//			UserInterfaceRemote.processMessage( strMessage );
//			UserInterfaceRemote.updateLines( strMessage );

			final String strResponse = Integer
					.toString(strURI.getBytes().length) + " byte(s) received.";

			try ( final OutputStream os = exchange.getResponseBody() ) {

				final byte[] bytes = strResponse.getBytes();
				exchange.sendResponseHeaders( 200, bytes.length );

				os.write( bytes );
				os.close();

			} catch ( final IOException e ) {
				// TODO handle this
				// ignore for now
//        		e.printStackTrace();
			}
		}
	}
	
	
	
	public String getHostedURL() {
		
//		final String strIP = Notifier.getInstance().getLocalIP();
		
		if ( StringUtils.isEmpty( this.strIP ) ) {
			LOGGER.severe( ()-> "IP not set in getHostedURL()." );
			return "";
		}
		
		final String strURL = "http://" + this.strIP + ":" + this.iPort 
										+ strEndpoint;
		return strURL;
	}
	

	public void setIP( final String strIP ) {
		this.strIP = strIP;
	}
	
	public String getLocalIP() {
		return this.strIP;
	}
	
	public void close() {
		if ( null != server ) {
			this.server.stop( 0 );
		}
	}
	
	

	public static void main( final String[] args ) {
//		final HttpListener server = new HttpListener(8090, "/test");
		final HttpListener server = new HttpListener( 8090 );
		server.toString();
		
		System.out.println( "Hosted URL: " + server.getHostedURL() );
		
		server.registerListener( new Listener() {
			@Override
			public void received( final Map<String,Object> map ) {
				System.out.println( "Data recieved" );
				for ( final Entry<String, Object> entry : map.entrySet() ) {
					System.out.println( "\t" 
								+ entry.getKey() + " = " + entry.getValue() );
				}
			}
		} );
		
		for (;;);
	}
}
