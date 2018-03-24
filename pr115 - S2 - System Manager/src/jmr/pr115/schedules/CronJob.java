package jmr.pr115.schedules;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;


public class CronJob {

	

	final public JobDetail jobdetail;
	
	public CronJob( final String strName ) {
//		final String strName = this.name();
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
		
		final CronJob job = new CronJob( "CronJob Test" );
		final JobDetail jobdetail = job.getJobDetail();
		final Trigger trigger = CronTrigger.TEST_SCHEDULE.getTrigger();
		
		scheduler.scheduleJob( jobdetail, trigger );
		scheduler.start();

		for (;;);
	}
	
	
}
