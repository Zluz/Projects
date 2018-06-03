package jmr.pr122;

public enum DocMetadataKey {

	CLASS,
	RESOLUTION,
	
	FILENAME,
	FILE_DATE,
	
	RECORD_EVENT,
	
	DEVICE_IP,
	DEVICE_MAC,
	
	SENSOR_ID,
	SENSOR_DESC,
	;
	
	final public static DocMetadataKey getKeyFor( final String strValue ) {
		if ( null==strValue ) return null;
		for ( final DocMetadataKey key : DocMetadataKey.values() ) {
			if ( key.name().equals( strValue ) ) {
				return key;
			}
		}
		return null;
	}
}
