package jmr.s2.ingest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jmr.pr102.Command;
import jmr.pr102.DataRequest;
import jmr.pr102.TeslaVehicleInterface;
import jmr.pr102.comm.TeslaLogin;
import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.s2db.imprt.WebImport;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.util.NetUtil;

public class TeslaIngestManager {


	private TeslaVehicleInterface tvi;
	
	private Client s2db;

	
	public void login() {

		System.out.println( "Registering with S2DB" );

	    /* S2DB stuff */
	    s2db = Client.get();
//	    final Date now = new Date();
//	    final String strIP = NetUtil.getIPAddress();
	    final String strClass = TeslaIngestManager.class.getName();
//	    s2db.register( 	NetUtil.getMAC(), strIP, 
//	    				NetUtil.getSessionID(), 
//	    				strClass, now );
	    s2db.register( ClientType.TEST, NetUtil.getSessionID(), strClass );

	    
	    
		final String strS2DBRegister = "/External/Ingest/Tesla";
		final Map<String,String> mapRegister = new HashMap<String,String>();
		mapRegister.put( "source.session_id", NetUtil.getSessionID() );
		mapRegister.put( "source.session_seq", "" + s2db.getSessionSeq() );
		s2db.savePage( strS2DBRegister, mapRegister );
		

		

//		final String strUsername = 
//				SystemUtil.getProperty( SUProperty.TESLA_USERNAME ); 
//		final String strPassword = 
//				SystemUtil.getProperty( SUProperty.TESLA_PASSWORD ); 
		final TeslaLogin login = new S2TeslaLogin( );

//		if ( null!=strUsername && null!=strPassword ) {
//			tvi = new TeslaVehicleInterface( 
//							strUsername, strPassword.toCharArray() );
//		} else {
//			tvi = new TeslaVehicleInterface();
//		}
		
		tvi = new TeslaVehicleInterface( login );
		
		

//		System.out.println( "Logging in to Tesla" );
//		final Map<String, String> mapLogin = tvi.getLoginDetails();
//		
////		final String strLoginPath = "/External/Ingest/Tesla/Login";
////		s2db.savePage( strLoginPath, mapLogin );
//		
//
//		if ( null!=mapLogin ) {
////			JsonUtils.print( mapLogin );
//			Reporting.print( mapLogin );
//		
//			final int iMillisExpire = 
//					Integer.parseInt( mapLogin.get( "expires_in" ) );
//			final long iMinutesExpire = 
//					TimeUnit.MILLISECONDS.toMinutes( iMillisExpire );
//			System.out.println( 
//					"Token may expire in " + iMinutesExpire + " minutes." );
//		}
	}
	
	

