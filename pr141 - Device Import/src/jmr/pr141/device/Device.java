package jmr.pr141.device;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Device {
	
	public static final Device DUMMY_DEVICE;
	
	public static enum TextProperty {
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
	}
	
	static {
		DUMMY_DEVICE = new Device( Collections.singletonList( 999999L ) );
		final EnumMap<TextProperty,String> map = DUMMY_DEVICE.mapProperties;
		for ( final TextProperty property : TextProperty.values() ) {
			map.put( property, property.getLabel() );
		}
		DUMMY_DEVICE.bBluetooth = true;
		DUMMY_DEVICE.bNFC = false;
		DUMMY_DEVICE.iSimCount = 1;
		DUMMY_DEVICE.iCountryCode = 999;
		DUMMY_DEVICE.mapChars.put( 
				"CharacteristicRandomName", "CharacteristicRandomValue" );
	}
	
	
	final List<Long> listTACs = new LinkedList<>();
	
	final EnumMap<TextProperty,String> 
					mapProperties = new EnumMap<>( TextProperty.class );
	
	final Map<String,String> mapChars = new HashMap<>();
	
	Integer iCountryCode;
	Integer iSimCount;
	Integer iImeiQtySupport;
	
	Boolean bBluetooth;
	Boolean bWLAN;
	Boolean bNFC;
	
//	String strImageBase64;
	
	
	public Device( final List<Long> listTACs ) {
		if ( null != listTACs ) {
			this.listTACs.addAll( listTACs );
		}
	}
	
	public List<Long> getTACs() {
		return this.listTACs;
	}
	
	public Integer getSimCount() {
		return this.iSimCount;
	}
	
	public Boolean getBluetooth() {
		return this.bBluetooth;
	}
	
	public Boolean getWLAN() {
		return this.bWLAN;
	}

	public Boolean getNFC() {
		return this.bNFC;
	}
	
	public String getProperty( final TextProperty property ) {
		return this.mapProperties.get( property );
	}
	
	public void set( final TextProperty property,
					 final String strValue ) {
		this.mapProperties.put( property, strValue );
	}
	
	
	/*package*/ void loadCharacteristics( final String strInput ) {
		if ( null == strInput ) return;
		if ( strInput.isEmpty() ) return;
		
		for ( final String strPair : strInput.split( "\\|" ) ) {
			final String[] strParts = strPair.split( "=" );
			if ( strParts.length > 1 ) {
				final String strKey = strParts[0];
				final String strValue = strParts[1];
				this.mapChars.put( strKey, strValue );
			}
		}
	}
	
}
