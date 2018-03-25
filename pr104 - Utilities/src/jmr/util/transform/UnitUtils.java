package jmr.util.transform;

public abstract class UnitUtils {

	public static double getFahrenheitFromCelsius( final double dTempC ) {
		final double dTempF = 9.0 / 5.0 * dTempC + 32;
		return dTempF;
	}
	
	public static double getCelsiusFromFahrenheit( final double dTempF ) {
		final double dTempC = 5.0 / 9.0 * ( dTempF - 32 );
		return dTempC;
	}
	
	
}
