package jmr.util.hardware.rpi;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.SysexMessage;

import org.apache.commons.lang3.StringUtils;

import jmr.util.FileUtil;

public class DeviceExamine {
	
	public static enum Key {
		CPU_THROTTLE( "cpu-throttle" ),
		CPU_TEMPERATURE( "cpu-temperature" ),
		SENSOR_TEMPERATURE_VALUE( "sensor-temperature-value" ),
		SENSOR_HUMIDITY_VALUE( "sensor-humidity-value" ),
		;
		
		public final String strKey;
		
		Key( final String strKey ) {
			this.strKey = strKey;
		}
	}

	private final static String DATA_FILENAME = "/tmp/device_examine.out";

	private static DeviceExamine instance;
	
	private Map<String,String> mapData = null;
	private long lLastLastModified;
	
	
	public static synchronized DeviceExamine get() {
		if ( null==instance ) {
			instance = new DeviceExamine();
		}
		return instance;
	}
	
	
	//TODO this is inefficient. the whole class is really. optimize sometime.
	private void readFile() {
		final File file = new File( DATA_FILENAME );
		if ( ! file.isFile() ) return;

		final long lThisLastModified = file.lastModified();
		if ( lLastLastModified == lThisLastModified ) return;

		lLastLastModified = lThisLastModified;
		
		final String strContent = FileUtil.readFromFile( file );
		
		if ( StringUtils.isBlank( strContent ) ) {
			return;
		}

		final Map<String,String> map = new HashMap<>();

		for ( final String strLine : strContent.split( "\\n" ) ) {
			final int iPos = strLine.indexOf( ": " );
			final int iLead = strLine.lastIndexOf( "  \"" );
			if ( 0==iLead && iPos > 5 ) {
				
				String strKey = strLine.substring( 0, iPos - 1 ).trim();
				String strValue = strLine.substring( iPos + 1 ).trim();
				strKey = StringUtils.remove( strKey, "\"" );
				strValue = StringUtils.removeEnd( strValue, "," );
				strValue = StringUtils.remove( strValue, '"' );
				strValue = StringUtils.replace( strValue, "_", " " );
				map.put( strKey, strValue );

			}
		}
		this.mapData = map;
	}
	
	
	public String getValue( final String strName ) {
		this.readFile();
		
		if ( null==mapData ) return null;
		if ( mapData.isEmpty() ) return null;
		
		final String strValue = mapData.get( strName );
		return strValue;
	}
	
	
	public String getValue( final Key key ) {
		final String strValue = this.getValue( key.strKey );
		if ( null!=strValue ) {
			return strValue.trim();
		} else {
			return "";
		}
	}
	
	
	public boolean bArmFreqCappedCurrently;
	public boolean bArmFreqCappedOccurred;
	public boolean bUnderVoltageCurrently;
	public boolean bUnderVoltageOccurred;
	public boolean bThrottleCurrently;
	public boolean bThrottleOccurred;
	
	
	// see https://www.raspberrypi.org/forums/viewtopic.php?f=63&t=147781&start=50#p972790
	public void calcThrottleValues() {

		bUnderVoltageCurrently = false;
		bArmFreqCappedCurrently = false;
		bThrottleCurrently = false;
		bUnderVoltageOccurred = false;
		bArmFreqCappedOccurred = false;
		bThrottleOccurred = false;

		String strHex = getValue( Key.CPU_THROTTLE );
		strHex = strHex.trim();
		if ( StringUtils.isBlank( strHex ) ) return;
		
		strHex = strHex.toLowerCase();
		strHex = StringUtils.remove( strHex, "\\" );
		strHex = StringUtils.removeStart( strHex, "0x" );
		
		final BigInteger bigint;
//		if ( strHex.contains( "x0\\" ) ) {
//			bigint = BigInteger.ZERO;
//		} else {
			int iValue;
			try {
//				iValue = Integer.parseInt( strHex.substring( 2 ), 16 );
				iValue = Integer.parseInt( strHex, 16 );
			} catch ( final NumberFormatException e ) {
				System.err.println( "Failed to read CPU_THROTTLE: " + strHex ); 
				iValue = 0;
			}
			bigint = BigInteger.valueOf( iValue );
//		}
//		final String strBin = Integer.toBinaryString( iValue );
//		final BigInteger bigint = BigInteger.valueOf( iValue );
		
		bUnderVoltageCurrently = bigint.testBit( 0 );
		bArmFreqCappedCurrently = bigint.testBit( 1 );
		bThrottleCurrently = bigint.testBit( 2 );
		bUnderVoltageOccurred = bigint.testBit( 16 );
		bArmFreqCappedOccurred = bigint.testBit( 17 );
		bThrottleOccurred = bigint.testBit( 18 );
	}
	
	public String getThrottleStatus() {
		this.calcThrottleValues();
		final StringBuilder sb = new StringBuilder();
		
		if ( this.bThrottleCurrently ) {
			sb.append( "+Th " );
		} else if ( this.bThrottleOccurred ) {
			sb.append( ".Th " );
		}
		
		if ( this.bUnderVoltageCurrently ) {
			sb.append( "+UV " );
		} else if ( this.bUnderVoltageOccurred ) {
			sb.append( ".UV " );
		}
		
		if ( this.bArmFreqCappedCurrently ) {
			sb.append( "+FC " );
		} else if ( this.bArmFreqCappedOccurred ) {
			sb.append( ".FC " );
		}
		
		if ( 0==sb.length() ) {
			sb.append( "(ok)" );
		}
		
		return sb.toString();
	}
	
	
}
