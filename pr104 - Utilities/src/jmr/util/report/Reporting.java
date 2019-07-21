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
	

	public static String reportThreadStack( final StackTraceElement[] stack ) {
		final StringBuilder sb = new StringBuilder();
		for ( final StackTraceElement frame : stack ) {
			sb.append( "\t" + frame.getClassName() + "." 
						+ frame.getMethodName() + "(): line " 
						+ frame.getLineNumber() + "\n" );
		}
		return sb.toString();
	}
	
	
	public static String reportAllThreads() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "Active threads:\n" );
		for ( final Entry<Thread, StackTraceElement[]> 
						entry : Thread.getAllStackTraces().entrySet() ) {
			final Thread thread = entry.getKey();
			final StackTraceElement[] stack = entry.getValue();
			sb.append( "  \"" + thread.getName() + "\"\n" );
			sb.append( reportThreadStack( stack ) );
		}
		return sb.toString();
	}
	
}
