package jmr.pr119;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;


public class CronJob {

	

	final public JobDetail jobdetail;
	
	final public Runnable runnable;
	
//	public CronJob( final String strName ) {
	public CronJob(	final CronTrigger trigger,
					final Runnable runnable ) {
		
//		final String strName = trigger.name();
		final JobDataMap map = new JobDataMap();
		map.put( CronTrigger.class.getName(), trigger );
		map.put( CronJob.class.getName(), this );
		
		this.jobdetail = JobBuilder
							.newJob( TestJob.class )
//							.withIdentity( new JobKey( strName ) )
//							.withIdentity( strName )
							.usingJobData( "test_name", "test_value" )
//							.usingJobData( "name", strName )
							.usingJobData( map )
							.build();
		
		this.runnable = runnable;
	}	
		
	

	public JobDetail getJobDetail() {
		return this.jobdetail;
	}
	
	public Runnable getRunnable() {
		return this.runnable;
	}
	

	public static class TestJob implements Job {
		@Override
		public void execute( JobExecutionContext context )
				throws JobExecutionException {
			
//			System.out.println( "TestJob.execute() - " + new Date().toString() );
			
			final JobDetail jobdetail = context.getJobDetail();
			final JobDataMap jdm = jobdetail.getJobDataMap();
			
//			final String strKeyName = jobdetail.getKey().getName();
//			System.out.println( "\tstrKeyName = " + strKeyName );
//			final String strValue = jdm.getString( "test_name" );
//			System.out.println( "\tstrValue = " + strValue );
//			final String strName = jdm.getString( "name" );
//			System.out.println( "\tstrName = " + strName );

//			final CronTrigger trigger = (CronTrigger)jdm.get( CronTrigger.class.getName() );
//			final String strCronTrigger = trigger.name();
//			System.out.println( "\tstrCronTrigger = " + strCronTrigger );

			final CronJob job = (CronJob)jdm.get( CronJob.class.getName() );
//			final String strCronJob = job.toString();
//			System.out.println( "\tstrCronJob = " + strCronJob );
			
			final Runnable runnable = job.getRunnable();
			if ( null!=runnable ) {
				runnable.run();
			}
			
//			context.getJobInstance().
		}
	}
	
	
	
	public static void main( final String[] args ) throws SchedulerException {

//		final SchedulerFactory sf = new StdSchedulerFactory();
//		final Scheduler scheduler = sf.getScheduler();
//		
////		final CronJob job = new CronJob( "CronJob Test" );
//		final CronJob job = new CronJob( CronTrigger.TEST_SCHEDULE, null );
//		final JobDetail jobdetail = job.getJobDetail();
//		final Trigger trigger = CronTrigger.TEST_SCHEDULE.getTrigger();
//		
//		scheduler.scheduleJob( jobdetail, trigger );
//		scheduler.start();
//
//		for (;;);
	}
	
	
}
