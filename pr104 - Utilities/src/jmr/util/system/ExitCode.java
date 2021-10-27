package jmr.util.system;

public enum ExitCode {
	
	UNSPECIFIED( 100 ),
	DATABASE_CONNECTION_LOST( 120 ),
	UI_EXCESSIVE_LATENCY( 130 ),
	
	;
	
	final int iCode;
	
	ExitCode( final int iCode ) {
		this.iCode = iCode;
	}

}
