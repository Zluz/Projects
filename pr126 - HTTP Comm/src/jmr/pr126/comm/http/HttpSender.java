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
public class HttpSender implements HttpCommConstants {
	

	private final static Logger 
				LOGGER = Logger.getLogger( HttpSender.class.getName() );
	
	
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
					+ ENDPOINT + "?" + PARAMETER + "=" 
							+ URLEncoder.encode( strJson ); 
		
		final URLReader reader = new URLReader( strURL );
		final String strResult = reader.getContent();
		return ( null!=strResult );
	}

	
	public void postHostActivated( final String strDestination,
								   final String strHostAlias,
								   final String strURL ) {
		final Map<String,Object> map = new HashMap<>();
		map.put( KEY_EVENT_SUBJECT, VALUE_LISTENER_ACTIVATED );
		map.put( KEY_HOST_ALIAS, strHostAlias );
		map.put( KEY_HOST_URL, strURL );
		
		LOGGER.info( ()-> "HttpSender.postHostActivated(), "
									+ "strAlias = " + strHostAlias );
		
		this.send( strDestination, map );
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