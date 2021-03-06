package jmr.rpclient.tab;

public enum TopSection {
	
	DAILY_INFO( "Daily Info" ),
	TILES( "Tiles" ),
	DEVICE_CONTROLS( "Device" ),
	S2DB( "S2DB Data" ),
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
