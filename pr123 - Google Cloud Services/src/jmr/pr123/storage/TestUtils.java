package jmr.pr123.storage;

import java.io.File;


public class TestUtils {

	public static final String BUCKET_NAME = "pr121-s2gae.appspot.com";

	public static File getFile() {
		final File file = new File( 
				"H:\\Sessions\\B8-27-EB-4E-46-E2\\test-1_0MP.jpg" );
		return file;
	}
	
	
	
	
	public static void main( final String[] args ) {

//		final CloudStorage storage = new CloudStorage();
		final CloudStorage02 storage = new CloudStorage02();
		final String strResult = storage.test();
		System.out.println( "\tResult of test(): " + strResult ); 
	}
	
}
