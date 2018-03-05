

public enum Column {
	
	SESSION_NAME( 200 ),
	MAC( 120 ),
	IP( 100 ),
	DESCRIPTION( 200 ),
	UNAME( 100 ),
	DEVICE_INFO( 300 ),
	SYSTEM_INFO( 100 ),
	
//	EMPTY,
	;
	
	private final int iWidth;
	
	
	Column(	final int iWidth ) {
		this.iWidth = iWidth;
	}
	
	
	public int getWidth() {
		return iWidth;
	}
	
	
	
	public static Column get( final int index ) {
		if ( index < Column.values().length ) {
			return Column.values()[ index ];
		}
		return null;
	}
	
}
