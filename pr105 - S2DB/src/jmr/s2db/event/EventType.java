package jmr.s2db.event;



/*
 * trigger types [subject in square brackets]:
		I - [HardwareInput] Hardware input port (digital or analog) 
		O - [HardwareOutput] Hardware output port (digital or analog) 
		P - (page name?) Page updated (may be imported content)
		E - (name?) External data loaded (imported content or picture taken)
		T - (schedule?) Time event
		U - (control name?) User input event (pressing a button)
		S - [SystemEvent] System event
 */
public enum EventType {
	
	INPUT,
	OUTPUT,
	PAGE,
	EXTERNAL,
	TIME,
	USER,
	ENVIRONMENT,
	SYSTEM,
	;
	
	public char getChar() {
		return this.name().charAt( 0 );
	}
	
	public static EventType getTriggerEventFor( final char c ) {
		for ( final EventType state : EventType.values() ) {
			if ( c==state.getChar() ) {
				return state;
			}
		}
		return null;
	}
}
