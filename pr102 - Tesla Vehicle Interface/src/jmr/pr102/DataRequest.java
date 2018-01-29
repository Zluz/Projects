package jmr.pr102;

public enum DataRequest {
	
	// {"response":{"charging_state":"Complete","charge_limit_soc":90,"charge_limit_soc_std":90,"charge_limit_soc_min":50,"charge_limit_soc_max":100,"charge_to_max_range":false,"battery_heater_on":false,"not_enough_power_to_heat":false,"max_range_charge_counter":0,"fast_charger_present":false,"fast_charger_type":"<invalid>","battery_range":225.95,"est_battery_range":223.84,"ideal_battery_range":282.19,"battery_level":90,"usable_battery_level":90,"charge_energy_added":14.04,"charge_miles_added_rated":49.0,"charge_miles_added_ideal":61.5,"charger_voltage":0,"charger_pilot_current":40,"charger_actual_current":0,"charger_power":0,"time_to_full_charge":0.0,"trip_charging":false,"charge_rate":0.0,"charge_port_door_open":true,"scheduled_charging_start_time":null,"scheduled_charging_pending":false,"user_charge_enable_request":null,"charge_enable_request":true,"charger_phases":null,"charge_port_latch":"Engaged","charge_current_request":20,"charge_current_request_max":40,"managed_charging_active":false,"managed_charging_user_canceled":false,"managed_charging_start_time":null,"motorized_charge_port":true,"eu_vehicle":false,"timestamp":1500692187430}}
	CHARGE_STATE( "charge_state" ), // ChargeState
	
	// {"response":{"inside_temp":34.0,"outside_temp":31.5,"driver_temp_setting":22.2,"passenger_temp_setting":22.2,"left_temp_direction":0,"right_temp_direction":0,"is_auto_conditioning_on":false,"is_front_defroster_on":false,"is_rear_defroster_on":false,"fan_status":0,"is_climate_on":false,"min_avail_temp":15.0,"max_avail_temp":28.0,"seat_heater_left":0,"seat_heater_right":0,"seat_heater_rear_left":0,"seat_heater_rear_right":0,"seat_heater_rear_center":0,"seat_heater_rear_right_back":0,"seat_heater_rear_left_back":0,"smart_preconditioning":false,"timestamp":1500692691986}}
	CLIMATE_STATE( "climate_state" ), // ClimateSettings

	DRIVE_STATE( "drive_state" ), // DrivingAndPosition
	
	GUI_SETTINGS_STATE( "gui_settings" ), // GuiSettings
	
	VEHICLE_STATE( "vehicle_state" ), // VehicleState

	;
	
	
	private final String strUrlSuffix;
	
	
	private DataRequest( final String strUrlSuffix ) {
		this.strUrlSuffix = strUrlSuffix;
	}
	
	public String getUrlSuffix() {
		return this.strUrlSuffix;
	}
	
	public String getBasePath() {
		return "/External/Ingest/Tesla - " + this.name();
	}
	
	public String getResponsePath() {
		return this.getBasePath() + "/data/response";
	}
	
	public static DataRequest getDataRequest( final String str ) {
		if ( null==str ) return null;
		if ( str.isEmpty() ) return null;
		
		final String strName = str.trim().toUpperCase();
		final String strSuffix = str.trim().toLowerCase();
		
		for ( final DataRequest request : DataRequest.values() ) {
			if ( strName.startsWith( request.name() ) ) {
				return request;
			}
			if ( strSuffix.startsWith( request.getUrlSuffix() ) ) {
				return request;
			}
		}
		
		return null;
	}

}
