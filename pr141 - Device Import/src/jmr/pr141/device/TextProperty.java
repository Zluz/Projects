package jmr.pr141.device;

public enum TextProperty {
	
	MARKETING_NAME( 20, "Marketing Name" ),
	MANUFACTURER( 50, "Manufacturer" ),
	BRAND_NAME( 16, "Brand Name" ),
	MODEL_NAME( 20, "Model Name" ),
	
	BANDS( 50, "Bands" ),
	BANDS_5G( 50, "5G Bands" ),
	RADIO_INTERFACE( 20, "Radio Interface" ),
	OPERATING_SYSTEM( 12, "Operating System" ),
	DEVICE_TYPE( 18, "Device Type" ),
	
	CHARACTERISTICS( 100, "Characteristics" ),
	IMAGE_BASE64( 0, "Base64 Image Data" ),
	;
	
	final String strLabel;
	final int iPadding;
	
	private TextProperty( final int iPadding,
						  final String strLabel ) {
		this.strLabel = strLabel;
		this.iPadding = iPadding;
	}
	
	public String getLabel() {
		return strLabel;
	}
	
	public int getPadding() {
		return this.iPadding;
	}
}
