package jmr.s2.ingest;

import java.util.Date;
import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class CronIngest {
	
	
	final public static TimeZone tz = TimeZone.getTimeZone( "EST" );
	
	
	// for cron formats, see: 
	// http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06.html
	// http://www.quartz-scheduler.org/api/2.2.1/index.html
	
	@SuppressWarnings("rawtypes")
	enum TeslaCronTrigger {
		
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
		TeslaCronTrigger( final String strCronSchedule ) {
			this.strCronSchedule = strCronSchedule;
			
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
	
	
	enum TeslaRecurringJob {
		
		GENERAL_CHECK,
		LOW_BATT_CHECK,
		TEST_CHECK,
		;
		
		final public JobDetail jobdetail;
		
		private TeslaRecurringJob() {
			final String strName = this.name();
			this.jobdetail = JobBuilder
								.newJob( TestJob.class )
								.withIdentity( new JobKey( strName ) )
								.withIdentity( strName )
								.usingJobData( "test_name", "test_value" )
								.usingJobData( "name", strName )
								.build();
		}
		
		public JobDetail getJobDetail() {
			return this.jobdetail;
		}
		
	}
	
	
	
	
	

	public static class TestJob implements Job {
		@Override
		public void execute( JobExecutionContext context )
				throws JobExecutionException {
			System.out.println( "TestJob.execute() - " + new Date().toString() );
			
			final JobDetail jobdetail = context.getJobDetail();
			final JobDataMap jdm = jobdetail.getJobDataMap();
			
			final String strKeyName = jobdetail.getKey().getName();
			System.out.println( "\tstrKeyName = " + strKeyName );
			final String strValue = jdm.getString( "test_name" );
			System.out.println( "\tstrValue = " + strValue );
			final String strName = jdm.getString( "name" );
			System.out.println( "\tstrName = " + strName );
//			context.getJobInstance().
		}
	}
	
	
	public static void main( final String[] args ) throws SchedulerException {
		
		final SchedulerFactory sf = new StdSchedulerFactory();
		final Scheduler scheduler = sf.getScheduler();

//		final Job job = new Job() {
//			@Override
//			public void execute( final JobExecutionContext context )
//					throws JobExecutionException {
//				System.out.println( "Job.execute()" );
//			}
//		};
		
//		final JobDetail jobdetail = JobBuilder.newJob( job ).build();
//		final JobDetail jobdetail = JobBuilder
//							.newJob( TestJob.class )
//							.withIdentity( "Test_Identity" )
//							.usingJobData( "test_key", "test_value" )
//							.build();
//		
//		final String strCron = "0/10 * * * * ?";
//		final TimeZone tz = TimeZone.getTimeZone( "EST" );
//		
//		final ScheduleBuilder schedule = 
//				CronScheduleBuilder.cronSchedule( strCron ).inTimeZone( tz );
//		
//		final Trigger trigger = TriggerBuilder
//				.newTrigger()
//				.withSchedule( schedule )
//				.build();
		
		
//		scheduler.scheduleJob( job, trigger );
//		scheduler.scheduleJob( jobdetail, trigger );

		scheduler.scheduleJob( 	TeslaRecurringJob.TEST_CHECK.getJobDetail(), 
								TeslaCronTrigger.TEST_SCHEDULE.getTrigger() );

		scheduler.start();
		
		for (;;);
	}
	
}
