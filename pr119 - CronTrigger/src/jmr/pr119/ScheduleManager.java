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


public class ScheduleManager {

	
	final SchedulerFactory sf = new StdSchedulerFactory();
	
	private final List<Listener> listeners = new LinkedList<>();
	
	
	public static interface Listener {
		public void alarm( final CronTrigger trigger );
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
		
		for ( final Listener listener : listeners ) {
			if ( null!=listener ) {
				listener.alarm( cron );
			}
		}
	}
		
	
	
	
	public static void main( final String[] args ) throws Exception {
		
		final ScheduleManager sm = new ScheduleManager();
		final Listener listener = new Listener() {
			@Override
			public void alarm( final CronTrigger trigger ) {
				System.out.println( "[" + LocalDateTime.now().toString() + "] "
								+ "Trigger: " + trigger.name() );
			}
		};
		sm.addListener( listener );
		
		for (;;) {
			Thread.sleep( 100 );
		}
	}
	
}
