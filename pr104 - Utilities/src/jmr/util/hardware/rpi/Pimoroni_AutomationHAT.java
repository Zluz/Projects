package jmr.util.hardware.rpi;

import java.util.EnumMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.util.MonitorProcess;
import jmr.util.OSUtil;

public class Pimoroni_AutomationHAT {

	public static enum Ports {

		// digital inputs
		IN_D_1,
		IN_D_2,
		IN_D_3,
		
		// analog inputs
		IN_A_1,
		IN_A_2,
		IN_A_3,
		IN_A_4, // available?
		
		// digital outputs
		OUT_D_1,
		OUT_D_2,
		OUT_D_3,
		OUT_D_4,
		
		// relay outputs
		OUT_R_1,
		OUT_R_2,
		OUT_R_3,

		;
	};
	

	private final static JsonParser PARSER = new JsonParser();

	public final static String[] COMMAND_STREAM_INPUT = {
//			"/bin/sh",
			"/usr/bin/python",
			"/Local/scripts/exec_automationhat_input.py",
		};

	
	private JsonElement jeLast;
	
	private final MonitorProcess mp;
	
	public final EnumMap<Ports,Boolean> 
				mapDigitalInput = new EnumMap<Ports,Boolean>( Ports.class );
	public final EnumMap<Ports,Float> 
				mapAnalogInput = new EnumMap<Ports,Float>( Ports.class );
	
	
	
	private static Pimoroni_AutomationHAT instance = null;
	
	private Pimoroni_AutomationHAT() {
		if ( !OSUtil.isWin() ) {
			mp = new MonitorProcess( 
						"Monitor Automation HAT", COMMAND_STREAM_INPUT );
			mp.start();
		} else {
			mp = null;
		}
	};
	
	
	public static Pimoroni_AutomationHAT get() {
		if ( null==instance ) {
			instance = new Pimoroni_AutomationHAT();
		}
		return instance;
	}


	public JsonElement updateData() {
		if ( null==mp ) return null;
		
		// [{"three": 0, "two": 0, "one": 0}, {"four": 0.53, "three": 0.03, "two": 0.03, "one": 0.03}]
		
		final JsonElement je;
		
		final String strLine = mp.getLatestLine();
//		System.out.println( strLine );
		if ( null==strLine || strLine.isEmpty() 
								|| !strLine.contains( "\"two\":" ) ) {
			je = null;
		} else {
			try {
				je = PARSER.parse( strLine );
				
//				System.out.println( je.toString() );
				
				final JsonArray ja = je.getAsJsonArray();
				
				final JsonObject joD = ja.get( 0 ).getAsJsonObject();
				final JsonObject joA = ja.get( 1 ).getAsJsonObject();
				
				mapDigitalInput.put( Ports.IN_D_1, 1==joD.get( "one" ).getAsInt() );
				mapDigitalInput.put( Ports.IN_D_2, 1==joD.get( "two" ).getAsInt() );
				mapDigitalInput.put( Ports.IN_D_3, 1==joD.get( "three" ).getAsInt() );
				
				mapAnalogInput.put( Ports.IN_A_1, joA.get( "one" ).getAsFloat() );
				mapAnalogInput.put( Ports.IN_A_2, joA.get( "two" ).getAsFloat() );
				mapAnalogInput.put( Ports.IN_A_3, joA.get( "three" ).getAsFloat() );
				mapAnalogInput.put( Ports.IN_A_4, joA.get( "four" ).getAsFloat() );
				
//				for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {
//					System.out.println( entry.getKey() + " = " + entry.getValue().toString() );
//				}
			} catch ( final IllegalStateException e ) {
				// may report line as a not a JSON Object
				// just ignore, return null this time, do not update joLast
				System.out.println( "Exception encountered while parsing JSON" );
				System.out.println( "Exception: " + e.toString() );
				System.out.println( "JSON: " + strLine );
				return null;
			}
		}

		this.jeLast = je;
		return this.jeLast;
	}

	public void close() {
		mp.close();
	}
	
	
	public static void main( final String[] args ) throws InterruptedException {
		
//		CPUMonitor.get();
//		
//		for (;;) {
//			Thread.sleep( 100 );
//			System.out.println( CPUMonitor.get().getLastLine() );
//		}
		
//		final String line = "{\"temp\":\"temp=63.4'C\",\"core\":\"volt=1.2313V\",\"sdram_c\":\"volt=1.2000V\",\"sdram_i\":\"volt=1.2000V\",\"sdram_p\":\"volt=1.2250V\"}";
		final String line = "[{\"three\": 0, \"two\": 0, \"one\": 0}, {\"four\": 0.53, \"three\": 0.03, \"two\": 0.03, \"one\": 0.03}]"; 
//		final String line = "";
		
		/*
				{"temp":"temp=63.4'C","core":"volt=1.2313V","sdram_c":"volt=1.2000V","sdram_i":"volt=1.2000V","sdram_p":"volt=1.2250V"}
				temp = "temp=63.4'C"
				core = "volt=1.2313V"
				sdram_c = "volt=1.2000V"
				sdram_i = "volt=1.2000V"
				sdram_p = "volt=1.2250V"
		 */

//		final Gson gson = new Gson();
//		gson.getp
		final JsonElement je = PARSER.parse( line );
		
		System.out.println( je.toString() );
		final JsonArray ja = je.getAsJsonArray();
		final JsonObject jo = ja.get( 0 ).getAsJsonObject();
		for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {
			System.out.println( entry.getKey() + " = " + entry.getValue().toString() );
		}
	}
	
	
}
