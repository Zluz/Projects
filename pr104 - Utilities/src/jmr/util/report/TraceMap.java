package jmr.util.report;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class TraceMap extends HashMap<String,Object> {

	/* for serialization */
	private static final long serialVersionUID = -6298064005164866264L;

	public TraceMap() {
		addFrame( this, null );
	}
	
	public TraceMap( final boolean bAddFrame ) {
		if ( bAddFrame ) {
			addFrame( this, null );
		}
	}

	public TraceMap( final Map<String,Object> map ) {
		addFrame( this, null );
		this.putAll( map );
	}

	
//	public TraceMap( final Map<String,String> map ) {
//		addFrame( this, null );
//		for ( final Entry<String, String> entry : map.entrySet() ) {
//			this.put( entry.getKey(), entry.getValue() );
//		}
//	}
	
	public void addStringMap( final Map<String,String> map ) {
		for ( final Entry<String, String> entry : map.entrySet() ) {
			this.put( entry.getKey(), entry.getValue() );
		}
	}
	
	
	public void addFrame( final String strComment ) {
		TraceMap.addFrame( this, strComment );
	}

	public void addFrame() {
		TraceMap.addFrame( this, null );
	}

	public static String getPrefixFor( final int i ) {
		return StringUtils.leftPad( ""+i, 2, "0" );
	}
	
	public static String getNextFramePrefix( final TraceMap map ) {
		if ( null==map ) return getPrefixFor( 1 );
		int i = 0;
		String strPrefix;
		do {
			i = i + 1;
			strPrefix = getPrefixFor( i );
		} while ( map.containsKey( strPrefix + "-time" ) );
		return strPrefix;
	}
	
	
	public static String getInitials( final String strInput ) {
		if ( StringUtils.isBlank( strInput ) ) return "";
		
		final String strOutput = strInput
		        .chars()
		        .filter( (c)-> false // ( '.' == c ) 
		        				|| Character.isUpperCase(c) 
		        				|| Character.isDigit(c) )
		        .collect( StringBuilder::new, // supplier
		                StringBuilder::appendCodePoint, // accumulator
		                StringBuilder::append ) // combiner
		        .toString();	
		return strOutput;
	}
	
	public static StackTraceElement getFrame() {
		final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for ( int i=1; i<stack.length; i++ ) {
//		for ( final StackTraceElement frame : stack ) {
			final StackTraceElement frame = stack[ i ];
			if ( ! frame.getClassName().equals( TraceMap.class.getName() ) ) {
				return frame;
			}
		}
		return stack[0];
	}
	
	public static TraceMap addFrame( final TraceMap map,
						    		 final String strComment ) {
		final long lNow = System.currentTimeMillis();
		final TraceMap tm;
		if ( null!=map ) {
			tm = map;
		} else {
			tm = new TraceMap( false );
		}
		
		final String strPrefix = getNextFramePrefix( tm );
		
		final StackTraceElement frame = getFrame();
		
		final String strClass = getInitials( frame.getClassName() );
		final String strMethod = frame.getMethodName().trim();
		final String strSource = 
				strClass + "." + strMethod + "():" + frame.getLineNumber();
		
		tm.put( strPrefix + "-time", lNow );
		tm.put( strPrefix + "-source", strSource );
		if ( null!=strComment ) {
			tm.put( strPrefix + "-comment", strComment );
		}
		
		return tm;
	}


	public static TraceMap addFrame( final TraceMap map ) {
		return addFrame( map, null );
	}
	
	
	public Object putIfAbsent( final String strKey, 
							   final Object objValue ) {
		if ( ! this.containsKey( strKey ) ) {
			return this.put( strKey, objValue );
		} else {
			return this.get( strKey );
		}
	}

	
}
