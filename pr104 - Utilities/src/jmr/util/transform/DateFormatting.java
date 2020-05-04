package jmr.util.transform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
	
	public static String getDateTime( final long lTime ) {
		final Date date = new Date( lTime );
		return getDateTime( date );
	}
	
	public static String getTimestamp( final Date date ) {
		return FORMAT_TIMESTAMP.format( date );
	}
	
	public static String getTimestamp() {
		return DateFormatting.getTimestamp( new Date() );
	}
	
	public static String getSmallTime( final long ms ) {
		final long lMinutes = TimeUnit.MILLISECONDS.toMinutes( ms );
		if ( lMinutes < 2 ) {
			final long lSeconds = TimeUnit.MILLISECONDS.toSeconds( ms );
			return "" + lSeconds + " s";
		} else if ( lMinutes < 120 ) {
			return "" + lMinutes + " m";
		}
		final long lHours = TimeUnit.MILLISECONDS.toHours( ms );
		if ( lHours < 48 ) {
			return "" + lHours + " h";
		} else {
			final long lDays = TimeUnit.MILLISECONDS.toDays( ms );
			return "" + lDays + " d";
		}
	}
	
	public static String getSmallTime( final String strLastTime ) {
		if ( null==strLastTime ) return "null";
		if ( strLastTime.isEmpty() ) return "empty";
		
		try {
			final long lLastTime = Long.parseLong( strLastTime );
			final long lNow = new Date().getTime();
			final long lElapsed = lNow - lLastTime;
			final String strElapsed = getSmallTime( lElapsed );
			return strElapsed;
		} catch ( final NumberFormatException e ) {
			return "ex:fmt";
		}
	}
	
}
