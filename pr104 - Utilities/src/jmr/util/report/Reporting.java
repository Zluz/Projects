package jmr.util.report;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
		if ( stack.length > 0 ) {
			for ( final StackTraceElement frame : stack ) {
				final int iLine = frame.getLineNumber();
				if ( iLine > 0 ) {
					sb.append( "\t" + frame.getClassName() + "." 
							+ frame.getMethodName() + "(): line " 
							+ iLine + "\n" );
				} else {
					sb.append( "\t" + frame.getClassName() + "." 
							+ frame.getMethodName() + "()\n" );
				}
			}
		} else {
			sb.append( "\t(no frames)\n" );
		}
		return sb.toString();
	}
	
	
	public static String reportAllThreads() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "Active threads:\n" );
		final Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		final List<Thread> list = new LinkedList<>( map.keySet() );
		Collections.sort( list, new Comparator<Thread>() {
			@Override
			public int compare( final Thread lhs, final Thread rhs ) {
				return Long.compare( lhs.getId(), rhs.getId() );
			}
		});
		for ( final Thread thread : list ) {
			final StackTraceElement[] stack = map.get( thread );
			if ( null!=stack ) {
				sb.append( "  \"" + thread.getName() + "\"  ("
						+ "state:" + thread.getState().name() + ", "
						+ "id:" + thread.getId() + ", "
						+ "priority:" + thread.getPriority() + ")\n" );
				sb.append( reportThreadStack( stack ) );
			} else {
				sb.append( "  \"" + thread.getName() + "\"  (null)\n" );
			}
		}
		return sb.toString();
	}
	
}
