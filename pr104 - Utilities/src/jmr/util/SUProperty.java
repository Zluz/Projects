package jmr.util;

public enum SUProperty {

	TESLA_USERNAME( "teslamotors.username" ),
	TESLA_PASSWORD( "teslamotors.password" ),
	
	S2DB_CONNECTION( "s2db.connection" ),
	S2DB_USERNAME( "s2db.username" ),
	S2DB_PASSWORD( "s2db.password" ),
	S2DB_SCHEMA( "s2db.schema" ),
	
	;
	
	private String strName;
	
	private SUProperty( final String strName ) {
		this.strName = strName;
	}
	
	public String getName() {
		return this.strName;
	}
	
}
