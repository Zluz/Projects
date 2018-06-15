package jmr.pr121.servlets;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public enum ParameterName {

	REQUEST_URL,
	REQUEST_BASE,
	NAME,
	BUTTON,
	EMAIL,
	CLIENT_INFO,
	;
	
	public static ParameterName getParameterName( final String text ) {
		if ( StringUtils.isEmpty( text ) ) {
			return null;
		}
		
		final String strNorm = text.trim().toUpperCase();
		for ( final ParameterName param : ParameterName.values() ) {
			if ( strNorm.equals( param.name() ) ) {
				return param;
			}
		}
		
		return null;
	}
	
	public static Map<ParameterName,String>
			getEnumMapOf( final Set<Entry<String, String[]>> entries ) {

		final EnumMap<ParameterName,String> 
				mapParams = new EnumMap<>( ParameterName.class );
		
		for ( final Entry<String, String[]> entry : entries ) {
			final String strName = entry.getKey();
			final String[] strValues = entry.getValue();
			final ParameterName param = getParameterName( strName );
			if ( null!=param ) {
				final String strValue = String.join( "\n", strValues );
				mapParams.put( param, strValue );
			}
		}
		return mapParams;
	}
	
}
