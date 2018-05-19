package jmr.pr121.doc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class DocumentData {


	final public LocalDateTime time;
	final public String strClass;
	final public String strContentType;
	
	final public byte[] data;
	public String strData;
	
	
	public DocumentData( 	final LocalDateTime time,
							final String strClass,
							final String strContentType,
							final byte[] data ) {
		this.time = time;
		this.strClass = strClass;
		this.strContentType = strContentType;
		this.data = data;
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
			return "[class:" + strClass + ", length:" + data.length + "]"; 
		}
	}
	
	public byte[] asBytes() {
		return data;
	}
	
}
