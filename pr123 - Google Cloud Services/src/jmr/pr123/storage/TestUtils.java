package jmr.pr123.storage;

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import jmr.util.http.ContentType;


public class TestUtils {

	public static final String BUCKET_NAME = "pr121-s2gae.appspot.com";

	public static File getFile() {
		final File file = new File( 
//				"H:\\Sessions\\B8-27-EB-4E-46-E2\\test-1_0MP.jpg" 
				"S:\\Sessions\\test_resources\\capture_vid1-thumb.jpg" 
				);
		return file;
	}
	

	public static String getTimeStamp() {
		final String strPattern = "YYYYMMdd-HHmmss";
		final DateTimeFormatter dtf = DateTimeFormat.forPattern(strPattern);
		final DateTime dt = DateTime.now(DateTimeZone.UTC);
		final String strTime = dt.toString( dtf );
		return strTime;
	}
	
	
	

	public static String test() {

		final GCSFactory factory = new GCSFactory( TestUtils.BUCKET_NAME );
		
		final File file = TestUtils.getFile();

		final GCSFileWriter gcsf = 
					factory.create( file.getName(), ContentType.IMAGE_JPEG );
		
		try {
			gcsf.upload( file );
			
			return "File saved (?)";
			
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Exception encountered: " + e.toString();
		}

	}

	
	public static void main( final String[] args ) {
		final String strResult = TestUtils.test();
		System.out.println( "\tResult of test(): " + strResult ); 

	}
	
}
