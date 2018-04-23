package jmr.pr119;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import jmr.pr119.quartz.CronJob;
import jmr.pr119.quartz.CronTrigger;


public class ScheduleManager {

	
	final SchedulerFactory sf = new StdSchedulerFactory();
	
	private final List<Listener> listeners = new LinkedList<>();
	
	//TODO consider possibly notifying of a JobWorker impl instead of
	//     of CronTrigger, if CronTrigger has implications/dependencies.
	//     JobWorker could also change to better represent the event.
	public static interface Listener {
		public void alarm( final TimeEvent event );
	}
	
	
	public ScheduleManager() {
		createCronJobs();
	}
	
	
	public void addListener( final Listener listener ) {
		listeners.add( listener );
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

			final CronJob.Listener listener = new CronJob.Listener() {
				@Override
				public void execute( final JobExecutionContext context ) {
					run( cron );
				}
			};
			
			final CronJob job = new CronJob( listener );
			final JobDetail jobdetail = job.getJobDetail();
			

			final Trigger trigger = cron.getTrigger();

			try {
				scheduler.scheduleJob( jobdetail, trigger );
				scheduler.start();
				
			} catch ( final SchedulerException e ) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void run( final CronTrigger cron ) {
		if ( null==cron ) return;
		
		final TimeEvent event = cron.getEvent();
		
		for ( final Listener listener : listeners ) {
			if ( null!=listener ) {
				listener.alarm( event );
			}
		}
	}
		
	
	
	
	public static void main( final String[] args ) throws Exception {
		
		final ScheduleManager sm = new ScheduleManager();
		final Listener listener = new Listener() {
			@Override
			public void alarm( final TimeEvent event ) {
				System.out.println( "[" + LocalDateTime.now().toString() + "] "
								+ "TimeEvent: " + event.name() );
			}
		};
		sm.addListener( listener );
		
		for (;;) {
			Thread.sleep( 100 );
		}
	}
	
}
