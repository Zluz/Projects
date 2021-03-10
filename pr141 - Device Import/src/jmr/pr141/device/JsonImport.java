package jmr.pr141.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import jmr.pr141.device.Device.TextProperty;

public class JsonImport {
	

	public final static Gson GSON = new Gson();

	
	final public static String NULL_INDICATOR = "-"; 
		

	private static String getJsonMember( final JsonObject jo,
								  		 final String strKey ) {
		if ( null == jo ) return NULL_INDICATOR;
		if ( ! jo.has( strKey ) ) return NULL_INDICATOR;
		final JsonElement je = jo.get( strKey );
		if ( null == je ) return NULL_INDICATOR;
		if ( je.isJsonNull() ) return NULL_INDICATOR;
		final String strValue = je.getAsString();
		if ( null == strValue ) return NULL_INDICATOR;
		return strValue;
		
	}
	
	
	public static Device importDeviceFromJSON( final File file ) 
													throws IOException {
		final Path path = file.toPath();
		final BufferedReader br = Files.newBufferedReader( path );
		
		final JsonObject jo = GSON.fromJson( br, JsonObject.class );
		if ( null == jo ) return null;
//		if ( 0 == jo.size() ) return null;
		
		if ( ! jo.has( "tac-list" ) ) {
			return null;
		}
		
		final JsonArray jaTACs = jo.getAsJsonArray( "tac-list" );
		
		final Type listType = new TypeToken<List<Long>>(){}.getType();
		final List<Long> listTACs = GSON.fromJson( jaTACs.toString(), listType);

		final Device device = new Device( listTACs );
		
		device.set(	TextProperty.MARKETING_NAME,
				getJsonMember( jo, "MARKETINGNAME" ) );
		device.set(	TextProperty.MODEL_NAME,
				getJsonMember( jo, "MODELNAME" ) );
		
		device.loadCharacteristics( 
				jo.get( "NETWORKCHARACTERISTICS" ).getAsString() );
		device.loadCharacteristics( 
				jo.get( "WDSNETWORKCHARACTERISTICS" ).getAsString() );

		final String strSimCount = jo.get( "SIMCOUNT" ).getAsString();
		try {
			final Integer iSimCount = Integer.valueOf( strSimCount );
			device.iSimCount = iSimCount;
		} catch ( NumberFormatException e ) {
			device.iSimCount = null;
		}
		
//		device.strImageBase64 = getJsonMember( jo, "image-thumbnail-binary" );
		device.set(	TextProperty.IMAGE_BASE64,
							getJsonMember( jo, "image-thumbnail-binary" ) );
		
		return device;
	}
	
	

}
