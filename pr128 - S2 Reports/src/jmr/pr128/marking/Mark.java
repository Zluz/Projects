package jmr.pr128.marking;

public enum Mark {

	NORMAL(    "color: #000000" ),
	HIGHLIGHT( "color: #000000" ),
	CRITICAL(  "color: #802020" ),
	SUPPRESS(  "color: #B0B0B0" ),
	;
	
	private String strHtmlStyle;
	
	Mark( final String strColor ) {
		this.strHtmlStyle = strColor;
	}
	
	public String getHtmlStyle() {
		return this.strHtmlStyle;
	}
}
