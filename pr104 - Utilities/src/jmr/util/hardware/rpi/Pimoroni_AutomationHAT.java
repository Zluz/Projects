package jmr.util.hardware.rpi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.util.MonitorProcess;
import jmr.util.OSUtil;
import jmr.util.hardware.HardwareInput;
import jmr.util.hardware.HardwareOutput;
import jmr.util.math.NormalizedFloat;
import jmr.util.transform.JsonUtils;

public class Pimoroni_AutomationHAT {


	private final static Logger 
			LOGGER = Logger.getLogger( Pimoroni_AutomationHAT.class.getName() );
	
	
	public static enum Port {

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
		OUT_D_1( '4' ),
		OUT_D_2( '5' ),
		OUT_D_3( '6' ),
		
		// relay outputs
		OUT_R_1( '1' ),
		OUT_R_2( '2' ),
		OUT_R_3( '3' ),

		;
		
		final char cCommIndex;
		
		Port( final char cCommIndex ) {
			this.cCommIndex = cCommIndex;
		}
		
		Port() {
			this.cCommIndex = 0;
		}
		
		public static Port getPortFor( final String value ) {
			if ( null==value ) return null;
			
			final String strNorm = value.trim().toUpperCase();
			for ( final Port port : Port.values() ) {
				if ( strNorm.equals( port.name() ) ) {
					return port;
				}
			}
			return null;
		}
		
		public boolean isInput() {
			return this.name().startsWith( "IN_" );
		}
		
		public boolean isAnalog() {
			return this.name().contains( "_A_" );
		}

		public boolean isRelay() {
			return this.name().contains( "_R_" );
		}
	};
	

	private final static JsonParser PARSER = new JsonParser();

	public final static String[] COMMAND_STREAM_INPUT = {
//			"/bin/sh",
//			"/bin/bash",
//			"/Local/scripts/exec_automationhat_input.sh",
			"/usr/bin/python",
			"/Local/scripts/exec_automationhat_input.py",
		};

	
	private final EnumMap<Port,HardwareInput> 
								mapInputs = new EnumMap<>( Port.class );
	private final EnumMap<Port,HardwareOutput> 
								mapOutputs = new EnumMap<>( Port.class );
	
	private final Map<Port,Runnable> listTriggers = new HashMap<>();

	
	private JsonElement jeLast = null;
	
	private final MonitorProcess mp;
	
	private final EnumMap<Port,Boolean> 
				mapDigitalInput = new EnumMap<>( Port.class );
	//TODO create a project for math functions/util, split from pr127
	private final EnumMap<Port,jmr.util.math.NormalizedFloat> 
				mapAnalogInput = new EnumMap<>( Port.class );
	
	private final EnumMap<Port,Boolean> 
				mapDigitalOutput = new EnumMap<>( Port.class );
	private final EnumMap<Port,Float> 
				mapAnalogOutput = new EnumMap<>( Port.class );
	
	
	private final String strCommFile;
	
	
	
	private static Pimoroni_AutomationHAT instance = null;
	
	
	private Pimoroni_AutomationHAT() {
		if ( !OSUtil.isWin() ) {
			
			strCommFile = "/tmp/" + UUID.randomUUID().toString() + ".txt";
			
			final String[] command = new String[3];
			command[0] = COMMAND_STREAM_INPUT[0]; // shell
			command[1] = COMMAND_STREAM_INPUT[1]; // script
			command[2] = strCommFile;
			
			mp = new MonitorProcess( 
						"Monitor Automation HAT", command );
			mp.start();
			mp.addRunnable( new Runnable() {
				@Override
				public void run() {
					updateData();
				}
			} );
		} else {
			mp = null;
			strCommFile = null;
		}
	};
	
	
	public void initialize( final Map<String,String> map ) {
		if ( null==map ) return;
		for ( final Entry<String, String> entry : map.entrySet() ) {
			final String strKey = entry.getKey().trim().toUpperCase();
			final Port port = Port.getPortFor( strKey );
			if ( null!=port ) {
				final String strValue = entry.getValue();
				final HardwareInput 
							input = HardwareInput.getValueFor( strValue );
				final HardwareOutput 
							output = HardwareOutput.getValueFor( strValue );
				if ( null==input && null==output ) {
					LOGGER.severe( ()-> 
							"Name not recognized as input or output: "
							+ "\"" + strValue + "\", port will not be mapped." );
				} else if ( null!=input && null!=output ) {
					// this should never happen. 
					// maybe typo in HardwareInput or HardwareOutput
					LOGGER.severe( ()-> 
							"Name found as both input AND output: "
							+ "\"" + strValue + "\". "
							+ "Please check HardwareInput and HardwareOutput." );
				} else if ( null!=input ) {
					mapInputs.put( port, input );
					System.out.println( "Registering input " 
								+ port.name() + " as " + input.name() );
				} else {
					mapOutputs.put( port, output );
					System.out.println( "Registering output " 
							+ port.name() + " as " + output.name() );
				}
			}
		}
	}
	
