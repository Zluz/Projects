package jmr.pr113;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.bwssystems.nest.controller.FanMode;
import com.bwssystems.nest.controller.Thermostat;
import com.bwssystems.nest.controller.ThermostatTargetType;
import com.bwssystems.nest.protocol.status.DeviceDetail;
import com.bwssystems.nest.protocol.status.SharedDetail;

public class FullStatus {
	
	/*

notable values:

SharedDetail
	"current_temperature":16.92999,
	"target_temperature":10.159,
	"target_temperature_type":"heat",
	"target_temperature_high":24.0,
	"target_temperature_low":20.0,
	"hvac_fan_state":false,

DeviceDetail
	"fan_current_speed":"off",
	"auto_away_enable":true,
	"current_humidity":46,
	"temperature_lock":false,

	 */


	final Thermostat thermostat;
	final DeviceDetail device;
	final SharedDetail shared;
	
	final Map<String,String> map = new HashMap<>();
	
	
	/*package*/ FullStatus(	final Thermostat thermostat,
							final DeviceDetail device,
							final SharedDetail shared ) {
		this.thermostat = thermostat;
		this.device = device;
		this.shared = shared;
		
		map.putAll( this.device.getOriginalMap() );
		map.putAll( this.shared.getOriginalMap() );
	}

	
	public String getDeviceDetailJSON() {
		return this.device.getOriginalJSON();
	}
	
	public String getSharedDetailJSON() {
		return this.shared.getOriginalJSON();
	}
	
	public Map<String,String> getMap() {
		return Collections.unmodifiableMap( this.map );
	}
	
	public String getValue( final StatusKey key ) {
		if ( null==key ) return null;
		final String strValue = this.map.get( key.getKey() );
		return strValue;
	}
	
	public void setTemperature( final double dFahrenheit ) {
		final double dCelsius = ( dFahrenheit - 32 ) * 5 / 9;
		this.thermostat.setTargetTemperature( dCelsius );
	}
	
	public void setFanMode( final String strFanMode ) {
		final FanMode mode = FanMode.valueOf( strFanMode );
		this.thermostat.setFanMode( mode );
	}

	public void setTargetType( final String strFanMode ) {
		final ThermostatTargetType 
						type = ThermostatTargetType.valueOf( strFanMode );
		this.thermostat.setTargetType( type );
	}

}
