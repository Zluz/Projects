package jmr.s2db.trigger;



/*
 * trigger types:
		I - Hardware input port (digital or analog)
		O - Hardware output port (digital or analog)
		P - Page updated (may be imported content)
		T - Time event
		U - User input event (pressing a button)
 */
public enum TriggerType {
	
	INPUT,
	OUTPUT,
	PAGE,
	TIME,
	USER,
	;
	
	public char getChar() {
		return this.name().charAt( 0 );
	}
	
	public static TriggerType getTriggerEventFor( final char c ) {
		for ( final TriggerType state : TriggerType.values() ) {
			if ( c==state.getChar() ) {
				return state;
			}
		}
		return null;
	}
}
