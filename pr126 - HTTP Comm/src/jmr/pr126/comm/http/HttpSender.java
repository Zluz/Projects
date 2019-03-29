package jmr.pr126.comm.http;


import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;


// from
// StarHost:jmr/home/comm/http/URLReader.java
public class HttpSender {
	

	private final static Logger 
				LOGGER = Logger.getLogger( HttpSender.class.getName() );
	
	
	
	
	public enum Type { 
		CLICK, TEXTEDIT, 
		SHUTDOWN, 
	};

	private static HttpSender instance;

	private String strHost;

	
	
	public static HttpSender get() {
		if ( null==instance ) {
			instance = new HttpSender();
		}
		return instance;
	}
	
	
	public void setHost( final String strHost ) {
		if ( null==strHost ) return;
		if ( strHost.isEmpty() ) return;
		this.strHost = strHost;
	}
	
	
	public boolean send( final String strDestination,
						 final Map<String,Object> map ) {
		
		final Gson gson = new Gson();
		final String strJson = gson.toJson( map );
		
		final URL url = HostRegistry.getInstance().getHost( strDestination );
		if ( null == url ) {
			LOGGER.warning( ()-> 
					"Unrecognized server destination: " + strDestination );
			return false;
		}
		
		final String strURL = url.toString() 
					+ HttpListener.ENDPOINT 
					+ "?" + HttpListener.PARAMETER + "=" 
							+ URLEncoder.encode( strJson ); 
		
		final URLReader reader = new URLReader( strURL );
		final String strResult = reader.getContent();
		return ( null!=strResult );
	}
	
	
	public boolean send(	final Type type,
							final String strID,
							final String strData ) {
		if ( null==strHost ) return false;
		
		@SuppressWarnings("deprecation")
		final String strURL = 
				"http://" + strHost + "/atom?Type=" + type.name() 
						+ "&id=" + URLEncoder.encode( strID )
						+ "&data=" + URLEncoder.encode( strData );
		
		final URLReader reader = new URLReader( strURL );
		final String strResult = reader.getContent();
		return ( null!=strResult );
	}
	
	
	public static void main( final String[] args ) 
											throws MalformedURLException {
		final URL url = new URL( "http://localhost:" + HttpListener.PORT );
		HostRegistry.getInstance().register( "local", url );
		
		final Map<String,Object> map = new HashMap<>();
		map.put( "name_01", "value_01" );
		map.put( "date", new Date().toString() );
		
		final HttpSender sender = new HttpSender();
		sender.send( "local", map );
	}
	
	
	
}