package jmr.sharedb;

import java.io.File;
import java.util.Date;

import jmr.util.NetUtil;

public class ClientSession {


	final public static String PATH_SHARE = 
			( '\\'==File.separatorChar )
					? "S:\\" 
					: "/Share";
	
	
	private String strSessionID;
	
	private File fileSessionDir;

	private static ClientSession instance;
	
	
	
	
	private ClientSession() {
		this.strSessionID = getSessionID();
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
			this.getSessionID();
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
	
	
	public String getSessionID() {
		if ( null==strSessionID ) {
			
			final long lTime = new Date().getTime();
//				final String strTimeNow = Long.toString( lTime );
//				final int iLen = strTimeNow.length();
//				final String strMark = strTimeNow.substring( iLen - 10 );
			final String strMark = String.format( "%011X", lTime );
			
			final String strProcessName = NetUtil.getProcessName();
			final String strPID = strProcessName.split( "@" )[0];
			final Long lPID = Long.parseLong( strPID );
			final String strPIDx = String.format( "%05X", lPID );

			strSessionID = NetUtil.get().getMAC() 
							+ "--" + strPIDx + "--" + strMark;
		}
		return strSessionID;
	}
	

	public static void main( final String[] args ) {
		final ClientSession session = new ClientSession();
		session.createSessionDirectory();
	}
	
	
}
