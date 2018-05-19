package jmr.pr122;

public enum DocKey {

	TESLA_COMBINED,
	NEST,
	CONFIG,
	;
	
	public boolean bJSON;
	
	DocKey() {
		bJSON = true;
	}
	
	DocKey( final boolean bJSON ) {
		this.bJSON = bJSON;
	}
	
	public boolean isJSON() {
		return this.bJSON;
	}
}
