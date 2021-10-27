package jmr.pr102.comm;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.google.gson.JsonObject;

import jmr.util.http.ContentRetriever;
import jmr.util.http.ContentType;

/*
 *  this class attempts to implement the new Tesla SSO authentication
 *  mechanism (auth.tesla.com, using OAuth 2.0), 
 *  replacing use of the original authentication endpoint (/oauth/token).
 *  
 *  see
 *  https://tesla-api.timdorr.com/api-basics/authentication
 *  https://teslamotorsclub.com/tmc/threads/tesla-owners-api-v3-enabled.219375/page-2
 *  https://groups.google.com/g/automate-user/c/de41Msc2GHQ
 *  https://forum.logicmachine.net/showthread.php?tid=3308
 * 
 */
public class TeslaAuth2 {

	public final String strUsr;
	public final char[] arrPwd;
	
	final String strCodeVerifier;
	final String strStateValue;
	final String strB64Encoded;
	
	final Map<String,String> mapElements = new HashMap<>();
	
	final List<HttpCookie> listCookies = new LinkedList<>();
	final List<String> listCookie = new LinkedList<>();
	final StringBuilder sbCookie = new StringBuilder();

	
	public TeslaAuth2(	final String strUsr,
						final char[] arrPwd ) {
		this.strUsr = strUsr;
		this.arrPwd = arrPwd;

		this.strCodeVerifier = generateRandomString( 86 );
		this.strStateValue = generateRandomString( 16 );

		String strB64EncodedLocal = "";
		try {
			final byte[] arrCodeVerifier = 
					strCodeVerifier.getBytes( StandardCharsets.UTF_8 );
			
			final MessageDigest md = MessageDigest.getInstance( "SHA-256" );
			final byte[] arrSHAEncoded = md.digest( arrCodeVerifier );
			
			final Base64Variant encoder = Base64Variants.MODIFIED_FOR_URL;
			strB64EncodedLocal = encoder.encode( arrSHAEncoded );
			
		} catch ( final Exception e ) {
			// should never happen
		}
		this.strB64Encoded = strB64EncodedLocal;
	}
	
	
	
