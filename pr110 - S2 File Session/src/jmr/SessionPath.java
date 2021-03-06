package jmr;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import jmr.util.NetUtil;

public class SessionPath {

	private final static String[] BASE_SESSION_PATHS =
					{	"/Share/",
						"\\\\192.168.6.220\\Share\\",
						"\\\\192.168.6.223\\Share\\",
						"S:\\",
						"H:\\Share\\"
					};
	final private static List<File> fileBaseSessionPath = new LinkedList<>();
	

	public static synchronized List<File> getPaths() {
		
		if ( fileBaseSessionPath.isEmpty() ) {
			for ( final String strPath : BASE_SESSION_PATHS ) {
				final File file = new File( strPath, "Sessions" );
				if ( file.isDirectory() ) {
					fileBaseSessionPath.add( file );
				}
			}
		}
//		System.err.println( "Could not locate base share path. "
//							+ "Please ensure Share is mapped." );
		return fileBaseSessionPath;
	}
	
	
	public static List<File> getSessionDirs() {
		final String strMAC = NetUtil.getMAC();
		final List<File> list = new LinkedList<>();
		for ( final File file : SessionPath.getPaths() ) {
			list.add( new File( file, strMAC ) );
		}
		return list;
	}

	public static File getSessionDir() {
		final List<File> list = getSessionDirs();
		if ( list.isEmpty() ) throw new IllegalStateException( 
										"Session directory not found." );
		return list.get( 0 );
	}
	
	
	public static List<File> getPath_DevelopmentExport() {
		final List<File> list = new LinkedList<>();
		for ( final String strFile : BASE_SESSION_PATHS ) {
			final File fileExport = 
					new File( strFile, "Development/Export" );
			if ( fileExport.isDirectory() ) { 
				list.add( fileExport );
			}
		}
		return list;
	}

	public static void main(String[] args) {
		System.out.println( "Session paths: " + getSessionDirs() );
		System.out.println( "Export path: " + getPath_DevelopmentExport() );
	}
	
}
