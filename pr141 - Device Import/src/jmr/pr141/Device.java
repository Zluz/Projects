package jmr.pr141;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
	

	private static String getJsonMember( final JsonObject jo,
								  		 final String strKey ) {
		if ( null == jo ) return NULL_INDICATOR;
		if ( ! jo.has( strKey ) ) return NULL_INDICATOR;
		final JsonElement je = jo.get( strKey );
		if ( null == je ) return NULL_INDICATOR;
		if ( je.isJsonNull() ) return NULL_INDICATOR;
		final String strValue = je.getAsString();
		if ( null == strValue ) return NULL_INDICATOR;
		return strValue;
		
	}
	
	
	public static Device importDeviceFromJSON( final File file ) 
													throws IOException {
		final Path path = file.toPath();
		final BufferedReader br = Files.newBufferedReader( path );
		
		final JsonObject jo = GSON.fromJson( br, JsonObject.class );
		if ( null == jo ) return null;
//		if ( 0 == jo.size() ) return null;
		
		if ( ! jo.has( "tac-list" ) ) {
			return null;
		}
		
		final JsonArray jaTACs = jo.getAsJsonArray( "tac-list" );
		
		final Type listType = new TypeToken<List<Long>>(){}.getType();
		final List<Long> listTACs = GSON.fromJson( jaTACs.toString(), listType);

		final Device device = new Device( listTACs );
		
		device.set(	TextProperty.MARKETING_NAME,
				getJsonMember( jo, "MARKETINGNAME" ) );
		device.set(	TextProperty.MODEL_NAME,
				getJsonMember( jo, "MODELNAME" ) );
		
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
		
		device.strImageBase64 = getJsonMember( jo, "image-thumbnail-binary" );
		
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
		sb.append( 0x0D0A ); // CR+LF
        
		return sb.toString();
	}
	
	
	
	public static void main( final String[] args ) throws IOException {
		
//		final String strFile = "/data/Development/CM/"
//				+ "jmr_Projects__20210129/pr141 - Device Import/files/"
//				+ "Samsung_Galaxy_S4_SGH_M919V_Galaxy_S4_48089.json";
//		final String strFile = "D:\\Development\\CM\\"
//				+ "jmr__Projects__20200908\\pr141 - Device Import\\files\\"
//				+ "Samsung_Galaxy_S4_SGH_M919V_Galaxy_S4_48089.json";
//		final File file = new File( strFile );
//		final Device device = Device.importDeviceFromJSON( file );
//		System.out.println( device.toTSV() );
		
		
		final String strWorkDir = "D:\\Tasks\\"
								+ "20210309 - COSMIC-417 - Devices\\";
		final File fileDatabase = new File( strWorkDir + "catalog.tsv" );
		final File fileHeader = new File( strWorkDir + "header.txt" );
		if ( fileDatabase.exists() ) fileDatabase.delete();
		
		final byte[] arrHeader = Files.readAllBytes( fileHeader.toPath() );
		
		Files.write( fileDatabase.toPath(), arrHeader,
						StandardOpenOption.CREATE );
		
		final Map<Integer,Long> mapTACCounts = new HashMap<>();
		
		final String strDir = strWorkDir + "device-mine-2019091104";
		final File fileDir = new File( strDir );

		final File[] arrFiles = fileDir.listFiles();

		final long lTotalFiles = arrFiles.length;
		long lCurrentFile = 0;
				
		for ( final File file : arrFiles ) {
			lCurrentFile++;
			if ( file.isFile() ) {
				final Device device = Device.importDeviceFromJSON( file );
				if ( null != device ) {
					
					final String strTSV = device.toTSV() + "\n";
					
//					System.out.print( strTSV );
					if ( 0 == lCurrentFile % 500 ) {
						System.out.println( "File " + lCurrentFile 
										+ " of " + lTotalFiles );
					}
					
					Files.write( fileDatabase.toPath(), strTSV.getBytes(),
									StandardOpenOption.APPEND );
					
					final int iTACCount = device.listTACs.size();
					if ( mapTACCounts.containsKey( iTACCount ) ) {
						final long lCount = mapTACCounts.get( iTACCount );
						mapTACCounts.put( iTACCount, lCount + 1 );
					} else {
						mapTACCounts.put( iTACCount, 1L );
					}
					
				} else {
//					System.out.println( 
//							"  Null import from: " + file.getName() );
				}
			}
			
//			if ( mapTACCounts.get( 1 ) > 1000 ) break;
//			if ( lCurrentFile > 4000 ) break;
		}
		
		System.out.println( "TAC count distribution:\n" 
						+ mapTACCounts.toString() );
		// for the first 1000:
		// {1=1001, 2=323, 3=155, 4=87, 5=86, 6=50, 7=30, 8=23, 9=25, ...

	}
	
}
