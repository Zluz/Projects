package jmr.util.hardware;

public enum HardwareOutput {

	TEST_PORT_1,
	TEST_PORT_2,
	TEST_PORT_3,
	
	GENERIC_RELAY_1,
	GENERIC_RELAY_2,
	GENERIC_RELAY_3,
	
	GENERIC_DIGITAL_OUT_1,
	GENERIC_DIGITAL_OUT_2,
	GENERIC_DIGITAL_OUT_3,
	

	GARAGE_VEH_DOOR_1_ACTIVATE,
	GARAGE_VEH_DOOR_2_ACTIVATE,
	GARAGE_VEH_DOOR_3_ACTIVATE,

	GARAGE_PARK_ASSIST_1,
	GARAGE_PARK_ASSIST_2,
	GARAGE_PARK_ASSIST_3,
	
	GARAGE_FAST_LIGHTS,
	
	ATTIC_AUDIO_PULSE,
	
	/** power the network audio encoder. Normally-ON, but can cycle to reset.*/
	AUDIO_BROADCAST_POWER_ENCODER,
	
	/** power the Blackvue cameras. (Normally-ON, but OFF in some cases) */ 
	TESLA_BLACKVUE_POWER,
	/** power the Tesla RPi with various sensors */
	TESLA_SENSOR_RPI,
	
	/** force/electronic override to send HDMI video on Tesla MCU */
	TESLA_FORCE_HDMI,
	/** select alternate video source to display (on Tesla MCU) */
	TESLA_VIDEO_ALTERNATE,
	
	;

	public static HardwareOutput getValueFor( final String value ) {
		if ( null==value ) return null;
		
		final String strNorm = value.trim().toUpperCase();
		for ( final HardwareOutput hi : HardwareOutput.values() ) {
			if ( strNorm.equals( hi.name() ) ) {
				return hi;
			}
		}
		
		return null;
	}
	
}
