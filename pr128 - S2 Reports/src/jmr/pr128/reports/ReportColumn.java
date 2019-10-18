package jmr.pr128.reports;

public enum ReportColumn {

	MAC,
	AGE_DB,
	IP_ADDRESS,
	JSON_DATA( "Data" ),
	OPTIONS( "Options" ),
	
	IP_ADDRESSES( "IP Addresses" ),
	AGE_S2FS( "age (S2FS)" ),
	DEVICE_CONFIG( "Device config" ),
	DEVICE_REPORT( "Device report" ),
	;
	
	final String strDisplayName;
	

	private ReportColumn( final String strDisplayName ) {
		this.strDisplayName = strDisplayName;
	}
	
	private ReportColumn() {
		this( null );
	}
	
	
	public String getDisplayName() {
		return strDisplayName;
	}
	
	public boolean match( final String strText ) {
		if ( null==strText ) {
			return false;
		} else if ( this.name().equalsIgnoreCase( strText ) ) {
			return true;
		} else if ( strText.equalsIgnoreCase( this.getDisplayName() ) ) {
			return true;
		} else {
			return false;
		}
	}
}
