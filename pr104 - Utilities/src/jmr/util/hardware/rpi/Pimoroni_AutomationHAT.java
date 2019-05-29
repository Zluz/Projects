package jmr.util.hardware.rpi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.util.MonitorProcess;
import jmr.util.OSUtil;
import jmr.util.hardware.HardwareInput;
import jmr.util.hardware.HardwareOutput;
import jmr.util.math.FunctionBase;
import jmr.util.math.FunctionParameter;
import jmr.util.math.NormalizedFloat;
import jmr.util.report.TraceMap;

public class Pimoroni_AutomationHAT {

	private static final double VOLTS_LOGICAL_ON = 1.5;


	private final static boolean DEBUG = false;
	

	private final static Logger 
			LOGGER = Logger.getLogger( Pimoroni_AutomationHAT.class.getName() );


	/**
	 * trigger on difference to last event
	 */
	final private static double ANALOG_THRESHOLD_DRIFT = 0.05;
	
	/**
	 * trigger on immediate percent change
	 */
	@SuppressWarnings("unused")
	final private static double ANALOG_THRESHOLD_PCT_DIFF = 10.0;
	
	/**
	 * minimum analog low (sanity check)
	 */
	final private static double ANALOG_MIN_VALUE = 0.1;
	
	
	
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
			"-u",
			"-O",
			"/Local/scripts/exec_automationhat_input.py",
		};



	protected abstract static class PortInterface {
		// fixed ?
		final public Port port;
		final public String strParameters;
//		public Listener listener;
		
		public PortInterface( final Port port, 
							  final String strParameters ) {
			this.port = port;
			this.strParameters = strParameters;
		}
	}

	protected static class OutputDigitalInterface extends PortInterface {
		public boolean bValue;
		public OutputDigitalInterface( final Port port ) {
			super( port, null );
		}
	}

	protected abstract static class InputInterface extends PortInterface {
		
		public boolean bLogical;
		
		public InputInterface( final Port port,
							   final String strParameters ) {
			super( port, strParameters );
		}
	}
	
	protected static class InputDigitalInterface extends InputInterface {
		public InputDigitalInterface( final Port port ) {
			super( port, null );
		}

		public Boolean bValue;
	}

	protected static class InputAnalogInterface extends InputInterface {
		public InputAnalogInterface( final Port port,
									 final String strParameters ) {
			super( port, strParameters );
		}
		
		public NormalizedFloat nfValue;
	}

	
	
	public final static EnumMap<Port,PortInterface> 
								mapInterface = new EnumMap<>( Port.class );
	
	//TODO use a class for all these Map<Port,*> fields (Map<Port,class>) 
	private final EnumMap<Port,HardwareInput> 
								mapInputs = new EnumMap<>( Port.class );
	private final EnumMap<Port,HardwareOutput> 
								mapOutputs = new EnumMap<>( Port.class );
//	private final EnumMap<Port,String> 
//								mapParameters = new EnumMap<>( Port.class );
//	private final EnumMap<Port,Boolean> 
//								mapLogical = new EnumMap<>( Port.class );
	
	private final Map<Port,Listener> listListeners = new HashMap<>();

	public static interface Listener {
		public void inputTrigger( // final Map<String,Object> map,
								  final TraceMap map,
								  final long lTime );
	}
	
	private JsonElement jeLast = null;
	
	private final MonitorProcess mp;
	
//	private final static EnumMap<Port,Boolean> 
//				mapDigitalInput = new EnumMap<>( Port.class );
	//TODO create a project for math functions/util, split from pr127
//	private final static EnumMap<Port,jmr.util.math.NormalizedFloat> 
//				mapAnalogInput = new EnumMap<>( Port.class );
	
