package jmr.pr126.comm.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
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
public class HttpListener implements HttpCommConstants {
	
	

	private final static Logger 
				LOGGER = Logger.getLogger( HttpListener.class.getName() );
	
	
	
	public interface Listener {
		public void received( final Map<String,Object> map );
	}
	
	private final static List<Listener> LISTENERS = new LinkedList<>();
	

	private HttpServer server;

	private HttpListener( final int iPort, 
			              final String strEndpoint ) {
		try {
			System.out.println("Hosting port " + iPort);
			final InetSocketAddress port = new InetSocketAddress(iPort);
			server = HttpServer.create(port, 0);

			server.createContext(strEndpoint, new MessageHandler());
			server.setExecutor(null); // creates a default executor
			server.start();

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public HttpListener() {
		this( PORT, ENDPOINT );
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

			try (final OutputStream os = exchange.getResponseBody()) {

				final byte[] bytes = strResponse.getBytes();
				exchange.sendResponseHeaders( 200, bytes.length );

				os.write(bytes);
				os.close();

			} catch (final IOException e) {
				// TODO handle this
				// ignore for now
//        		e.printStackTrace();
			}
		}
	}

	public static void main(final String[] args) {
//		final HttpListener server = new HttpListener(8090, "/test");
		final HttpListener server = new HttpListener();
		server.toString();
		
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
