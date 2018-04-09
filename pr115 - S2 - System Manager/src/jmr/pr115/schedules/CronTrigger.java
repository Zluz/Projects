package jmr.pr115.schedules;

import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import jmr.pr115.schedules.run.*;

public enum CronTrigger {

	// see:  http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html#format
	//						  v-----------1:seconds
	//							 v--------2:minutes
	//								  v---3:hours
	//											   v---------4:day-of-month
	//												  v------5:month
	//													 v---6:day-of-week
//	GENERAL_HOURLY_CHECK( 	" 0  0    *            *  *  ?        ", RunTest.class ),
//	LOW_BATT_CHECK_WEEKDAY( " 0  0/30 19,20,21,22  ?  *  MON-FRI  ", new TeslaJob() ),
//	LOW_BATT_CHECK_WEEKEND( " 0  0    10/1          ?  *  SUN,SAT  ", new TeslaJob() ),
//	ONE_TIME_TESLA_QUERY_0(	" 0  59   16           *  *  ?        ", new TeslaJob() ),
//	ONE_TIME_TESLA_QUERY_1(	" 0 35,40 *            *  *  ?        ", new TeslaJob() ),
//	LOW_BATT_CHECK_TEST( 	"0/30 *   *            *  *  ?        ", new TeslaJob() ),
//	LOW_HUMID_CHECK_TEST( 	"0/30 *   *            *  *  ?        ", new NestJob( true ) ),
	HUMID_AND_TEMP_CHECK( 	" 0  0    *            *  *  ?        ", new NestJob( true ) ),
//	TEST_SCHEDULE( 			"0/10 *   *            *  *  ?        ", RunTest.class ),
	//								  ^--? not local time? 
	;

	
	
	public final String strCronSchedule;
	public final ScheduleBuilder<?> schedule;
	public final Trigger trigger;
	public final JobWorker runInstance;
	public final Class<? extends JobWorker> runClass;
	
	
//	@SuppressWarnings("unchecked")
	CronTrigger(	final String strCronSchedule,
					final Class<? extends JobWorker> classRun,
					final JobWorker runInstance ) {
		this.strCronSchedule = strCronSchedule;
		this.runClass = classRun;
		this.runInstance = runInstance;

		final TimeZone tz = TimeZone.getTimeZone( "EST" );
		
		this.schedule = CronScheduleBuilder
								.cronSchedule( this.strCronSchedule )
								.inTimeZone( tz );
		
		this.trigger = TriggerBuilder
				.newTrigger()
				.withSchedule( this.schedule )
				.build();
	}

	
	CronTrigger(	final String strCronSchedule,
					final Class<? extends JobWorker> classRun ) {
		this( strCronSchedule, classRun, null );
	}

	CronTrigger(	final String strCronSchedule,
					final JobWorker runInstance ) {
		this( strCronSchedule, null, runInstance );
	}

	public ScheduleBuilder<?> getSchedule() {
		return this.schedule;
	}
	
	public Trigger getTrigger() {
		return this.trigger;
	}
	
	public Class<? extends JobWorker> getRunClass() {
		return this.runClass;
	}
	
	public JobWorker getRunInstance() {
		return this.runInstance;
	}
	
}
