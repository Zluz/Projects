package jmr.pr116.messaging;

public enum EmailProvider {

	GMAIL_API(	 "smtp.gmail.com", "587" ),
	
	@Deprecated
	GMAIL_JAVAX( "smtp.gmail.com", "587" ),
	
	;
	
	private String strHost;
	private String strPort;
	
	EmailProvider(	final String strHost,
					final String strPort ) {
		this.strHost = strHost;
		this.strPort = strPort;
	}
	
	public String getHost() {
		return this.strHost;
	}
	
	public String getPort() {
		return this.strPort;
	}
	
}
