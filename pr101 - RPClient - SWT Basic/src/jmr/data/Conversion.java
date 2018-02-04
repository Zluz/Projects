package jmr.data;

public class Conversion {
	
	public static int getIntFromString(	final String strValue,
										final int iDefault ) {
		if ( null==strValue ) return iDefault;
		if ( strValue.isEmpty() ) return iDefault;
		try {
			final int iValue = Integer.parseInt( strValue );
			return iValue;
		} catch ( final NumberFormatException e ) {
			return iDefault;
		}
	}
	
	public static int getIntFromString( final String strValue ) {
		return getIntFromString( strValue, Integer.MAX_VALUE );
	}
	
	public static Long getMaxLongFromStrings( final String... strings ) {
		if ( null==strings ) return null;
		if ( 0==strings.length ) return null;
		Long lMax = null;
		for ( final String strValue : strings ) {
			if ( null!=strValue && !strValue.isEmpty() ) {
				try {
					final long lValue = Long.parseLong( strValue );
					if ( null==lMax || lValue>lMax ) {
						lMax = lValue;
					}
				} catch ( final NumberFormatException e ) {
					// just pass
				}
			}
		}
		return lMax;
	}

}
