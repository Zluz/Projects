package jmr.pr141;

import java.util.LinkedList;
import java.util.List;

public abstract class Util {


	public static List<Long> getTACsFromLine( final String strLine ) {
		final List<Long> list = new LinkedList<>();
		if ( null == strLine ) return list;
		if ( strLine.length() < 8 ) return list;
		
		final String strTACs;
		final int iPosTab = strLine.indexOf( '\t' );
		if ( -1 == iPosTab ) {
			strTACs = strLine;
		} else {
			strTACs = strLine.substring( 0, iPosTab );
		}
		
		for ( final String strRawTAC : strTACs.split( "," ) ) {
			final String strTAC = strRawTAC.trim();
			try {
				final Long lTAC = Long.parseLong( strTAC );
				if ( null != lTAC ) {
					list.add( lTAC );
				} 
			} catch ( final NumberFormatException e ) {
				// just ignore
			}
		}
		return list;
	}
	
	
}
