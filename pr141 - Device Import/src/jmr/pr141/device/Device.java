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
		DUMMY_DEVICE = new Device( 999999L );
		final EnumMap<TextProperty,String> map = DUMMY_DEVICE.mapProperties;
		for ( final TextProperty property : TextProperty.values() ) {
			map.put( property, property.getLabel() );
		}
		DUMMY_DEVICE.setProperty( BooleanProperty.BLUETOOTH, true );
		DUMMY_DEVICE.setProperty( BooleanProperty.NFC, false );
		DUMMY_DEVICE.setProperty( IntegerProperty.SIM_COUNT, 1 );
		DUMMY_DEVICE.setProperty( IntegerProperty.COUNTRY_CODE, 999 );
		DUMMY_DEVICE.mapChars.put( 
				"CharacteristicRandomName", "CharacteristicRandomValue" );
	}
	
	
	final private List<Long> listTACs = new LinkedList<>();
	
	final private EnumMap<TextProperty,String> 
					mapProperties = new EnumMap<>( TextProperty.class );
	
	final private Map<String,String> mapChars = new HashMap<>();
	
	final private Boolean[] arrBooleanProperties;
	final private Integer[] arrIntegerProperties;

	
	public Device( final List<Long> listTACs ) {
		if ( null != listTACs ) {
			this.listTACs.addAll( listTACs );
		}
		arrBooleanProperties = new Boolean[ BooleanProperty.values().length ];
		arrIntegerProperties = new Integer[ IntegerProperty.values().length ];
	}
	
	public Device( final long lTAC ) {
		this( Collections.singletonList( lTAC ) );
	}
	
	public EnumMap<TextProperty,String> getPropertyMap() {
		return this.mapProperties;
	}
	
	public Map<String,String> getCharacteristicsMap() {
		return this.mapChars;
	}
	
	public void setSimCount( final Integer iSimCount ) {
		arrIntegerProperties[ IntegerProperty.SIM_COUNT.ordinal() ] = iSimCount;
	}
	
	public void setCountryCode( final Integer iCountryCode ) {
		arrIntegerProperties[ IntegerProperty.COUNTRY_CODE.ordinal() ] = iCountryCode;
	}
		
	public List<Long> getTACs() {
		return this.listTACs;
	}
	
	public Integer getSimCount() {
		return arrIntegerProperties[ IntegerProperty.SIM_COUNT.ordinal() ];
	}
	
	public Boolean getBluetooth() {
		return arrBooleanProperties[ BooleanProperty.BLUETOOTH.ordinal() ];
	}
	
	public Boolean getWLAN() {
		return arrBooleanProperties[ BooleanProperty.WLAN.ordinal() ];
	}

	public Boolean getNFC() {
		return arrBooleanProperties[ BooleanProperty.NFC.ordinal() ];
	}
	
	public void setProperty( final BooleanProperty property,
									final Boolean bValue ) {
		final int iIndex = property.ordinal();
		this.arrBooleanProperties[ iIndex ] = bValue;
	}
	
	public void setProperty( final BooleanProperty property,
									final String strValue ) {
		final Boolean bValue = Utils.parseBoolean( strValue );
		this.setProperty( property, bValue ); 
	}

	public Boolean getProperty( final BooleanProperty property ) {
		final int iIndex = property.ordinal();
		final Boolean bValue = this.arrBooleanProperties[ iIndex ];
		return bValue;
	}

	
	public void setProperty( final IntegerProperty property,
									final Integer bValue ) {
		final int iIndex = property.ordinal();
		this.arrIntegerProperties[ iIndex ] = bValue;
	}
	
	public void setProperty( final IntegerProperty property,
									final String strValue ) {
		final Integer bValue = Utils.parseNumber( strValue );
		this.setProperty( property, bValue ); 
	}

	public Integer getProperty( final IntegerProperty property ) {
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
	
	
	// attempt to generate a helpful name
	public String getName() {
		final StringBuilder sb = new StringBuilder();
		if ( 1 == this.listTACs.size() ) {
			sb.append( "TAC " + listTACs.get( 0 ) );
		}
		
		final String strMarketing = 
						this.getProperty( TextProperty.MARKETING_NAME );
		if ( null != strMarketing && strMarketing.length() > 2 ) {
			if ( sb.length() > 0 ) sb.append( " - " );
			sb.append( strMarketing );
		}
		
		if ( sb.length() > 0 ) {
			return sb.toString();
		}
		
		sb.append( "TACs" );
		this.listTACs.forEach( l-> sb.append( " " + l ) );
		return sb.toString();
	}
	
}
