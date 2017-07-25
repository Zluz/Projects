package jmr.util;

import java.io.File;

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
	
}
