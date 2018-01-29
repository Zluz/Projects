package jmr.s2.ingest;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import jmr.pr102.DataRequest;
import jmr.s2db.Client;
import jmr.util.NetUtil;

public class RequestAll {

	


	
	public static void scanAll( final TeslaIngestManager tim ) {

		System.out.println( "------ ----------------------------------------------------------------" );
		System.out.println( "Now: " + new Date().toString() );
//		System.out.println( "Token: " + tvi.getLoginToken() );
		
		
//		boolean bIsCharging = true;
		final Boolean[] arrFlags = { true };

		for ( final DataRequest request : DataRequest.values() ) {
			
			try {
				
				if ( false
						|| DataRequest.VEHICLE_STATE.equals( request ) 
						|| DataRequest.CHARGE_STATE.equals( request )
						|| DataRequest.DRIVE_STATE.equals( request ) 
						|| DataRequest.GUI_SETTINGS_STATE.equals( request )
						|| DataRequest.CLIMATE_STATE.equals( request )
												) {

					System.out.println( "------ ----------------------------------------------------------------" );
//					System.out.println( "Requesting: " + request.name() );

					tim.requestToWebService( request, arrFlags );
	
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
	
	
	public static void main( final String[] args ) {
		
		final String strSession = NetUtil.getSessionID();
		final String strClass = TeslaIngestManager.class.getName();
		Client.get().register( strSession, strClass );
		
		final TeslaIngestManager tim = new TeslaIngestManager();
		tim.login();
		scanAll( tim );
	}

}
