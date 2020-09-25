package jmr.pr138;

public enum JitterFrequency {

	OFF,
	_60,
	_120,
	_300,
	_600,
	;
	
	public long getDuration() {
		final String strName = this.name();
		try {
			final long lDuration = Long.parseLong( strName.substring( 1 ) );
			return lDuration;
		} catch ( final NumberFormatException e ) {
			return 0;
		}
	}
	
	public String getText() {
		final long lDuration = this.getDuration();
		if ( lDuration > 0 ) {
			return Long.toString( lDuration ) + " seconds"; 
		} else {
			return "Off";
		}
	}
	
}
