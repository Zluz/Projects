package jmr.pr151;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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


public class S2ES {

	public final static ObjectMapper MAPPER = new ObjectMapper();
	
	public final static int TIMEOUT = 5000;
	
	enum Index {
		STATUS_NODE,
		STATUS_WEATHER,
		EVENT_MOTION,
	}
	
	private final String strURLBase;
	
	
	public S2ES( final String strURLBase ) {
		this.strURLBase = strURLBase;
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
	

	
	public JsonNode retrieveLatestWeatherForecast() {
		final String strURL = strURLBase + "/status-weather/_search"; 
		final String strBody = 
					"{\n" + 
					"  \"size\": 1,\n" + 
					"  \"sort\": { \"updated\": \"desc\"},\n" + 
					"  \"query\": {\n" + 
					"    \"match_all\": {}\n" + 
					"  }\n" + 
					"}";
		
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
			if ( jnDoc.has( "updated" ) && jnDoc.has( "periods" ) ) {
				return jnDoc;
			} else {
				return null;
			}
			
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

	
	
	public static void main( final String[] args ) {
		final String strURLBase = "<ES-server>";
		final S2ES client = new S2ES( strURLBase );
		final JsonNode jn = client.retrieveLatestWeatherForecast();
		System.out.println( jn.toPrettyString() );
	}

}
