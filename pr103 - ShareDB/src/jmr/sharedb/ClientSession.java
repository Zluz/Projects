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

	
	public void deleteDirRecurse( final File f ) {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				deleteDirRecurse(c);
		}
		if (!f.delete()) {
//			throw new FileNotFoundException("Failed to delete file: " + f);
		}
	}
	
	
	public void createSessionDirectory() {
		if ( null==this.fileSessionDir ) {

			final String strSessionDir = PATH_SHARE 
						+ File.separator + "Sessions" 
						+ File.separator + strSessionID;
			
			// clear out old sessions of the same machine..
			int iPos = strSessionID.indexOf( "--" );
			iPos = strSessionID.indexOf( "--", iPos+1 );
			final String strSessionOldPattern = strSessionID.substring( 0, iPos );
			final File fileParent = new File( PATH_SHARE 
						+ File.separator + "Sessions" );
			for ( final File fileChild : fileParent.listFiles() ) {
				try {
					final String strName = fileChild.getName();
					if ( strName.startsWith( strSessionOldPattern ) ) {
						deleteDirRecurse( fileChild );
					}
				} catch ( final Exception e ) {
					e.printStackTrace();
				}
			}
			
			// now create the new directory for this session
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
