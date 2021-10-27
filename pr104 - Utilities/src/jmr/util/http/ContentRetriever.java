package jmr.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

//import javax.xml.ws.http.HTTPException;

import org.apache.commons.lang3.StringUtils;

public class ContentRetriever {


	public final static String UTF_8 = "UTF-8";
//	public final static String UTF_8 = StandardCharsets.UTF_8.displayName().toString();

	
	final private Map<String,String> mapProperties = new HashMap<>();
	final private Map<String,String> mapFormValues = new HashMap<>();
	
	final private String strURL;


	private Map<String, List<String>> mapResponseHeaderFields = new HashMap<>();
	private final List<HttpCookie> listCookies = new LinkedList<>();
	
	
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

	public void addPropertyEncoded(	final String strName,
									final String strValue ) {
		try {
			final String strEncoded = URLEncoder.encode( strValue, UTF_8 );
			mapProperties.put( strName, strEncoded );
		} catch ( final UnsupportedEncodingException e ) {
			e.printStackTrace(); // should not happen ..
		}
	}

	
	public Map<String,String> getProperties() {
		return Collections.unmodifiableMap( mapProperties );
	}

	
	public void addFormValue(	final String strName,
								final String strValue ) {
		mapFormValues.put( strName, strValue );
	}
	
	
	

	public String getContent() throws IOException { // throws Exception {
		return getContent( ContentType.TEXT_PLAIN );
	}

	
	
	/*
	 * _//TODO sometimes getting an HTTP 408
	 * https://stackoverflow.com/questions/14594840/http-client-408-status-code
	 */


	public String getContent( final ContentType type ) throws IOException { // throws Exception {
		
		final URL url = new URL( strURL );
		
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
		
//		conn.setDoOutput( true );
//		conn.setInstanceFollowRedirects( false );
		
		conn.setRequestMethod( "GET" );
//		conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
		conn.setRequestProperty( "Content-Type", type.getMimeType() ); 
//		conn.setRequestProperty( "Content-Type", "application/json"); 
		conn.setRequestProperty( "charset", "utf-8" );
//		conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//		conn.setRequestProperty( "Authorization", "Bearer " + strTokenValue );
		for ( final Entry<String, String> entry : mapProperties.entrySet() ) {

//			conn.setRequestProperty( entry.getKey(), entry.getValue() );

//			final String strKey = URLEncoder.encode( entry.getKey(), UTF_8 );
//			final String strValue = URLEncoder.encode( entry.getValue(), UTF_8 );

			final String strKey = entry.getKey();
			final String strValue = entry.getValue();

			conn.setRequestProperty( strKey, strValue );
		}
//		conn.setUseCaches( false );
		
		final int iCode = conn.getResponseCode();
		if ( iCode<200 || iCode>=300 ) {
//			throw new HTTPException( iCode );
//			throw new RuntimeException( "Exception, HTTP " + iCode );
			//( "HTTP code " + iCode + " received " + "for URL " + strURL );
			System.err.println( "HTTP GET returned code " + iCode );
		}
		
		mapResponseHeaderFields.clear();
		mapResponseHeaderFields = conn.getHeaderFields();
		
		// https://www.baeldung.com/java-http-request
		final String strCookies = conn.getHeaderField( "Set-Cookie" );
		listCookies.clear();
		listCookies.addAll( HttpCookie.parse( strCookies ) );
		
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
	

	public List<HttpCookie> getCookies() {
		return Collections.unmodifiableList( this.listCookies );
	}
	
	public String postContent( final ContentType type,
							   final String strPost ) throws Exception {
		return postContent( type, strPost.getBytes( StandardCharsets.UTF_8 ) );
	}
	
	
	public String postForm() throws Exception {
		final StringJoiner sj = new StringJoiner( "&" );
		for ( final Entry<String, String> entry : this.mapFormValues.entrySet() ) {
			final String strKey = URLEncoder.encode( entry.getKey(), UTF_8 );
			final String strValue = URLEncoder.encode( entry.getValue(), UTF_8 );
			sj.add( strKey + "=" + strValue );
		}
		final byte[] bytes = sj.toString().getBytes( StandardCharsets.UTF_8 );
		final String strResult = this.postContent( ContentType.POST_FORM, bytes );
		return strResult;
	}
	


	public String postContent( final ContentType type,
							   final byte[] data ) throws Exception {
		
//		byte[] postData       = strURLParams.getBytes( StandardCharsets.UTF_8 );
		int    postDataLength = data.length;
//		String request        = "http://example.com/index.php";
		URL    url            = new URL( strURL );
		
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();           
		conn.setDoOutput( true );
		conn.setInstanceFollowRedirects( false );
		conn.setRequestMethod( "POST" );
		
		//NOTE: does something somewhere already need this to be "text/plain" ?
		conn.setRequestProperty( "Content-Type", type.getMimeType() ); 
//		conn.setRequestProperty( "Content-Type", "text/plain");
		
		conn.setRequestProperty( "charset", "utf-8");
		conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));

