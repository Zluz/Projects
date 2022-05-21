package jmr.util.hardware.rpi;

import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.util.MonitorProcess;
import jmr.util.OSUtil;

public class CPUMonitor {


	private final static JsonParser PARSER = new JsonParser();

	public final static String[] COMMAND_MONITOR_CPU_RPI = {
			"/bin/sh",
			"/Local/scripts/exec_cpu_monitor.sh",
		};

	public final static String[] COMMAND_MONITOR_CPU_WIN = {
			"C:\\WINDOWS\\system32\\cmd.exe /c \"dir /s c:\\\"",
//			"C:\\PROGRA~1\\7-Zip\\7z.exe";
			""
		};

	public final static double WARNING_TEMP_HIGH = 80.0;
	
	
	
	private JsonObject joLast;
	
	private final MonitorProcess mp;
	
	
	
	private static CPUMonitor instance = null;
	
	private CPUMonitor() {
		final String[] arrCommand = OSUtil.isWin() 
				? COMMAND_MONITOR_CPU_WIN : COMMAND_MONITOR_CPU_RPI;
		
		if ( ! OSUtil.isWin() ) {
			mp = new MonitorProcess( "Monitor CPU temperature", arrCommand );
			mp.start();
		} else {
			mp = null;
		}
	};
	
	
	public static CPUMonitor get() {
		if ( null==instance ) {
			instance = new CPUMonitor();
		}
		return instance;
	}


	public JsonObject updateData() {
		if ( null==mp ) return null;
		
		// {"temp":"temp=63.4'C","core":"volt=1.2313V","sdram_c":"volt=1.2000V","sdram_i":"volt=1.2000V","sdram_p":"volt=1.2250V"}
		
		final JsonObject jo;
		
		final String strLine = mp.getLatestLine();
		if ( null==strLine || strLine.isEmpty() || !strLine.contains( ":\"temp=" ) ) {
			jo = null;
		} else {
			final JsonElement je = PARSER.parse( strLine );
			jo = je.getAsJsonObject();
		}

		this.joLast = jo;
		return this.joLast;
	}


	public Double getTemperature() {
		
		final JsonObject jo = updateData();
		if ( null==jo ) return null;
		if ( !jo.has( "temp" ) ) return null;
		
		// "temp=63.4'C"
		final String strLine = jo.get( "temp" ).toString();
		final String[] arrLines = strLine.split( "=" );
		if ( 2 == arrLines.length ) {
			final String strValue = arrLines[1].split( "'" )[0];
			try {
				final double dTemp = Double.parseDouble( strValue );
				return dTemp;
			} catch ( final NumberFormatException e ) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	
	public boolean isHeatWarning() {
		final Double dTemp = getTemperature();
		if ( null != dTemp ) {
			return ( dTemp > WARNING_TEMP_HIGH );
		} else {
			return false;
		}
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
