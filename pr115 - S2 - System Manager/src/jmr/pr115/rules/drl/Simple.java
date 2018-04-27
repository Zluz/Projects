package jmr.pr115.rules.drl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;

import jmr.pr115.schedules.run.TeslaJob;
import jmr.s2.ingest.Import;
import jmr.s2db.imprt.WebImport;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.util.TimeUtil;

public class Simple {
	
	static {
		TimeUtil.isHourOfDay(); // just to get the import
	}


	public static void doRefreshWeather() {
		System.out.println( "--- doRefreshWeather(), "
					+ "time is " + LocalDateTime.now().toString() );
		try {

			
	//		ConnectionProvider.get();
	//		
	//		final String strSession = NetUtil.getSessionID();
	//		final String strClass = Import.class.getName();
	//		Client.get().register( strSession, strClass );
			
	//		final Import source = Import.NEWS_CURRENT__CNN_NEWSAPI;
			final Import source = Import.WEATHER_FORECAST__YAHOO;
			
			final String strURL = source.getURL();
			final String strTitle = source.getTitle();
	
	//		System.out.println( "Refreshing weather import.." );
			
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
				
				workJobs( list );
			}
		}
	}

	
	public static void workJobs( final List<Job> jobs ) {
		if ( null==jobs ) return;
		if ( jobs.isEmpty() ) return;
		
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
	
}
