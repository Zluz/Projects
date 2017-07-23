package jmr.sharedb;

import java.io.File;

import jmr.util.NetUtil;

public class ClientSession {


	final public static String PATH_SHARE = 
			( '\\'==File.separatorChar )
					? "S:\\" 
					: "/Share";
	
	
	private final String strSessionID;
	
	private File fileSessionDir;

	private static ClientSession instance;
	
	
	
	
	private ClientSession() {
		this.strSessionID = NetUtil.getSessionID();
		this.createSessionDirectory();
	}


	public static ClientSession get() {
		if ( null==instance ) {
			instance = new ClientSession();
		}
		return instance;
	}

	
	public void createSessionDirectory() {
		if ( null==this.fileSessionDir ) {
			final String strSessionDir = PATH_SHARE 
						+ File.separator + "Sessions" 
						+ File.separator + strSessionID;
			this.fileSessionDir = new File( strSessionDir );
			this.fileSessionDir.mkdirs();
		}
	}
	
	
	public File getSessionDir() {
		return this.fileSessionDir;
	}
	

	public static void main( final String[] args ) {
		final ClientSession session = new ClientSession();
		session.createSessionDirectory();
	}
	
	
}
