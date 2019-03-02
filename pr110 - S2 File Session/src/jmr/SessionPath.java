package jmr;

import java.io.File;

import jmr.util.NetUtil;

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
	
	
	public static File getSessionDir() {
		final String strMAC = NetUtil.getMAC();
		final File fileDir = new File( SessionPath.getPath(), strMAC );
		return fileDir;
	}
	
	
	public static File getPath_DevelopmentExport() {
		final File fileSession = getPath();
		final File fileExport = 
				new File( fileSession.getParentFile(), "Development/Export" );
		return fileExport;
	}
	
	public static void main(String[] args) {
		System.out.println( "Session path: " + getSessionDir() );
		System.out.println( "Export path: " + getPath_DevelopmentExport() );
	}
	
}
