package jmr.util.hardware;

public enum HardwareOutput {

	TEST_PORT_1,
	TEST_PORT_2,
	TEST_PORT_3,
	
	

	GARAGE_VEH_DOOR_1_ACTIVATE,
	GARAGE_VEH_DOOR_2_ACTIVATE,
	GARAGE_VEH_DOOR_3_ACTIVATE,

	GARAGE_PARK_ASSIST_1,
	GARAGE_PARK_ASSIST_2,
	GARAGE_PARK_ASSIST_3,
	
	GARAGE_FAST_LIGHTS,
	
	ATTIC_AUDIO_PULSE,
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
