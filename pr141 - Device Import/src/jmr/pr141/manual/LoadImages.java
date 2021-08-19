package jmr.pr141.manual;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public class LoadImages {
	
	
	public static void process( final File fileRead,
								final File fileWrite ) throws Exception {
		final String strFilename = fileRead.getName();

		final String strTAC = strFilename.substring( 0, 8 );

		final Base64.Encoder encoder = Base64.getEncoder();
		
		final byte[] arrBin = Files.readAllBytes( fileRead.toPath() );
		
		final byte[] arrEnc = encoder.encode( arrBin );
		
		final String strEncoded = new String( arrEnc ); // .substring( 0, 40 );
		
		final String strChars = "Image Source=" + strFilename;
	
		final String strLine = "  " + strTAC 
				+ "\t-\t-,-,-,-,-,-,\t-\t-\t-\t-\t-\t-\t-\t"  
		//           1  2             3  4  5  6  7  8  9  10
				+ strChars + "\t " + strEncoded + "\n";
		
		Files.write( fileWrite.toPath(), strLine.getBytes(), 
										StandardOpenOption.APPEND );
		
		System.out.print( "\tTAC: " + strTAC ); 
		System.out.print( "\t" + arrBin.length + " binary bytes" );
		System.out.print( "\tB64: " + strEncoded.substring( 0, 20 ) + ".." );
		System.out.print( "\tFile: " + strFilename );
		
		System.out.println();
	}
	
	public static void main( final String[] args ) throws Exception {
		
//		final String strWorkDir = "/data/Tasks/20210309__COSMIC-417__Devices/";
//		final String strReadDir = strWorkDir + "Phone Images";
//		final String strTSVFile = strWorkDir + "new_images.tsv";
		
//		final File fileThisDir = new File( "." );
		
		final String strTSVFile = "new_images.tsv";
//		final String strTSVFile = fileThisDir.getName() + ".tsv";
		final String strReadDir = ".";
		
		final String strRegex = "[0-9]+_.+\\.(JPG|PNG)";
		
		final File fileTSV = new File( strTSVFile );

		System.out.println( "Creating file: " + fileTSV.getName() );
		
		fileTSV.delete();
		Files.write( fileTSV.toPath(), "\n\n".getBytes(), 
									StandardOpenOption.CREATE );
		Thread.sleep( 200 );
		
		final File fileReadDir = new File( strReadDir );
		int iCount = 0;
		for ( final File fileRead : fileReadDir.listFiles() ) {
			final String strFilename = fileRead.getName().toUpperCase();
			if ( strFilename.matches( strRegex ) ) {
				iCount++;
				process( fileRead, fileTSV );
			}
		}
		
		if ( iCount > 0 ) {
			System.out.println( ""+ iCount + " file(s) processed." );
		} else {
			System.out.println( "No files found matching pattern: " 
							+ strRegex );	
		}
	}
}
