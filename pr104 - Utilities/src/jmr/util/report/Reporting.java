package jmr.util.report;

import java.util.Map;
import java.util.Map.Entry;

public abstract class Reporting {

	public static String print( final Map<String,String> map ) {
		if ( null==map ) return "Map:(null)";
		
		final StringBuilder sb = new StringBuilder();
		sb.append( "Map (" + map.size() + " entries)\n" );
		for ( final Entry<String, String> entry : map.entrySet() ) {
			sb.append( "\t\"" + entry.getKey() + "\""
					+ " = \"" + entry.getValue() + "\"\n" );
		}
		return sb.toString();
	}
	
}
