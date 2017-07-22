package jmr.pr102.comm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGet {

	private final String strURL;
	private final TeslaLogin login;
	
	public HttpGet(	final String strURL,
					final TeslaLogin login ) {
		this.strURL = strURL;
		this.login = login;
	}
	

	public String getContent() throws Exception {
		
		final String strTokenValue = login.getTokenValue();
		final String strTokenType = login.getTokenType();

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
		conn.setRequestProperty( "Authorization", strTokenType + " " + strTokenValue );
//		conn.setUseCaches( false );
		
		final int iCode = conn.getResponseCode();
		if ( iCode<200 || iCode>=300 ) {
			throw new Exception( "HTTP code " + iCode + " received." );
		}
		
        final InputStream is = conn.getInputStream();
		final InputStreamReader isr = new InputStreamReader( is, "UTF-8" );
		final Reader in = new BufferedReader( isr );

		final StringBuffer strbuf = new StringBuffer();
		
        for (int c; (c = in.read()) >= 0;) {
//            System.out.print((char)c);
            strbuf.append( (char)c );
        }
//        System.out.println();
        
        return strbuf.toString();
	}
	
	
}
