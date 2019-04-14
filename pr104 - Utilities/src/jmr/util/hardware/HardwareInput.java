package jmr.util.hardware;

public enum HardwareInput {

	TEST_DIGITAL_INPUT_1,
	TEST_DIGITAL_INPUT_2,
	TEST_DIGITAL_INPUT_3,

	TEST_ANALOG_INPUT_1( true ),
	TEST_ANALOG_INPUT_2( true ),
	TEST_ANALOG_INPUT_3( true ),

	
	VEH_SPACE_1_RANGE_DOWN( true ),

	
	GARAGE_PED_DOOR_CLOSED_STOP,
	
	GARAGE_VEH_DOOR_1_OPEN_STOP,
	GARAGE_VEH_DOOR_1_OPEN_AWAY,
	GARAGE_VEH_DOOR_1_CLOSED_STOP,
	GARAGE_VEH_DOOR_1_CLOSED_AWAY,

	GARAGE_VEH_DOOR_2_OPEN_STOP,
	GARAGE_VEH_DOOR_2_OPEN_AWAY,
	GARAGE_VEH_DOOR_2_CLOSED_STOP,
	GARAGE_VEH_DOOR_2_CLOSED_AWAY,

	GARAGE_VEH_DOOR_3_OPEN_STOP,
	GARAGE_VEH_DOOR_3_OPEN_AWAY,
	GARAGE_VEH_DOOR_3_CLOSED_STOP,
	GARAGE_VEH_DOOR_3_CLOSED_AWAY,
	
	SUMP_WATER_LEVEL( true ),
	
	HOME_WATER_PRESSURE( true ),
	
	POWER_HPWC( true ),
	POWER_AUX2C_OUTLET( true ),
	
	L_POWER_THEATER_RECEIVER,
	L_POWER_THEATER_PROJECTOR,
	
	L_POWER_WELL_PUMP,
	L_POWER_HOT_WATER,
	L_POWER_HVAC_BASEMENT,
	L_POWER_HVAC_UPSTAIRS,
	L_POWER_SUMP_PUMP,
	L_POWER_LAUNDY_WASHER,
	L_POWER_LAUNDY_DRYER,
	
	/*
	 * format:
	 *  <[<L|A>_]<type>>_<input>[_<state>]
	 * 
	 *  prefix:
	 * 	    L/A - logical or analog input
	 *  type:
	 *      POWER
	 *      VEH_DOOR
	 *      PED_DOOR
	 *		..  
	 *  state:
	 *  	STOP
	 *  	AWAY
	 *  	..
	 */
	;

	private final boolean bAnalog;
	
	HardwareInput( final boolean bAnalog ) {
		this.bAnalog = bAnalog;
	}
	
	HardwareInput() {
		this( false );
	}
	
	public boolean isAnalog() {
		return this.bAnalog;
	}
	
	public static HardwareInput getValueFor( final String value ) {
		if ( null==value ) return null;
		
		final String strNorm = value.trim().toUpperCase();
		for ( final HardwareInput hi : HardwareInput.values() ) {
			if ( strNorm.equals( hi.name() ) ) {
				return hi;
			}
		}
		
		return null;
	}
	
}
