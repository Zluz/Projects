package jmr.pr102.comm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class JsonUtils {

	

	public static Map<String,String> transformJsonToMap( 
										final String str ) {
		final JsonElement element = new JsonParser().parse( str );
		return transformJsonToMap( element.getAsJsonObject() );
	}
		
	
	public static Map<String,String> transformJsonToMap( 
										final JsonObject details ) {
		
        final Map<String,String> map = new HashMap<>();
        for ( final Entry<String, JsonElement> entry : details.entrySet() ) {
        	
        	final String key = entry.getKey();
        	final JsonElement value = entry.getValue();
        	
        	map.put(key, transformJsonToElement( value ) );
        }
        
        return map;
	}


	public static String transformJsonToElement( final JsonElement element ) {

		if ( element.isJsonObject() ) {
			return element.getAsString();

		} else if ( element.isJsonArray() ) {
			return element.getAsJsonArray().toString();
//			return element.getAsString();

		} else if ( element.isJsonNull() ) {
			return "null";
			
		} else {
			final String str = element.getAsString();
			return str;
		}
	}
	

	public static Map<String,String> transformJsonToMap(
											final JsonArray array ) {

		final Map<String,String> map = new HashMap<>();
		
		for ( int i=0; i<array.size(); i++ ) {
			
			final String key = Integer.toString( i );
			final JsonElement value = array.get( i );

			map.put(key, transformJsonToElement( value ) );
		}

		return map;
	}

	

	public static String report( final Object obj ) {
		if ( null==obj ) return "null";
		
		if ( obj instanceof Map ) {
			@SuppressWarnings( "unchecked" )
			final Map<String,Object> map = (Map<String,Object>)obj;
			return reportMap( map );
		} else {
			return obj.toString();
		}
	}
	
	
	public static String reportMap( final Map<String,Object> map ) {

		String str = "Map:[\n";
		for ( Entry<String, Object> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final Object objValue = entry.getValue();
			
			str += "\t" + strKey + "=\"" + report( objValue ) + "\"\n";
		}
		str += "]";
		return str;
	}
	
	public static void print( final Object obj ) {
		System.out.println( "\tReport: " + report( obj ) );
	}
	

	
}
