package jmr.pr140;

public enum Domain {

	H( "HAZYSKY\\..+", false ),
	E( ".+FORGE\\..+", true ),
	C( "CORP\\..+\\.G.+", true ),
	UNKNOWN,
	;
	
	final String strRegex;
	final boolean bRecommendMouseJitter;
	
	Domain( final String strRegex,
			final boolean bRecommendMouseJitter ) {
		this.strRegex = strRegex;
		this.bRecommendMouseJitter = bRecommendMouseJitter;
	}
	
	Domain() {
		this( null, false );
	}
	
	
	public static Domain getDomain() {
		final String strEnvVar = System.getenv( "USERDNSDOMAIN" );
		if ( null != strEnvVar ) {
			final String strEVNorm = strEnvVar.trim().toUpperCase();
			for ( final Domain domain : Domain.values() ) {
				if ( strEVNorm.matches( domain.strRegex ) ) {
					return domain;
				}
			}
		}
		return Domain.UNKNOWN;
	}
	
	public boolean getRecommendedMouseJitter() {
		return this.bRecommendMouseJitter;
	}
	
	public static void main( final String[] args ) {
		System.out.println( "Domain: " + getDomain().name() );
	}
	
}
