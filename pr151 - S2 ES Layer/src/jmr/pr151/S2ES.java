package jmr.pr151;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jmr.S2Path;
import jmr.S2Properties;


public class S2ES {

	public final static ObjectMapper MAPPER = new ObjectMapper();
	
	public final static int TIMEOUT = 5000;
	
	enum Index {
		STATUS_NODE,
		STATUS_WEATHER,
		EVENT_MOTION,
	}
	
	private final String strURLBase;
	
	private static S2ES instance;
	
//	public S2ES( final String strURLBase ) {
//		this.strURLBase = strURLBase;
//	}

	private S2ES() {
		final S2Properties properties = S2Properties.get();
		final String strESKey = "home.elasticsearch.url";
		final String strESBase = properties.getProperty( strESKey );
		this.strURLBase = strESBase;
	}
	
	public static S2ES get() {
		if ( null == instance ) {
			instance = new S2ES();
		}
		return instance;
	}

	
	public JsonNode retrieveDocument( 	final String strPath,
										final String strBody ) {
		final String strURL = strURLBase + strPath; 
		
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			final HttpPost post = new HttpPost( strURL );
			post.addHeader( "Accept", "application/json" );
			post.setHeader( "Content-Type", "application/json" );
			final StringEntity entityPost = new StringEntity( strBody );
			post.setEntity( entityPost );
		
			final CloseableHttpResponse response = httpclient.execute( post );
//			final int iCode = response.getCode();
			
			final HttpEntity entityResp = response.getEntity();
			final String strResp = EntityUtils.toString( entityResp );
			
			final JsonNode jnRaw = MAPPER.readTree( strResp );
			final JsonNode jnDoc = jnRaw.at( "/hits/hits/0/_source" );
			return jnDoc;
		
		} catch ( final IOException e ) {
			e.printStackTrace();
		} catch ( final ParseException e ) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch ( final Exception e ) {
				e.printStackTrace();
			}
		}
		return null;
	}

	
	public JsonNode retrieveJsonContent( final URL url ) {

		final URLConnection c;
		try {
			c = url.openConnection();
		} catch ( final IOException e ) {
			e.printStackTrace();
			return null;
		}
		
		c.setReadTimeout( TIMEOUT );
		c.setConnectTimeout( TIMEOUT );
		
		try ( final InputStream is = c.getInputStream() ) {

			final JsonNode jn = MAPPER.readTree( is );
			return jn;
			
		} catch ( final IOException e ) {
			e.printStackTrace();
			return null;
		}
	}
	

	public JsonNode retrieveLatestCamMotion() {
		final String strPath = "/event-motion/_search"; 
		final String strBody = 
					"{\n" + 
					"  \"size\": 1,\n" + 
					"  \"sort\": { \"core.time-event-iso\": \"desc\"},\n" + 
					"  \"query\": {\n" + 
					"    \"match_all\": {}\n" + 
					"  }\n" + 
					"}";
		final JsonNode jnDoc = retrieveDocument( strPath, strBody ); 

		if ( null == jnDoc ) {
			return null;
		} else if ( jnDoc.has( "core" ) && jnDoc.has( "image-identify" ) ) {
			return jnDoc;
		} else {
			return null;
		}
	}

	public JsonNode retrieveLatestWeatherForecast() {
		final String strPath = "/status-weather/_search"; 
		final String strBody = 
					"{\n" + 
					"  \"size\": 1,\n" + 
					"  \"sort\": { \"updated\": \"desc\"},\n" + 
					"  \"query\": {\n" + 
					"    \"match_all\": {}\n" + 
					"  }\n" + 
					"}";
		final JsonNode jnDoc = retrieveDocument( strPath, strBody ); 

		if ( null == jnDoc ) {
			return null;
		} else if ( jnDoc.has( "updated" ) && jnDoc.has( "periods" ) ) {
			return jnDoc;
		} else {
			return null;
		}
	}

	
	public static void main( final String[] args ) {
//		final String strURLBase = "<ES-server>";
		
//		final S2Properties properties = S2Properties.get();
//		final String strESKey = "home.elasticsearch.url";
//		final String strESBase = properties.getProperty( strESKey );
//		
//		final S2ES client = new S2ES( strESBase );
		final S2ES client = S2ES.get();
//		final JsonNode jn = client.retrieveLatestWeatherForecast();
		final JsonNode jn = client.retrieveLatestCamMotion();
		
		System.out.println( jn.toPrettyString() );
		
		final JsonNode jnCore = jn.at( "/core" );
		final String strImageFile = jnCore.at( "/file-image" ).asText();
		System.out.println( "   Image filename: " + strImageFile );
		final String strJSONFile = jnCore.at( "/file-json" ).asText();
		System.out.println( "    JSON filename: " + strJSONFile );
		final Set<String> list = S2Path.getLocalAlts( strJSONFile );
		System.out.println( "Alternates:" );
		for ( final String strAlt : list ) {
			System.out.println( "\t" + strAlt );
		}
	}

}
