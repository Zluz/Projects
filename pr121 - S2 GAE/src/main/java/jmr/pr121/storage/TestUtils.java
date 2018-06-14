package jmr.pr121.storage;

import java.io.File;

public abstract class TestUtils {

	private TestUtils(){}
	
	public static final String BUCKET_NAME = "pr121-s2gae.appspot.com";

	public static File getFile() {
		final File file = new File( 
				"H:\\Sessions\\B8-27-EB-4E-46-E2\\test-1_0MP.jpg" );
		return file;
	}
	
	
	
}
