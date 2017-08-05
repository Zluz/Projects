
package jmr.pr102;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.pr102.comm.HttpGet;
import jmr.pr102.comm.HttpPost;
import jmr.pr102.comm.JsonUtils;
import jmr.pr102.comm.TeslaLogin;
import jmr.pr102.comm.TeslaVehicleID;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class TeslaVehicleInterface implements TeslaConstants {

	
	private TeslaLogin login;
	private TeslaVehicleID vehicle;
	
	public TeslaVehicleInterface(	final String strUsername,
									final char[] arrPassword,
									final int iVehicleIndex ) {
		this.login = new TeslaLogin( strUsername, arrPassword );
		this.vehicle = new TeslaVehicleID( this.login, iVehicleIndex );
	}
	

	public TeslaVehicleInterface(	final String strUsername,
									final char[] arrPassword ) {
		this( strUsername, arrPassword, 0 );
	}

	public TeslaVehicleInterface() {
		this.login = TeslaLogin.DUMMY_LOGIN;
		this.vehicle = TeslaVehicleID.DUMMY_VEHICLE_ID;
	}

	
	public Map<String,String> getLoginDetails() {
		return this.login.getLoginDetails();
	}
	
	
	public String getLoginToken() {
		try {
			return this.login.getTokenValue();
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<no value; " + e.toString() + " encountered>";
		}
	}
	

	
	
	private String getContent(	final String strURL,
								final String strPostContent ) {

		final HttpGet get = new HttpGet( strURL, this.login );
		final HttpPost post = new HttpPost( strURL, this.login );

		try {
			final String strResponse = (null!=strPostContent) 
							? post.postContent( strPostContent ) 
							: get.getContent();
			return strResponse;
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// some error. authentication may have expired.
		this.login.invalidate();
		this.vehicle.invalidate();

		Exception cause = null;
		
		try {
			final String strResponse = (null!=strPostContent) 
					? post.postContent( strPostContent ) 
					: get.getContent();
			return strResponse;
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			cause = e;
		}

		// nope. no good.
		throw new IllegalStateException( "Unable to retrieve content", cause );
	}
	
	
	public Map<String,String> request(	final DataRequest request ) {
		if ( null==request ) throw new IllegalStateException( "Null request" );

		final String strVID = (null!=this.vehicle)
						? this.vehicle.getVehicleID()
						: DUMMY_VEHICLE_ID;
		
		final String strURL = TeslaConstants.URL_BASE_TESLA_API_PROD 
						+ "api/1/vehicles/" + strVID + "/data_request/" 
						+ request.getUrlSuffix();
		
		final String strResponse = getContent( strURL, null );
		// {"response":{"charging_state":"Complete","charge_limit_soc":90,"charge_limit_soc_std":90,"charge_limit_soc_min":50,"charge_limit_soc_max":100,"charge_to_max_range":false,"battery_heater_on":false,"not_enough_power_to_heat":false,"max_range_charge_counter":0,"fast_charger_present":false,"fast_charger_type":"<invalid>","battery_range":225.95,"est_battery_range":223.84,"ideal_battery_range":282.19,"battery_level":90,"usable_battery_level":90,"charge_energy_added":14.04,"charge_miles_added_rated":49.0,"charge_miles_added_ideal":61.5,"charger_voltage":0,"charger_pilot_current":40,"charger_actual_current":0,"charger_power":0,"time_to_full_charge":0.0,"trip_charging":false,"charge_rate":0.0,"charge_port_door_open":true,"scheduled_charging_start_time":null,"scheduled_charging_pending":false,"user_charge_enable_request":null,"charge_enable_request":true,"charger_phases":null,"charge_port_latch":"Engaged","charge_current_request":20,"charge_current_request_max":40,"managed_charging_active":false,"managed_charging_user_canceled":false,"managed_charging_start_time":null,"motorized_charge_port":true,"eu_vehicle":false,"timestamp":1500692187430}}

		/* can get:
java.lang.Exception: HTTP code 408 received.
	at jmr.pr102.comm.HttpGet.getContent(HttpGet.java:43)
	at jmr.pr102.TeslaVehicleInterface.getContent(TeslaVehicleInterface.java:56)
	at jmr.pr102.TeslaVehicleInterface.request(TeslaVehicleInterface.java:96)
	at jmr.pr102.TeslaVehicleInterface.null(Unknown Source)
		 */
		
		
		
		if ( null!=strResponse ) {
			
			final JsonElement element = new JsonParser().parse( strResponse );
			final JsonElement response = element.getAsJsonObject().get( "response" );
			final JsonObject jo = response.getAsJsonObject();
			final Map<String,String> map = JsonUtils.transformJsonToMap( jo );
			
	        return map;
	        
		} else {
			return null;
		}
	}
	
	

	public Map<String,String> command(	final Command command, 
										final String strPost ) {
		if ( null==command ) throw new IllegalStateException( "Null command" );

		final String strVID = this.vehicle.getVehicleID();
		
		final String strURL = TeslaConstants.URL_BASE_TESLA_API_PROD 
						+ "api/1/vehicles/" + strVID + "/" 
						+ command.getUrlSuffix();
		
		final String strResponse = getContent( strURL, strPost );
		// {"response":{"reason":"","result":true}}
		// {"response":{"reason":"could_not_wake_buses","result":false}}

		if ( null!=strResponse ) {
			
			final JsonElement element = new JsonParser().parse( strResponse );
			final JsonElement response = element.getAsJsonObject().get( "response" );
			final JsonObject jo = response.getAsJsonObject();
			final Map<String,String> map = JsonUtils.transformJsonToMap( jo );
			
	        return map;
	        
		} else {
			return null;
		}
	}
	
	
	
	

	
	
	
	public static void main( final String[] args ) throws Exception {
		
		
		final TeslaVehicleInterface tvi; 

		{
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
		}
				
		
		System.out.println( "Logging in" );
		final Map<String, String> mapLogin = tvi.login.login();
		if ( null!=mapLogin ) {
			JsonUtils.print( mapLogin );
		
			final int iMillisExpire = 
					Integer.parseInt( mapLogin.get( "expires_in" ) );
			final long iMinutesExpire = 
					TimeUnit.MILLISECONDS.toMinutes( iMillisExpire );
			System.out.println( 
					"Token may expire in " + iMinutesExpire + " minutes." );
		}
		
		
//		tvi.command( Command.FLASH_LIGHTS, "" );
		
		
		while ( true ) {
			
			System.out.println( "------ ----------------------------------------------------------------" );
			System.out.println( "Now: " + new Date().toString() );
			System.out.println( "Token: " + tvi.login.getTokenValue() );
			
			for ( final DataRequest request : DataRequest.values() ) {
				System.out.println( "Requesting: " + request );
				final Map<String, String> map = tvi.request( request );
//				JsonUtils.print( map );
				System.out.println( "\t" + map.size() + " entries" );
			}

			Thread.sleep( 10 * 60 * 1000 );
		}
	}

}
