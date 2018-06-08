package jmr.pr113;

import java.util.Set;

import com.bwssystems.nest.controller.Nest;
import com.bwssystems.nest.controller.NestSession;
import com.bwssystems.nest.controller.Thermostat;
import com.bwssystems.nest.protocol.error.LoginException;
import com.bwssystems.nest.protocol.status.DeviceDetail;
import com.bwssystems.nest.protocol.status.SharedDetail;

public class Session {

	
	
	
	
	private final char[] cUsername;
	private final char[] cPassword;
	
	NestSession session;
	
	public Session(	final char[] cUsername,
						final char[] cPassword ) {
		this.cUsername = cUsername;
		this.cPassword = cPassword;

		this.initSession();
	}
	
	private void initSession() {
		if ( null!=this.session ) {
			this.session.close();
		}
		try {
			this.session = new NestSession( cUsername, cPassword );
		} catch ( final LoginException e ) {
			System.err.println( "Exception encountered during Nest login." );
			e.printStackTrace();
			this.session = null;
		}
	}
	
	public FullStatus getStatus() {
		if ( null==session ) return null;
		
		try {
			final Nest nest = new Nest( session );
	
	//		final Set<String> setHomeNames = nest.getHomeNames(); 
	//		final String strHomeName = setHomeNames.iterator().next();
	//		final Home home = nest.getHome( strHomeName );
			
			final Set<String> setDeviceNames = nest.getThermostatNames();
			final String strDeviceName = setDeviceNames.iterator().next();
			
			final Thermostat thermostat = nest.getThermostat( strDeviceName );
	
			
			// helpful: https://www.freeformatter.com/json-formatter.html
			
			// shared: {"$version":19194,"$timestamp":1521945738763,"auto_away":0,"touched_by":{},"hvac_alt_heat_x2_state":false,"target_change_pending":false,"compressor_lockout_timeout":0,"compressor_lockout_enabled":false,"can_heat":true,"current_temperature":19.09,"hvac_cool_x2_state":false,"hvac_heater_state":false,"hvac_heat_x3_state":false,"hvac_aux_heater_state":false,"target_temperature_high":24.0,"hvac_heat_x2_state":false,"target_temperature":19.179,"hvac_alt_heat_state":false,"hvac_ac_state":false,"hvac_emer_heat_state":false,"target_temperature_type":"heat","can_cool":true,"name":"","auto_away_learning":"ready","hvac_fan_state":false,"target_temperature_low":20.0,"hvac_cool_x3_state":false}
			// device: {"$version":19431,"$timestamp":1521937968685,"temperature_scale":"F","leaf_type":1,"hot_water_active":false,"time_to_target":0,"max_nighttime_preconditioning_seconds":18000,"cooling_source":"electric","alt_heat_delivery":"forced-air","leaf_away_high":28.87999,"time_to_target_training":"ready","upper_safety_temp":35.0,"cooling_x3_delivery":"unknown","pin_star_description":"none","pin_w1_description":"heat","heat_x2_delivery":"forced-air","has_x3_heat":false,"dehumidifier_type":"unknown","compressor_lockout_leaf":-17.79999,"sunlight_correction_active":false,"fan_timer_speed":"stage1","has_heat_pump":false,"backplate_mono_info":"TFE (BP_DVT) 4.0.20 (root@bamboo) 2014-02-03 10:06:36","temperature_lock_low_temp":20.0,"hot_water_boiling_state":true,"fan_heat_cool_speed":"auto","device_locale":"en_US","note_codes":[],"leaf_threshold_heat":18.06902,"dual_fuel_breakpoint":-1.0,"nlclient_state":"","schedule_learning_reset":false,"has_humidifier":false,"learning_days_completed_cool":225,"emer_heat_enable":false,"fan_cooling_enabled":true,"hvac_staging_ignore":false,"backplate_model":"Backplate-1.9a","has_fan":true,"rssi":69.0,"has_aux_heat":false,"away_temperature_low_enabled":true,"alt_heat_x2_delivery":"forced-air","sunlight_correction_enabled":true,"lower_safety_temp_enabled":true,"heater_delivery":"forced-air","alt_heat_source":"gas","available_locales":"en_US,fr_CA,es_US,en_GB,fr_FR,nl_NL,es_ES,it_IT","home_away_input":true,"range_enable":true,"pro_id":"","lower_safety_temp":7.222,"humidity_control_lockout_end_time":0,"filter_reminder_enabled":true,"heater_source":"gas","heat_x3_source":"gas","fan_current_speed":"off","maint_band_lower":0.39,"equipment_type":"gas","safety_temp_activating_hvac":false,"is_on_stand":false,"learning_days_completed_range":17,"auto_away_enable":true,"hot_water_away_enabled":true,"away_temperature_high":24.44444,"forced_air":true,"maint_band_upper":0.39,"hvac_wires":"Heat,Cool,Fan,Common Wire,Rh","has_hot_water_temperature":false,"dual_fuel_breakpoint_override":"none","fan_duty_start_time":0,"hvac_safety_shutoff_active":false,"humidity_control_lockout_start_time":0,"country_code":"US","leaf_schedule_delta":1.10999,"logging_priority":"informational","demand_charge_icon":false,"error_code":"","heat_x2_source":"gas","fan_cooling_readiness":"ready","learning_mode":true,"away_temperature_low_adjusted":10.0,"hvac_smoke_safety_shutoff_active":false,"current_humidity":34,"has_alt_heat":false,"tou_icon":false,"mac_address":"18b430034be2","upper_safety_temp_enabled":false,"auto_dehum_state":false,"fan_capabilities":"stage1","wiring_error":"","backplate_mono_version":"4.0.20","heatpump_setback_active":false,"fan_cooling_state":false,"target_time_confidence":0.0,"has_hot_water_control":false,"pin_rc_description":"none","fan_schedule_speed":"stage1","learning_days_completed_heat":469,"preconditioning_enabled":true,"star_type":"unknown","aux_heat_source":"electric","creation_time":1338703345436,"filter_changed_date":1513314000,"touched_by":{},"has_x2_heat":false,"has_emer_heat":false,"heat_pump_aux_threshold_enabled":true,"hot_water_away_active":false,"preconditioning_active":false,"type":"TBD","aux_lockout_leaf":10.0,"oob_wires_completed":true,"temperature_lock_pin_hash":"","ob_orientation":"O","ob_persistence":true,"switch_preconditioning_control":false,"is_furnace_shutdown":false,"learning_state":"slow","leaf_away_low":10.0,"should_wake_on_approach":true,"has_dual_fuel":false,"gear_threshold_high":0.0,"has_x2_alt_heat":false,"humidifier_state":false,"local_ip":"192.168.2.205","heatpump_savings":"off","oob_interview_completed":true,"fan_duty_cycle":3600,"backplate_serial_number":"01BA02AB131208PV","oob_where_completed":false,"target_humidity_enabled":false,"model_version":"Diamond-1.12","away_temperature_high_adjusted":24.44444,"oob_temp_completed":true,"heat_pump_comp_threshold":-31.5,"dehumidifier_orientation_selected":"unknown","auto_away_reset":false,"oob_test_completed":true,"fan_mode":"auto","current_schedule_mode":"HEAT","pin_y1_description":"cool","temperature_lock_high_temp":22.222,"heat_x3_delivery":"forced-air","leaf_learning":"ready","cooling_delivery":"unknown","oob_summary_completed":true,"filter_reminder_level":0,"battery_level":3.921,"has_dehumidifier":false,"serial_number":"01AA02AB111205MR","leaf":false,"safety_state_time":0,"pin_g_description":"fan","where_id":"00000000-0000-0000-0000-000100000006","temperature_lock":false,"cooling_x2_source":"electric","last_software_update_utc_secs":1516167398,"humidifier_type":"unknown","user_brightness":"auto","y2_type":"unknown","smoke_shutoff_supported":true,"pin_ob_description":"none","target_humidity":35.0,"postal_code":"21737","pin_c_description":"power","alt_heat_x2_source":"gas","heat_pump_comp_threshold_enabled":false,"gear_threshold_low":0.0,"eco":{"mode":"schedule","touched_by":1,"mode_update_timestamp":1521929172},"away_temperature_high_enabled":false,"leaf_threshold_cool":0.0,"hvac_pins":"W1,Y1,C,Rh,G","eco_onboarding_needed":true,"auto_dehum_enabled":false,"pin_y2_description":"none","fan_control_state":false,"radiant_control_enabled":false,"backplate_bsl_info":"BSL","heatpump_ready":false,"dehumidifier_state":false,"capability_level":5.66,"fan_duty_end_time":0,"away_temperature_low":10.0,"preconditioning_ready":true,"cooling_x3_source":"electric","has_x2_cool":false,"heat_pump_aux_threshold":10.0,"aux_heat_delivery":"forced-air","oob_wifi_completed":true,"filter_changed_set_date":1514750831,"has_x3_cool":false,"has_fossil_fuel":true,"emer_heat_delivery":"forced-air","learning_time":3456,"sunlight_correction_ready":true,"cooling_x2_delivery":"unknown","safety_state":"none","has_air_filter":true,"humidity_control_lockout_enabled":false,"backplate_bsl_version":"1.1","heat_link_connection":0,"oob_startup_completed":true,"pin_rh_description":"power","current_version":"5.6.6-4","click_sound":"on","switch_system_off":false,"fan_timer_timeout":0,"fan_timer_duration":28800,"emer_heat_source":"electric","pin_w2aux_description":"none","schedules":[]}
			
			final DeviceDetail device = thermostat.getDeviceDetail();
			final SharedDetail shared = thermostat.getSharedDetail();
			
			final FullStatus status = new FullStatus( thermostat, device, shared );
			return status;
		} catch ( final Exception e ) { 
			// can get JsonSyntaxException on NestSession ctor
			System.err.println( "Failed to get Nest data." );
			e.printStackTrace();
			return null;
		}
	}
	
	
}
