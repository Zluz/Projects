package jmr.s2.ingest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jmr.pr102.DataRequest;
import jmr.pr102.TeslaVehicleInterface;
import jmr.s2db.Client;
import jmr.s2db.comm.JsonIngest;
import jmr.util.NetUtil;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;
import jmr.util.report.Reporting;

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
		

		

		final String strUsername = 
				SystemUtil.getProperty( SUProperty.TESLA_USERNAME ); 
		final String strPassword = 
				SystemUtil.getProperty( SUProperty.TESLA_PASSWORD ); 


		if ( null!=strUsername && null!=strPassword ) {
			tvi = new TeslaVehicleInterface( 
							strUsername, strPassword.toCharArray() );
		} else {
			tvi = new TeslaVehicleInterface();
		}
		
		
		

		System.out.println( "Logging in to Tesla" );
		final Map<String, String> mapLogin = tvi.getLoginDetails();
		
		final String strLoginPath = "/External/Ingest/Tesla/Login";
		s2db.savePage( strLoginPath, mapLogin );
		

		if ( null!=mapLogin ) {
//			JsonUtils.print( mapLogin );
			Reporting.print( mapLogin );
		
			final int iMillisExpire = 
					Integer.parseInt( mapLogin.get( "expires_in" ) );
			final long iMinutesExpire = 
					TimeUnit.MILLISECONDS.toMinutes( iMillisExpire );
			System.out.println( 
					"Token may expire in " + iMinutesExpire + " minutes." );
		}
	}
	
	
	public void scanAll() {

		System.out.println( "------ ----------------------------------------------------------------" );
		System.out.println( "Now: " + new Date().toString() );
		System.out.println( "Token: " + tvi.getLoginToken() );
		
		for ( final DataRequest request : DataRequest.values() ) {
			
			try {
				
			if ( DataRequest.VEHICLE_STATE.equals( request ) 
					|| DataRequest.CHARGE_STATE.equals( request )
					|| DataRequest.DRIVE_STATE.equals( request ) ) {

				
				System.out.println( "Requesting: " + request );
				
//				final Map<String, String> map = tvi.request( request );
				final String strResponse = tvi.request( request );
				
	//			JsonUtils.print( map );
//				System.out.println( "\t" + map.size() + " entries" );
	
	
//				final String strLoginPath = 
//								"/External/Ingest/Tesla/" + request.name();
//				s2db.savePage( strLoginPath, map );
				
				
				final String strNode = 
						"/tmp/Import_Tesla_" + request.name() 
						+ "_" + System.currentTimeMillis();

				final JsonIngest ingest = new JsonIngest();
				final Long seq = ingest.saveJson( strNode, strResponse );
				System.out.println( "Page saved: seq " + seq );
				
				
				try {
					Thread.sleep( TimeUnit.HOURS.toMillis( 1 ) );
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				
			}
			
			} catch ( final Exception e ) {
				e.printStackTrace();
				try {
					Thread.sleep( TimeUnit.HOURS.toMillis( 4 ) );
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
		
		
		while ( true ) {
			
			tim.scanAll();

//			Thread.sleep( 10 * 60 * 1000 );
			Thread.sleep( TimeUnit.HOURS.toMillis( 2 ) );
		}
	}
}
