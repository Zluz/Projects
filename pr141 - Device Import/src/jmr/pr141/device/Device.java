package jmr.pr141.device;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jmr.pr141.conversion.Utils;

public class Device {
	
	public static final Device DUMMY_DEVICE;
	
	public static enum BooleanProperty {
			BLUETOOTH,
			WLAN,
			NFC,
			;
	}
	
	public static enum IntegerProperty {
			SIM_COUNT,
			IMEI_QTY_SUPPORT,
			COUNTRY_CODE,
			;
	}
	
	static {
		DUMMY_DEVICE = new Device( Collections.singletonList( 999999L ) );
		final EnumMap<TextProperty,String> map = DUMMY_DEVICE.mapProperties;
		for ( final TextProperty property : TextProperty.values() ) {
			map.put( property, property.getLabel() );
		}
//		DUMMY_DEVICE.bBluetooth = true;
		DUMMY_DEVICE.arrBooleanProperties[ 0 ] = Boolean.TRUE;
//		DUMMY_DEVICE.bNFC = false;
		DUMMY_DEVICE.arrBooleanProperties[ 2 ] = Boolean.FALSE;
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
	
//	Boolean bBluetooth;
//	Boolean bWLAN;
//	Boolean bNFC;
	
	final private Boolean[] arrBooleanProperties;
	final private Integer[] arrIntegerProperties;

//	String strImageBase64;
	
	
	public Device( final List<Long> listTACs ) {
		if ( null != listTACs ) {
			this.listTACs.addAll( listTACs );
		}
		arrBooleanProperties = new Boolean[ BooleanProperty.values().length ];
		arrIntegerProperties = new Integer[ IntegerProperty.values().length ];
	}
	
	public EnumMap<TextProperty,String> getPropertyMap() {
		return this.mapProperties;
	}
	
	public Map<String,String> getCharacteristicsMap() {
		return this.mapChars;
	}
	
	public void setSimCount( final Integer iSimCount ) {
		this.iSimCount = iSimCount;
	}
	
	public void setCountryCode( final Integer iCountryCode ) {
		this.iCountryCode = iCountryCode;
	}
		
	public List<Long> getTACs() {
		return this.listTACs;
	}
	
	public Integer getSimCount() {
		return this.iSimCount;
	}
	
	public Boolean getBluetooth() {
//		return this.bBluetooth;
		return arrBooleanProperties[ BooleanProperty.BLUETOOTH.ordinal() ];
	}
	
	public Boolean getWLAN() {
//		return this.bWLAN;
		return arrBooleanProperties[ BooleanProperty.WLAN.ordinal() ];
	}

	public Boolean getNFC() {
//		return this.bNFC;
		return arrBooleanProperties[ BooleanProperty.NFC.ordinal() ];
	}
	
	public void setBooleanProperty( final BooleanProperty property,
									final Boolean bValue ) {
		final int iIndex = property.ordinal();
		this.arrBooleanProperties[ iIndex ] = bValue;
	}
	
	public void setBooleanProperty( final BooleanProperty property,
									final String strValue ) {
		final Boolean bValue = Utils.parseBoolean( strValue );
		this.setBooleanProperty( property, bValue ); 
	}

	public Boolean getBooleanProperty( final BooleanProperty property ) {
		final int iIndex = property.ordinal();
		final Boolean bValue = this.arrBooleanProperties[ iIndex ];
		return bValue;
	}

	
	

	public void setIntegerProperty( final IntegerProperty property,
									final Integer bValue ) {
		final int iIndex = property.ordinal();
		this.arrIntegerProperties[ iIndex ] = bValue;
	}
	
	public void setIntegerProperty( final IntegerProperty property,
									final String strValue ) {
		final Integer bValue = Utils.parseNumber( strValue );
		this.setIntegerProperty( property, bValue ); 
	}

	public Integer getIntegerProperty( final IntegerProperty property ) {
		final int iIndex = property.ordinal();
		final Integer bValue = this.arrIntegerProperties[ iIndex ];
		return bValue;
	}
	
	
	
	
	
	
	
	public String getProperty( final TextProperty property ) {
		return this.mapProperties.get( property );
	}
	
	public void set( final TextProperty property,
					 final String strValue ) {
		this.mapProperties.put( property, strValue );
	}
	
	
	/*package*/ public void loadCharacteristics( final String strInput ) {
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
