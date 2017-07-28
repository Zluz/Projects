package jmr.rpclient.tab;

public enum TopSection {
	
	DAILY_INFO( "Daily Info" ),
	DEVICE_CONTROLS( "Device" ),
	DATA_LISTING( "Data Listing" ),
	TREE_DEMO( "Tree Demo" ),
	CANVAS( "Calibration" ),
	LOG( "Debug Control" ),
	;
	
	final private String strCaption;
	
	TopSection( final String strCaption ) {
		this.strCaption = strCaption;
	}
	
	public String getCaption() {
		return this.strCaption;
	}
}
