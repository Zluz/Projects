package jmr.pr115.rules.ingest;

import java.util.List;

import jmr.pr115.rules.RulesProcessing;
import jmr.s2db.tables.Job;

/*
 * parts copied from jmr.s2.ingest.TeslaIngestManager.
 */
public class SubmitJobs {

	
	
	public SubmitJobs() {
		this.startMonitor();
	}

	

	private long lLastJobScanned = 0;


	private boolean executeJobs() {

		final List<Job> jobs = 
//				Job.get( "( ( state=\"R\" ) "
//						+ "AND ( request LIKE \"TESLA_%\" ) )", 100 );
				Job.get( "( ( state=\"R\" ) "
						+ "AND ( seq>" + lLastJobScanned + " ) )", 100 );
		
		if ( null!=jobs && !jobs.isEmpty() ) {
		
//			final Map<DataRequest,Long> setRequests = new HashMap<>();
//			final Map<Command,Long> mapCommands = new HashMap<>();
//			
			for ( final Job job : jobs ) {
				final long lSeq = job.getJobSeq();
				
				lLastJobScanned = Math.max( lLastJobScanned, lSeq );
				
				final Thread thread = new Thread( "Job " + job.getJobSeq() ) {
					@Override
					public void run() {
						RulesProcessing.get().process( job );
					}	
				};
				thread.start();
				
				
				
				
//				final String strRequest = job.getRequest();
//				final JobType type = job.getJobType();
//				final boolean bRead = JobType.TESLA_READ.equals( type );
//				final boolean bWrite = JobType.TESLA_WRITE.equals( type );
//
//				if ( bRead ) {
//					final int iPos = strRequest.indexOf(":");
//					final String strSub = strRequest.substring( iPos+1 ).trim();
//					final DataRequest request = DataRequest.getDataRequest( strSub );
//					if ( null!=request ) {
//						setRequests.put( request, lSeq );
//					}
//				} else if ( bWrite ) {
//					final int iPos = strRequest.indexOf(":");
//					final String strSub = strRequest.substring( iPos+1 ).trim();
//					final Command command = Command.getCommand( strSub );
//					if ( null!=command ) {
//						mapCommands.put( command, lSeq );
//					}
//
//				}
			}

//			final Map<Long,String> mapResults = new HashMap<>();
//			
//			if ( !mapCommands.isEmpty() ) {
//				for ( final Entry<Command, Long> 
//								entry : mapCommands.entrySet() ) {
//					final Command command = entry.getKey();
//					final Long lSeq = entry.getValue();
//					
//					final Map<String,String> map = 
//									tvi.command( command, null );
//					
//					mapResults.put( lSeq, map.get( 
//									TeslaVehicleInterface.MAP_KEY_FULL_JSON ) );
//				}
//			}
//			
//			if ( !setRequests.isEmpty() ) {
//				final Boolean[] arrFlags = { true };
//				
//				for ( final Entry<DataRequest, Long> 
//											entry : setRequests.entrySet() ) {
//					final DataRequest request = entry.getKey();
//					final Long lSeq = entry.getValue();
//					
//					final String strResponse = 
//							requestToWebService( request, arrFlags );
//					
//					mapResults.put( lSeq, strResponse );
//				}
//			}
//			
//			for ( final Job job : jobs ) {
//				final long lSeq = job.getJobSeq();
//				if ( mapResults.containsKey( lSeq ) ) {
//					final String strResult = mapResults.get( lSeq );
//					job.setState( JobState.COMPLETE, strResult );
//				} else {
//					job.setState( JobState.COMPLETE );
//				}
//			}
//
//			return !setRequests.isEmpty() || !mapCommands.isEmpty();
			return !jobs.isEmpty();
			
		} else {
			return false;
		}
	}

	
	
	
	private void startMonitor() {
		final Thread threadMonitorJobs = new Thread( "Monitor Jobs" ) {
			@Override
			public void run() {
				try {
					for (;;) {
						Thread.sleep( 1000 );
						
						executeJobs();
					}
				} catch ( final Exception e ) {
					// just quit
					e.printStackTrace();
					System.err.println( 
							"Exception in 'Monitor Jobs' thread. Quitting." );
					System.out.println( "SubmitJobs.startMonitor()" );
					Runtime.getRuntime().exit( 100 );
				}
			}
		};
		try {
			Thread.sleep( 1000 );
		} catch ( final InterruptedException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threadMonitorJobs.start();		
	}
	
}
