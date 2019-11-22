package jmr.s2db.job;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jmr.pr126.comm.http.HttpListener;
import jmr.pr126.comm.http.HttpListener.Listener;
import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.s2db.comm.Notifier;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;

public class JobMonitor {

	private final static Logger 
					LOGGER = Logger.getLogger( JobMonitor.class.getName() );
	
	final public static List<Job> listing = new LinkedList<>();

	private static Thread threadUpdater;
	
	private String strName = null;
	
	private RunRemoteJob runner = null;
	
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
		this.runner = new RunRemoteJob( strName );
		this.initializeJobMonitorThread();
	}
	
	public void check() {
		
		if ( null==this.runner ) {
			LOGGER.warning( "JobMonitor not correctly initialized." );
			this.runner = new RunRemoteJob( "<JobMonitor_not_initialized>" );
		}
		
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

	

	/* similar to EventMonistor.listener */ 
	private Listener listener = new Listener() {
		@Override
		public void received( final Map<String, Object> map ) {
			System.out.println( "--- JobMonitor HttpListener.received()" );
			if ( Notifier.EVENT_TABLE_UPDATE.equals( map.get( "job" ) ) ) {
				if ( "event".equals( map.get( "table" ) ) ) {
					System.out.print( "\tScanning for new jobs..." );
					updateListing();
					System.out.println( "Done." );
				}
			}
		}
	};
	
	
	
	public void initializeJobMonitorThread() {
		
		HttpListener.getInstance( ClientType.TILE_GUI.getPort() )
											.registerListener( listener );
		
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
				"( job.state = \"R\" AND ISNULL( job.step ) )", 100 );
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
					+ "OR ( job.state = \"W\" ) ) AND ISNULL( job.step )", 8 );
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
	
	
	private void doWorkJobs( final List<Job> jobs ) {
		if ( null==jobs ) return;
		if ( jobs.isEmpty() ) return;

//		System.out.println( "JobMonitor.doWorkJobs(), jobs.size: " + jobs.size() );
		
		final Thread threadWorkJobs = new Thread( "Work Jobs" ) {
			@Override
			public void run() {
				
				int i=0;
				for ( final Job job : jobs ) {
					i++;
					final JobType type = job.getJobType();
					
					System.out.print( "[" + System.currentTimeMillis() + "] " );
					System.out.print( "JobMonitor: "
							+ "(job " + i + " of " + jobs.size() + ") " );

					if ( ! type.isRemoteType() ) {
						System.out.println( "No remote." );
					} else if ( runner.isIntendedHere( job ) ) {
						
						System.out.println( 
								"Remote job identified to run here "
								+ "(Job " + job.getJobSeq() + ")" );
						
						job.setState( JobState.WORKING );

						// execute job?
						if ( JobType.REMOTE_EXECUTE.equals( type ) ) {
							runner.runRemoteExecute( job );
						} else if ( JobType.REMOTE_OUTPUT.equals( type ) ) {
							runner.postRemoteOutput( job );
						} else if ( JobType.REMOTE_SHUTDOWN.equals( type ) ) {
							runner.runShutdown( job );
						} else if ( JobType.REMOTE_GET_CALL_STACK.equals( type ) ) {
							runner.runGetCallStack( job );
						}

					} else {
						System.out.println( "  Not intended here." );
					}
				}
			}
		};
		threadWorkJobs.start();
	}
	
	

	
	
	
	
}
