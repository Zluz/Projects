package jmr.pr121.storage;

import jmr.pr123.storage.GCSFactory;

public class GCSHelper {

	public static final GCSFactory GCS_FACTORY;
	
	static {
		//TODO have the S2 server push this up to init (maybe?)
		GCS_FACTORY = new GCSFactory( "pr121-s2gae.appspot.com" );
	}
	
	
	
	
}
