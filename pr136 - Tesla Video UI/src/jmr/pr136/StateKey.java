package jmr.pr136;

import jmr.util.hardware.rpi.pimoroni.Port;

public enum StateKey {

	// hardware: Automation HAT (HA)
	
	HA_IN_D1( "vD", Port.IN_D_1 ),
	HA_IN_D2( "vD", Port.IN_D_2 ),
	HA_IN_D3( "vD", Port.IN_D_3 ),
	
	HA_IN_A1( "VA", Port.IN_A_1 ),
	HA_IN_A2( "VA", Port.IN_A_2 ),
	HA_IN_A3( "VA", Port.IN_A_3 ),
	
	HA_OUT_D1( "vD", Port.OUT_D_1 ),
	HA_OUT_D2( "vD", Port.OUT_D_2 ),
	HA_OUT_D3( "vD", Port.OUT_D_3 ),
	
	HA_OUT_R1( "VD", Port.OUT_R_1 ),
	HA_OUT_R2( "VD", Port.OUT_R_2 ),
	HA_OUT_R3( "VD", Port.OUT_R_3 ),
	
	// server application (software-server)
	
	SS_INPUT( "VX" ),
	SS_FPS( "VA" ),
	SS_JAR( "VX" ),
	
	SS_IP_LAN( "VX" ),
	SS_IP_WIFI( "VX" ),
	SS_LAST_URL( "VX" ),
	
	SS_HOME_NET( "VD" ),
	
	// client information (software-overhead)
	
	SO_IP( "VX" ),
	SO_IN_1( "VD" ),
	SO_IN_2( "VD" ),
	
	SO_KEY_RATE( "VA" ),
	SO_IMG_RATE( "VA" ),
	
	SO_IMG_KEY( "VX" ),
	SO_IMG_LAST( "VA" ),  // seconds since last image request
	SO_KEY_LAST( "VX" ),  // seconds since last key request
	
	;
	
	final boolean bVisible;
	final char cType;
	final Port port;
	
	StateKey( 	final boolean bVisible,
				final char cType,
				final Port port ) {
		this.bVisible = bVisible;
		this.cType = cType;
		this.port = port;
	}

	StateKey( 	final String strOptions, 
				final Port port ) {

		this.port = port;
		
		if ( strOptions.contains( "V" ) ) {
			this.bVisible = true;
		} else if ( strOptions.contains( "v" ) ) {
			this.bVisible = false;
		} else {
			throw new IllegalStateException( "Visibility not specified." );
		}

		if ( strOptions.contains( "A" ) ) {
			this.cType = 'A'; // analog
		} else if ( strOptions.contains( "D" ) ) {
			this.cType = 'D'; // digital
		} else if ( strOptions.contains( "C" ) ) {
			this.cType = 'C'; // character
		} else if ( strOptions.contains( "S" ) ) {
			this.cType = 'S'; // short string
		} else if ( strOptions.contains( "X" ) ) {
			this.cType = 'X'; // long string
		} else {
			throw new IllegalStateException( "Type not specified." );
		}
	}
	
	StateKey( final String strOptions ) {
		this( strOptions, null );
	}

	StateKey() {
		this( "Vd" );
	}
	
	public Port getPort() {
		return this.port;
	}
	
	public char getType() {
		return this.cType;
	}
	
	public boolean isAnalog() {
		return 'A' == this.cType;
	}
	
	public boolean isVisible() {
		return this.bVisible;
	}
	
	
	public String asString( final Object objValue ) {
		if ( null == objValue ) {
			return "-";
		} else if ( 'A' == this.cType ) {
			try {
				final String strValue = String.format( "%06.3f", objValue );
				return strValue;
			} catch ( final Exception e ) {
				return objValue.toString();
			}
		} else {
			//TODO expand on this..
			return objValue.toString();
		}
	}
	
}
