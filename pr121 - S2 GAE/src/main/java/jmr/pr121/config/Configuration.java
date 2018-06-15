package jmr.pr121.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment.Value;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.pr121.servlets.Log;
import jmr.pr121.storage.GCSHelper;
import jmr.pr122.CommGAE;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileReader;
import jmr.pr123.storage.GCSFileWriter;
import jmr.util.http.ContentType;

public class Configuration {


	final static public String GCS_FILENAME = "CONFIG.json";


	final static Configuration instance = new Configuration();
	
	final Map<String,String> mapString = new HashMap<>();
	final Map<String,Long> mapLong = new HashMap<>();
	
	
	private Configuration() {
		try {
			loadFromGCS();
		} catch ( final Throwable t ) {
			Log.add( "ERROR during Configuration ctor: " + t.toString() );
			Log.add( ExceptionUtils.getStackTrace( t ) );
		}
	}
	
	public static Configuration get() {
		return instance;
	}
	
	
	public void put( 	final String strName,
						final String strValue ) {
		if ( null==strName ) return;
		if ( strName.isEmpty() ) return;
		
		mapString.put( strName, strValue );
		try {
			final long lValue = Long.parseLong( strValue );
			mapLong.put( strName, lValue );
		} catch ( NumberFormatException e ) {
//			mapLong.put( strName, null );
			mapLong.remove( strName );
		}
	}

	public void put( 	final String strName,
						final Long lValue ) {
		if ( null==strName ) return;
		if ( strName.isEmpty() ) return;
		
		mapString.put( strName, lValue.toString() );
		mapLong.put( strName, lValue );
	}
	
	public String get( final String strName ) {
		return mapString.get( strName );
	}

	public Long getAsLong( final String strName ) {
		return mapLong.get( strName );
	}

	
	public Set<Entry<String, String>> entrySet() {
		return mapString.entrySet();
	}

	
	public boolean isBrowserTestInitialized() {
		if ( mapString.isEmpty() ) return false;
		final String strTestList = mapString.get( "browser_accept" );
		if ( null==strTestList ) return false;
		return ( ! strTestList.isEmpty() );
	}
	
	
	public boolean isValidUser(	final String strUsr,
								final String strPwd ) {
		
		Log.add( "--- Configuration.isValidUser()" );
		
		if ( StringUtils.isEmpty( strUsr ) ) return false;

		Log.add( "\tstrUsr = " + strUsr );

		final String strConfigUsers = this.get( "gae.user" );
		if ( StringUtils.isEmpty( strConfigUsers ) ) return false;

//		Log.add( "\tstrConfigUsers = " + strConfigUsers );

		final String[] arrConfigUsers = strConfigUsers.split( "\n" );
		for ( final String strConfigLine : arrConfigUsers ) {
			
//			Log.add( "\tstrConfigLine = " + strConfigLine );
			
			final String[] arrUserDetail = strConfigLine.split( "\\|" );
			
			boolean bCandidate = true;
			bCandidate = bCandidate && arrUserDetail.length > 0;
			if ( bCandidate ) {
				final String strConfigUser = arrUserDetail[0].trim();
				
//				Log.add( "\tstrConfigUser = " + strConfigUser );
				
				if ( !strUsr.equals( strConfigUser ) ) {
					bCandidate = false;
				}
			}
			
			if ( bCandidate && null!=strPwd ) {
				bCandidate = bCandidate && arrUserDetail.length > 1;
				if ( bCandidate ) {
					final String strConfigPwd = arrUserDetail[1].trim();
					if ( !strPwd.equals( strConfigPwd ) ) {
						bCandidate = false;
					}
				}
			}
			
			if ( bCandidate ) {
				Log.add( "\tUser authorized." );
				return true;
			}
		}
		Log.add( "\tUser NOT authorized." );
		return false;
	}
	
	
	/*
	 * Super simple authentication. 
	 * DEFINITELY not reliable for meaningful client authentication.
	 * (Is is necessary or useful to have first level protection 
	 * from rando/malicious? requests on the internet?) 
	 */
	public boolean isBrowserAccepted( final HttpServletRequest req ) {
		if ( null==req ) return false;
		
		// User-Agent||Accept||Accept-Language

		final String strUserAgent = req.getHeader( "User-Agent" );
//		final String strAccept = req.getHeader( "Accept" );
		final String strAcceptLanguage = req.getHeader( "Accept-Language" );

		final String strMatchExact = 
//				strUserAgent + "||" + strAccept + "||" + strAcceptLanguage;
				strUserAgent + "||" + strAcceptLanguage;
//		final String strMatchClose01 = 
//				strUserAgent + "||*/*||" + strAcceptLanguage;
		
		Log.add( "isBrowserAccepted() - Testing: " + strMatchExact );
		
		final String strTestList = mapString.get( "browser_accept" );
		if ( null==strTestList ) {
			Log.add( "isBrowserAccepted() - No accept config loaded." );
			return false;
		}
		
		final String[] strings = strTestList.split( "\n" );
		for ( final String strTest : strings ) {
			if ( strMatchExact.equals( strTest ) ) return true;
//			if ( strMatchClose01.equals( strTest ) ) return true;
			
//			final String[] parts = strTest.split( "||" );
//			if ( strMatchExact.startsWith( parts[0] ) 
//					&& strMatchExact.endsWith( parts[2] ) ) {
//				Log.add( "matched on "
//						+ "\"" + parts[0] + "\" and \"" + parts[2] + "\"" );
//				return true;
//			}
			
		}
		Log.add( "isBrowserAccepted() - "
				+ "Tested " + strings.length + " lines, no matches." );
		return false;
	}
	
	
	public static boolean isGAEDevelopment() {
		final Value env = SystemProperty.environment.value();
		return SystemProperty.Environment.Value.Development.equals( env );
	}
	
	
//	public void putGCSConfig(	final String strKey,
//								final String strValue ) {
//		final GCSFactory factory = GCSHelper.GCS_FACTORY;
//		
//		factory.create( GCS_FILENAME, ContentType.TEXT_PLAIN );
//	}

