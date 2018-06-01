package jmr.pr122;

import jmr.util.http.ContentType;

public enum DocKey {

	TESLA_COMBINED( ContentType.JSON_TEXT ),
	
	NEST_SHARED_DETAIL( ContentType.APP_JSON ),
	NEST_DEVICE_DETAIL( ContentType.APP_JSON ),
	
	CONFIG( ContentType.TEXT_PLAIN ),
	
	TEST( ContentType.TEXT_PLAIN ),
	
	DEVICE_SCREENSHOT( ContentType.IMAGE_PNG ),
	DEVICE_STILL_CAPTURE( ContentType.IMAGE_PNG ),

	;
	
	public final ContentType type;
	
	DocKey( final ContentType type ) {
		this.type = type;
	}
	
	DocKey() {
		this( null );
	}

	public ContentType getType() {
		return this.type;
	}
}
