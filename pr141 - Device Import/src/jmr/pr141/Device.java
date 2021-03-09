package jmr.pr141;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class Device {
	
	final public static String NULL_INDICATOR = "-"; 
		
	public final static Gson GSON = new Gson();

	
	public static enum TextProperty {
		MARKETING_NAME( 20 ),
		MANUFACTURER( 50 ),
		BRAND_NAME( 16 ),
		MODEL_NAME( 20 ),
		
		BANDS( 50 ),
		BANDS_5G( 50 ),
		RADIO_INTERFACE( 20 ),
		OPERATING_SYSTEM( 12 ),
		DEVICE_TYPE( 10 ),
		
		CHARACTERISTICS( 100 ),
		;
		
		final int iPadding;
		
		private TextProperty( final int iPadding ) {
			this.iPadding = iPadding;
		}
	}
	
	
	final List<Long> listTACs = new LinkedList<>();
	
	final EnumMap<TextProperty,String> 
					mapProperties = new EnumMap<>( TextProperty.class );
	
	final Map<String,String> mapChars = new HashMap<>();
	
	Integer iCountryCode;
	Integer iSimCount;
	
	Boolean bBluetooth;
	Boolean bWLAN;
	
	String strImageBase64;
	
	
	public Device( final List<Long> listTACs ) {
		if ( null != listTACs ) {
			this.listTACs.addAll( listTACs );
		}
	}
	
	public void set( final TextProperty property,
					 final String strValue ) {
		this.mapProperties.put( property, strValue );
	}
	
	
	private void loadCharacteristics( final String strInput ) {
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
	
	
	public static Device importDeviceFromJSON( final File file ) 
													throws IOException {
		final Path path = file.toPath();
		final BufferedReader br = Files.newBufferedReader( path );
		
		final JsonObject jo = GSON.fromJson( br, JsonObject.class );
		if ( null == jo ) return null;
		if ( 0 == jo.size() ) return null;
		
		final JsonArray jaTACs = jo.getAsJsonArray( "tac-list" );
		final Type listType = new TypeToken<List<Long>>(){}.getType();
		final List<Long> listTACs = GSON.fromJson( jaTACs.toString(), listType);

		final Device device = new Device( listTACs );
		
		device.set(	TextProperty.MARKETING_NAME, 
					jo.get( "MARKETINGNAME" ).getAsString() );
		device.set(	TextProperty.MODEL_NAME, 
					jo.get( "MODELNAME" ).getAsString() );
		
		device.loadCharacteristics( 
				jo.get( "NETWORKCHARACTERISTICS" ).getAsString() );
		device.loadCharacteristics( 
				jo.get( "WDSNETWORKCHARACTERISTICS" ).getAsString() );

		final String strSimCount = jo.get( "SIMCOUNT" ).getAsString();
		try {
			final Integer iSimCount = Integer.valueOf( strSimCount );
			device.iSimCount = iSimCount;
		} catch ( NumberFormatException e ) {
			device.iSimCount = null;
		}
		
		device.strImageBase64 = 
					jo.get( "image-thumbnail-binary" ).getAsString();
		
		return device;
	}
	
	
	private String getTSVProperty( final TextProperty property ) {
		final String strValue;
		if ( this.mapProperties.containsKey( property ) ) {
			strValue = this.mapProperties.get( property );
		} else {
			strValue = NULL_INDICATOR;
		}
		final int iPadding = property.iPadding;
		final String strPadded = 
					String.format( "%-" + iPadding + "s\t ", strValue );
		return strPadded;
	}
	
	private String getNumeric( final Integer iValue ) {
		if ( null != iValue ) {
			return ""+ iValue;
		} else {
			return NULL_INDICATOR;
		}
	}

	private String getBoolean( final Boolean bValue ) {
		if ( null != bValue ) {
			return bValue ? "Y" : "N";
		} else {
			return NULL_INDICATOR;
		}
	}
	
	
	public String toTSV() {
		final StringBuilder sb = new StringBuilder();
		
        final String strTACs = this.listTACs.stream()
		                .map( l-> ""+ l )
		                .collect( Collectors.joining(",") );
        sb.append( String.format( "%18s \t ", strTACs ) );
		
		sb.append( getNumeric( this.iSimCount ) + "," );
		sb.append( getBoolean( this.bBluetooth ) + "," );
		sb.append( getBoolean( this.bWLAN ) + "," );
		sb.append( getNumeric( this.iCountryCode ) + " \t " );
		
		sb.append( getTSVProperty( TextProperty.BRAND_NAME ) );
		sb.append( getTSVProperty( TextProperty.MODEL_NAME ) );
		sb.append( getTSVProperty( TextProperty.MARKETING_NAME ) );
		sb.append( getTSVProperty( TextProperty.DEVICE_TYPE ) );

		for ( final Entry<String, String> entry: mapChars.entrySet() ) {
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();
			sb.append( strKey + "=" + strValue + "|" );
		}
		
		sb.append( " \t " );
		if ( null != this.strImageBase64 ) {
			sb.append( this.strImageBase64 );
		} else {
			sb.append( NULL_INDICATOR );
		}
		sb.append( 0x0A ); // LF
        
		return sb.toString();
	}
	
	
	
	public static void main( final String[] args ) throws IOException {
		final String strFile = "/data/Development/CM/"
				+ "jmr_Projects__20210129/pr141 - Device Import/files/"
				+ "Samsung_Galaxy_S4_SGH_M919V_Galaxy_S4_48089.json";
		final File file = new File( strFile );
		final Device device = Device.importDeviceFromJSON( file );
		
		System.out.println( device.toTSV() );
	}
	
}
