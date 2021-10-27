package jmr.util.transform;

public class Temperature {

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
	
	

	public static void main( final String[] args ) {}

}
