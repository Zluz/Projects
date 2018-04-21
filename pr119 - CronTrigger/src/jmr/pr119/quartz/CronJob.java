package jmr.pr119.quartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;


public class CronJob {

	

	final public JobDetail jobdetail;

	public interface Listener {
		void execute( final JobExecutionContext context );
	}

	
	public CronJob(	final Listener listener ) {
		
		final JobDataMap map = new JobDataMap();
		map.put( CronJob.class.getName(), this );
		map.put( Listener.class.getName(), listener );

		this.jobdetail = JobBuilder
							.newJob( JobExecutor.class )
							.usingJobData( map )
							.build();
	}	
	

	public JobDetail getJobDetail() {
		return this.jobdetail;
	}

	public static class JobExecutor implements Job {
		@Override
		public void execute( JobExecutionContext context )
				throws JobExecutionException {

			final JobDetail jobdetail = context.getJobDetail();
			final JobDataMap jdm = jobdetail.getJobDataMap();
			
			final Object untyped = jdm.get( Listener.class.getName() );
			if ( untyped instanceof Listener ) {
				final Listener listener = (Listener)untyped;
				listener.execute( context );
			}
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
