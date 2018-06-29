package jmr.pr121.storage;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import jmr.pr121.servlets.Log;

public class ClientData {

	
	public static final Map<String,ClientData> CLIENTS = new HashMap<>();
	
	
	public enum Key {
		OPTION_STILL_CAPTIONS( "OPTION/STILL_CAPTIONS" ),
		OPTION_ADVANCED( "OPTION/ADVANCED" ),
		OPTION_SCREENSHOT_CAPTIONS( "OPTION/SCREENSHOT_CAPTIONS" ),
		OPTION_DISPLAY_MODE( "OPTION/DISPLAY_MODE" ),
		OPTION_REFRESH_FREQUENCY( "OPTION/REFRESH_FREQUENCY" ),
		OPTION_REFRESH_DURATION( "OPTION/REFRESH_DURATION" ),
		FRAME_BODY_DIM( "body-frame-inner" ),
		;
		private String strKey;
		
		Key( final String strKey ) {
			this.strKey = strKey;
		}
		
		public String getKey() {
			return strKey;
		}
	}
	
	

	final String strUserAgent;

//	String strInfo = null;
	Integer iWidth = null;
	Integer iHeight = null;
	
	final Set<String> setInfo = new HashSet<>(); 
	
	final EnumMap<Key,String> mapProperties = new EnumMap<>( Key.class );
	
	
	public static ClientData register( final HttpServletRequest req ) {
		if ( null==req ) return null;
		
//		final HttpSession session = req.getSession( false );
//		if ( null==session ) return null;
//		final String strKey = session.getId();
		
		final ClientData cd = new ClientData( req );
		
		final String strKey = cd.getUserAgent();
		
		if ( CLIENTS.containsKey( strKey ) ) {
			return CLIENTS.get( strKey );
		} else {
			CLIENTS.put( strKey, cd );
			return cd;
		}
	}
	
	
	public ClientData( final HttpServletRequest req ) {
		if ( null==req ) throw new IllegalStateException( 
						"HttpServletRequest is null" );
		this.strUserAgent = req.getHeader( "User-Agent" );
	}
	
	
	public void setClientInfo( final String strInfo ) {
		Log.add( "Recording client info: " + strInfo );
		if ( StringUtils.isEmpty( strInfo ) ) return;
		
//		this.strInfo = strInfo;
		this.setInfo.add( strInfo );
		
		final String[] arrParts = strInfo.split( ":" );
		
		if ( arrParts.length > 1 ) {
			for ( final Key key : Key.values() ) {
				final String strKey = key.strKey;
				if ( strKey.equals( arrParts[0] ) ) {
					mapProperties.put( key, arrParts[1] );
				}
			}
		}
		
		
//		final String[] arrParts = strInfo.split( "," );
//		
//		final String strSize = arrParts[0];
//		final int iPosX = strSize.indexOf( 'x' );
//		if ( iPosX > 0 ) {
//			final String strWidth = strSize.substring( 0, iPosX - 1 );
//			final String strHeight = strSize.substring( iPosX + 1 );
//			try {
//				this.iWidth = Integer.parseInt( strWidth );
//				this.iHeight = Integer.parseInt( strHeight );
//			} catch ( final NumberFormatException e ) {
//				Log.add( "Failed to examine client info text: " + strInfo );
//				this.iWidth = null;
//				this.iHeight = null;
//			}
//		}
		
		Log.add( "Client info loaded." );
	}
	

//	public String getClientInfo() {
//		return this.strInfo;
//	}

	public List<String> getClientInfoList() {
		final List<String> list = new LinkedList<>( this.setInfo );
		Collections.sort( list );
		return list;
	}
	
	public Map<Key,String> getClientInfoMap() {
		return Collections.unmodifiableMap( this.mapProperties );
	}


	public String get( final Key key ) {
//		for ( final Key key : Key.values() ) {
//		final String strKey = key.strKey;
		return this.mapProperties.get( key );
	}
	
	
	public void clearClientInfo() {
		this.setInfo.clear();
	}
	
	
	public String getUserAgent() {
		return this.strUserAgent;
	}
	
	
}
