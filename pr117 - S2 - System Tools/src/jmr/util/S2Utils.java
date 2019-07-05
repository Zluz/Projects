package jmr.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class S2Utils {

	public static String strUnixPathOf( final File file ) {
		if ( null==file ) return "";
		String strPath = file.getAbsolutePath().toString();
		return strUnixPathOf( strPath );
	}

	public static String strUnixPathOf( String strFilename ) {
		strFilename = StringUtils.replace( strFilename, "\\\\", "/" );
		strFilename = StringUtils.replace( strFilename, "\\", "/" );
		strFilename = StringUtils.replaceIgnoreCase( 
								strFilename, "S:/Sessions", "/Share/Sessions" );
		return strFilename;
	}
	
	public static void main( final String[] args ) {
		final String strTest01 = "S:\\Sessions\\94-C6-91-19-C5-CC\\capture_vid0-mask.jpg";
		System.out.println( strUnixPathOf( new File( strTest01 ) ) );
	}
	
}