	public static String generateRandomString( final int iLength ) {
		final StringBuilder sb = new StringBuilder();
		while ( sb.length() < iLength ) {
			final String strUUID = UUID.randomUUID().toString();
			sb.append( strUUID.replace( "-", "" ) );
		}
		final String strResult = sb.toString().substring( 0, iLength );
		return strResult;
	}
	
	
	
	
	public boolean getLoginPage() throws Exception {
			
		final String strURL = "https://auth.tesla.com/oauth2/v3/authorize";

		final Map<String,String> map = new HashMap<>();

		// fixed values
		map.put( "client_id", "ownerapi" );
		map.put( "code_challenge_method", "S256" );
		map.put( "redirect_uri", "https://auth.tesla.com/void/callback" );
		map.put( "response_type", "code" );
		map.put( "scope", "openid email offline_access" );

		// required variables
		map.put( "code_challenge", strB64Encoded );
		map.put( "state", strStateValue );

		// optional variable
		map.put( "login_hint", strUsr );
		

		final StringBuilder sbURL = new StringBuilder();
		sbURL.append( strURL );
		sbURL.append( "?" );
		
		final String strCharEnc = "UTF-8";
		
		map.entrySet().forEach(
			entry-> {
				final String strValue = entry.getValue();
				try {
					final String strEncoded = 
								URLEncoder.encode( strValue, strCharEnc );
					sbURL.append( entry.getKey()  ); 
					sbURL.append( "=" ); 
					sbURL.append( strEncoded ); 
					sbURL.append( "&" ); 
				} catch ( final UnsupportedEncodingException e ) {
					e.printStackTrace();
				}
			} );

		
//		final ContentRetriever retriever = new ContentRetriever( strURL );
		final ContentRetriever retriever = new ContentRetriever( sbURL.toString() );

		// testing other values
		retriever.addProperty( "User-Agent", "Java" );
		
//		map.entrySet().forEach( 
//				entry-> retriever.addProperty( 
//								entry.getKey(), entry.getValue() ) );
//		map.entrySet().forEach( 
//			entry-> {
//				final String strValue = entry.getValue();
//				final String strEncoded = URLEncoder.encode( strValue );
//				retriever.addProperty( entry.getKey(), strEncoded );
//			} );
		
		System.out.println( "Calling URL: " + sbURL.toString() );
		
		final String strResponse = 
//				retriever.postContent( ContentType.TEXT_PLAIN, "" );
				retriever.getContent( ContentType.TEXT_HTML );
		
		listCookies.clear();
		listCookies.addAll( retriever.getCookies() );
		
		final Map<String, List<String>> mapResponseFields = 
										retriever.getResponseHeaderFields();
		mapResponseFields.keySet().forEach( strKey-> {
			if ( "set-cookie".equalsIgnoreCase( strKey ) ) {
				final List<String> listValues = mapResponseFields.get( strKey );
				listValues.forEach( strItem -> listCookie.add( strItem ) );
			}
		});
		
		System.out.println( "Response:\n" + strResponse );
		
		final String[] arrResponseLines = strResponse.split( "\\n" );
		
		mapElements.put( "_csrf", "" );
		mapElements.put( "_phase", "" );
		mapElements.put( "_process", "" );
		mapElements.put( "transaction_id", "" );
		mapElements.put( "cancel", "" );
		
		for ( final String strKey : mapElements.keySet() ) {
			for ( final String strLine : arrResponseLines ) {
				final String strLower = strLine.toLowerCase();
				if ( strLower.contains( "type=\"hidden\"" ) 
						&& strLower.contains( "<input" ) 
						&& strLower.contains( "value=\"" ) 
						&& strLower.contains( "name=\"" + strKey ) ) {
					
					final String[] strParts = strLine.split( " " );
					for ( final String strPart  : strParts ) {
						if ( strPart.contains( "value" ) ) {
							final String[] strPieces = strPart.split( "\"" );
							for ( final String strPiece : strPieces ) {
								if ( ! strPiece.isEmpty() 
										&& ! strPiece.contains( "=" ) ) {
									mapElements.put( strKey, strPiece );
								}
							}
						}
					}
				}
			}
		}
		
		System.out.println( "elements: " + mapElements.toString() );
		
		return true;
	}
	
	

	
	public boolean getAuthCode() throws Exception {
			
		final String strURL = "https://auth.tesla.com/oauth2/v3/authorize";

		final Map<String,String> map = new HashMap<>();

		// fixed values
		map.put( "client_id", "ownerapi" );
		map.put( "code_challenge_method", "S256" );
		map.put( "redirect_uri", "https://auth.tesla.com/void/callback" );
		map.put( "response_type", "code" );
		map.put( "scope", "openid email offline_access" );

		// required variables
		map.put( "code_challenge", strB64Encoded );
		map.put( "state", strStateValue );

		// optional variable
//		map.put( "login_hint", strUsername );
		
		final JsonObject jo = new JsonObject();
		jo.addProperty( "identity", this.strUsr );
		jo.addProperty( "credential", new String( arrPwd ) );

//		jo.addProperty( "_csrf", mapElements.get( "_csrf" ) );
		mapElements.entrySet().forEach( 
				entry-> jo.addProperty( entry.getKey(), entry.getValue() ) );
		final String strContentRaw = jo.toString();
		final String strContentEncoded = URLEncoder.encode( strContentRaw );
		

		final StringBuilder sbURL = new StringBuilder();
		sbURL.append( strURL );
		sbURL.append( "?" );
		
		final String strCharEnc = "UTF-8";
		
		map.entrySet().forEach(
			entry-> {
				final String strValue = entry.getValue();
				try {
					final String strEncoded = 
								URLEncoder.encode( strValue, strCharEnc );
					sbURL.append( entry.getKey()  ); 
					sbURL.append( "=" ); 
					sbURL.append( strEncoded ); 
					sbURL.append( "&" ); 
				} catch ( final UnsupportedEncodingException e ) {
					e.printStackTrace();
				}
			} );

		
//		final ContentRetriever retriever = new ContentRetriever( strURL );
		final ContentRetriever retriever = new ContentRetriever( sbURL.toString() );

		// testing other values
		retriever.addProperty( "User-Agent", "Java" );
		
//		final CookieManager manager
//		final CookieHandler handler = CookieManager.getDefault();
//		handler.getc
		
		final StringBuilder sbCookieValue = new StringBuilder();
		// listCookie.forEach( str-> {
//		for ( final String str : listCookie ) {
//			sbCookieValue.append( URLEncoder.encode( str ) );
//			sbCookieValue.append( "; " ); //FIXME
//		};
		for ( final HttpCookie cookie : listCookies ) {
//			sbCookieValue.append( URLEncoder.encode( str ) );
//			sbCookieValue.append( "; " ); //FIXME
			sbCookieValue.append( cookie.getName() );
//			sbCookieValue.append( "'" + cookie.getName() + "'" );
			sbCookieValue.append( "=" );
			sbCookieValue.append( cookie.getValue() );
//			sbCookieValue.append( "'" + cookie.getValue() + "'" );
		};
		retriever.addProperty( "Cookie", sbCookieValue.toString() );
		
//		listCookies.get( 0 ).get
		
		
		
		
		// unnecessary? ..since we're saving it in the POST body
//		map.entrySet().forEach( 
//				entry-> retriever.addProperty( 
//								entry.getKey(), entry.getValue() ) );
		map.entrySet().forEach( 
			entry-> {
				final String strValue = entry.getValue();
				final String strEncoded = URLEncoder.encode( strValue );
				retriever.addProperty( entry.getKey(), strEncoded );
			} );
		
		System.out.println( "Calling URL: " + sbURL.toString() );
		
		final String strResponse = 
				retriever.postContent( ContentType.POST_FORM, strContentEncoded );
//				retriever.postContent( ContentType.POST_FORM, strContentRaw );
//				retriever.getContent( ContentType.TEXT_HTML );
		
//		final Map<String, List<String>> mapResponseFields = 
//										retriever.getResponseHeaderFields();
//		mapResponseFields.keySet().forEach( strKey-> {
//			if ( "set-cookie".equalsIgnoreCase( strKey ) ) {
//				final List<String> listValues = mapResponseFields.get( strKey );
//				listValues.forEach( strItem -> listCookie.add( strItem ) );
//			}
//		});
		
		System.out.println( "Response:\n" + strResponse );
		
		final String[] arrResponseLines = strResponse.split( "\\n" );
		
		mapElements.put( "_csrf", "" );
		mapElements.put( "_phase", "" );
		mapElements.put( "_process", "" );
		mapElements.put( "transaction_id", "" );
		mapElements.put( "cancel", "" );
		
		for ( final String strKey : mapElements.keySet() ) {
			for ( final String strLine : arrResponseLines ) {
				final String strLower = strLine.toLowerCase();
				if ( strLower.contains( "type=\"hidden\"" ) 
						&& strLower.contains( "<input" ) 
						&& strLower.contains( "value=\"" ) 
						&& strLower.contains( "name=\"" + strKey ) ) {
					
					final String[] strParts = strLine.split( " " );
					for ( final String strPart  : strParts ) {
						if ( strPart.contains( "value" ) ) {
							final String[] strPieces = strPart.split( "\"" );
							for ( final String strPiece : strPieces ) {
								if ( ! strPiece.isEmpty() 
										&& ! strPiece.contains( "=" ) ) {
									mapElements.put( strKey, strPiece );
								}
							}
						}
					}
				}
			}
		}
		
		System.out.println( "elements: " + mapElements.toString() );
		
		return true;
	}
	
	
	

	public static void main( final String[] args ) throws Exception {

		final String strUsr = "jeff.rabenhorst@gmail.com";
		final char[] arrPwd = ( "pass" + "word" ).toCharArray();
		final TeslaAuth2 auth = new TeslaAuth2( strUsr, arrPwd );
		System.out.println( "----------------------------------------------");
		System.out.println( "Calling: getLoginPage()" );
		auth.getLoginPage();
		System.out.println( "----------------------------------------------");
		System.out.println( "Calling: getAuthCode()" );
		auth.getAuthCode();
	}
	

	
}
