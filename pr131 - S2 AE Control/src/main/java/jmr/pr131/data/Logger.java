package jmr.pr131.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Logger {

	
	final static SimpleDateFormat DATE_FORMATTER = 
							new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );


	private static List<String> LINES = new LinkedList<>();
	
	public static void info( final String strMessage ) {
		final String strTime = DATE_FORMATTER.format( new Date() );
		final String strText = strTime + "  " + strMessage;
		System.out.println( strText );
		LINES.add( strText );
	}
	
	public static String getText() {
		final StringBuilder sb = new StringBuilder();
		for ( final String strLine : LINES ) {
			sb.append( strLine );
			sb.append( "\n" );
		}
		return sb.toString();
	}
	
	public static List<String> getLines() {
		return LINES;
	}
	
}
