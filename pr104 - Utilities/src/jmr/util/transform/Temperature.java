package jmr.util.transform;


/**
 * Temperature (and Wind) utilities..
 */
public class Temperature {


	public enum CardinalDirection {
		North, East, South, West
	}
	
	
	
	
	public static double 
	getFahrenheitFromCelsius( final double dCelcius ) {
		return dCelcius * 1.8 + 32.0;
	}

	
	public static double 
	getFahrenheitFromCelsius( final String strCelsius ) {
		try {
			final double dCelsius = Double.parseDouble( strCelsius );
			final double dFahrenheit = getFahrenheitFromCelsius( dCelsius );
			return dFahrenheit;
		} catch ( final NumberFormatException e ) {
			return Double.NaN;
		}
	}

	
	public static double 
	getCelsiusFromFahrenheit( final double dFahrenheit ) {
		return ( dFahrenheit - 32.0 ) / 1.8;
	}
	
	
	
	public static String
	getLongCardinalDirection( final String strDirAbbr ) {
		if ( null == strDirAbbr ) return "?";
		if ( strDirAbbr.isEmpty() ) return "?";
		
		final String strNorm = strDirAbbr.trim().toUpperCase();

		for ( final CardinalDirection dir : CardinalDirection.values() ) {
			if ( dir.name().startsWith( strNorm ) ) {
				return dir.name();
			}
		}
		return strDirAbbr;
	}
	
	

	public static void main( final String[] args ) {}

}
