package jmr.s2db.job;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jmr.s2db.Client;
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
		synchronized ( listing ) {
			return new LinkedList<Job>( listing );
		}
	}

	
	public final static long MONITOR_INTERVAL = 200;
	
	private int iLastActiveCount = 0;
	private int iCompletedQuerySkipCount = 0;
	private int iCompletedQuerySkipMax = 10;
	private List<Job> listingLastCompleted = new LinkedList<>();

	
	public void initializeJobMonitorThread() {
		if ( null!=threadUpdater ) return;
		
		threadUpdater = new Thread( "Job Monitor Updater" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( 1000 * 2 );
		
					for (;;) {
//						synchronized ( listing ) {
							try {
								updateListing();
							} catch ( final Exception e ) {
								// ignore.. 
								// JDBC connection may have been dropped..
								e.printStackTrace();
							}
//						}
		
						Thread.sleep( MONITOR_INTERVAL );
					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
	}

	
	/*
	 * NOTE: This will run busy if there are no completed records 
	 * in the Jobs table.
	 */
	private void updateListing() {
		
//		System.out.println( "--> updateListing()" );

		final JobManager manager = Client.get().getJobManager();
		
		final List<Job> listingActive = manager.getJobListing( 
//				"( job.request LIKE \"%\" )" );
				"( job.state = \"R\" )", 100 );
		final boolean bQueryCompleted =
				( listingActive.size() != iLastActiveCount )
				|| ( iCompletedQuerySkipCount > iCompletedQuerySkipMax )
				|| ( listingLastCompleted.isEmpty() );
		iLastActiveCount = listingActive.size();

		if ( bQueryCompleted ) {
//			System.out.println( "    updateListing() - querying completed" );

			final List<Job> listingCompleted = manager.getJobListing( 
	//				"( job.request LIKE \"%\" )" );
					"( ( job.state = \"C\" ) OR ( job.state = \"F\" ) "
					+ "OR ( job.state = \"W\" ) )", 8 );
			synchronized ( listingLastCompleted ) {
				listingLastCompleted.clear();
				listingLastCompleted.addAll( listingCompleted );
			}
			iCompletedQuerySkipCount = 0;
		} else {
			iCompletedQuerySkipCount++;
		}

		synchronized (listing) {
			listing.clear();
			listing.addAll( listingActive );
			listing.addAll( listingLastCompleted );
		}
		
		doWorkJobs( listingActive );
		
//		System.out.println( "<-- updateListing()" );
	}

	
	public static void processLine(	final Job job,
									final String strLine ) {
		if ( null==job ) return;
		if ( null==strLine ) return;
		
		final String strNorm = strLine.trim();
		job.updateProgress( strNorm );
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
		
		private final Job job;

		/**
		 * Constructor.
		 * 
		 * @param
		 */
		InputStreamHandler(	final Job job,
							final StringBuffer captureBuffer, 
							final InputStream stream,
							final PrintStream echo ) {
			this.job = job;
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
				String strLine = "";
				while ((nextChar = m_stream.read()) != -1) {
					final char c = (char) nextChar;
					m_captureBuffer.append( c );
					if ( null!=this.echo ) {
						this.echo.print( c );
					}
					if ( '\n'==c ) {
						if ( null!=job ) {
							processLine( job, strLine );
						}
						strLine = "";
					} else {
						strLine = strLine + c;
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
				new InputStreamHandler( job, inBuffer, inStream, System.out );

				final StringBuffer errBuffer = new StringBuffer();
				final InputStream errStream = process.getErrorStream();
				new InputStreamHandler( job, errBuffer , errStream, System.err );

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
