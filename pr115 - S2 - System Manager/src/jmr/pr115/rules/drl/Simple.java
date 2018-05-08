package jmr.pr115.rules.drl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import jmr.pr102.DataRequest;
import jmr.pr115.actions.SendMessage;
import jmr.pr115.actions.SendMessage.MessageType;
import jmr.pr115.schedules.run.NestJob;
import jmr.pr115.schedules.run.TeslaJob;
import jmr.s2.ingest.Import;
import jmr.s2db.imprt.WebImport;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.util.TimeUtil;
import jmr.util.transform.JsonUtils;
import jmr.pr120.EmailEvent;
import jmr.pr120.Command;

public class Simple {
	
	static {
		TimeUtil.isHourOfDay(); // just to get the import
	}
	
	static NestJob nest = null;

	
	

	public static void doRefreshNest() {
		System.out.println( "--- doRefreshNest(), "
					+ "time is " + LocalDateTime.now().toString() );
		try {
			
			// just to separate the jobs.
			Thread.sleep( TimeUnit.MINUTES.toMillis( 1 ) );
			
			if ( null==nest ) {
				nest = new NestJob( true );
			}
			
			nest.run();
			
//			final long lNow = System.currentTimeMillis();
//			final FullStatus status = nest.callNest();
//			final boolean bResult = nest.process( lNow, status );
			
			
		} catch ( final Throwable t ) {
			System.err.println( 
						"Error during doRefreshNest(): " + t.toString() );
			t.printStackTrace();
		}
	}

	public static void doRefreshWeather() {
		System.out.println( "--- doRefreshWeather(), "
					+ "time is " + LocalDateTime.now().toString() );
		try {
			final Import source = Import.WEATHER_FORECAST__YAHOO;
			
			final String strURL = source.getURL();
			final String strTitle = source.getTitle();
	
			final WebImport wi = new WebImport( strTitle, strURL );
			final Long seq = wi.save();
			
			System.out.println( "Weather refreshed. Result: seq = " + seq );
			
		} catch ( final Throwable t ) {
			System.err.println( 
						"Error during doRefreshWeather(): " + t.toString() );
			t.printStackTrace();
		}
	}
	
	
	public final static List<Job> JOBS = new LinkedList<>();
	
	
	public static synchronized void queueJob( final Job job ) {
		if ( null==job ) return;
		
		if ( null==job.getPartCount() || job.getPartCount()<2 ) {
			workJobs( Collections.singletonList( job ) );
		} else {
			JOBS.add( job );
			
			final long lPartSeq = job.getPartSeq();
			final int iPartCount = job.getPartCount();
			final List<Job> list = new LinkedList<>();
			for ( final Job element : JOBS ) {
				if ( lPartSeq == element.getPartSeq().longValue() ) {
					list.add( element );
				}
			}
			if ( iPartCount == list.size() ) {
				
				for ( final Job element : list ) {
					JOBS.remove( element );
				}
				
				Collections.sort( list, new Comparator<Job>() {
					@Override
					public int compare( final Job lhs, final Job rhs ) {
						return Long.compare( lhs.getJobSeq(), rhs.getJobSeq() );
					}
				} );
				
				workJobs( list );
			}
		}
	}

	
	public static void workJobs( final List<Job> jobs ) {
		if ( null==jobs ) return;
		if ( jobs.isEmpty() ) return;
		
		final LocalDateTime now = LocalDateTime.now();
		final JobType type = jobs.get( 0 ).getJobType();
		
		if ( ( JobType.TESLA_READ == type ) 
				|| ( JobType.TESLA_WRITE == type ) ) {
			
			final TeslaJob tj = new TeslaJob( false );
			for ( final Job job : jobs ) {
				job.setState( JobState.WORKING );
				tj.addJob( job );
			}
			
			final JsonObject jo = tj.request();

			if ( null!=jo ) {
				System.out.println( "Combined JsonObject "
									+ "from Tesla (size): " + jo.size() );
			} else {
				System.err.println( "Combined JsonObject from Tesla is null" );
			}
			
			if ( jobs.size() >= 3 ) {
				final StringBuilder strbuf = new StringBuilder();
				strbuf.append( "Tesla Combined JSON\n"
						+ now.toString() + "\n\n"
						+ "Jobs:\n" );
				for ( final Job job : jobs ) {
					strbuf.append( "\t" + job.getJobSeq() );
					strbuf.append( "\t" + job.getRequest() + "\n" );
				}
				strbuf.append( "\n\nCombined JSON:\n" );
				strbuf.append( JsonUtils.getPretty( jo ) );
				
				SendMessage.send( MessageType.EMAIL, 
						"Tesla Combined JSON", strbuf.toString() );
			}
			
			
			for ( final Job job : jobs ) {
				job.setState( JobState.COMPLETE );
			}

//			return jo;
		}
	}
	
	
	

	public static JsonObject doCheckTeslaState( final Object obj ) {
		System.out.println( "--- doCheckTeslaState(), "
				+ "time is " + LocalDateTime.now().toString() );
		
//		if ( 1==1 ) return null;
		
		if ( null==obj ) {

			
			try {
			
				final TeslaJob job = new TeslaJob();
				final JsonObject jo = job.request();
				
				if ( null!=jo ) {
					System.out.println( "Combined JsonObject "
										+ "from Tesla (size): " + jo.size() );
				} else {
					System.err.println( "Combined JsonObject from Tesla is null" );
				}
				return jo;
	
			} catch ( final Throwable t ) {
				System.err.println( 
							"Error during doCheckTeslaState(): " + t.toString() );
				t.printStackTrace();
				return null;
			}

			
			
		} else if ( obj instanceof Job ) {
			final Job job = (Job)obj;
			if ( ( JobType.TESLA_READ == job.getJobType() ) 
					|| ( JobType.TESLA_WRITE == job.getJobType() ) ) {
//				job.setState( JobState.WORKING );
				queueJob( job );
			}
		}
		return null;
		
	}
	
	
	
	public static String getEmailCommandHelp() {
		final StringBuilder strbuf = new StringBuilder();
		
		strbuf.append( "Available email commands:\n" );
		for ( final Command command : Command.values() ) {
			strbuf.append( "\t" + command.name() + "\n" );
		}
		
		return strbuf.toString();
	}
	
	
	public static void doHandleEmailEvent( final EmailEvent event ) {
		if ( null==event ) return;
		
		final Command command = event.getCommand();
		if ( null==command ) return;
		
		System.out.println( "--- Simple.doHandleEmailEvent() - "
							+ "Command: " + command.name() );
		
		switch ( command ) {
			case HELP: {
				SendMessage.send( MessageType.EMAIL, 
						"Email Command Help", getEmailCommandHelp() );
				break;
			}
			case TESLA_REFRESH: {
				final Job.JobSet set = new Job.JobSet( 3 );
				Job.add( JobType.TESLA_READ, set, DataRequest.CHARGE_STATE.name() );
				Job.add( JobType.TESLA_READ, set, DataRequest.VEHICLE_STATE.name() );
				Job.add( JobType.TESLA_READ, set, DataRequest.CLIMATE_STATE.name() );
				break;
			}
			default: {
				System.out.println( "Command not matched, no action performed." );
			}
		}
	}
	
	
	
}
