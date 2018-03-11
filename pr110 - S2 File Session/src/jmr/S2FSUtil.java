package jmr;

public abstract class S2FSUtil {

	public static String normalizeMAC( final String strInput ) {
		if ( null==strInput ) return null;
		final String strTrimmed = strInput.trim().toUpperCase();
		
		if ( 17==strTrimmed.length() ) {
			final String strExpanded = "" 
					+ strTrimmed.substring( 0, 2 ) + "-"
					+ strTrimmed.substring( 3, 5 ) + "-"
					+ strTrimmed.substring( 6, 8 ) + "-"
					+ strTrimmed.substring( 9, 11 ) + "-"
					+ strTrimmed.substring( 12, 14 ) + "-"
					+ strTrimmed.substring( 15, 17 );
			return strExpanded;
		} else if ( 12==strTrimmed.length() ) {
			final String strExpanded = "" 
					+ strTrimmed.substring( 0, 2 ) + "-"
					+ strTrimmed.substring( 2, 4 ) + "-"
					+ strTrimmed.substring( 4, 6 ) + "-"
					+ strTrimmed.substring( 6, 8 ) + "-"
					+ strTrimmed.substring( 8, 10 ) + "-"
					+ strTrimmed.substring( 10, 12 );
			return strExpanded;
		} else {
			return null;
		}
	}
	
	
	
	
	public static void main(String[] args) {
		
		final String str01 = "B8-27-EB-E6-6A-EC";
		System.out.println( "\t" + normalizeMAC( str01 ) + "\t" + str01 );
		
		final String str02 = "b8-27-eb-E6-6A-EC";
		System.out.println( "\t" + normalizeMAC( str02 ) + "\t" + str02 );
		
		final String str03 = "\t \t B8-27-EB-E6-6A-EC \t \t ";
		System.out.println( "\t" + normalizeMAC( str03 ) + "\t" + str03 );
		
		final String str04 = "B8.27.EB_E6 6A-EC";
						//    12345678901234567
		System.out.println( "\t" + normalizeMAC( str04 ) + "\t" + str04 );
		
		final String str05 = "B827EBE66AEC";
						//    123456789012
		System.out.println( "\t" + normalizeMAC( str05 ) + "\t" + str05 );
		
	}

}