//	private final static EnumMap<Port,Boolean> 
//				mapDigitalOutput = new EnumMap<>( Port.class );
//	private final static EnumMap<Port,Float> 
//				mapAnalogOutput = new EnumMap<>( Port.class );
	
	
	private final static int POLLING_AVG_SAMPLES = 40;
	
	private final NormalizedFloat nfPollingInterval = 
					new NormalizedFloat( POLLING_AVG_SAMPLES, "seconds" );
	private long lLastPollingData;
	private double dAvgPollingInterval = 0;
	
	private final String strCommFile;
	
	
	
	private static Pimoroni_AutomationHAT instance = null;
	
	
	private Pimoroni_AutomationHAT() {
		if ( ! OSUtil.isWin() ) {
			
			strCommFile = "/tmp/" + UUID.randomUUID().toString() + ".txt";
			
			final List<String> listCommand = 
					new LinkedList<>( Arrays.asList( COMMAND_STREAM_INPUT ) );
			listCommand.add( strCommFile );
			
			mp = new MonitorProcess( 
							"Monitor Automation HAT", listCommand, false );
			mp.start();
			mp.addListener( new MonitorProcess.Listener() {
				@Override
				public void process( final long lTime, 
									 final String strLine ) {
//					System.out.print( ">" );
					updateData( lTime, strLine );
					
					final long lElapsed = lTime - lLastPollingData;
					nfPollingInterval.add( (float) lElapsed / 1000 );
					lLastPollingData = lTime;
					
					if ( 0==dAvgPollingInterval 
							&& nfPollingInterval.hasEnoughSamples() ) {
						final Double dAvg = nfPollingInterval.evaluate();
						if ( null != dAvg ) {
							dAvgPollingInterval = dAvg.doubleValue();
						}
					}
				}
			});
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
				
				System.out.println( "Initializing hardware port: " + port );
				
				final String[] strValues = entry.getValue().split( ":" );
				
				final String strHwName = strValues[0];
				final String strParameters;
				if ( strValues.length > 1 ) {
					strParameters = strValues[1];
				} else {
					strParameters = "";
				}

				final HardwareInput 
							input = HardwareInput.getValueFor( strHwName );
				final HardwareOutput 
							output = HardwareOutput.getValueFor( strHwName );

				final PortInterface pi;
				
				if ( null==input && null==output ) {
					pi = null;

					LOGGER.severe( ()-> 
							"Name not recognized as input or output: "
							+ "\"" + strHwName + "\", "
							+ "port will not be mapped." );
				} else if ( null!=input && null!=output ) {
					pi = null;

					// this should never happen. 
					// maybe typo in HardwareInput or HardwareOutput
					LOGGER.severe( ()-> 
							"Name found as both input AND output: "
							+ "\"" + strHwName + "\". "
							+ "Please check HardwareInput and HardwareOutput." );
				} else if ( null!=input ) {

					mapInputs.put( port, input );
					System.out.println( "Registering input " 
								+ port.name() + " as " + input.name() );
					
					if ( port.isAnalog() ) {
						pi = new InputAnalogInterface( port, strParameters );
					} else {
						pi = new InputDigitalInterface( port );
					}
					
				} else {
					pi = new OutputDigitalInterface( port );
					
					mapOutputs.put( port, output );
					System.out.println( "Registering output " 
								+ port.name() + " as " + output.name() );
				}
				
				if ( null != pi ) {
					mapInterface.put( port, pi );
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
										final boolean bNewValue,
										final TraceMap map,
										final long lTime ) {
//		final Boolean bOrigValue = mapDigitalInput.get( port );
		
		final PortInterface pi = mapInterface.get( port );
		final InputDigitalInterface input;
		if ( pi instanceof InputDigitalInterface ) {
			input = (InputDigitalInterface)pi;
		} else {
			LOGGER.severe( "Digital port cannot be updated: " 
					+ port.name() + ", registered as " + pi );
			return;
		}
		
		final boolean bRunCheck;
		
		if ( null == input.bValue ) { // set initial value
			input.bValue = bNewValue;
			bRunCheck = true;
			
		} else if ( input.bValue != bNewValue ) { // value has changed
			input.bValue = bNewValue;
			bRunCheck = true;
			
		} else { // value did not change
			bRunCheck = false;
		}
		
		if ( bRunCheck ) {
			final TraceMap tm;
			if ( null==map ) {
				tm = new TraceMap( true );
			} else {
				tm = map;
				tm.addFrame();
			}
			checkRunTrigger( port, tm, lTime );
		}
		
////		if ( null!=bOrigValue && bOrigValue.booleanValue() != bNewValue ) {
//		if ( null != input.bValue && input.bValue != bNewValue ) {
//			
////			mapDigitalInput.put( port, bNewValue );
//			input.bValue = bNewValue;
//			
////			final Map<String,Object> map = new HashMap<>();
////			final TraceMap map = new TraceMap();
////			map.put( "time-initiate", new Long( lTime ) );
////			map.put( "source-initiate", "PAHAT.updateDigitalInput()" );
//			checkRunTrigger( port, map, lTime );
//		} else {
////			mapDigitalInput.put( port, bNewValue );
//			input.bValue
//		}
	}
	

	private NormalizedFloat getAnalogInputData( final Port port, 
											    final boolean bCreate ) {
		if ( null==port ) return null;
		
		final PortInterface pi = mapInterface.get( port );
		if ( ! ( pi instanceof InputAnalogInterface ) ) {
			return null;
		}
		final InputAnalogInterface input = (InputAnalogInterface)pi;
		
//		if ( mapAnalogInput.containsKey( port ) ) {
		if ( null != input.nfValue ) {
//			final NormalizedFloat nf = mapAnalogInput.get( port );
			final NormalizedFloat nf = input.nfValue;
			return nf;
		} else if ( bCreate ) {

			System.out.println();
			System.out.println( "Initializing analog port statistical monitor" );
			
			
			int iSampleSize = 9;
			int iDropTop = 3;
			int iDropBottom = 3;
			double dIntercept = 0;
			double dMultiplier = 0;
			
			double fDriftThreshold = ANALOG_THRESHOLD_DRIFT;
			String strUnit = null;
			
//			if ( mapParameters.containsKey( port ) ) {
			if ( mapInterface.containsKey( port ) ) {
//				final String strParameters = mapParameters.get( port );
//				final PortInterface pi = mapInterface.get( port );
				final String strParameters = input.strParameters;
				
System.out.println( "port parameters: " + strParameters );
				final String[] strParams = strParameters.split( "," );
				if ( strParams.length >= 4 ) {
					try {
						iSampleSize = Integer.parseInt( strParams[0] );
//						iDropTop = Integer.parseInt( strParams[1] );
//						iDropBottom = Integer.parseInt( strParams[2] );
						dIntercept = Double.parseDouble( strParams[1] );
						dMultiplier = Double.parseDouble( strParams[2] );
						fDriftThreshold = Double.parseDouble( strParams[3] );
						strUnit = strParams[4];

						LOGGER.info( "Applied input "
								+ "parameters \"" + strParameters + "\"" );
					} catch ( final NumberFormatException e ) {
						LOGGER.severe( "Failed to process input "
								+ "parameters \"" + strParameters + "\"" );
					}
//				} else {
//					System.out.println( "Missing parameters, using defaults." );
				}
			}
			
			final NormalizedFloat nf;
			if ( StringUtils.isNotBlank( strUnit ) ) {
				nf = new NormalizedFloat( 
						iSampleSize, iDropTop, iDropBottom, strUnit );
			} else {
				nf = NormalizedFloat.INVALID;
			}
			
			nf.setParamDouble( FunctionParameter.TRIGGER_NORM_DRIFT_THRESHOLD, 
										fDriftThreshold );
			nf.setParamDouble( FunctionParameter.IRL_INTERCEPT, dIntercept );
			nf.setParamDouble( FunctionParameter.IRL_MULTIPLIER, dMultiplier );
//			mapAnalogInput.put( port, nf );
			input.nfValue = nf;
			
			final Boolean bIsLogical = 
					( 1.0 == dMultiplier && 0.0 == dIntercept 
					&& "volts".equals( strUnit ) );
//			mapLogical.put( port, bIsLogical );
			input.bLogical = bIsLogical;
			
			System.out.println( 
						"TRIGGER_NORM_DRIFT_THRESHOLD = " + fDriftThreshold );
			
			return nf;
		} else {
			return null;
		}
	}
	
	
	private void updateAnalogInput( final Port port,
									final float fNewValue,
									final TraceMap map,
									final long lTime ) {
		if ( null==port ) return;

		final HardwareInput hw = this.getHardwareInputForPort( port );
		if ( null==hw ) return;

		final PortInterface pi = mapInterface.get( port );
		if ( ! ( pi instanceof InputAnalogInterface ) ) {
			return;
		}
		final InputAnalogInterface input = (InputAnalogInterface)pi;

		
//		final Float fOrigValue = mapAnalogInput.get( port );
//		mapAnalogInput.put( port, fNewValue );
		
		final NormalizedFloat nf = getAnalogInputData( port, true );
		if ( ! nf.isValid() ) return;
		final Double dOrigNorm = nf.evaluate();
		
		nf.add( fNewValue );

		if ( null!=dOrigNorm ) {

			final Float fOrigValue = new Float( dOrigNorm );
			final Double dNewNorm = nf.evaluate();
			
			if ( DEBUG ) {
				System.out.print( "" 
						+ StringUtils.right( ""+System.currentTimeMillis(), 5 ) 
						+ " --- updateAnalogInput() " 
	//					+ this.hashCode() + ", "
						+ "port: " + port.name() + ", "
						+ "val/raw: " + String.format( "%.3f", fNewValue ) + ", "
						+ "val/norm: " + String.format( "%.5f", dNewNorm ) //+ ", "
	//					+ "map: " + JsonUtils.report( mapAnalogInput )
						);
			}
			

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

			
			boolean bPost = false;
			String strTriggerName = null;
			final Double dTriggerValue;
//			final Map<String,Object> map = new HashMap<>();
//			final TraceMap map = new TraceMap( false );
			final double dAdjust;
			final double dThreshold;
			
			final Double dLastPostValue = nf.getParamDouble( 
							FunctionParameter.VAR_VALUE_LAST_POSTED );
			if ( null!=dLastPostValue ) {
				
				final double dDiff = Math.abs( dLastPostValue - dNewNorm );
				
				if ( DEBUG ) {
					System.out.print( ", drift: " + String.format( "%.5f", dDiff ) );
				}

				final double dParam = nf.getParamDouble( 
							FunctionParameter.TRIGGER_NORM_DRIFT_THRESHOLD,
							ANALOG_THRESHOLD_DRIFT );
				final Double dLastPostTime = nf.getParamDouble( 
							FunctionParameter.VAR_TIME_LAST_POSTED );
				if ( null!=dLastPostTime ) {
					dAdjust = ( (double) lTime - dLastPostTime ) / 1000000000;
				} else {
					dAdjust = 0;
				}
				dThreshold = dParam - dAdjust;

				if ( DEBUG ) {
					System.out.print( 
						", adjusted(" + String.format( "%.5f", dParam ) + ")= " 
						+ String.format( "%.5f", dThreshold ) );
				}
				if ( dDiff > dThreshold ) {
					if ( DEBUG ) {
						System.out.print( " - HIT " );
					}

					bPost = true;
					map.addFrame();
					strTriggerName = "drift";
					
					dTriggerValue = dDiff;
				} else {
					dTriggerValue = null;
				}
			} else {
				bPost = true;
				map.addFrame();
				strTriggerName = "initialize";
				
				dTriggerValue = null;
				dAdjust = 0;
				dThreshold = 0;
			}
			
			final float fPctDiff = fDiff * 100 / fOld;
//			if ( fPctDiff > ANALOG_THRESHOLD_PCT_DIFF ) {
//				bPost = true;
//			}
//			System.out.print( ", pct-diff: " 
//									+ String.format( "%.5f", fPctDiff ) );
			
			if ( DEBUG ) {
				System.out.println();
			}

			if ( bPost && ( fOld > ANALOG_MIN_VALUE ) ) { 

				if ( null!=dTriggerValue ) {
					map.put( "trigger-value", dTriggerValue );
					map.put( "trigger-adjust", dAdjust );
					map.put( "trigger-threshold", dThreshold );
				}

				nf.setParamDouble( 
						FunctionParameter.VAR_VALUE_LAST_POSTED, dNewNorm );
				nf.setParamDouble( 
						FunctionParameter.VAR_TIME_LAST_POSTED, (double) lTime );

				map.put( "value-latest", fNewValue );
				map.put( "value-normalized", dNewNorm );
				map.put( "percent-diff", fPctDiff );
//				map.put( "time-initiate", lTime );
//				map.put( "source-initiate", "PAHAT.updateAnalogInput()" );

				map.put( "last-post-value", dLastPostValue );
//x				map.put( "last-post-elapsed", x ); //TODO continue..

				final Double dIntercept = 
						nf.getParamDouble( FunctionParameter.IRL_INTERCEPT );
				final Double dMultiplier = 
						nf.getParamDouble( FunctionParameter.IRL_MULTIPLIER );
				if ( null!=dIntercept && null!=dMultiplier ) {
					final double dIRLValue = 
									( dNewNorm + dIntercept ) * dMultiplier;
					map.put( "value-irl", dIRLValue );
					map.put( "value-unit", nf.getUnit() );
					
					nf.setParamDouble( 
							FunctionParameter.VAR_VALUE_IRL_LAST_POSTED, 
							dIRLValue );
				}

				if ( StringUtils.isNotBlank( strTriggerName ) ) {
					map.put( "trigger-name", strTriggerName );
				}
				if ( null != dTriggerValue ) {
					map.put( "trigger-value", dTriggerValue );
				}
				
//				if ( Boolean.TRUE.equals( mapLogical.get( port ) ) ) {
				if ( input.bLogical ) {
					final boolean bLogicalValue = dNewNorm > VOLTS_LOGICAL_ON;
					map.put( "value-logical", bLogicalValue );
				}

				if ( DEBUG ) {
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
				}
				
//System.out.println( "--- updateAnalogInput(), "
//		+ "map: " + JsonUtils.report( mapAnalogInput ) );


				checkRunTrigger( port, map, lTime );
			}
		}
	}
	
	
	public Double getAveragePollingInterval() {
		if ( dAvgPollingInterval > 0 ) {
			return dAvgPollingInterval;
		} else {
			return null;
		}
	}
	
	private void checkRunTrigger( final Port port,
//								  final Map<String,Object> map,
								  final TraceMap map,
								  final long lTime ) {
		if ( null==port ) return;
		
		final Double dInterval = nfPollingInterval.evaluate();
		if ( null != dInterval ) {
			dAvgPollingInterval = dInterval;
			map.put( "data-collection-interval", dAvgPollingInterval );
		}
		
		if ( this.listListeners.containsKey( port ) ) {
//		if ( this.mapInterface.containsKey( port ) ) {
			final Listener listener = this.listListeners.get( port );
//			final PortInterface pi = mapInterface.get( port );
//			final Listener listener = pi.listener;
			
//			listener.inputTrigger( null, null );
			if ( null!=listener ) {
				listener.inputTrigger( map, lTime );
			}
//			final Thread thread = new Thread( 
//					"Input event: " + port.name() ) {
//				@Override
//				public void run() {
//					runnable.run();
//				}
//			};
//			thread.start();
		}
		
//		if ( this.listTriggers.containsKey( port ) ) {
//			final Runnable runnable = this.listTriggers.get( port );
//			final Thread thread = new Thread( 
//					"Input event: " + port.name() ) {
//				@Override
//				public void run() {
//					runnable.run();
//				}
//			};
//			thread.start();
//		}
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
	

	public void registerChangeExec(	final Port port,
									final Listener listener ) {
		this.listListeners.put( port, listener );
//		final PortInterface pi = mapInterface.get( port );
//		if ( null==pi ) {
//			LOGGER.severe( "Failed to register PAHAT.Listener." );
//		} else {
//			pi.listener = listener;
//		}
	}

	
	public void registerChangeExec(	final HardwareInput input,
									final Listener listener ) {
		final Port port = getPortForInput( input );
		this.registerChangeExec( port, listener );
	}


	private JsonElement updateData( final long lTime,
									final String strLine ) {
		if ( null==mp ) return null;
		
		// [{"three": 0, "two": 0, "one": 0}, {"four": 0.53, "three": 0.03, "two": 0.03, "one": 0.03}]
		
		final JsonElement je;
		
		final TraceMap map = new TraceMap();
		map.put( "initial-time", lTime );
		map.put( "initial-event", "hardware-input" );
		
//		final String strLine = mp.getLatestLine();
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

//				synchronized ( mapInterface ) {

//				synchronized ( mapDigitalInput ) {
					updateDigitalInput( Port.IN_D_1, 1==joD.get( "one" ).getAsInt(), map, lTime );
					updateDigitalInput( Port.IN_D_2, 1==joD.get( "two" ).getAsInt(), map, lTime );
					updateDigitalInput( Port.IN_D_3, 1==joD.get( "three" ).getAsInt(), map, lTime );
//				}
				
//				synchronized ( mapAnalogInput ) {
					
//					System.out.println( "Updating to data: " + joA.toString() );
					
					updateAnalogInput( Port.IN_A_1, joA.get( "one" ).getAsFloat(), map, lTime );
					updateAnalogInput( Port.IN_A_2, joA.get( "two" ).getAsFloat(), map, lTime );
					updateAnalogInput( Port.IN_A_3, joA.get( "three" ).getAsFloat(), map, lTime );
					updateAnalogInput( Port.IN_A_4, joA.get( "four" ).getAsFloat(), map, lTime );
//				}
				
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
		
		final PortInterface pi = mapInterface.get( port );
		
		if ( pi instanceof InputDigitalInterface ) {
			final InputDigitalInterface input = (InputDigitalInterface)pi;
			synchronized ( input ) {
				final Boolean value = input.bValue;
				return value;
			}
		} else if ( pi instanceof OutputDigitalInterface ) {
			final OutputDigitalInterface output = (OutputDigitalInterface)pi;
			synchronized ( output ) {
				final Boolean value = output.bValue;
				return value;
			}
		}
		
		
//		synchronized ( mapDigitalInput ) {
//			if ( mapDigitalInput.containsKey( port ) ) {
//				final Boolean value = mapDigitalInput.get( port );
//				return value;
//			}
//		}
//		synchronized ( mapDigitalOutput ) {
//			if ( mapDigitalOutput.containsKey( port ) ) {
//				final Boolean value = mapDigitalOutput.get( port );
//				return value;
//			}
//		}
		return null;
	}

	
	public FunctionBase getAnalogPortStatistics( final Port port ) {
		return this.getAnalogInputData( port, false );
	}
	
	
	public Float getAnalogPortValue( final Port port ) {
//		synchronized ( mapAnalogInput ) {
//			if ( mapAnalogInput.containsKey( port ) ) {
//				final Float value = mapAnalogInput.get( port );
//				return value;
//			}
//		}
		
		if ( port.isInput() ) {
			final NormalizedFloat nf = this.getAnalogInputData( port, true );
			if ( null!=nf ) {
				final Double dInValue = nf.evaluate();
				if ( null!=dInValue ) {
					return new Float( dInValue );
				} else {
					return null;
				}
			} else {
				return null;
			}
			
		} else { // output
			// no analog output yet
//			synchronized ( mapAnalogOutput ) {
//				if ( mapAnalogOutput.containsKey( port ) ) {
//					final Float value = mapAnalogOutput.get( port );
//					return value;
//				}
//			}
		}
		return null;
	}
	
	
	public void setPortValue(	final Port port,
								final boolean bOn ) {
		if ( null==port ) return;
		final char cCommPort = port.cCommIndex;
		if ( 0==cCommPort ) return;
		
		final PortInterface pi = mapInterface.get( port );
		
		
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

//			synchronized ( mapDigitalOutput ) {
//				Pimoroni_AutomationHAT.mapDigitalOutput.put( port, bOn );
//			}
			if ( pi instanceof OutputDigitalInterface ) {
				final OutputDigitalInterface output = (OutputDigitalInterface)pi;
				synchronized ( output ) {
					output.bValue = bOn;
				}
			}
			
		} catch ( final IOException e ) {
			e.printStackTrace();

//			synchronized ( mapDigitalOutput ) {
//				Pimoroni_AutomationHAT.mapDigitalOutput.remove( port );
//			}
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
