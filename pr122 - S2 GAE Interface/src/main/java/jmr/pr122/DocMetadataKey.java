package jmr.pr122;

import java.io.File;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

public enum DocMetadataKey {
	
	CLASS,
	
	FILENAME,
	FILE_DATE,
	LAST_MODIFIED_MS,
	
	RECORD_EVENT,
	
	DEVICE_IP,
	DEVICE_MAC,
	DEVICE_COMMENT,
	
	SENSOR_ID,
	SENSOR_DESC,

	IMAGE_RESOLUTION,
	IMAGE_DIM_X,
	IMAGE_DIM_Y,
	IMAGE_COLOR_DEPTH,

	SOURCE,
	SCRIPT,
	PID,
	
	COMMENT,
	NOTES,
	DATA_JSON,
	;
	
	public static final DocMetadataKey getKeyFor( final String strValue ) {
		if ( null==strValue ) return null;
		for ( final DocMetadataKey key : DocMetadataKey.values() ) {
			if ( key.name().equals( strValue ) ) {
				return key;
			}
		}
		return null;
	}
	
	
	public static EnumMap<DocMetadataKey,String> createMetadataMap(	
						final Map<DocMetadataKey,String> mapSource,
						final File file ) {
		final EnumMap<DocMetadataKey, String> 
						map = new EnumMap<>( DocMetadataKey.class );
		
		if ( null!=mapSource && !mapSource.isEmpty() ) {
			map.putAll( mapSource );
		}
		
		if ( null==file ) return map;

		map.put( DocMetadataKey.LAST_MODIFIED_MS, ""+file.lastModified() );
		
		final Instant time = Instant.ofEpochMilli( file.lastModified() );
		map.put( DocMetadataKey.FILE_DATE, time.toString() );
		
		return map;
	}
	
	public static class Marker_20180628_1845 {};
}
