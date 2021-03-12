package jmr.pr141.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import jmr.pr141.device.Device;
import jmr.pr141.device.TextProperty;

public class JsonImport {
	

	public final static Gson GSON = new Gson();

	
	final public static String NULL_INDICATOR = "-"; 
		

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
			device.setSimCount( iSimCount );
		} catch ( NumberFormatException e ) {
			device.setSimCount( null );
		}
		
//		device.strImageBase64 = getJsonMember( jo, "image-thumbnail-binary" );
		device.set(	TextProperty.IMAGE_BASE64,
							getJsonMember( jo, "image-thumbnail-binary" ) );
		
		return device;
	}
	
	
	
	
	
	

	
	public static void main( final String[] args ) throws IOException {
		
		// running this will rebuild catalog.tsv from the DM dir
		
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
		final File fileDatabase = new File( strWorkDir + "device-mine.tsv" );
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
//				final Device device = Device.importDeviceFromJSON( file );
				final Device device = JsonImport.importDeviceFromJSON( file );
				if ( null != device ) {
					
//					final String strTSV = device.toTSV() + "\n";
					final TSVRecord tsv = new TSVRecord( device );
					final String strTSV = tsv.toTSV(); //  + "\n";
					
//					System.out.print( strTSV );
					if ( 0 == lCurrentFile % 500 ) {
						System.out.println( "File " + lCurrentFile 
										+ " of " + lTotalFiles );
					}
					
					Files.write( fileDatabase.toPath(), strTSV.getBytes(),
									StandardOpenOption.APPEND );
					
					final int iTACCount = device.getTACs().size();
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
//			if ( lCurrentFile > 1000 ) break;
		}
		
		System.out.println( "TAC count distribution:\n" 
						+ mapTACCounts.toString() );
		// for the first 1000:
		// {1=1001, 2=323, 3=155, 4=87, 5=86, 6=50, 7=30, 8=23, 9=25, ...

	}
	
	

}
