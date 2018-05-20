package jmr.pr121.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import jmr.pr121.servlets.Log;

public class Configuration {


	final static Configuration instance = new Configuration();
	
	final static Map<String,String> map = new HashMap<>();
	
	private Configuration() {};
	
	public static Configuration get() {
		return instance;
	}

	public void put( 	final String strName,
						final String strValue ) {
		if ( null==strName ) return;
		if ( strName.isEmpty() ) return;
		
		map.put( strName, strValue );
	}
	
	public String get( final String strName ) {
		return map.get( strName );
	}

	
	public Set<Entry<String, String>> entrySet() {
		return map.entrySet();
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
		
		final String strTestList = map.get( "browser_accept" );
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
