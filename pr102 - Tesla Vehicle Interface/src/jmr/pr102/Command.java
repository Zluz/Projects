package jmr.pr102;


/*
 * good resource (but may be outdated)
 * 
 * https://tesla-api.timdorr.com/vehicle/commands/sunroof
 * 
 */
public enum Command {

	WAKE_UP( "wake_up" ), // WakeUpCar

	REMOTE_START( "command/remote_start_drive" ), // RemoteStart
	RESET_VALET_PIN( "command/reset_valet_pin" ), // ResetValetPin
	SET_VALET_MODE( "command/set_valet_mode" ), // SetValetMode

	SET_TEMPS( "command/set_temps" ), // SetTemperature
	HVAC_START( "command/auto_conditioning_start" ), // StartHvacSystem
	HVAC_STOP( "command/auto_conditioning_stop" ), // StopHvacSystem
	
	FLASH_LIGHTS( "command/flash_lights" ), // FlashLights
	HONK_HORN( "command/honk_horn" ), // HonkHorn
	
	DOORS_LOCK( "command/door_lock" ), // LockDoors
	DOORS_UNLOCK( "command/door_unlock" ), // UnlockDoors
	SUNROOF( "command/sun_roof_control" ), // MovePanoRoof

	@Deprecated // maybe? https://tesla-api.timdorr.com/vehicle/commands/trunk
	TRUNK_OPEN( "command/trunk_open" ), // OpenTrunk
	ACTUATE_TRUNK( "command/actuate_trunk" ), // OpenTrunk
	
	CHARGE_PORT_DOOR( "command/charge_port_door_open" ), // OpenChargePort

	SET_CHARGE_LIMIT( "command/set_charge_limit" ), // SetChargeLimit
	SET_CHARGE_MAX_RANGE( "command/charge_max_range" ), // SetChargeLimitToMaxRange
	SET_CHARGE_LIMIT_TO_STANDARD( "command/charge_standard" ), // SetChargeLimitToStandard
	
	CHARGE_START( "command/charge_start" ), // StartCharging
	CHARGE_STOP( "command/charge_stop" ), // StopCharging

	;

	
	private final String strUrlSuffix;
	
	
	private Command( final String strUrlSuffix ) {
		this.strUrlSuffix = strUrlSuffix;
	}
	
	public String getUrlSuffix() {
		return this.strUrlSuffix;
	}
	

	public static Command getCommand( final String str ) {
		if ( null==str ) return null;
		if ( str.isEmpty() ) return null;
		
		final String strName = str.trim().toUpperCase();
		final String strSuffix = str.trim().toLowerCase();
		
		for ( final Command command : Command.values() ) {
			if ( strName.startsWith( command.name() ) ) {
				return command;
			}
			if ( strSuffix.startsWith( command.getUrlSuffix() ) ) {
				return command;
			}
		}
		
		return null;
	}


}
