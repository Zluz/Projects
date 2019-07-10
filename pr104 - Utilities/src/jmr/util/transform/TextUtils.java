package jmr.util.transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class TextUtils {

	public static Map<String,String> getMapFromIni( final String strContent ) {
		final Map<String,String> map = new HashMap<>();
		if ( StringUtils.isBlank( strContent ) ) return map;
		
		for ( final String strLine : strContent.split( "\\n" ) ) {
			final int iPosEqual = strLine.indexOf( '=' );
			if ( iPosEqual > 0 ) {
				final String strKey = strLine.substring( 0, iPosEqual );
				final String strValue = strLine.substring( iPosEqual + 1 );
				map.put( strKey.trim(), strValue.trim() );
			}
		}
		return map;
	}
	
	public static String convertMapToString( final Map<String,String> map ) {
		final StringBuilder sb = new StringBuilder();
		if ( null!=map && ! map.isEmpty() ) {
			final List<String> list = new LinkedList<>( map.keySet() );
			Collections.sort( list );
			for ( final String strKey : list ) {
				sb.append( strKey );
				sb.append( "=" );
				sb.append( map.get( strKey ) );
				sb.append( "\n" );
			}
		}
		return sb.toString();
	}
	
	public static void main( final String[] args ) {
		final String strTest = "one=first\ntwo=second\nthree=third";
		final Map<String,String> map = getMapFromIni( strTest );
		System.out.println( map );
		System.out.println( JsonUtils.report( map ) );
	}
	
	
}
