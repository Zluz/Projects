package jmr.pr141.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jmr.pr141.device.Device;
import jmr.pr141.device.Device.BooleanProperty;
import jmr.pr141.device.Device.IntegerProperty;
import jmr.pr141.device.TextProperty;

/**
 * Import (convert to TSV database file) from a 
 * (tab-separated) text file export from the excel database spreadsheet.
 */
public class SpreadsheetImport {

	public final static String FILE_INPUT = 
		"D:\\Tasks\\20210309 - COSMIC-417 - Devices\\"
		+ "TAC Lookup 20210122\\TAC Lookup 20210122_tab_separated.txt";
	
	
	
	public static Device processLine( final String strLine ) {
		if ( null == strLine ) return null;
		
		final String[] arrParts = strLine.split( "\t" );
		if ( arrParts.length < 5 ) return null;

//		for ( final String strPart : arrParts ) {
//			System.out.println( "\t" + strPart );
//		}
		
		final String strTACs = arrParts[ 0 ];
		
		final List<Long> listTACs = 
							Utils.getNumbersFromLine( strTACs );
		
		if ( null == listTACs ) return null;
		if ( listTACs.isEmpty() ) return null;
		
		final Device device = new Device( listTACs );
		
		final EnumMap<TextProperty,String> mapP = device.getPropertyMap();
		final Map<String,String> mapC = device.getCharacteristicsMap();
		int i = 1;
		mapP.put( TextProperty.MARKETING_NAME, arrParts[ i++ ] );
		mapP.put( TextProperty.MANUFACTURER, arrParts[ i++ ] );
		mapP.put( TextProperty.BANDS, arrParts[ i++ ] );
		mapP.put( TextProperty.BANDS_5G, arrParts[ i++ ] );
		final String strLPWAN = arrParts[ i++ ]; // LPWAN, ex: "EC-GSM-IoT"
		final String strAllocDate = arrParts[ i++ ]; // Allocation Date
		final String strCountryCode = arrParts[ i++ ]; // Country Code
		final String strFixedCode = arrParts[ i++ ]; // Fixed Code
		final String strManufCode = arrParts[ i++ ]; // Manufacturer Code
		mapP.put( TextProperty.RADIO_INTERFACE, arrParts[ i++ ] );
		mapP.put( TextProperty.BRAND_NAME, arrParts[ i++ ] );
		mapP.put( TextProperty.MODEL_NAME, arrParts[ i++ ] );
		mapP.put( TextProperty.OPERATING_SYSTEM, arrParts[ i++ ] );
		final String strNFC = arrParts[ i++ ]; // NFC
		final String strBluetooth = arrParts[ i++ ]; // Bluetooth
		final String strWLAN = arrParts[ i++ ]; // WLAN
		mapP.put( TextProperty.DEVICE_TYPE, arrParts[ i++ ] );
		final String strOEM = arrParts[ i++ ]; // OEM
		final String strRemUICC = arrParts[ i++ ]; // Removable UICC
		final String strRemEUICC = arrParts[ i++ ]; // Removable EUICC
		final String strNRemUICC = arrParts[ i++ ]; // NonRemovable UICC
		final String strNRemEUICC = arrParts[ i++ ]; // NonRemovable EUICC
		final String strSimSlot = arrParts[ i++ ]; // Simslot
		final String strImeiQtySpt = arrParts[ i++ ]; // Imeiquantitysupport
		
		device.setSimCount( Utils.parseNumber( strSimSlot ) );
		device.setCountryCode( Utils.parseNumber( strCountryCode ) );
//		device.bBluetooth = Utils.parseBoolean( strBluetooth );
		device.setBooleanProperty( BooleanProperty.BLUETOOTH, strBluetooth );
//		device.bWLAN = Utils.parseBoolean( strWLAN );
		device.setBooleanProperty( BooleanProperty.WLAN, strWLAN );
//		device.bNFC = Utils.parseBoolean( strNFC );
		device.setBooleanProperty( BooleanProperty.NFC, strNFC );
//		device.iImeiQtySupport = Utils.parseNumber( strImeiQtySpt );
		device.setIntegerProperty( IntegerProperty.IMEI_QTY_SUPPORT, strImeiQtySpt );

		if ( useful( strAllocDate ) ) mapC.put( "AllocationDate", strAllocDate );
		if ( useful( strFixedCode ) ) mapC.put( "FixedCode", strFixedCode );
		if ( useful( strManufCode ) ) mapC.put( "ManufacturerCode", strManufCode );
		if ( useful( strLPWAN ) ) mapC.put( "LPWAN", strLPWAN );
		if ( useful( strOEM ) ) mapC.put( "OEM", strOEM );
		if ( useful( strRemUICC ) ) mapC.put( "RemovableUICC", strRemUICC );
		if ( useful( strRemEUICC ) ) mapC.put( "RemovableEUICC", strRemEUICC );
		if ( useful( strNRemUICC ) ) mapC.put( "NonRemovableUICC", strNRemUICC );
		if ( useful( strNRemEUICC ) ) mapC.put( "NonRemovableEUICC", strNRemEUICC );

		mapP.entrySet().removeIf( e-> "Not Known".equals( e.getValue() ) );
		mapP.entrySet().removeIf( e-> "-".equals( e.getValue() ) );
		
		for ( final TextProperty property : TextProperty.values() ) {
			if ( mapP.containsKey( property ) ) {
				
				final String strValue = mapP.get( property );
				String strNew = strValue.trim();
				final int iLen = strNew.length();
				if ( strNew.length() > 2 
							&& '"' == strNew.charAt( 0 )
							&& '"' == strNew.charAt( iLen - 1 ) ) {
					strNew = strNew.substring( 1, iLen - 1 );
				}
				
				if ( strValue != strNew ) {
					mapP.put( property, strNew ); 
				}
				
			}
		}
		
		mapP.entrySet().removeIf( e-> "".equals( e.getValue() ) );

//		final TSVRecord tsv = new TSVRecord( device );
//		System.out.println( tsv.toTSV() );
		
		return device;
	}
	
