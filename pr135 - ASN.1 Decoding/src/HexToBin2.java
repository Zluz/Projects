import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HexToBin2 {
	
	// BCCH-DL-SCH-Message
	final static String OUT_FILE = "BCCH-DL-SCH-Message.bin";
	final static String INPUT = "000E8409D590440590808FC208817A100802411016C1E00242348261892228880800";
	
	// SystemInformationBlockType7
//	final static String OUT_FILE = "SIB7.bin";
//	final static String INPUT = "3C 11 94 C4 67 1A 46 B1 B4 6F" +
//							    "1C 47 31 D4 77 1E 47 B1 F4 7F" +
//							    "20 48 32 14 87 22 48 B2 34 8F" +
//							    "24 49 7C 00 00 00 FA AF FA AF";
	
	public static void main( final String[] args ) throws IOException {
		
		String strInput = INPUT.trim().toUpperCase();
		
		final File file = new File( OUT_FILE );
		final FileOutputStream fos = new FileOutputStream( file );
		
		System.out.println( "Writing to: " + file.getAbsolutePath() );
		
		while ( ! strInput.isEmpty() ) {
			
			final String strLead = strInput.substring( 0, 2 ).trim();
			strInput = strInput.substring( 2 ).trim();
			
			final int iLead = Integer.parseInt( strLead, 16 );
			
			System.out.print( iLead + "," );
			fos.write( iLead );
		}
		
		fos.close();
	}
}
