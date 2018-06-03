package jmr.pr121.doc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.EnumMap;

import jmr.pr122.DocMetadataKey;
import jmr.util.http.ContentType;

public class DocumentData {


	final public LocalDateTime time;
	final public ContentType type;
	
	final public byte[] data;
	
	final public String strSource;
	
	public String strData;
	
	final public EnumMap<DocMetadataKey, String> 
			mapMetadata = new EnumMap<>( DocMetadataKey.class );

	
	public DocumentData( 	final LocalDateTime time,
							final ContentType type,
							final String strSource,
							final EnumMap<DocMetadataKey, String> map,
							final byte[] data ) {
		this.time = time;
		this.type = type;
		this.strSource = strSource;
		this.data = data;
		if ( null!=map ) {
			this.mapMetadata.putAll( map );
		}
	}
	
	public DocumentData( 	final LocalDateTime time,
							final String strSource,
							final String strData ) {
		this( time, ContentType.TEXT_PLAIN, strSource, null, 
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
			return "[type:" + type.name() + ", length:" + data.length + "]"; 
		}
	}
	
	public byte[] asBytes() {
		return data;
	}

	public String get( final DocMetadataKey key ) {
		if ( null==key ) return "<DocMetadataKey is null>";
		final String strValue = this.mapMetadata.get( key );
		if ( null!=strValue ) return strValue;
		return "";
	}
	
}
