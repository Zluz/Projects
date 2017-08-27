package jmr.util.transform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatting {


	final public static String DATETIME = "yyyy-MM-dd HH:mm:ss";
	final public static String TIMESTAMP = "yyyyMMdd-HHmmss-SSS";
	
	final public static SimpleDateFormat FORMAT_DATETIME;
	final public static SimpleDateFormat FORMAT_TIMESTAMP;

	final public static TimeZone 
				TIMEZONE = TimeZone.getTimeZone( "US/Eastern" );

	static {
		FORMAT_DATETIME = new SimpleDateFormat( DATETIME );
		FORMAT_TIMESTAMP = new SimpleDateFormat( TIMESTAMP );
		FORMAT_DATETIME.setTimeZone( TIMEZONE );
		FORMAT_TIMESTAMP.setTimeZone( TIMEZONE );
	}

	public static String getDateTime( final Date date ) {
		return FORMAT_DATETIME.format( date );
	}
	
	public static String getTimestamp( final Date date ) {
		return FORMAT_TIMESTAMP.format( date );
	}
	
}
