package jmr.util;

public enum SUProperty {

	TESLA_USERNAME( "teslamotors.username" ),
	TESLA_PASSWORD( "teslamotors.password" ),
	
	NEST_USERNAME( "nest.username" ),
	NEST_PASSWORD( "nest.password" ),
	
	CONTROL_EMAIL_USERNAME( "email.sender.address" ),
	CONTROL_EMAIL_PASSWORD( "email.sender.password" ),
	CONTROL_EMAIL_PROVIDER( "email.sender.provider" ),
	
	S2DB_CONNECTION( "s2db.connection" ),
	S2DB_USERNAME( "s2db.username" ),
	S2DB_PASSWORD( "s2db.password" ),
	S2DB_SCHEMA( "s2db.schema" ),
	
	GAE_URL( "gae.url" ),
	GAE_USERNAME( "gae.username" ),
	GAE_PASSWORD( "gae.password" ),
	GAE_USER_PRE( "gae.user" ),
	
	GCS_BUCKET( "gcs.bucket" ),
	
	BROWSER_ACCEPT_001( "browser_accept.001" ),
	BROWSER_ACCEPT_PRE( "browser_accept" ),

	UPDATE_TIME( "update.time" ),
	UPDATE_SOURCE( "update.source" ),

	;
	
	private String strName;
	
	private SUProperty( final String strName ) {
		this.strName = strName;
	}
	
	public String getName() {
		return this.strName;
	}
	
}
