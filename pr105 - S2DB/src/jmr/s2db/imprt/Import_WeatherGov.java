package jmr.s2db.imprt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

//TODO this probably belongs in pr107
public class Import_WeatherGov extends ImportBase {

	@Override
	public String getURL() {
		return "https://api.weather.gov/gridpoints/LWX/95,87/forecast";
	}

	
	public static String getIconFilenameFrom( final String strURL ) {
		if ( null == strURL ) return "";
		
		// ex:  https://api.weather.gov/icons/
		//					land/day/rain_showers,40?size\u003dmedium
		String strIcon = strURL.trim().toLowerCase();
		strIcon = StringUtils.substringAfterLast( strIcon, "/" );
//		strIcon = StringUtils.substringAfterLast( strIcon, "/land/" );
		strIcon = StringUtils.substringBefore( strIcon, "," );
		strIcon = StringUtils.substringBefore( strIcon, "?" );
		return strIcon;
	}
	
	
	public static String getNamePrefix( final int i ) {
		final String strNamePrefix = String.format( "period_%02d", i );
		return strNamePrefix;
	}
	
	
	@Override
	public Map<String, String> getJsonPaths() {
		final Map<String,String> map = new HashMap<>();

		map.put( "update_time", 
						"$['properties'].['updated']" );
		map.put( "coordinates", 
						"$['geometry'].['geometries']"
								+ ".[?(@.type == 'Point')].['coordinates']" );
		map.put( "elevation", 
						"$['properties'].['elevation'].['value']" );
		
		
		for ( int i = 0; i < 20; i++ ) {
			
			final String strNamePrefix = getNamePrefix( i ); 
			final String strPathPrefix = 
					"$['properties'].['periods'].[?(@.number == " + i + ")]";
			
			map.put( strNamePrefix + ".name", 
									strPathPrefix + ".['name']" );
			map.put( strNamePrefix + ".daytime", 
									strPathPrefix + ".['isDaytime']" );
			map.put( strNamePrefix + ".time_start", 
									strPathPrefix + ".['startTime']" );
			map.put( strNamePrefix + ".time_end", 
									strPathPrefix + ".['endTime']" );
			
			map.put( strNamePrefix + ".temperature", 
									strPathPrefix + ".['temperature']" );
			
			map.put( strNamePrefix + ".wind_speed", 
									strPathPrefix + ".['windSpeed']" );
			map.put( strNamePrefix + ".wind_direction", 
									strPathPrefix + ".['windDirection']" );
			
			map.put( strNamePrefix + ".icon_url", 
									strPathPrefix + ".['icon']" );
			map.put( strNamePrefix + ".forecast_short", 
									strPathPrefix + ".['shortForecast']" );
			map.put( strNamePrefix + ".forecast_detailed", 
									strPathPrefix + ".['detailedForecast']" );
		}
		
		return map;
	}
	
	
	@Override
	public Map<String, String> summarize( final Map<String, String> mapInput ) {
		
		final Map<String,String> map = new HashMap<>();
		map.putAll( mapInput );
		map.putAll( super.summarize( mapInput ) );
		
		for ( int i = 0; i < 20; i++ ) {
			final String strPrefix = getNamePrefix( i );
			final String strSourceKey = strPrefix + ".icon_url";
			if ( map.containsKey( strSourceKey ) ) {
				final String strSourceValue = map.get( strSourceKey );
				final String strIconFile = getIconFilenameFrom( strSourceValue );
				final String strTargetKey = strPrefix + ".icon_file";
				map.put( strTargetKey, strIconFile ); 
			}
		}
		
		return map;
	}

}
