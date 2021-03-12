package jmr.pr141.conversion;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jmr.pr141.device.Device;
import jmr.pr141.device.Device.BooleanProperty;
import jmr.pr141.device.Device.IntegerProperty;
import jmr.pr141.device.TextProperty;


public class TSVRecord {

	final public static String NULL_INDICATOR = "-"; 
		
	private final Device device;
	
	
	public TSVRecord( final Device device ) {
		this.device = device;
	}
	

	private String getTSVProperty( final TextProperty property ) {
		final String strValue;
		final EnumMap<TextProperty,String> map = device.getPropertyMap();
		if ( map.containsKey( property ) ) {
			strValue = map.get( property );
		} else {
			strValue = NULL_INDICATOR;
		}
		final int iPadding = property.getPadding();
		final String strPadded = 
					String.format( "%-" + iPadding + "s \t ", strValue );
		return strPadded;
	}
	
	public static String getNumeric( final Integer iValue ) {
		if ( null != iValue ) {
			return ""+ iValue;
		} else {
			return NULL_INDICATOR;
		}
	}

	public static String getBoolean( final Boolean bValue ) {
		if ( null != bValue ) {
			return bValue ? "Y" : "N";
		} else {
			return NULL_INDICATOR;
		}
	}

	
	public static String shorten( final String strLine ) {
		if ( null == strLine ) return "";
		final int iLen = strLine.length();
		if ( 0 == iLen ) return "";
		
		if ( ' ' == strLine.charAt( iLen - 1 ) ) {
			final String strNew = strLine.substring( 0, iLen - 1 );
			return strNew;
		}
		
		final int iPosTS = strLine.lastIndexOf( "\t " );
		if ( iPosTS > -1 ) {
			final String strFront = strLine.substring( 0, iPosTS + 1 );
			final String strBack = strLine.substring( iPosTS + 2, iLen );
			String strNew = strFront + strBack;
			return strNew;
		}

//		final int iPosST = strLine.lastIndexOf( " \t" );
//		if ( iPosST > -1 ) {
//			final String strFront = strLine.substring( 0, iPosST );
//			final String strBack = strLine.substring( iPosST + 1, iLen );
//			String strNew = strFront + strBack;
//			return strNew;
//		}
		
		if ( ' ' == strLine.charAt( 0 ) ) {
			final String strCropped = strLine.substring( 1 );
			return strCropped;
		}

		return strLine;
	}
	
	public static String check( final StringBuilder sb,
							    final int iTargetLength ) {
		String strLine = sb.toString();
		int iCharLen = strLine.length();
		sb.setLength( 0 );
		final String strTail = strLine.substring( iCharLen - 2, iCharLen - 0 );
		if ( "\t ".equals( strTail ) ) {
			strLine = strLine.substring( 0, iCharLen - 2 );
			iCharLen = strLine.length();
		}
		
		int iTabLen = Utils.getTabbedLength( strLine );
		
		if ( iTabLen < iTargetLength ) { // too short
			do {
				strLine += " ";
				iTabLen = Utils.getTabbedLength( strLine );
			} while ( iTabLen < iTargetLength );
			return strLine;
		} else if ( iTabLen > iTargetLength ) { // too long
			
			int iLastCharLen;
			do {
				iLastCharLen = strLine.length();
				strLine = shorten( strLine );
				iTabLen = Utils.getTabbedLength( strLine );
			} while ( iTabLen > iTargetLength 
							&& iLastCharLen != strLine.length() );

			while ( iTabLen < iTargetLength ) {
				strLine += " ";
				iTabLen = Utils.getTabbedLength( strLine );
			}
			
			return strLine;
		} else { // just right
			return strLine;
		}
	}
						
	
	
	public String toTSV() {
		
		final StringBuilder sb = new StringBuilder();
		
        final String strTACs = device.getTACs().stream()
				                .map( l-> ""+ l )
				                .collect( Collectors.joining(",") );
        sb.append( String.format( "%18s \t ", strTACs ) );

		sb.append( getTSVProperty( TextProperty.DEVICE_TYPE ) );
		
		sb.append( check( sb, 43 ) );
		sb.append( "\t " );
		
		sb.append( getNumeric( device.getSimCount() ) + "," );
		
		sb.append( getNumeric( device.getIntegerProperty( 
						IntegerProperty.IMEI_QTY_SUPPORT ) ) + "," );
		
		sb.append( getBoolean( device.getBluetooth() ) + "," );
		sb.append( getBoolean( device.getWLAN() ) + "," );
		sb.append( getBoolean( device.getNFC() ) + "," );

		sb.append( getNumeric( device.getIntegerProperty( 
						IntegerProperty.COUNTRY_CODE ) ) + "," );
		
		final String strFront = sb.toString();
		sb.setLength( 0 );
		
		sb.append( getTSVProperty( TextProperty.MODEL_NAME ) );
		sb.append( getTSVProperty( TextProperty.MARKETING_NAME ) );
		sb.append( getTSVProperty( TextProperty.BRAND_NAME ) );
		
		sb.append( check( sb, 64 ) );
		sb.append( "\t " );

		final Map<String, String> map = device.getCharacteristicsMap();
		for ( final Entry<String, String> entry: map.entrySet() ) {
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
//		sb.append( 0x0D0A ); // CR+LF
//		sb.append( "\n" );
		sb.append( (char)0x0D );sb.append( (char)0x0A ); // CR+LF
        
		return strFront + sb.toString();
	}
	
	

	public static Device fromTSV( final String strLine ) {
		if ( null == strLine ) return null;
		if ( strLine.isEmpty() ) return null;
		
		final String[] arrParts = strLine.split( "\t" );
		if ( arrParts.length < 7 ) return null;
		
		final String strTACs = arrParts[ 0 ];
		final String strInfoBlock = arrParts[ 2 ];
		final String[] arrInfo = strInfoBlock.split( "," );
		if ( 6 != arrInfo.length ) return null;
		
		final String strDeviceType = arrParts[ 1 ].trim();
		final String strModelName = arrParts[ 3 ].trim();
		final String strMarketing = arrParts[ 4 ].trim();
		final String strBrandName = arrParts[ 5 ].trim();
		
		final String strCharacteristics = arrParts[ 6 ];
		final String strImageBase64 = arrParts[ 7 ].trim();
		
		final List<Long> listTACs = Utils.getNumbersFromLine( strTACs );
		
		final Device device = new Device( listTACs );
		final EnumMap<TextProperty, String> map = device.getPropertyMap();
		map.put( TextProperty.BRAND_NAME, strBrandName );
		map.put( TextProperty.MODEL_NAME, strModelName );
		map.put( TextProperty.MARKETING_NAME, strMarketing );
		map.put( TextProperty.DEVICE_TYPE, strDeviceType );
		
		map.put( TextProperty.IMAGE_BASE64, strImageBase64 );

		device.setIntegerProperty( IntegerProperty.SIM_COUNT, arrInfo[ 0 ] );
		device.setIntegerProperty( IntegerProperty.IMEI_QTY_SUPPORT, arrInfo[ 0 ] );
		device.setIntegerProperty( IntegerProperty.COUNTRY_CODE, arrInfo[ 0 ] );
		
		device.setBooleanProperty( BooleanProperty.BLUETOOTH, arrInfo[ 2 ] );
		device.setBooleanProperty( BooleanProperty.WLAN, arrInfo[ 3 ] );
		device.setBooleanProperty( BooleanProperty.NFC, arrInfo[ 4 ] );
				
		return device;
	}
	
}
