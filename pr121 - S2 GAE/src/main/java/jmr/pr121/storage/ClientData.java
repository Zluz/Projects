package jmr.pr121.storage;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import jmr.pr121.servlets.Log;

public class ClientData {

	
	public static final Map<String,ClientData> CLIENTS = new HashMap<>();
	

	final String strUserAgent;

	String strInfo = null;
	Integer iWidth = null;
	Integer iHeight = null;
	
	
	
	
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
		
		this.strInfo = strInfo;
		
		final String[] arrParts = strInfo.split( "," );
		
		final String strSize = arrParts[0];
		final int iPosX = strSize.indexOf( 'x' );
		final String strWidth = strSize.substring( 0, iPosX - 1 );
		final String strHeight = strSize.substring( iPosX + 1 );
		try {
			this.iWidth = Integer.parseInt( strWidth );
			this.iHeight = Integer.parseInt( strHeight );
		} catch ( final NumberFormatException e ) {
			Log.add( "Failed to examine client info text: " + strInfo );
			this.iWidth = null;
			this.iHeight = null;
		}
		
		Log.add( "Client info loaded." );
	}
	

	public String getClientInfo() {
		return this.strInfo;
	}
	
	
	public String getUserAgent() {
		return this.strUserAgent;
	}
	
	
}
