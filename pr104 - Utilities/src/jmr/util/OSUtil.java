package jmr.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class OSUtil {

	/**
	 * Very simple quick test of OS.
	 * This only indicates Windows vs Non-Windows (Linux).
	 * @return
	 */
	public static boolean isWin() {
		return ( '\\'==File.separatorChar );
	}
	
	private static Integer iArch = null;
	
	public static int getArch() {
		if ( null==iArch ) {

//			final String strOSName = System.getProperty( "os.name" ).toUpperCase();
//			System.out.println( "\tOS Name: " + strOSName );
			final String strOSArch = System.getProperty( "sun.arch.data.model" );
//			System.out.println( "\tOS Arch: " + strOSArch );
			
			try {
				iArch = Integer.parseInt( strOSArch );
			} catch ( final NumberFormatException e ) {
				// ignore, but should not happen
			}
		}
		return iArch;
	}
	
	public static String _getProgramName() {
//		final String strPath = OSUtil.class.getProtectionDomain()
//				  .getCodeSource().getLocation().getPath();
//		final String strFile = new java.io.File( strPath ).getName();
		try {
			final URI uri = OSUtil.class.getProtectionDomain()
					  .getCodeSource().getLocation().toURI();
			final String strFile = new java.io.File( uri ).getName();
			return strFile;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "(unknown)";
	}
	
	

	public static String getProgramName() {
	    String path = OSUtil.class.getResource(
	    		OSUtil.class.getSimpleName() + ".class" ).getFile();
	    if(path.startsWith("/")) {
	    	return "(not running from a jar)";
	    }
	    path = ClassLoader.getSystemClassLoader().getResource(path).getFile();

	    final File file = new File(path.substring(0, path.lastIndexOf('!')));
	    return file.getName();
	}

	
	
}
