package jmr;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import jmr.util.OSUtil;

public enum S2Path {
	
	H01_SHARE( 		"\\\\192.168.6.220\\Share", null, "/Share" ),
	H04_SHARE( 		"\\\\H04\\Share", 			null, "/Share" ),
	H04_HISTORICAL( "\\\\H04\\Share\\Historical", null, "/Share/Historical" ),
	;

	private String strWindowsUNC;
	private String strWindowsDrive;
	private String strUnixPath;
//	private boolean bValid;
	private static boolean bInitialized = false;

	
	S2Path( 	final String strWindowsUNC,
				final String strWindowsDrive,
				final String strUnixPath ) {
		this.strWindowsUNC = strWindowsUNC;
		this.strWindowsDrive = strWindowsDrive;
		this.strUnixPath = strUnixPath;
	}
	
	private static void initialize() {
		if ( bInitialized ) return;
		
		for ( final S2Path path : S2Path.values() ) {

			if ( OSUtil.isWin() ) {
				if ( null != path.strWindowsUNC 
						&& ! new File( path.strWindowsUNC ).canRead() ) {
					path.strWindowsUNC = null;
				}
	
				if ( null != path.strWindowsDrive 
						&& ! new File( path.strWindowsDrive ).canRead() ) {
					path.strWindowsDrive = null;
				}
			} else {
				if ( null != path.strUnixPath 
						&& ! new File( path.strUnixPath ).canRead() ) {
					path.strUnixPath = null;
				}
			}
		}
		bInitialized = true;
	}
	
	public static Set<String> getLocalAlts( final String strInput ) {
		final Set<String> set = new HashSet<>();
		if ( StringUtils.isBlank( strInput ) ) return set;
		
		initialize();
		
		final boolean bWin = OSUtil.isWin();
		for ( final S2Path path : S2Path.values() ) {
			if ( bWin ) {
				if ( StringUtils.startsWith( strInput, path.strUnixPath ) ) {
					if ( null != path.strWindowsDrive ) {
						String strAlt = StringUtils.replaceOnce( 
								strInput, path.strUnixPath, path.strWindowsDrive );
						strAlt = StringUtils.replaceChars( strAlt, '/', '\\' );
						set.add( strAlt );
					}
					if ( null != path.strWindowsUNC ) {
						String strAlt = StringUtils.replaceOnce( 
								strInput, path.strUnixPath, path.strWindowsUNC );
						strAlt = StringUtils.replaceChars( strAlt, '/', '\\' );
						set.add( strAlt );
					}
				}
			} else {
				if ( StringUtils.startsWithIgnoreCase( strInput, path.strWindowsUNC ) ) {
					if ( null != path.strUnixPath ) {
						String strAlt = StringUtils.replaceOnce( 
								strInput, path.strWindowsUNC, path.strUnixPath );
						strAlt = StringUtils.replaceChars( strAlt, '\\', '/' );
						set.add( strAlt );
					}
				}
			}
		}
		
		return set;
	}
	
	

}