	private boolean executeJobs() {

		final List<Job> jobs = 
				Job.get( "( ( state=\"R\" ) "
						+ "AND ( request LIKE \"TESLA_%\" ) )", 100 );
		
		if ( null!=jobs && !jobs.isEmpty() ) {
		
			final Map<DataRequest,Long> setRequests = new HashMap<>();
			final Map<Command,Long> mapCommands = new HashMap<>();
			
			for ( final Job job : jobs ) {
				
				// much of this is duplicated in
				// jmr.pr115.schedules.run.TeslaJob
				
				final long lSeq = job.getJobSeq();
				final String strRequest = job.getRequest();
				final JobType type = job.getJobType();
				final boolean bRead = JobType.TESLA_READ.equals( type );
				final boolean bWrite = JobType.TESLA_WRITE.equals( type );

				if ( bRead ) {
					final int iPos = strRequest.indexOf(":");
					final String strSub = strRequest.substring( iPos+1 ).trim();
					final DataRequest request = DataRequest.getDataRequest( strSub );
					if ( null!=request ) {
						setRequests.put( request, lSeq );
					}
				} else if ( bWrite ) {
					final int iPos = strRequest.indexOf(":");
					final String strSub = strRequest.substring( iPos+1 ).trim();
					final Command command = Command.getCommand( strSub );
					if ( null!=command ) {
						mapCommands.put( command, lSeq );
					}
				}
			}

			final Map<Long,String> mapResults = new HashMap<>();
			
			if ( !mapCommands.isEmpty() ) {
				for ( final Entry<Command, Long> 
								entry : mapCommands.entrySet() ) {
					final Command command = entry.getKey();
					final Long lSeq = entry.getValue();
					
					final Map<String,String> map = 
									tvi.command( command, null );
					
					mapResults.put( lSeq, map.get( 
									TeslaVehicleInterface.MAP_KEY_FULL_JSON ) );
				}
			}
			
			if ( !setRequests.isEmpty() ) {
				final Boolean[] arrFlags = { true };
				
				for ( final Entry<DataRequest, Long> 
											entry : setRequests.entrySet() ) {
					final DataRequest request = entry.getKey();
					final Long lSeq = entry.getValue();
					
					final String strResponse = 
							requestToWebService( request, arrFlags );
					
					mapResults.put( lSeq, strResponse );
				}
			}
			
			for ( final Job job : jobs ) {
				final long lSeq = job.getJobSeq();
				if ( mapResults.containsKey( lSeq ) ) {
					final String strResult = mapResults.get( lSeq );
					job.setState( JobState.COMPLETE, strResult );
				} else {
					job.setState( JobState.COMPLETE );
				}
			}

			return !setRequests.isEmpty() || !mapCommands.isEmpty();
			
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


	
	private final static Logger LOGGER = 
					Logger.getLogger(TeslaIngestManager.class.getName());
	
	
	/**
	 * arrFlags:
	 *  	0 - is charging
	 * @param request
	 * @param arrFlags
	 */
	/*package*/ String requestToWebService(	final DataRequest request,
											final Boolean[] arrFlags ) {

		System.out.println( "Requesting: " + request );
		
//		final Map<String, String> map = tvi.request( request );
		final String strResponse = tvi.request( request );
		
//			JsonUtils.print( map );
//		System.out.println( "\t" + map.size() + " entries" );


//		final String strNode = 
//						"/External/Ingest/Tesla/" + request.name();
//		s2db.savePage( strNode, map );
		
		
//		final String strNode = 
//				"/tmp/Import_Tesla_" + request.name() 
//				+ "_" + System.currentTimeMillis();

//		final JsonIngest ingest = new JsonIngest();
//		final Long seq = ingest.saveJson( strNode, strResponse );
		
		final WebImport ingest = new WebImport( 
						"Tesla - " + request.name(),
						tvi.getURL( request ),
						strResponse );
		Long seq;
		try {
			seq = ingest.save();
		} catch ( final IOException e ) {
			LOGGER.severe( "Failed to save data to " + ingest.getURL() );
			e.printStackTrace();
			return null;
		}
		final String strResult = ingest.getResponse();
		
		System.out.println( "Page saved: seq " + seq );
		
		
		if ( DataRequest.CHARGE_STATE.equals( request ) ) {
			final String strChargeState = 
						"/External/Ingest/Tesla/CHARGE_STATE/response";
			final Map<String, String> mapChargeState = 
						Client.get().loadPage( strChargeState );
			
//			final String strKey = "time_to_full_charge";
//			final String strValue = mapChargeState.get( strKey );
//			if ( null!=strValue && !strValue.isEmpty() ) {
//				final boolean bNumber = strValue.contains( "." );
//				if ( bNumber ) {
//					bIsCharging = !"0.0".equals( strValue.trim() );
//				}
//			};
			
			final String strKey = "charge_port_door_open";
			final String strValue = mapChargeState.get( strKey );
			arrFlags[0] = "true".equals( strValue );
			System.out.println( 
					"key: " + strKey + ", value: " + strValue );
		}
		
		return strResult;
	}
	
	
	public void requestToWebService( final DataRequest request ) {
		final Boolean[] arrFlags = { true };
		this.requestToWebService( request, arrFlags );
	}
	
	
	public void scanAll() {

		if ( null==tvi ) {
			System.out.println( "Initializing Tesla login.." );
			final TeslaLogin login = new S2TeslaLogin( );
			tvi = new TeslaVehicleInterface( login );
		}
		
		System.out.println( "------ ----------------------------------------------------------------" );
		System.out.println( "Now: " + new Date().toString() );
		System.out.println( "Token: " + tvi.getLoginToken() );
		
		TeslaSummarizers.register();
		
//		boolean bIsCharging = true;
		final Boolean[] arrFlags = { true };

		for ( final DataRequest request : DataRequest.values() ) {
			
			try {
				
			if ( false
					|| DataRequest.VEHICLE_STATE.equals( request ) 
					|| DataRequest.CHARGE_STATE.equals( request )
//					|| DataRequest.DRIVE_STATE.equals( request ) 
//					|| DataRequest.GUI_SETTINGS_STATE.equals( request )
//					|| DataRequest.CLIMATE_STATE.equals( request )
											) {

				this.requestToWebService( request, arrFlags );
				

				try {
					System.out.println( "Charging: " + arrFlags[0] );
					if ( arrFlags[0] ) {
						Thread.sleep( TimeUnit.MINUTES.toMillis( 30 ) );
					} else {
						Thread.sleep( TimeUnit.HOURS.toMillis( 2 ) );
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
			
			} catch ( final Exception e ) {
				e.printStackTrace();
				try {
					Thread.sleep( TimeUnit.HOURS.toMillis( 2 ) );
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	
	
	public static void main( final String[] args ) throws Exception {
		
		final String strSession = NetUtil.getSessionID();
		final String strClass = TeslaIngestManager.class.getName();
		Client.get().register( ClientType.TEST, strSession, strClass );
		
//		System.out.println( TimeUnit.HOURS.toMillis( 1 ) );

		final TeslaIngestManager tim = new TeslaIngestManager();
		
		tim.login();
		
//		tvi.command( Command.FLASH_LIGHTS, "" );
		
		tim.startMonitor();
		
		while ( true ) {
			
			tim.scanAll();

//			Thread.sleep( 10 * 60 * 1000 );
			Thread.sleep( TimeUnit.HOURS.toMillis( 2 ) );
		}
	}

}
