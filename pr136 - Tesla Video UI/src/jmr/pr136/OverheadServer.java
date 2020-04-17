package jmr.pr136;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class OverheadServer {

	private final HttpServer server;
	
	public final static int PORT = 1080;
	public final static String ENDPOINT = "/overhead"; 

	private final Listener listener;
	

	public static interface Listener {
		public void emitRequestHandled( final String strURL,
										final String strRemote,
										final int iResponse );
	}

	
	public OverheadServer( final Listener listener ) {
		final InetSocketAddress isa = new InetSocketAddress( PORT );
		
		this.listener = listener;
		
		HttpServer serverCand = null;
		try {
			serverCand = HttpServer.create( isa,  0 );
			
			final HttpContext context = 
					serverCand.createContext( ENDPOINT, new RequestHandler() );
			serverCand.setExecutor( null );
			serverCand.start();

			System.out.println( "getPath(): " + context.getPath() );
			final HttpHandler handler = context.getHandler();
			System.out.println( "getHandler(): " + handler );

		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			serverCand = null;
		}
		this.server = serverCand;
	}
	
	
	
	
	private class RequestHandler implements HttpHandler {
		@Override
		public void handle( final HttpExchange exchange ) throws IOException {
			if ( null == exchange ) return;
			
			int iResponse;
			

			final InetAddress addrRemote = exchange.getRemoteAddress().getAddress();
			final String strURI = exchange.getRequestURI().toString();
			final String strRemote = addrRemote.getHostAddress();

			exchange.getResponseHeaders().set( "ContentType", "image/png" );

			try ( final OutputStream os = exchange.getResponseBody() ) {

//				final byte[] bytes = strResponse.getBytes();
				final byte[] bytes = arrImageBuffer;
				exchange.sendResponseHeaders( 200, bytes.length );
				iResponse = 200;

				os.write( bytes );
				os.close();
				
			} catch ( final IOException e ) {
				// TODO handle this
				// ignore for now
//        		e.printStackTrace();

				iResponse = -1;

				UI_TeslaMain.log( e.toString() );
			}

			listener.emitRequestHandled( strURI, strRemote, iResponse );
		}
	}
	
	
	private byte[] arrImageBuffer;
	
	public void prepareImage( final Image image ) {
		if ( null == image ) return;
		if ( image.isDisposed() ) return;
		
		//TODO does all this have to be on the UI thread?
		
		final ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		loader.save( os, SWT.IMAGE_PNG );
		arrImageBuffer = os.toByteArray();
	}
	

	
	public String getHostedURL() {
		final String strURL = "http://localhost:" + PORT + ENDPOINT;
		return strURL;
	}
	


	public static void main( final String[] args ) {
//		final HttpListener server = new HttpListener(8090, "/test");
		final OverheadServer server = new OverheadServer( null );
		server.toString();
		
		System.out.println( "Hosted URL: " + server.getHostedURL() );
		
		for (;;);
	}
	
}
