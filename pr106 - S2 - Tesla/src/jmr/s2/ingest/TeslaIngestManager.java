package jmr.s2.ingest;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jmr.pr102.Command;
import jmr.pr102.DataRequest;
import jmr.pr102.TeslaVehicleInterface;
import jmr.pr102.comm.TeslaLogin;
import jmr.s2db.Client;
import jmr.s2db.imprt.WebImport;
import jmr.s2db.tables.Job;
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
	    s2db.register( NetUtil.getSessionID(), strClass );

	    
	    
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
						+ "AND ( request LIKE \"TESLA_%\" ) )" );
		
		if ( null!=jobs && !jobs.isEmpty() ) {
		
			final Set<DataRequest> setRequests = new HashSet<>();
			final Map<Command,String> mapCommands = new HashMap<>();
			
			for ( final Job job : jobs ) {
				final String strRequest = job.getRequest();
				final boolean bRead = strRequest.startsWith( "TESLA_READ" );
				final boolean bWrite = strRequest.startsWith( "TESLA_WRITE" );

				if ( bRead ) {
					final int iPos = strRequest.indexOf(":");
					final String strSub = strRequest.substring( iPos+1 ).trim();
					final DataRequest request = DataRequest.getDataRequest( strSub );
					if ( null!=request ) {
						setRequests.add( request );
					}
				} else if ( bWrite ) {
					final int iPos = strRequest.indexOf(":");
					final String strSub = strRequest.substring( iPos+1 ).trim();
					final Command command = Command.getCommand( strSub );
					if ( null!=command ) {
						mapCommands.put( command, "" );
					}

				}
			}

			if ( !mapCommands.isEmpty() ) {
				for ( final Entry<Command, String> 
								entry : mapCommands.entrySet() ) {
					final Command command = entry.getKey();
					final String strPost = entry.getValue();
					tvi.command( command, strPost );
				}
			}
			
			if ( !setRequests.isEmpty() ) {
				final Boolean[] arrFlags = { true };
				
				for ( final DataRequest request : setRequests ) {
					requestToWebService( request, arrFlags );
				}
			}
			
			for ( final Job job : jobs ) {
				job.setState( 'C' );
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
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
		threadMonitorJobs.start();
	}


	
	/**
	 * arrFlags:
	 *  	0 - is charging
	 * @param request
	 * @param arrFlags
	 */
	/*package*/ void requestToWebService(	final DataRequest request,
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
		final Long seq = ingest.save();
		
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
		
	}
	
	
	public void scanAll() {

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
		Client.get().register( strSession, strClass );
		
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
