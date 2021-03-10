package jmr.pr141.device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Device {
	
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
//				final Device device = Device.importDeviceFromJSON( file );
				final Device device = JsonImport.importDeviceFromJSON( file );
				if ( null != device ) {
					
//					final String strTSV = device.toTSV() + "\n";
					final TSVRecord tsv = new TSVRecord( device );
					final String strTSV = tsv.toTSV() + "\n";
					
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
