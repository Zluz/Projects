
package jmr.pr102;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.pr102.comm.HttpGet;
import jmr.pr102.comm.JsonUtils;
import jmr.pr102.comm.TeslaLogin;
import jmr.pr102.comm.TeslaVehicleID;

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
	
	
	public Map<String,String> request(	final DataRequest request ) {
		if ( null==request ) throw new IllegalStateException( "Null request" );

		final String strVID = this.vehicle.getVehicleID();
		
		final String strURL = TeslaConstants.URL_BASE_TESLA_API_PROD 
						+ "api/1/vehicles/" + strVID + "/data_request/" 
						+ request.getUrlSuffix();
		
		final HttpGet get = new HttpGet( strURL, this.login );
		
		try {
			final String strResponse = get.getContent();
			
			// {"response":{"charging_state":"Complete","charge_limit_soc":90,"charge_limit_soc_std":90,"charge_limit_soc_min":50,"charge_limit_soc_max":100,"charge_to_max_range":false,"battery_heater_on":false,"not_enough_power_to_heat":false,"max_range_charge_counter":0,"fast_charger_present":false,"fast_charger_type":"<invalid>","battery_range":225.95,"est_battery_range":223.84,"ideal_battery_range":282.19,"battery_level":90,"usable_battery_level":90,"charge_energy_added":14.04,"charge_miles_added_rated":49.0,"charge_miles_added_ideal":61.5,"charger_voltage":0,"charger_pilot_current":40,"charger_actual_current":0,"charger_power":0,"time_to_full_charge":0.0,"trip_charging":false,"charge_rate":0.0,"charge_port_door_open":true,"scheduled_charging_start_time":null,"scheduled_charging_pending":false,"user_charge_enable_request":null,"charge_enable_request":true,"charger_phases":null,"charge_port_latch":"Engaged","charge_current_request":20,"charge_current_request_max":40,"managed_charging_active":false,"managed_charging_user_canceled":false,"managed_charging_start_time":null,"motorized_charge_port":true,"eu_vehicle":false,"timestamp":1500692187430}}
			
			final JsonElement element = new JsonParser().parse( strResponse );
			final JsonElement response = element.getAsJsonObject().get( "response" );
			final JsonObject jo = response.getAsJsonObject();
			final Map<String,String> map = JsonUtils.transformJsonToMap( jo );
			
	        return map;

		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	
	
	public static void main( final String[] args ) {
		
		final String strUsername = "abc"; //FIXME replace
		final char[] arrPassword = "123".toCharArray(); //FIXME replace
		
		final TeslaVehicleInterface tvi = 
				new TeslaVehicleInterface( strUsername, arrPassword );
		
		for ( final DataRequest request : DataRequest.values() ) {
			System.out.println( "Requesting: " + request );
			final Map<String, String> map = tvi.request( request );
			JsonUtils.print( map );
		}
		
	}

}
