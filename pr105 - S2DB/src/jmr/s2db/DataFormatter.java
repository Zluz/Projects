package jmr.s2db;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DataFormatter {

	
	final static SimpleDateFormat DATE_FORMATTER = 
							new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );



	public static String format( final Object value ) {
//		if ( null==value ) return "''";
		if ( null==value ) return "null";
		if ( value instanceof String ) {
			String str = value.toString();;
			if ( str.length()<1 ) {
				return "''";
			} else {
				if ( ( '\'' == str.charAt( 0 ) ) 
						&& ( '\'' == str.charAt( str.length() - 1 ) ) ) {
					str = str.substring( 1, str.length() - 2 );
				}
				str = str.replace( "\\", "\\\\" );
				str = str.replace( "'", "\\'" );
				str = str.replace( "\"", "\\\"" );
				return "'" + str + "'";
			}
		} else if ( value instanceof Number ) {
			return "" + value.toString();
		} else if ( value instanceof Date ) {
			return "'" + DATE_FORMATTER.format( ((Date) value) ) + "'";
		} else {
			return "'" + value.toString() + "'";
		}
//		return "";
	}

}