	public static synchronized Pimoroni_AutomationHAT get() {
		if ( null==instance ) {
			instance = new Pimoroni_AutomationHAT();
		}
		return instance;
	}

	
	public boolean isActive() {
		if ( null==mp ) return false;
		if ( null==jeLast ) return false;
		return true;
	}

	private void updateDigitalInput( 	final Port port,
										final boolean bNewValue ) {
		final Boolean bOrigValue = mapDigitalInput.get( port );
		if ( null!=bOrigValue && bOrigValue.booleanValue() != bNewValue ) {
			
			mapDigitalInput.put( port, bNewValue );
			checkRunTrigger( port );
		} else {
			mapDigitalInput.put( port, bNewValue );
		}
	}
	

	private NormalizedFloat getAnalogInputData( final Port port ) {
		if ( null==port ) return null;
		
		if ( ! mapAnalogInput.containsKey( port ) ) {
			final NormalizedFloat nf = new NormalizedFloat( 4, 1, 1 );
			mapAnalogInput.put( port, nf );
			return nf;
		} else {
			final NormalizedFloat nf = mapAnalogInput.get( port );
			return nf;
		}
	}
	
	
	private void updateAnalogInput( final Port port,
									final float fNewValue ) {
		if ( null==port ) return;

		final HardwareInput input = this.getHardwareInputForPort( port );
		if ( null==input ) return;

//		final Float fOrigValue = mapAnalogInput.get( port );
//		mapAnalogInput.put( port, fNewValue );
		
		final NormalizedFloat nf = getAnalogInputData( port );
		final Double dOrigNorm = nf.evaluate();
		
		nf.add( fNewValue );

		if ( null!=dOrigNorm ) {

			final Float fOrigValue = new Float( dOrigNorm );
			final Double dNewNorm = nf.evaluate();
			
//			System.out.println( "--- updateAnalogInput() " 
//					+ this.hashCode() + ", "
//					+ "port: " + port.name() + ", "
//					+ "value/raw: " + fNewValue + ", "
//					+ "value/norm: " + dNewNorm //+ ", "
////					+ "map: " + JsonUtils.report( mapAnalogInput )
//					);
			

			if ( null==dNewNorm ) {
				System.out.println( "\tIncomplete data, no normalized value." );
				return;
			}
			
//System.out.println( "--- updateAnalogInput() " );			
			
			final float fOld = fOrigValue.floatValue();
//			final float fNew = fNewValue; // fNewValue.floatValue();
			final float fNew = dNewNorm.floatValue();
			final float fDiff = fNew - fOld;

//System.out.println( "--- updateAnalogInput()"
//				+ "\n\tfOld = " + fOld 
//				+ "\n\tfNew = " + fNew 
//				+ "\n\tfDiff = " + fDiff );			

			final float fPctDiff = fDiff * 100 / fOld;

			if ( ( fOld > ANALOG_MIN_VALUE ) 
					&& ( fPctDiff > ANALOG_TRIGGER_THRESHOLD ) ) {
				
System.out.println( "--- updateAnalogInput()"
			+ "\n\tport = " + port.name() 
			+ "\n\tinput = " + input 
			+ "\n\tfOrigValue = " + fOrigValue 
			+ "\n\tfNewValue/raw  = " + fNewValue 
			+ "\n\tfNewValue/norm = " + dNewNorm 
			+ "\n\tfOld = " + fOld 
			+ "\n\tfNew = " + fNew 
			+ "\n\tfDiff    = " + fDiff			
			+ "\n\tfPctDiff = " + fPctDiff			
				);			
				
System.out.println( "--- updateAnalogInput(), "
		+ "map: " + JsonUtils.report( mapAnalogInput ) );


//if (1==1) return; //FIXME disable posting this for now
/*
 * need to find out why fNew is always about 2x fOld, always triggering
 * 
 * --- updateAnalogInput()
	port = IN_A_1
	input = VEH_SPACE_1_RANGE_DOWN
	fOrigValue = 2.18
	fNewValue  = 4.22
	fOld = 2.18
	fNew = 4.22
	fDiff    = 2.0399997
	fPctDiff = 93.577965
 */

				checkRunTrigger( port );
			}
		}
	}
	
	
	final static double ANALOG_TRIGGER_THRESHOLD = 20.0;
	final static double ANALOG_MIN_VALUE = 1.0;
	
	
	
