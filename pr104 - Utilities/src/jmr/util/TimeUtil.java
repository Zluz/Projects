package jmr.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Utility methods to make some time functions as simple as possible 
 * (so they may be easily implemented in rules).
 * <br><br>
 * NOTE: Methods may assume to calculate the current time.
 */
public abstract class TimeUtil {

	public static boolean isDayOfWeek( final DayOfWeek... days ) {
		if ( null==days ) return false;
		if ( 0==days.length ) return false;
		
		final LocalDate now = LocalDate.now();
		final DayOfWeek today = now.getDayOfWeek();
		
		for ( final DayOfWeek day : days ) {
			if ( today.equals( day ) ) {
				return true;
			}
		}
		
		return false;
	}

	
	public static boolean isWeekday() {
		return isDayOfWeek( 	DayOfWeek.MONDAY,
								DayOfWeek.TUESDAY,
								DayOfWeek.WEDNESDAY,
								DayOfWeek.THURSDAY,
								DayOfWeek.FRIDAY );
	}
	
	public static boolean isWeekend() {
		return isDayOfWeek( 	DayOfWeek.SUNDAY,
								DayOfWeek.SATURDAY );
	}
	

	public static boolean isHourOfDay( final Integer... hours ) {
		if ( null==hours ) return false;
		if ( 0==hours.length ) return false;
		
		final int now = LocalDateTime.now().getHour();
		
		for ( final Integer hour : hours ) {
			if ( hour.equals( now ) ) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static boolean isMinuteInHour( final Integer... minutes ) {
		if ( null==minutes ) return false;
		if ( 0==minutes.length ) return false;
		
		final int now = LocalDateTime.now().getMinute();
		
		for ( final Integer minute : minutes ) {
			if ( minute.equals( now ) ) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static void main( final String[] args ) {
//		final Set<DayOfWeek> set = new HashSet<>() { DayOfWeek.MONDAY };
		System.out.println( "isHourOfDay( 15 ): " + isHourOfDay( 15 ) );
		System.out.println( "isHourOfDay( 16 ): " + isHourOfDay( 16 ) );
		System.out.println( "isHourOfDay( 17 ): " + isHourOfDay( 17 ) );
		System.out.println( "isHourOfDay( 16,17,18,19,20,21 ): " 
							+ isHourOfDay( 16,17,18,19,20,21 ) );
		
		
		System.out.println( "isWeekday(): " + isWeekday() );
		System.out.println( "isDayOfWeek( DayOfWeek.MONDAY ): " 
							+ isDayOfWeek( DayOfWeek.MONDAY ) );
		System.out.println( "isDayOfWeek( DayOfWeek.TUESDAY ): " 
							+ isDayOfWeek( DayOfWeek.TUESDAY ) );
	}
	
}
