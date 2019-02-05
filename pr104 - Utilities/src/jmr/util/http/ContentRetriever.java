package jmr.util.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.ws.http.HTTPException;

public class ContentRetriever {

	
	final private Map<String,String> mapProperties = new HashMap<>();
	
	final private String strURL;
	
	
	public ContentRetriever( final String strURL ) {
		this.strURL = strURL;
	}

	public static String cleanURL( final String strInput ) {
		if ( null==strInput ) throw new IllegalStateException();
		if ( strInput.isEmpty() ) throw new IllegalStateException();
		
		final int iHostPos = strInput.indexOf( "://" ) + 3;
		if ( iHostPos<0 ) throw new IllegalStateException();
		
		int iParamPos = (strInput + "?").indexOf( "?" );
		
		String strTarget = strInput;
		
		int iDoublePos = strTarget.indexOf( "//", iHostPos );
		while ( iDoublePos>0 && iDoublePos<iParamPos ) {
			strTarget = strTarget.substring( 0, iDoublePos ) 
					+ strTarget.substring( iDoublePos + 1 );
			iDoublePos = strTarget.indexOf( "//", iHostPos );
			iParamPos = (strInput + "?").indexOf( "?" );
		}
		
		return strTarget;
	}
	
	
	
	public void addProperty(	final String strName,
								final String strValue ) {
		mapProperties.put( strName, strValue );
	}
	
	
	public Map<String,String> getProperties() {
		return Collections.unmodifiableMap( mapProperties );
	}
	
	
	/*
	 * _//TODO sometimes getting an HTTP 408
	 * https://stackoverflow.com/questions/14594840/http-client-408-status-code
	 */
	
	public String getContent() throws IOException { // throws Exception {
		
		final URL url = new URL( strURL );
		
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
		
//		conn.setDoOutput( true );
//		conn.setInstanceFollowRedirects( false );
		conn.setRequestMethod( "GET" );
//		conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
		conn.setRequestProperty( "Content-Type", "text/plain"); 
		conn.setRequestProperty( "charset", "utf-8");
//		conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//		conn.setRequestProperty( "Authorization", "Bearer " + strTokenValue );
		for ( final Entry<String, String> entry : mapProperties.entrySet() ) {
			conn.setRequestProperty( entry.getKey(), entry.getValue() );
		}
//		conn.setUseCaches( false );
		
		final int iCode = conn.getResponseCode();
		if ( iCode<200 || iCode>=300 ) {
			throw new HTTPException( iCode ); 
			//( "HTTP code " + iCode + " received " + "for URL " + strURL );
		}
		
		final StringBuffer strbuf = new StringBuffer();

		try ( 	final InputStream is = conn.getInputStream();
				final InputStreamReader isr = new InputStreamReader( is, "UTF-8" );
				final Reader in = new BufferedReader( isr ); ) {
			
	        for ( int c; (c = in.read()) >= 0; ) {
//	            System.out.print((char)c);
	            strbuf.append( (char)c );
	        }
//	        System.out.println();
		} finally {
			conn.disconnect();
		}
        
        return strbuf.toString();
	}
	
	
	
	public String postContent( final String strPost ) throws Exception {
		return postContent( strPost.getBytes( StandardCharsets.UTF_8 ) );
	}

	public String postContent( final byte[] data ) throws Exception {
		
//		byte[] postData       = strURLParams.getBytes( StandardCharsets.UTF_8 );
		int    postDataLength = data.length;
//		String request        = "http://example.com/index.php";
		URL    url            = new URL( strURL );
		
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();           
		conn.setDoOutput( true );
		conn.setInstanceFollowRedirects( false );
		conn.setRequestMethod( "POST" );
//		conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
		conn.setRequestProperty( "Content-Type", "text/plain"); 
		conn.setRequestProperty( "charset", "utf-8");
		conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));

		
		for ( final Entry<String, String> entry : mapProperties.entrySet() ) {
			conn.setRequestProperty( entry.getKey(), entry.getValue() );
		}
		
		
		conn.setUseCaches( false );
		
		try ( final DataOutputStream wr = 
						new DataOutputStream( conn.getOutputStream() )) {
		   wr.write( data );
		}

		final StringBuffer strbuf = new StringBuffer();

		try (   final InputStream is = conn.getInputStream();
				final InputStreamReader isr = new InputStreamReader( is, "UTF-8" );
				final Reader in = new BufferedReader( isr ) ) {
		
	        for ( int c; (c = in.read()) >= 0; ) {
	            System.out.print((char)c);
	            strbuf.append( (char)c );
	        }
	        System.out.println();
		}
		
		conn.disconnect();
        
        final String strResponse = strbuf.toString();

        return strResponse;
	}

	
	
	
	
}
