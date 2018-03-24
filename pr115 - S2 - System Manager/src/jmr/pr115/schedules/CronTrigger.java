package jmr.pr115.schedules;

import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public enum CronTrigger {

	
	//						  v-----------1:seconds
	//							 v--------2:minutes
	//								  v---3:hours
	//											   v---------4:day-of-month
	//												  v------5:month
	//													 v---6:day-of-week
	GENERAL_HOURLY_CHECK( 	" 0  0    *            *  *  ?        " ),
	LOW_BATT_CHECK_WEEKDAY( " 0  0/10 19,20,21,22  ?  *  MON-FRI  " ),
	TEST_SCHEDULE( 			"0/10 *   *            *  *  ?        " ),
	;

	
	
	public final String strCronSchedule;
	public final ScheduleBuilder schedule;
	public final Trigger trigger;
	
	@SuppressWarnings("unchecked")
	CronTrigger( final String strCronSchedule ) {
		this.strCronSchedule = strCronSchedule;

		final TimeZone tz = TimeZone.getTimeZone( "EST" );
		
		this.schedule = CronScheduleBuilder
								.cronSchedule( this.strCronSchedule )
								.inTimeZone( tz );
		this.trigger = TriggerBuilder
								.newTrigger()
								.withSchedule( this.schedule )
								.build();
	}
	
	public ScheduleBuilder getSchedule() {
		return this.schedule;
	}
	
	public Trigger getTrigger() {
		return this.trigger;
	}
	
}
