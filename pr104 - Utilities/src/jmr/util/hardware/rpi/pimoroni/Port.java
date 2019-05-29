package jmr.util.hardware.rpi.pimoroni;


public enum Port {


	// digital inputs
	IN_D_1,
	IN_D_2,
	IN_D_3,
	
	// analog inputs
	IN_A_1,
	IN_A_2,
	IN_A_3,
	IN_A_4, // available?
	
	// digital outputs
	OUT_D_1( '4' ),
	OUT_D_2( '5' ),
	OUT_D_3( '6' ),
	
	// relay outputs
	OUT_R_1( '1' ),
	OUT_R_2( '2' ),
	OUT_R_3( '3' ),

	;
	
	final char cCommIndex;
	
	Port( final char cCommIndex ) {
		this.cCommIndex = cCommIndex;
	}
	
	Port() {
		this.cCommIndex = 0;
	}
	
	public static Port getPortFor( final String value ) {
		if ( null==value ) return null;
		
		final String strNorm = value.trim().toUpperCase();
		for ( final Port port : Port.values() ) {
			if ( strNorm.equals( port.name() ) ) {
				return port;
			}
		}
		return null;
	}
	
	public boolean isInput() {
		return this.name().startsWith( "IN_" );
	}
	
	public boolean isAnalog() {
		return this.name().contains( "_A_" );
	}

	public boolean isRelay() {
		return this.name().contains( "_R_" );
	}
	
}
