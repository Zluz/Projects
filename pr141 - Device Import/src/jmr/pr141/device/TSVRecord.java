package jmr.pr141.device;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jmr.pr141.device.Device.TextProperty;

public class TSVRecord {

	final public static String NULL_INDICATOR = "-"; 
		
	private final Device device;
	
	public TSVRecord( final Device device ) {
		this.device = device;
	}
	
	

	public static List<Long> getTACsFromLine( final String strLine ) {
		final List<Long> list = new LinkedList<>();
		if ( null == strLine ) return list;
		if ( strLine.length() < 8 ) return list;
		
		final String strTACs;
		final int iPosTab = strLine.indexOf( '\t' );
		if ( -1 == iPosTab ) {
			strTACs = strLine;
		} else {
			strTACs = strLine.substring( 0, iPosTab );
		}
		
		for ( final String strRawTAC : strTACs.split( "," ) ) {
			final String strTAC = strRawTAC.trim();
			try {
				final Long lTAC = Long.parseLong( strTAC );
				if ( null != lTAC ) {
					list.add( lTAC );
				} 
			} catch ( final NumberFormatException e ) {
				// just ignore
			}
		}
		return list;
	}
	

	private String getTSVProperty( final Device.TextProperty property ) {
		final String strValue;
		if ( device.mapProperties.containsKey( property ) ) {
			strValue = device.mapProperties.get( property );
		} else {
			strValue = NULL_INDICATOR;
		}
		final int iPadding = property.iPadding;
		final String strPadded = 
					String.format( "%-" + iPadding + "s\t ", strValue );
		return strPadded;
	}
	
	private static String getNumeric( final Integer iValue ) {
		if ( null != iValue ) {
			return ""+ iValue;
		} else {
			return NULL_INDICATOR;
		}
	}

	private static String getBoolean( final Boolean bValue ) {
		if ( null != bValue ) {
			return bValue ? "Y" : "N";
		} else {
			return NULL_INDICATOR;
		}
	}
	
	private static Boolean setBoolean( final String strValue ) {
		if ( null == strValue ) return null;
		final String strTrimmed = strValue.trim();
		if ( "Y" == strTrimmed ) {
			return Boolean.TRUE;
		} else if ( "N" == strTrimmed ) {
			return Boolean.FALSE;
		} else {
			return null;
		}
	}
	
	
	
	public String toTSV() {
		final StringBuilder sb = new StringBuilder();
		
        final String strTACs = device.listTACs.stream()
				                .map( l-> ""+ l )
				                .collect( Collectors.joining(",") );
        sb.append( String.format( "%18s \t ", strTACs ) );
		
		sb.append( getNumeric( device.iSimCount ) + "," );
		sb.append( getBoolean( device.bBluetooth ) + "," );
		sb.append( getBoolean( device.bWLAN ) + "," );
		sb.append( getNumeric( device.iCountryCode ) + " \t " );
		
		sb.append( getTSVProperty( TextProperty.BRAND_NAME ) );
		sb.append( getTSVProperty( TextProperty.MODEL_NAME ) );
		sb.append( getTSVProperty( TextProperty.MARKETING_NAME ) );
		sb.append( getTSVProperty( TextProperty.DEVICE_TYPE ) );

		for ( final Entry<String, String> entry: device.mapChars.entrySet() ) {
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();
			sb.append( strKey + "=" + strValue + "|" );
		}
		
		sb.append( " \t " );
		final String strBase64 = 
						device.getProperty( TextProperty.IMAGE_BASE64 );
		if ( null != strBase64 ) {
			sb.append( strBase64 );
		} else {
			sb.append( NULL_INDICATOR );
		}
		sb.append( 0x0D0A ); // CR+LF
        
		return sb.toString();
	}
	
	

	public static Device fromTSV( final String strLine ) {
		if ( null == strLine ) return null;
		if ( strLine.isEmpty() ) return null;
		
		final String[] arrParts = strLine.split( "\t" );
		if ( arrParts.length < 7 ) return null;
		
		final String strTACs = arrParts[ 0 ];
		final String strInfoBlock = arrParts[ 1 ];
		final String[] arrInfo = strInfoBlock.split( "," );
		if ( 4 != arrInfo.length ) return null;
		
		final String strBrandName = arrParts[ 2 ].trim();
		final String strModelName = arrParts[ 3 ].trim();
		final String strMarketing = arrParts[ 4 ].trim();
		final String strDeviceType = arrParts[ 5 ].trim();
		
		final String strCharacteristics = arrParts[ 6 ];
		final String strImageBase64 = arrParts[ 7 ].trim();
		
		final List<Long> listTACs = getTACsFromLine( strTACs );
		
		final Device device = new Device( listTACs );
		device.mapProperties.put( TextProperty.BRAND_NAME, strBrandName );
		device.mapProperties.put( TextProperty.MODEL_NAME, strModelName );
		device.mapProperties.put( TextProperty.MARKETING_NAME, strMarketing );
		device.mapProperties.put( TextProperty.DEVICE_TYPE, strDeviceType );
		
//		device.strImageBase64 = strImageBase64;
		device.mapProperties.put( TextProperty.IMAGE_BASE64, strImageBase64 );

		try {
			final String strSimCount = arrInfo[ 0 ].trim();
			final int iSimCount = Integer.parseInt( strSimCount );
			device.iSimCount = iSimCount;
		} catch ( final NumberFormatException e ) {
			// then do not record a sim count
		}
		device.bBluetooth = setBoolean( arrInfo[ 1 ] );
		device.bWLAN = setBoolean( arrInfo[ 2 ] );
		try {
			final String strCountryCode = arrInfo[ 3 ].trim();
			final int iCountryCode = Integer.parseInt( strCountryCode );
			device.iCountryCode = iCountryCode;
		} catch ( final NumberFormatException e ) {
			// then do not record a country code
		}
				
		return device;
	}
	
	
}
