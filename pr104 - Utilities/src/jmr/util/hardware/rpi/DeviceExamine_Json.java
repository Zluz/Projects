package jmr.util.hardware.rpi;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class DeviceExamine_Json {
	
	public static enum Key {
		CPU_THROTTLE( "cpu-throttle" ),
		CPU_TEMPERATURE( "cpu-temperature" ),
		SENSOR_TEMPERATURE_VALUE( "sensor-temperature-value" ),
		SENSOR_HUMIDITY_VALUE( "sensor-humidity-value" ),
		;
		
		public final String strKey;
		
		Key( final String strKey ) {
			this.strKey = strKey;
		}
	}

	private final static String DATA_FILENAME = "/tmp/device_examine.out";

//	private final static JsonParser PARSER = new JsonParser();
	private final static Gson GSON = new Gson();

	private static DeviceExamine_Json instance;
	
	private JsonObject joData;
	private long lLastLastModified;
	
	
	public static synchronized DeviceExamine_Json get() {
		if ( null==instance ) {
			instance = new DeviceExamine_Json();
		}
		return instance;
	}
	
	
	private void readFile() {
		final File file = new File( DATA_FILENAME );
		if ( ! file.isFile() ) return;
		
		final long lThisLastModified = file.lastModified();
		if ( lLastLastModified == lThisLastModified ) return;
		
		try {
			lLastLastModified = lThisLastModified;
			
			final FileReader fr = new FileReader( DATA_FILENAME );
			
//			final JsonElement je = PARSER.parse( fr );
//			joData = je.getAsJsonObject();
			
			final JsonReader jr = new JsonReader( fr );
			jr.setLenient( true );
			
			jr.beginObject();
//			final Gson gson = new Gson();
//			joData = GSON.fromJson( jr, JsonObject.class );
			final JsonElement je = GSON.fromJson( jr, JsonElement.class );
			joData = je.getAsJsonObject();
			
		} catch ( final JsonParseException | IllegalStateException e ) {
			System.out.println( 
						e.toString() + " encountered in DeviceExamine." );
		} catch ( final IOException e ) {
			// just ignore, may not be accessible for valid reasons.
		}
	}
	
	
	public String getValue( final String strName ) {
		this.readFile();
		if ( null==joData ) return null;
		
		final JsonElement je = joData.get( strName );
		if ( null!=je ) {
			return je.getAsString();
		} else {
			return null;
		}
	}
	
	public String getValue( final Key key ) {
		return this.getValue( key.strKey );
	}
	
	
}
