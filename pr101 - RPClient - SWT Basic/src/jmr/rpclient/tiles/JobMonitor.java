package jmr.rpclient.tiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jmr.s2db.Client;
import jmr.s2db.job.JobManager;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;

public class JobMonitor {

	
	final public static List<Job> listing = new LinkedList<>();

	private static Thread threadUpdater;
	
	private String strName = null;
	
	
	private static JobMonitor instance;
	
	private JobMonitor() {};
	
	public static JobMonitor get() {
		if ( null==instance ) {
			instance = new JobMonitor();
		}
		return instance;
	}
	
	public void initialize( final Map<String,String> mapOptions ) {
		this.strName = mapOptions.get( "remote" );
		this.initializeJobMonitorThread();
	}
	
	public void check() {
		if ( null==threadUpdater ) {
			this.initializeJobMonitorThread();
		}
		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
	}
	
	public String getName() {
		return this.strName;
	}
	
	public LinkedList<Job> getListing() {
		return new LinkedList<Job>( listing );
	}

	

	public void initializeJobMonitorThread() {
		if ( null!=threadUpdater ) return;
		
		threadUpdater = new Thread( "NetworkList Updater" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( TimeUnit.SECONDS.toMillis( 1 ) );
		
					for (;;) {
						synchronized ( listing ) {
							try {
								updateListing();
							} catch ( final Exception e ) {
								// ignore.. 
								// JDBC connection may have been dropped..
							}
						}
		
						Thread.sleep( TimeUnit.SECONDS.toMillis( 10 ) );
					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
	}
	

	private void updateListing() {

		final JobManager manager = Client.get().getJobManager();
		
		final List<Job> listingActive = manager.getJobListing( 
//				"( job.request LIKE \"%\" )" );
				"( job.state = \"R\" )", 100 );

		final List<Job> listingCompleted = manager.getJobListing( 
//				"( job.request LIKE \"%\" )" );
				"( ( job.state = \"C\" ) OR ( job.state = \"F\" ) "
				+ "OR ( job.state = \"W\" ) )", 8 );

		synchronized (listing) {
			listing.clear();
			listing.addAll( listingActive );
			listing.addAll( listingCompleted );
		}
		
		doWorkJobs( listingActive );
	}

	
	/*
	 * From  http://archive.oreilly.com/pub/h/1092
	 */
	static class InputStreamHandler extends Thread {
		
		/**
		 * Stream being read
		 */
		private final InputStream m_stream;

		/**
		 * The StringBuffer holding the captured output
		 */
		private final StringBuffer m_captureBuffer;
		
		private final PrintStream echo;

		/**
		 * Constructor.
		 * 
		 * @param
		 */
		InputStreamHandler(	final StringBuffer captureBuffer, 
							final InputStream stream,
							final PrintStream echo ) {
			m_stream = stream;
			m_captureBuffer = captureBuffer;
			this.echo = echo;
			start();
		}

		/**
		 * Stream the data.
		 */
		public void run() {
			try {
				int nextChar;
				while ((nextChar = m_stream.read()) != -1) {
					m_captureBuffer.append( (char) nextChar );
					if ( null!=this.echo ) {
						this.echo.print( nextChar );
					}
				}
			} catch ( final IOException ioe ) {
				System.err.println( ioe.toString() );
			}
		}
	}
	
	
	
	private void runRemoteExecute( final Job job ) {
		final Map<String,String> map = job.getJobDetails();
		
		if ( null!=this.strName 
				&& this.strName.equals( map.get( "remote" ) ) ) {

			job.setState( JobState.WORKING );

			final String strCommand = map.get( "command" );
			
			System.out.println( "Running command: " + strCommand );
			
			try {
				final Process process = 
								Runtime.getRuntime().exec( strCommand );
//				process.waitFor();
				
				
//				Process application = Runtime.getRuntime().exec(command);

				final StringBuffer inBuffer = new StringBuffer();
				final InputStream inStream = process.getInputStream();
				new InputStreamHandler( inBuffer, inStream, System.out );

				final StringBuffer errBuffer = new StringBuffer();
				final InputStream errStream = process.getErrorStream();
				new InputStreamHandler( errBuffer , errStream, System.err );

				process.waitFor();
				
				
				
				
//				final String strResult = "Exit value = " + process.exitValue();
				final String strOutput = inBuffer.toString();
				final String strError = errBuffer.toString();
				
				final Map<String,String> mapResult = new HashMap<>();
				mapResult.put( "exit_code", ""+process.exitValue() );
				mapResult.put( "std_out", strOutput );
				mapResult.put( "std_err", strError );
				
				final Gson GSON = new Gson();
				final JsonElement jsonResult = GSON.toJsonTree( mapResult );
				final String strResult = jsonResult.toString();
				
				job.setState( JobState.COMPLETE, strResult );
			} catch ( final Exception e ) {
				job.setState( JobState.FAILURE, e.toString() );
			}
			
		}
		
	}
	
	private void doWorkJobs( final List<Job> jobs ) {
		if ( jobs.isEmpty() ) return;
		
		final Thread threadWorkJobs = new Thread( "Work Jobs" ) {
			@Override
			public void run() {
				for ( final Job job : jobs ) {
					final JobType type = job.getJobType();

					// execute job?
					if ( JobType.REMOTE_EXECUTE.equals( type ) ) {
						runRemoteExecute( job );
					}
				}
			}
		};
		threadWorkJobs.start();
	}
	
	

	
	
	
	
}
