package jmr.pr115.schedules;

import java.util.concurrent.TimeUnit;

import jmr.pr115.rules.RulesProcessing;
import jmr.pr115.schedules.run.Heartbeat;
import jmr.pr115.schedules.run.JobWorker;
import jmr.pr119.ScheduleManager.Listener;
import jmr.pr119.TimeEvent;
import jmr.s2db.Client;

public class ScheduleManager {

	
//	final private EnumMap<CronTrigger,JobDetail> 
//					mapCronDetail = new EnumMap<>( CronTrigger.class );
	
	
//	final SchedulerFactory sf = new StdSchedulerFactory();
	
	
	
	public ScheduleManager() {
//		createCronJobs();
		RulesProcessing.get(); // just initialize, have it register..
		createCronJobs_pr119();
	}
	
	public void createCronJobs_pr119() {
		final jmr.pr119.ScheduleManager sm = new jmr.pr119.ScheduleManager();
		sm.addListener( new Listener() {
			@Override
			public void alarm( final TimeEvent event ) {
				
				final JobWorker worker;
				
				switch ( event ) {
					case DAY : worker = new Heartbeat( TimeUnit.DAYS ); break;
					case HOUR : worker = new Heartbeat( TimeUnit.HOURS ); break;
					case MINUTE : worker = new Heartbeat( TimeUnit.MINUTES ); break;
					default : worker = null;
				}
				
				if ( null!=worker ) {
					RulesProcessing.get().process( worker );
				}
			}
		});
	}

	
//	public void createCronJobs() {
//
//		final Scheduler scheduler;
//		try {
//			scheduler = sf.getScheduler();
//		} catch ( final SchedulerException e ) {
//			e.printStackTrace();
//			return;
//		}
//
//		for ( final CronTrigger cron : CronTrigger.values() ) {
//
//			final Runnable runnable = new Runnable() {
//				public void run() {
//					ScheduleManager.this.run( cron );
//				};
//			};
//			
//			final CronJob job = new CronJob( cron, runnable );
//			final JobDetail jobdetail = job.getJobDetail();
//			
//
//			final Trigger trigger = cron.getTrigger();
//
//			try {
//				scheduler.scheduleJob( jobdetail, trigger );
//				scheduler.start();
//				
//				mapCronDetail.put( cron, jobdetail );
//				
//			} catch ( final SchedulerException e ) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	
	
//	public void run( final CronTrigger cron ) {
//		if ( null==cron ) return;
//
//		// step 1 of 2: try to assign worker
//		JobWorker worker = null;
//		
//		worker = cron.getRunInstance();
//		
//		if ( null==worker ) {
//			
//			final Class<? extends JobWorker> classWorker = cron.getRunClass();
//			if ( null!=classWorker ) {
//				
//				try {
//					worker = classWorker.newInstance();
//				} catch ( final Exception e ) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		// step 2 of 2: call run() and submit this item to rules processing
//		if ( null!=worker ) {
//			worker.run();
//			RulesProcessing.get().process( worker );
//		}
//	}
		
	
	
	
	@SuppressWarnings("unused")
//	public static void main( final String[] args ) throws SchedulerException {
	public static void main( final String[] args ) {
		
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