		for ( final Entry<String, String> entry : mapProperties.entrySet() ) {

//			final String strKey = URLEncoder.encode( entry.getKey(), UTF_8 );
//			final String strValue = URLEncoder.encode( entry.getValue(), UTF_8 );

			// cannot be URL Encoded .. messes up Tesla authentication
			// should be  :  Authorization=bearer 01234abcd
			// encoded as :  Authorization=bearer+01234abcd 
			
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();

			conn.setRequestProperty( strKey, strValue );
		}
		
		conn.setUseCaches( false );
		
		try ( final OutputStream os = conn.getOutputStream() ) {
		   os.write( data );
		}

		final StringBuffer strbuf = new StringBuffer();

		try (   final InputStream is = conn.getInputStream();
				final InputStreamReader isr = new InputStreamReader( is, "UTF-8" );
				final Reader in = new BufferedReader( isr ) ) {
		
	        for ( int c; (c = in.read()) >= 0; ) {
//	            System.out.print((char)c);
	            strbuf.append( (char)c );
	        }
//	        System.out.println();
		}
		
		final int iResponseCode = conn.getResponseCode();
		System.out.println( "Response code: " + iResponseCode );
		
		this.mapResponseHeaderFields = conn.getHeaderFields();
		
		System.out.println( "Response header fields:" );
		this.mapResponseHeaderFields.keySet().forEach( strKey-> {
			if ( null != strKey ) {
				System.out.println( "\t" + strKey );
				final List<String> listFields = 
								this.mapResponseHeaderFields.get( strKey );
				final String strFields = listFields.toString();
				final String strLower = strFields.toLowerCase();
				if ( strLower.contains( "code=" ) ) {
					System.out.println( "\t\t-->  " + strFields );
				} else if ( strLower.contains( "code" ) ) {
					System.out.println( "\t\t?    " + strFields );
				}
			}
		} );
		
		conn.disconnect();
        
        final String strResponse = strbuf.toString();

        return strResponse;
	}

	
	public Map<String, List<String>> getResponseHeaderFields() {
		return Collections.unmodifiableMap( this.mapResponseHeaderFields );
	}
	
	
	
	public static void main( final String[] args ) throws IOException {
		
//		final String strURL = "http://wiki/wiki/extensions/BlueSpiceFoundation/resources/bluespice/images/bs-logo.png";
//		final String strURL = "http://192.168.6.1/image/logo.png";
		final String strURL = "http://192.168.6.1/DEV_show_device.htm";
		
		final ContentRetriever retriever = new ContentRetriever( strURL );
		final String strContent = retriever.getContent( ContentType.TEXT_PLAIN );
		System.out.println( 
				"Recieved " + strContent.getBytes().length + " bytes." );
		System.out.println( StringUtils.abbreviate( strContent, 800 ) );
	}
	
}