	private void checkRunTrigger( final Port port ) {
		if ( null==port ) return;
		
		if ( this.listTriggers.containsKey( port ) ) {
			final Runnable runnable = this.listTriggers.get( port );
			final Thread thread = new Thread( 
					"Input event: " + port.name() ) {
				@Override
				public void run() {
					runnable.run();
				}
			};
			thread.start();
		}
	}
	
	public Port getPortForInput( final HardwareInput input ) {
		for ( final Port port : Port.values() ) {
			if ( mapInputs.get( port ) == input ) {
				return port;
			}
		}
		return null;
	}

	public HardwareInput getHardwareInputForPort( final Port port ) {
		final HardwareInput input = mapInputs.get( port );
		return input;
	}

	public HardwareOutput getHardwareOutputForPort( final Port port ) {
		final HardwareOutput output = mapOutputs.get( port );
		return output;
	}
	
	public void registerChangeExec(	final HardwareInput input,
									final Runnable runnable ) {
		final Port port = getPortForInput( input );
		this.listTriggers.put( port, runnable );
	}

	public void registerChangeExec(	final Port port,
									final Runnable runnable ) {
		this.listTriggers.put( port, runnable );
	}

	private JsonElement updateData() {
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
				
				synchronized ( mapDigitalInput ) {
					updateDigitalInput( Port.IN_D_1, 1==joD.get( "one" ).getAsInt() );
					updateDigitalInput( Port.IN_D_2, 1==joD.get( "two" ).getAsInt() );
					updateDigitalInput( Port.IN_D_3, 1==joD.get( "three" ).getAsInt() );
				}
				
				synchronized ( mapAnalogInput ) {
					
//					System.out.println( "Updating to data: " + joA.toString() );
					
					updateAnalogInput( Port.IN_A_1, joA.get( "one" ).getAsFloat() );
					updateAnalogInput( Port.IN_A_2, joA.get( "two" ).getAsFloat() );
					updateAnalogInput( Port.IN_A_3, joA.get( "three" ).getAsFloat() );
					updateAnalogInput( Port.IN_A_4, joA.get( "four" ).getAsFloat() );
				}
				
//				for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {
//					System.out.println( entry.getKey() + " = " + entry.getValue().toString() );
//				}
			} catch ( final Exception e ) {
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

	
	/**
	 * Get the value associated with this (either input or output) 
	 * digital port. 
	 * @param port
	 * @return
	 */
	public Boolean getDigitalPortValue( final Port port ) {
		synchronized ( mapDigitalInput ) {
			if ( mapDigitalInput.containsKey( port ) ) {
				final Boolean value = mapDigitalInput.get( port );
				return value;
			}
		}
		synchronized ( mapDigitalOutput ) {
			if ( mapDigitalOutput.containsKey( port ) ) {
				final Boolean value = mapDigitalOutput.get( port );
				return value;
			}
		}
		return null;
	}

	
	public Float getAnalogPortValue( final Port port ) {
//		synchronized ( mapAnalogInput ) {
//			if ( mapAnalogInput.containsKey( port ) ) {
//				final Float value = mapAnalogInput.get( port );
//				return value;
//			}
//		}
		
		if ( port.isInput() ) {
			final NormalizedFloat nf = this.getAnalogInputData( port );
			final Double dInValue = nf.evaluate();
			if ( null!=dInValue ) {
				return new Float( dInValue );
			} else {
				return null;
			}
			
		} else { // output
			synchronized ( mapAnalogOutput ) {
				if ( mapAnalogOutput.containsKey( port ) ) {
					final Float value = mapAnalogOutput.get( port );
					return value;
				}
			}
		}
		return null;
	}
	
	
	public void setPortValue(	final Port port,
								final boolean bOn ) {
		if ( null==port ) return;
		final char cCommPort = port.cCommIndex;
		if ( 0==cCommPort ) return;
		
		/*
		mp.write( Character.toString( cCommPort ) );
		if ( bOn ) {
			mp.write( "+" );
		} else {
			mp.write( "-" );
		}
		*/
		
		final String strCommand =
				Character.toString( cCommPort )
				+ ( bOn ? "+" : "-" );
		
		try {
			final Path path = Paths.get( this.strCommFile );
			Files.write( path, strCommand.getBytes(), StandardOpenOption.CREATE );

			synchronized ( mapDigitalOutput ) {
				this.mapDigitalOutput.put( port, bOn );
			}
			
		} catch ( final IOException e ) {
			e.printStackTrace();

			synchronized ( mapDigitalOutput ) {
				this.mapDigitalOutput.remove( port );
			}
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
