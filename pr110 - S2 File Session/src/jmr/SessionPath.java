package jmr;

import java.io.File;

public class SessionPath {

	private final static String[] BASE_SESSION_PATHS =
					{	"/Share/Sessions",
						"S:\\Sessions\\",
						"H:\\Share\\Sessions\\"
					};
	private static File fileBaseSessionPath = null;
	

	public static synchronized File getPath() {
		if ( null!=fileBaseSessionPath ) {
			return fileBaseSessionPath;
		} else {
			for ( final String strPath : BASE_SESSION_PATHS ) {
				final File file = new File( strPath );
				if ( file.isDirectory() ) {
					fileBaseSessionPath = file;
					return file;
				}
			}
		}
		System.err.println( "Could not locate base share path. "
							+ "Please ensure Share is mapped." );
		return null;
	}
	
}
