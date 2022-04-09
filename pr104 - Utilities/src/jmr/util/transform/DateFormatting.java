package jmr.util.transform;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
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
	
	/**
	 * convert the given date string to an epoch millisecond 
	 * @param strDate
	 * @return
	 */
	public static Long getDateTime( final String strDate ) {
		if ( null == strDate ) return null;
		
		if ( strDate.contains( "T" ) && strDate.contains( "+" ) ) {
			// looks like an ISO-8601 time
			try {
				final ZonedDateTime zdt = ZonedDateTime.parse( strDate );
				if ( null != zdt ) {
					return zdt.toEpochSecond() * 1000;
				}
			} catch ( final Exception e ) {
				// just move along
			}
		}
		
		try {
			String strNorm = strDate.trim();
			strNorm = strNorm.substring( 0, 20 );
			strNorm = strNorm.replace( 'T', ' ' );
			final Date date = FORMAT_DATETIME.parse( strNorm );
			return date.getTime();
		} catch ( final ParseException e ) {
			return null;
		}
	}

	public static String getSmallTime(	final long ms,
										final boolean bMSAccuracy ) {
		final long lMinutes = TimeUnit.MILLISECONDS.toMinutes( ms );
		if ( lMinutes < 2 ) {
			final long lSeconds = TimeUnit.MILLISECONDS.toSeconds( ms );
			if ( lSeconds > 3 || ! bMSAccuracy ) {
				return "" + lSeconds + " s";
			} else {
				return String.format( "%1.1f s", ( (float)ms / 1000 ) );
			}
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

	public static String getSmallTime( final long ms ) {
		return getSmallTime( ms, false );
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
	
	
	public static String getShortDayOfWeek( final String strLongName ) {
		if ( null == strLongName ) return "";
		
		final String strFormatted = strLongName.trim().toUpperCase();
		
		if ( strFormatted.startsWith( "SUN" ) ) {
			return "Sun";
		} else if ( strFormatted.startsWith( "MON" ) ) {
			return "Mon";
		} else if ( strFormatted.startsWith( "TUE" ) ) {
			return "Tue";
		} else if ( strFormatted.startsWith( "WED" ) ) {
			return "Wed";
		} else if ( strFormatted.startsWith( "THU" ) ) {
			return "Thu";
		} else if ( strFormatted.startsWith( "FRI" ) ) {
			return "Fri";
		} else if ( strFormatted.startsWith( "SAT" ) ) {
			return "Sat";

		} else if ( strFormatted.startsWith( "TODAY" ) ) {
			return "Today";
		} else if ( strFormatted.startsWith( "THIS AFTERNOON" ) ) {
			return "Afternoon";
			
		} else {
			return "";
		}
	}
	
}
