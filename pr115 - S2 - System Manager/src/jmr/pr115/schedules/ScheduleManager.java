package jmr.pr115.schedules;

import java.util.concurrent.TimeUnit;

import jmr.pr115.rules.RulesProcessing;
import jmr.pr115.rules.ingest.SubmitJobs;
import jmr.pr115.schedules.run.Heartbeat;
import jmr.pr115.schedules.run.JobWorker;
import jmr.pr119.ScheduleManager.Listener;
import jmr.pr119.TimeEvent;
import jmr.s2db.Client;

public class ScheduleManager {

	
	
	
	public ScheduleManager() {
		RulesProcessing.get(); // just initialize, have it register..
		createCronJobs();
		new SubmitJobs();
	}
	
	public void createCronJobs() {
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

	
	@SuppressWarnings("unused")
	public static void main( final String[] args ) {
		
		final Client client = Client.get();
		client.register( "test", ScheduleManager.class.getName() );
		
		final ScheduleManager sm = new ScheduleManager();
		for (;;);
	}
	
}
