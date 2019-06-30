package jmr.pr115.schedules;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import jmr.pr115.rules.RulesProcessing;
import jmr.pr115.rules.ingest.SubmitJobs;
import jmr.pr115.schedules.run.Heartbeat;
import jmr.pr115.schedules.run.JobWorker;
import jmr.pr119.ScheduleManager.Listener;
import jmr.pr119.TimeEvent;
import jmr.pr120.EmailControl;
import jmr.pr120.EmailEvent;
import jmr.pr120.EmailEventListener;
import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class ScheduleManager {

	
	
	
	public ScheduleManager() {
		RulesProcessing.get(); // just initialize, have it register..
		createCronJobs();
		new SubmitJobs();
		
		registerEmailListener();
		
		try {
			final String strLoadClass = IOUtils.resourceToString(
					"/jmr/pr115/schedules/ScheduleManager.java", 
					Charset.defaultCharset() );
		} catch ( final IOException e ) {
			System.err.println( "Problem loading resources." );
		}
	}
	
	
	private void registerEmailListener() {
		
		final EmailEventListener listener = new EmailEventListener() {
			@Override
			public void incoming( final EmailEvent event ) {
				System.out.println( "Calling: RulesProcessing.get().process( EmailEvent );");
				RulesProcessing.get().process( event );
			}
		};
		
		final char[] cUsername = SystemUtil.getProperty( 
						SUProperty.CONTROL_EMAIL_USERNAME ).toCharArray(); 
		final char[] cPassword = SystemUtil.getProperty( 
						SUProperty.CONTROL_EMAIL_PASSWORD ).toCharArray(); 

		final EmailControl 
				control = new EmailControl( cUsername, cPassword, listener );
		control.start();
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
		client.register( ClientType.TILE_GUI, "test", ScheduleManager.class.getName() );
		
		final ScheduleManager sm = new ScheduleManager();
		
		try {
			for (;;) {
				Thread.sleep( 100 );
			}
		} catch ( final InterruptedException e ) {
			System.out.println( "Main thread interrupted." );
		}
	}
	
}
