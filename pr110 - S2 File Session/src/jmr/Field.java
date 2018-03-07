package jmr;


public enum Field {
	
	// source data
	UNAME( 1 ),
	CONKY( 40 ),
	IFCONFIG( 30 ),

	UNAME_FORMATTED( 80 ),
	
	// derived data
	SESSION_NAME( "session_id", 20 ),
	MAC( "device.mac", 20 ),
	IP( "device.ip", 16 ),
	NIC( 6 ),
	DESCRIPTION( 40 ),
//	UNAME( 100 ),
	DEVICE_INFO( 40 ),
	SYSTEM_INFO( 20 ),

	DEVICE_NAME( "device.name", 40 ),
	HOST_PORT( "device.host.port", 8 ),
	LAST_MODIFIED( ".last_modified_uxt", 20 ),
	PAGE_STATE( ".state", 4 ),
	EXECUTABLE( "executable", 36 ),
	PAGE_SOURCE_CLASS( "page.source.class", 24 ),
	SESSION_ID( "session.id", 50 ),
	SESSION_START( "session.start", 20 ),
	SESSION_SEQ( ".seq_session", 12 ),
	
	TIMESTR_PAGE( 30 ),
	TIMESTR_SCREENSHOT( 30 ),
	TIMEE_SCREENSHOT( 20 ),
	
	

//	EMPTY,
	;
	
	private final int iWidth;
	private final String strOriginalName;
	
	
	Field(	final String strOriginalName,
			final int iWidth ) {
		this.strOriginalName = strOriginalName;
		this.iWidth = iWidth;
	}
	
	Field( final int iWidth ) {
		this.strOriginalName = this.name().toLowerCase();
		this.iWidth = iWidth;
	}
	
	
	public int getWidth() {
		return iWidth;
	}
	
	
	public static Field get( final int index ) {
		if ( index < Field.values().length ) {
			return Field.values()[ index ];
		}
		return null;
	}
	
	public static Field get( final String strKey ) {
		if ( null==strKey ) return null;
		final String strNorm = strKey.trim();
		if ( strNorm.isEmpty() ) return null;
		for ( final Field field : Field.values() ) {
			if ( strNorm.equals( field.strOriginalName ) ) {
				return field;
			}
		}
		return null;
	}
	
}
