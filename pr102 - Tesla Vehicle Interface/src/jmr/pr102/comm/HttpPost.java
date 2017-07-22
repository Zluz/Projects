package jmr.pr102.comm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jmr.pr102.TeslaConstants;

public class HttpPost implements TeslaConstants {


	private final String strURL;
	private final TeslaLogin login;
	
	public HttpPost(	final String strURL,
						final TeslaLogin login ) {
		this.strURL = strURL;
		this.login = login;
	}
	
	public String postContent( final String strPost ) throws Exception {
		
//		byte[] postData       = strURLParams.getBytes( StandardCharsets.UTF_8 );
		byte[] postData       = strPost.getBytes( StandardCharsets.UTF_8 );
		int    postDataLength = postData.length;
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

		final String strTokenValue;
		if ( null==this.login ) {
//			strTokenValue = DUMMY_AUTH_TOKEN_VALUE;
			strTokenValue = null;
		} else if ( this.login.isAuthenticating() ) {
			strTokenValue = null;
		} else {
			strTokenValue = this.login.getTokenValue();
		}
		
		if ( null!=strTokenValue ) {
			final String strTokenType = login.getTokenType();
			final String strTokenString = 
					strTokenType + " " + strTokenValue;
//					HEADER_AUTHORIZATION_BEARER + " " + strTokenValue;
			conn.setRequestProperty( HEADER_AUTHORIZATION, strTokenString );

//			conn.getHeaderFields().put( HEADER_AUTHORIZATION, 
//					Collections.singletonList( strTokenString ) );
		}
		
		conn.setUseCaches( false );
		
		try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream() )) {
		   wr.write( postData );
		}

        final InputStream is = conn.getInputStream();
		final InputStreamReader isr = new InputStreamReader( is, "UTF-8" );
		final Reader in = new BufferedReader( isr );

		StringBuffer strbuf = new StringBuffer();
		
        for (int c; (c = in.read()) >= 0;) {
            System.out.print((char)c);
            strbuf.append( (char)c );
        }
        System.out.println();
        
        final String strResponse = strbuf.toString();

        return strResponse;
	}

	
	
}
