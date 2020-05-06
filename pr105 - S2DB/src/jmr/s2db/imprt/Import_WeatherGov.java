package jmr.s2db.imprt;

import java.util.HashMap;
import java.util.Map;

public class Import_WeatherGov extends ImportBase {

	@Override
	public String getURL() {
		return "https://api.weather.gov/gridpoints/LWX/95,87/forecast";
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
			
			final String strNamePrefix = String.format( "period_%02d", i ); 
			final String strPathPrefix = 
					"$['properties'].['periods'].[?(@.number == " + i + ")]";
			
			map.put( strNamePrefix + ".name", 
									strPathPrefix + ".['name']" );
			map.put( strNamePrefix + ".daytime", 
									strPathPrefix + ".['isDaytime']" );
			map.put( strNamePrefix + ".temperature", 
									strPathPrefix + ".['temperature']" );
			map.put( strNamePrefix + ".wind_speed", 
									strPathPrefix + ".['windSpeed']" );
			map.put( strNamePrefix + ".icon", 
									strPathPrefix + ".['icon']" );
			map.put( strNamePrefix + ".forecast_short", 
									strPathPrefix + ".['shortForecast']" );
			map.put( strNamePrefix + ".forecast_detailed", 
									strPathPrefix + ".['detailedForecast']" );
		}
		
		return map;
	}

}
