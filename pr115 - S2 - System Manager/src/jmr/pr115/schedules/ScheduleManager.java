package jmr.pr115.schedules;

import java.lang.reflect.Method;
import java.util.EnumMap;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import jmr.pr115.rules.RulesProcessing;
import jmr.pr115.schedules.run.JobWorker;
import jmr.s2db.Client;

public class ScheduleManager {

	
	final private EnumMap<CronTrigger,JobDetail> 
					mapCronDetail = new EnumMap<>( CronTrigger.class );
	
	
	final SchedulerFactory sf = new StdSchedulerFactory();
	
	
	
	public ScheduleManager() {
		createCronJobs();
		RulesProcessing.get(); // just initialize, have it register..
	}
	
	
	
	public void createCronJobs() {

		final Scheduler scheduler;
		try {
			scheduler = sf.getScheduler();
		} catch ( final SchedulerException e ) {
			e.printStackTrace();
			return;
		}

		for ( final CronTrigger cron : CronTrigger.values() ) {

			final Runnable runnable = new Runnable() {
				public void run() {
					ScheduleManager.this.run( cron );
				};
			};
			
			final CronJob job = new CronJob( cron, runnable );
			final JobDetail jobdetail = job.getJobDetail();
			

			final Trigger trigger = cron.getTrigger();

			try {
				scheduler.scheduleJob( jobdetail, trigger );
				scheduler.start();
				
				mapCronDetail.put( cron, jobdetail );
				
			} catch ( final SchedulerException e ) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void run( final CronTrigger cron ) {
		if ( null==cron ) return;
		
		final JobWorker run = cron.getRunInstance();
		
		if ( null!=run ) {
			
			run.run();
			
		} else {
			
			final Class<? extends JobWorker> classRun = cron.getRunClass();
			if ( null==classRun ) return;
			
			try {

				final JobWorker instance = classRun.newInstance();
				
				RulesProcessing.get().process( instance );

				final Method method = classRun.getMethod( "run", new Class<?>[]{} );
				if ( null==method ) return;
				method.invoke( instance );
				
			} catch ( final Exception e ) {
				e.printStackTrace();
			}
		}
	}
		
	
	
	
	
	
	@SuppressWarnings("unused")
	public static void main( final String[] args ) throws SchedulerException {
		
		final Client client = Client.get();
		client.register( "test", ScheduleManager.class.getName() );
		
//		final SchedulerFactory sf = new StdSchedulerFactory();
//		final Scheduler scheduler = sf.getScheduler();
//		
//		for ( final CronTrigger cron : CronTrigger.values() ) {
//
//			final CronJob job = new CronJob( cron );
//			final JobDetail jobdetail = job.getJobDetail();
//
//			final Trigger trigger = cron.getTrigger();
//
//			scheduler.scheduleJob( jobdetail, trigger );
//			scheduler.start();
//		}
//		
//		for (;;);
		
		final ScheduleManager sm = new ScheduleManager();
		for (;;);
	}
	
}
