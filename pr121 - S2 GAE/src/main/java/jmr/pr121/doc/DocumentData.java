package jmr.pr121.doc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DocumentData {


	final public LocalDateTime time;
//	final public String strClass;
	final public String strContentType;
	
	final public byte[] data;
	public String strData;
	
	final Map<String, String> mapMetadata; 
	
	
	public DocumentData( 	final LocalDateTime time,
//							final String strClass,
							final String strContentType,
							final Map<String,String> map,
							final byte[] data ) {
		this.time = time;
//		this.strClass = strClass;
		this.strContentType = strContentType;
		this.data = data;
		this.mapMetadata = null!=map ? map : new HashMap<String,String>();
	}
	
	public DocumentData( 	final LocalDateTime time,
							final String strData ) {
		this( time, String.class.getName(), null, 
							strData.getBytes( StandardCharsets.UTF_8 ) );
		this.strData = strData;
	}

	public String asString() {
		return this.asString( 0 );
	}

	public String asString( final int iMaxLength ) {
		if ( null!=strData ) {
			if ( iMaxLength>0 && strData.length() > iMaxLength ) {
				return strData.substring( 0, iMaxLength - 2 ) + "..";
			} else {
				return strData;
			}
		} else {
			return "[type:" + strContentType + ", length:" + data.length + "]"; 
		}
	}
	
	public byte[] asBytes() {
		return data;
	}

	public String get( final String strKey ) {
		return this.mapMetadata.get( strKey );
	}
	
}
