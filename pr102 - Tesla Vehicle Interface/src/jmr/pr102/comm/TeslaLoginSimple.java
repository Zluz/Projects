package jmr.pr102.comm;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.pr102.TeslaConstants;
import jmr.util.http.ContentRetriever;
import jmr.util.http.ContentType;
import jmr.util.transform.JsonUtils;

public class TeslaLoginSimple implements TeslaLogin, TeslaConstants {

	
	public static final TeslaLoginSimple 
				DUMMY_LOGIN = new TeslaLoginSimple( null, null );
	
	
	private final String strUsername;
	private final char[] arrPassword;
	
	protected String strTokenType = null;
	protected String strTokenValue = null;
	
	private boolean bIsAuthenticating = false;
	
	
	private Map<String,String> mapLoginDetails = null;
	
	
	
	public TeslaLoginSimple(	final String strUsername,
								final char[] arrPassword ) {
		this.strUsername = strUsername;
		this.arrPassword = arrPassword;
	}
	
	
	

	public Map<String,String> login() throws Exception {
		if ( DUMMY_LOGIN==this ) return null;

		this.bIsAuthenticating = true;
		
		final String strURL =
				URL_BASE_TESLA_API_PROD + "oauth/token"
					+ "?grant_type=password"
					+ "&client_id=" + REQUEST_CLIENT_ID
					+ "&client_secret=" + REQUEST_CLIENT_SECRET
					+ "&email=" + URLEncoder.encode( strUsername, UTF8 )
					+ "&password="  + URLEncoder.encode( 
									new String( arrPassword ), UTF8 );
	
		final ContentRetriever retriever = new ContentRetriever( strURL );
		

		final String strTokenValue;
		if ( this.isAuthenticating() ) {
			strTokenValue = null;
		} else {
			strTokenValue = this.getTokenValue();
		}
		
		if ( null!=strTokenValue ) {
			final String strTokenType = this.getTokenType();
			final String strTokenString = 
					strTokenType + " " + strTokenValue;
//					HEADER_AUTHORIZATION_BEARER + " " + strTokenValue;
			retriever.addProperty( HEADER_AUTHORIZATION, strTokenString );

//			conn.getHeaderFields().put( HEADER_AUTHORIZATION, 
//					Collections.singletonList( strTokenString ) );
		}
		
		
		
		
//		final String strResponse = post.postContent( "" );
		final String strResponse = 
//						retriever.postContent( ContentType.TEXT_PLAIN, "" );
						retriever.postContent( ContentType.APP_JSON, "" );
		
		this.bIsAuthenticating = false;
//		
//		final String strPost = "";
//		
////		byte[] postData       = strURLParams.getBytes( StandardCharsets.UTF_8 );
//		byte[] postData       = strPost.getBytes( StandardCharsets.UTF_8 );
//		int    postDataLength = postData.length;
////		String request        = "http://example.com/index.php";
//		URL    url            = new URL( strURL );
//		
//		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();           
//		conn.setDoOutput( true );
//		conn.setInstanceFollowRedirects( false );
//		conn.setRequestMethod( "POST" );
////		conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
//		conn.setRequestProperty( "Content-Type", "text/plain"); 
//		conn.setRequestProperty( "charset", "utf-8");
//		conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//		conn.setUseCaches( false );
//		
//		try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
//		   wr.write( postData );
//		}
//
//        final InputStream is = conn.getInputStream();
//		final InputStreamReader isr = new InputStreamReader(is, "UTF-8");
//		final Reader in = new BufferedReader( isr );
//
//		StringBuffer strbuf = new StringBuffer();
//		
//        for (int c; (c = in.read()) >= 0;) {
////            System.out.print((char)c);
//            strbuf.append( (char)c );
//        }
////        System.out.println();
//        
//        final String strResponse = strbuf.toString();

        // {"access_token":"05f734a521a82d664d3934c15b8f6f6afde1159fd49617d6ecf4606cf64c7c74","token_type":"bearer","expires_in":3888000,"refresh_token":"2db277d051ee66560847d432489144367f97daa046abb7fb645c930b763d3fb6","created_at":1500694326}
        // {"access_token":"55d14b8b332e8eb8ac0cdbe265c33106bd7e4839b6e0969970a6d3c8878886d0","token_type":"bearer","expires_in":3888000,"refresh_token":"555a826968f470813c91aa7ce98d237a901d4c8f3bc053c6598ef02b458fd7bd","created_at":1500694497}
        
//        final Map<String, Object> map = getMapFromJSON( strbuf.toString() ); 
		
		final JsonElement element = new JsonParser().parse( strResponse );
		final JsonObject jo = element.getAsJsonObject();
		this.mapLoginDetails = JsonUtils.transformJsonToMap( jo );
		
		this.strTokenType = mapLoginDetails.get( KEY_TOKEN_TYPE );
		this.strTokenValue = mapLoginDetails.get( KEY_ACCESS_TOKEN );
		// also available:
		// 	"created_at"
		//	"expires_in"
		// 	"refresh_token"
		
        return mapLoginDetails;
	}

	
	public Map<String,String> getLoginDetails() {
		if ( null==this.mapLoginDetails ) {
			try {
				this.login();
			} catch ( final Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableMap( this.mapLoginDetails );
	}
	
	
	public synchronized void invalidate() {
		this.strTokenType = null;
		this.strTokenValue = null;
	}
	

	public synchronized String getTokenValue() throws Exception {
		if ( DUMMY_LOGIN==this ) {
			return TeslaConstants.DUMMY_AUTH_TOKEN_VALUE;
		}
		if ( null==this.strTokenValue ) {
			this.login();
		}
		return this.strTokenValue;
	}
	

	public synchronized String getTokenType() throws Exception {
		if ( DUMMY_LOGIN==this ) {
			return TeslaConstants.DUMMY_AUTH_TOKEN_TYPE;
		}
		if ( null==this.strTokenType ) {
			this.login();
		}
		return this.strTokenType;
	}
	
	

	public boolean isAuthenticating() {
		return bIsAuthenticating;
	}
	
	
	public static void main( final String[] args ) throws Exception {
//		final String strUsername = // <add username here>
//		final char[] arrPassword = // <add password here> .toCharArray();
//		
//		TeslaLogin login = new TeslaLogin( strUsername, arrPassword );
//		final Map<String, String> map = login.login();
//		
//		JsonUtils.print( map );
	}


	
}
