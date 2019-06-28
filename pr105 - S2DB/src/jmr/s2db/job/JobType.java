package jmr.s2db.job;

import java.util.HashMap;
import java.util.Map;

public enum JobType {

	TESLA_READ,
	TESLA_WRITE,
	
	REMOTE_EXECUTE( true ),
	REMOTE_OUTPUT( true ),
	REMOTE_SHUTDOWN( true ),
	REMOTE_GET_CALL_STACK( true ),
	
	PROCESSING_IMAGE_DIFF,
	
	SEND_EMAIL,
	SEND_TEXT,
	
	UNDEFINED,
	;

	final boolean bRemote;
	
	JobType( final boolean bRemote ) {
		this.bRemote = bRemote;
	}

	JobType() {
		this( false );
	}
	
	public boolean isRemoteType() {
		return this.bRemote;
	}
	

	public static JobType getType( final String text ) {
		if ( null==text ) return UNDEFINED;
		if ( text.isEmpty() ) return UNDEFINED;
		
		final String strUpper = text.trim().toUpperCase();
		for ( final JobType type : JobType.values() ) {
			if ( strUpper.startsWith( type.name() ) ) {
				return type;
			}
		}
		return UNDEFINED;
	}


	public static Map<String,String> getDetails( final String text ) {
		final Map<String,String> map = new HashMap<>();
		map.put( "", "" );
		if ( null==text ) return map;
		if ( text.isEmpty() ) return map;

		for ( final String line : text.split( "\\\\" ) ) {
			if ( line.contains( "=" ) ) {
				final String[] parts = line.split( "=" );
				map.put( parts[0], parts[1] );
			} else {
				map.put( "", line );
			}
		}
		return map;
	}
	
}