	public static boolean useful( final String strValue ) {
		if ( null == strValue ) return false;
		if ( strValue.isEmpty() ) return false;
		final String strTrimmed = strValue.trim().toUpperCase();
		if ( strTrimmed.isEmpty() ) return false;
		if ( "NOT KNOWN".equals( strTrimmed ) ) return false;
		if ( "NOT SUPPORTED".equals( strTrimmed ) ) return false;
		
		if ( strTrimmed.startsWith( "0 " ) 
				&& strTrimmed.length() < 8 ) return false;
		
		if ( "-".equals( strTrimmed ) ) return false;
		return true;
	}
	
	
	public static void main( final String[] args ) throws IOException {
		
		// prepare the new "TAC_Lookup.tsv"
		final String strWorkDir = "D:\\Tasks\\"
							+ "20210309 - COSMIC-417 - Devices\\";
		final File fileDatabase = new File( strWorkDir + "TAC_Lookup.tsv" );
		final File fileHeader = new File( strWorkDir + "header.txt" );
		if ( fileDatabase.exists() ) fileDatabase.delete();

		final byte[] arrHeader = Files.readAllBytes( fileHeader.toPath() );
		
		Files.write( fileDatabase.toPath(), arrHeader,
						StandardOpenOption.CREATE );
		

		
		
		
		
		
		final File fileInput = new File( FILE_INPUT );
		final FileReader frInput = new FileReader( fileInput );
//		final BufferedReader brInput;
		
		
		
		
		int iLine = 0;
		
		final List<Device> listDevices = new LinkedList<>();
		listDevices.add( Device.DUMMY_DEVICE );
		
		try( final BufferedReader brInput = new BufferedReader( frInput ) ) {
		    for( String strLine; (strLine = brInput.readLine()) != null; ) {
		    	iLine++;
//		        System.out.println( "Line: \"" + strLine + "\"" );
		        final Device device = processLine( strLine );
		        if ( null != device ) {
		        	listDevices.add( device );

					final TSVRecord tsv = new TSVRecord( device );
//					System.out.print( tsv.toTSV() );
		        
					final String strTSV = tsv.toTSV();

					Files.write( fileDatabase.toPath(), strTSV.getBytes(),
									StandardOpenOption.APPEND );
							        			        	
		        }
		        if ( 0 == iLine % 2000 ) {
		        	final double dPercent = (double)iLine * 100.0 / 191930;
//		        	System.out.println( "Line " + iLine );
		        	System.out.println( "Progress: " + (int)dPercent + " %" );
		        }
		        if ( iLine > 10000 ) break;
		    }
		    System.out.println( "Done." );
		    // line is not visible here.
		}
		
//		for ( final Device device : listDevices ) {
//			final TSVRecord tsv = new TSVRecord( device );
//			System.out.print( tsv.toTSV() );
//		}
		
		
	}
	
}
