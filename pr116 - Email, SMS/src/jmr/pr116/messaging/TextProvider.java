package jmr.pr116.messaging;

public enum TextProvider {
	
	VERIZON( "vzwpiz.com" ),
	;
	
	private String strHost;
	
	TextProvider( final String strHost ) {
		this.strHost = strHost;
	}
	
	public String getHost() {
		return this.strHost;
	}

}
