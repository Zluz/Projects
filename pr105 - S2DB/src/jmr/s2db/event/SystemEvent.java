package jmr.s2db.event;

public enum SystemEvent {

	CLIENT_REGISTERED,
	CLIENT_EXIT,
	
	DEVICE_INFO,
	DEVICE_STARTED,
	DEVICE_PROBLEM,
	DEVICE_SHUTDOWN,
	
	HEARTBEAT_HOUR,
	HEARTBEAT_DAY,
	
	GENERAL_MESSAGE,
	GENERAL_ERROR,
	GENERAL_WARNING,
	
	TEST_SYSTEM_EVENT,
	;
	
	public static SystemEvent getSystemEvent( final String str ) {
		if ( null==str ) return null;
		final String strNorm = str.trim().toUpperCase();
		for ( final SystemEvent event : SystemEvent.values() ) {
			if ( event.name().equals( strNorm ) ) {
				return event;
			}
		}
		return null;
	}
}
