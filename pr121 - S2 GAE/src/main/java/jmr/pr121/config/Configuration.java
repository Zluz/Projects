package jmr.pr121.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import jmr.pr121.servlets.Log;

public class Configuration {


	final static Configuration instance = new Configuration();
	
	final static Map<String,String> mapString = new HashMap<>();
	final static Map<String,Long> mapLong = new HashMap<>();
	
	private Configuration() {};
	
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
			
			Log.add( "\tstrConfigLine = " + strConfigLine );
			
			final String[] arrUserDetail = strConfigLine.split( "\\|" );
			
			boolean bCandidate = true;
			bCandidate = bCandidate && arrUserDetail.length > 0;
			if ( bCandidate ) {
				final String strConfigUser = arrUserDetail[0].trim();
				
				Log.add( "\tstrConfigUser = " + strConfigUser );
				
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
		final String strAccept = req.getHeader( "Accept" );
		final String strAcceptLanguage = req.getHeader( "Accept-Language" );
		final String strMatch = 
				strUserAgent + "||" + strAccept + "||" + strAcceptLanguage;
		Log.add( "isBrowserAccepted() - Testing: " + strMatch );
		
		final String strTestList = mapString.get( "browser_accept" );
		if ( null==strTestList ) {
			Log.add( "isBrowserAccepted() - No accept config loaded." );
			return false;
		}
		
		final String[] strings = strTestList.split( "\n" );
		for ( final String strTest : strings ) {
			if ( strMatch.equals( strTest ) ) return true;
		}
		Log.add( "isBrowserAccepted() - "
				+ "Tested " + strings.length + " lines, no matches." );
		return false;
	}

}