	public void loadFromGCS() {
		
		Log.add( "--> Configuration.loadFromGCS" );
		
		final GCSFactory factory = GCSHelper.GCS_FACTORY;
		
		final GCSFileReader file = factory.getFile( GCS_FILENAME );
		if ( null==file ) throw new IllegalStateException( 
						"GCS file not found: " + GCS_FILENAME );
		
		Log.add( "GCS config file found." );
		Log.add( "\ttoString(): " + file.toString() );
		Log.add( "\tgetName(): " + file.getName() );
		Log.add( "\tgetSize(): " + file.getSize() );
		Log.add( "\tgetContentType(): " + file.getContentType() );

		final byte[] bytes = file.getContent();
		if ( null==bytes ) throw new IllegalStateException( 
						"Null data from GCS file: " + GCS_FILENAME );
		
		final String strData = new String( bytes );
		
		final JsonElement jeConfig = new JsonParser().parse( strData );
		if ( null==jeConfig ) throw new IllegalStateException( 
						"Null result from parsing JSON" );

		if ( ! jeConfig.isJsonObject() ) throw new IllegalStateException( 
						"JSON config is not a JsonObject" );
		
		final JsonObject joConfig = jeConfig.getAsJsonObject();
		
		for ( final Entry<String, JsonElement> entry : joConfig.entrySet() ) {
			final String strKey = entry.getKey();
//			final JsonElement jeValue = entry.getValue();
			final String strValue = entry.getValue().getAsString();
			
			mapString.put( strKey, strValue );
		}
		
		Log.add( "<-- Configuration.loadFromGCS" );
	}
	
	
	
	
	// generate config, push to gcs
	public static void main( final String[] args ) throws IOException {
		final CommGAE comm = new CommGAE( null );
		comm.generateConfig();
		
		final JsonObject joConfig = comm.getConfig();
		
		final GCSFactory factory = GCSHelper.GCS_FACTORY;
		
		final GCSFileWriter file = 
						factory.create( GCS_FILENAME, ContentType.APP_JSON );
		final String strData = joConfig.toString();
		file.upload( strData.getBytes() );
	}
	
	

}
