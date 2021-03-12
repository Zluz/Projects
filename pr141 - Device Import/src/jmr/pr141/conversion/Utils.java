package jmr.pr141.conversion;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {



//	public static List<Long> getTACsFromLine( final String strLine ) {
	public static List<Long> getNumbersFromLine( final String strLine ) {
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
	

	public static Boolean setBoolean( final String strValue ) {
		if ( null == strValue ) return null;
		final String strTrimmed = strValue.trim();
		if ( "Y" == strTrimmed ) {
			return Boolean.TRUE;
		} else if ( "N" == strTrimmed ) {
			return Boolean.FALSE;
		} else {
			return null;
		}
	}
	
	
	public static Integer parseNumber( final String str ) {
		if ( null == str ) return null;
		final String strTrimmed = str.trim();
		if ( strTrimmed.isEmpty() ) return null;
		if ( 'N' == strTrimmed.charAt( 0 ) ) return null; // "Not Known"
		try {
			final Integer iValue = Integer.parseInt( strTrimmed );
			return iValue;
		} catch ( final NumberFormatException e ) {
			return null;
		}
	}
	
	public static Boolean parseBoolean( final String str ) {
		if ( null == str ) return null;
		final String strTrimmed = str.trim().toUpperCase();
		if ( strTrimmed.isEmpty() ) return null;
		final char c = strTrimmed.charAt( 0 );
		
		if ( "NOT KNOWN".equals( strTrimmed ) ) {
			return null;
		} else if ( "-".equals( strTrimmed ) ) {
			return null;
		} else if ( strTrimmed.contains( "NULL" ) ) {
			return null;
		} else if ( 'N' == c ) {
			return false;
		} else if ( 'Y' == c ) {
			return true;
		} else {
			return true;
		}
//		return null;
	}
	

	public static int getTabbedLength( final String strLine ) {
		if ( null == strLine ) return 0;
		if ( strLine.isEmpty() ) return 0;
		
		if ( -1 == strLine.indexOf( '\t' ) ) return strLine.length();
		
		final StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < strLine.length(); i++ ) {
			final char c = strLine.charAt( i );
			if ( '\t' != c ) {
				sb.append( c );
			} else {
				do {
					sb.append( ' ' );
				} while ( 0 != sb.length() % 8 );
			}
		}
		return sb.length();
	}
	
	
	public static Map<String,String> getMapFrom( final String strLine ) {
		final Map<String,String> map = new HashMap<>();
		if ( null == strLine ) return map;
		if ( strLine.isEmpty() ) return map;
		
		for ( final String strEntry : strLine.split( "\\|" ) ) {
			final String[] strParts = strEntry.split( "=" );
			if ( 2 == strParts.length ) {
				final String strKey = strParts[ 0 ].trim();
				final String strValue = strParts[ 1 ].trim();
				map.put( strKey, strValue );
			}
		}
		return map;
	}
	
	
	public static void main( final String[] args ) {
		final String strTest = "test\ttest\tx1234567\tX";
		final int iTabbedLength = getTabbedLength( strTest );
		System.out.println( strTest + "#" );
		for ( int i = 0; i < iTabbedLength; i++ ) {
			System.out.print( "." );
		}
		System.out.println( "#" );
	}
	
	
}
