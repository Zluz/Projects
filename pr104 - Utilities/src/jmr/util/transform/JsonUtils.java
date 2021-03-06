package jmr.util.transform;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class JsonUtils {

	public final static JsonParser PARSER = new JsonParser();
	
	public final static Gson GSON_PRETTY = 
						new GsonBuilder().setPrettyPrinting().create();
	

	public static Map<String,String> transformJsonToMap_( 
										final String str ) {
		final JsonElement element = PARSER.parse( str );
		return transformJsonToMap( element.getAsJsonObject() );
	}
		

//	final static private Gson GSON = new Gson();
	final static private Gson GSON;
	
	static {
		final GsonBuilder gb = new GsonBuilder();
		GSON = gb.serializeSpecialFloatingPointValues().create();
	}
	
	

	public static Map<String,Object> transformJsonToMap( 
										final String strJson ) {
		final Type type = new TypeToken<Map<String, String>>(){}.getType();
//		try {
			final Map<String, Object> map = GSON.fromJson( strJson, type);
			return map;
//		} catch ( final JsonSyntaxException e ) {
//			final Map<String,Object> map = Collections.emptyMap();
//			return map;
//		}
	}

	
	public static Map<String,String> transformJsonToMap( 
										final JsonObject details ) {
		
        final Map<String,String> map = new HashMap<>();
        for ( final Entry<String, JsonElement> entry : details.entrySet() ) {
        	
        	final String key = entry.getKey();
        	final JsonElement value = entry.getValue();
        	
        	map.put(key, transformJsonToElement( value ) );
//        	map.put(key, value. );
        }
        
        return map;
	}


	public static String transformJsonToElement( final JsonElement element ) {

		if ( element.isJsonObject() ) {
//			return element.getAsString();
			return element.toString();

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
			try {
				@SuppressWarnings( "unchecked" )
				final Map<String,Object> map = (Map<String,Object>)obj;
				return reportMap( map );
			} catch ( final ClassCastException e ) {
				// just fall back to object
			}
		}
//		final Gson gson = new Gson();
//		final GsonBuilder gb = new GsonBuilder();
//		final Gson gson = gb.serializeSpecialFloatingPointValues().create();
		return GSON.toJson( obj );
	}
	
	
	public static String reportMap( final Map<String,Object> map ) {
		String str = "Map:[\n";
		final List<String> list = new LinkedList<>( map.keySet() );
		Collections.sort( list );
		for ( final String strKey : list ) {
			final Object objValue = map.get( strKey );
			
			str += "\t" + strKey + "=" + report( objValue ) + "\n";
		}
		str += "]";
		return str;
	}
	
	public static void print( final Object obj ) {
		System.out.println( "\tReport: " + report( obj ) );
	}
	
	
	public static String getJsonValue( final String strInput, 
									   final String strPath ) {
		if ( StringUtils.isBlank( strInput ) ) return "";
		if ( StringUtils.isBlank( strPath ) ) return "";
		
		final DocumentContext jc = JsonPath.parse( strInput );
		final String strValue = jc.read( strPath );
		if ( null!=strValue ) {
			return strValue;
		} else {
			return "";
		}
	}
	
	
	public static String getPretty( final JsonElement je ) {
		if ( null==je ) return "<null, JsonElement was null>";
		if ( je instanceof JsonObject ) {
			final JsonObject jo = je.getAsJsonObject();
			final StringBuilder sb = new StringBuilder();
			if ( 0==jo.size() ) {
				sb.append( "{}" );
			} else {
				sb.append( "{\n" );
				final List<String> list = new LinkedList<>();
				for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {
					list.add( entry.getKey() );
				}
				Collections.sort( list );
				boolean bFirst = true;
				for ( final String strKey : list ) {
					if ( bFirst ) {
						bFirst = false;
					} else {
						sb.append( ",\n" );
					}
					sb.append( "  \"" + strKey + "\": " );
					sb.append( GSON_PRETTY.toJson( jo.get( strKey ) ) );
				}
				sb.append( "\n}" );
			}
			return sb.toString();
		} else {
			try {
				final String strPretty = GSON_PRETTY.toJson( je );
				if ( null==strPretty ) {
					return "<null, from toJson()>";
				} else if ( strPretty.isEmpty() ) {
					return "<empty, from toJson()>";
				} else {
					return strPretty;
				}
			} catch ( final Exception e ) {
				return "<" + e.toString() + ">";
			}
		}
	}

	public static String getPretty( final String str ) {
		if ( null==str ) return "<null>";
		final JsonElement je = PARSER.parse( str );
		return getPretty( je );
	}



	public static JsonObject getJsonObjectFor( final String strResult ) {

		if ( null!=strResult ) {
			try {
				final JsonElement je = PARSER.parse( strResult );
				if ( je.isJsonObject() ) {
					return je.getAsJsonObject();
				}
			} catch ( final Exception e ) {
				// just return a blank JsonObject, below
			}
		}
		
		return new JsonObject();
	}

	
	public static JsonElement getJsonElementFor( final String strResult ) {
		if ( null!=strResult ) {
			try {
				final JsonElement je = PARSER.parse( strResult );
				return je;
			} catch ( final Exception e ) {
				// just return a blank JsonObject, below
			}
		}
		return JsonNull.INSTANCE;
	}
	
	
	public static void main( final String[] args ) {
//		final String strLessPretty = "";
		final String strExampleChargeState = "{\"response\":{\"charging_state\":\"Stopped\",\"fast_charger_type\":\"ACSingleWireCAN\",\"fast_charger_brand\":\"\\u003cinvalid\\u003e\",\"charge_limit_soc\":84,\"charge_limit_soc_std\":90,\"charge_limit_soc_min\":50,\"charge_limit_soc_max\":100,\"charge_to_max_range\":false,\"max_range_charge_counter\":0,\"fast_charger_present\":false,\"battery_range\":132.22,\"est_battery_range\":92.85,\"ideal_battery_range\":165.13,\"battery_level\":53,\"usable_battery_level\":53,\"charge_energy_added\":0.0,\"charge_miles_added_rated\":0.0,\"charge_miles_added_ideal\":0.0,\"charger_voltage\":null,\"charger_pilot_current\":null,\"charger_actual_current\":null,\"charger_power\":null,\"time_to_full_charge\":0.0,\"trip_charging\":null,\"charge_rate\":0.0,\"charge_port_door_open\":null,\"conn_charge_cable\":\"\\u003cinvalid\\u003e\",\"scheduled_charging_start_time\":null,\"scheduled_charging_pending\":false,\"user_charge_enable_request\":false,\"charge_enable_request\":false,\"charger_phases\":null,\"charge_port_latch\":\"\\u003cinvalid\\u003e\",\"charge_current_request\":48,\"charge_current_request_max\":48,\"managed_charging_active\":false,\"managed_charging_user_canceled\":false,\"managed_charging_start_time\":null,\"battery_heater_on\":false,\"not_enough_power_to_heat\":false,\"timestamp\":1521924536395}}";
		final String strMorePretty = JsonUtils.getPretty( strExampleChargeState );
		System.out.println( strMorePretty );
	}
	
}
