package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.tiles.HistogramTile.Graph;
import jmr.s2db.event.EventType;
import jmr.s2db.job.JobMonitor;
import jmr.s2db.job.RemoteJobMonitor;
import jmr.s2db.job.RemoteJobMonitor.Listener;
import jmr.s2db.tables.Event;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.s2db.tables.Session;
import jmr.util.NetUtil;
import jmr.util.hardware.HardwareInput;
import jmr.util.hardware.HardwareOutput;
import jmr.util.hardware.rpi.pimoroni.AutomationHAT;
import jmr.util.hardware.rpi.pimoroni.Port;
import jmr.util.math.FunctionBase;
import jmr.util.math.FunctionParameter;
import jmr.util.report.TraceMap;

public class IO_AutomationHatTile extends TileBase {

	private final static Logger 
				LOGGER = Logger.getLogger(IO_AutomationHatTile.class.getName());
	
	public static enum TileType {
		DISPLAY, // full display
		GRAPH,
		CONTROL,
		;
	}

	public static enum HardwareTest {
		RELAY_1_ON( "Relay 1 ON", Port.OUT_R_1, true ),
		RELAY_1_OFF( "Relay 1 OFF", Port.OUT_R_1, false ),
		DOUT_1_ON( "D-1 Out ON", Port.OUT_D_1, true ),
		DOUT_1_OFF( "D-1 Out OFF", Port.OUT_D_1, false ),
		DOUT_2_ON( "D-2 Out ON", Port.OUT_D_2, true ),
		DOUT_2_OFF( "D-2 Out OFF", Port.OUT_D_2, false ),
		;
		
		public final String strTitle;
		public final Port port;
		public final boolean bValue;
		
		HardwareTest(	final String strTitle,
						final Port port,
						final boolean bValue ) {
			this.strTitle = strTitle;
			this.port = port;
			this.bValue = bValue;
		}
	}
	
	final TileType type;
	
	final static List<Job> listing = new LinkedList<>();
	
	final List<String> listStatus = new LinkedList<>();

	public final static int PIN_COUNT = 32;

	private final AutomationHAT hat;
	
	
	public static RemoteJobMonitor monitor = null;
	


	public IO_AutomationHatTile(	final TileType type,
									final Map<String, String> mapOptions  ) {
		this.type = type;
		
		hat = AutomationHAT.get();
		hat.initialize( mapOptions );
		
		registerJobListener( mapOptions );

//		hat.registerChangeExec( HardwareInput.TEST_DIGITAL_INPUT_2, 
//				new Runnable() {
//			@Override
//			public void run() {
//				System.out.println( "Runnable - test-input-2 fired." );
//			}
//		} );
		
		for ( final Port port : Port.values() ) {
			if ( port.isInput() ) {
				
				hat.registerChangeExec( port, 
									new AutomationHAT.Listener() {
					@Override
					public void inputTrigger( // final Map<String, Object> map,
											  final TraceMap map,
											  final long lTime ) {
						map.addFrame();
						processInputEvent( port, lTime, map );
					}
				});
			}
		}
	}
	
	
	public void registerJobListener( final Map<String, String> mapOptions ) {
		
		//TODO re-add this if possible..
//		if ( ! hat.isActive() ) return;

		if ( null != monitor ) return;
		
		LOGGER.info( "Registering Job Listener" );
		System.out.println( "**** Job Listener registered from IO_AutomationHatTile" );

		JobMonitor.get().initialize( mapOptions );
		monitor = RemoteJobMonitor.get();
		
		monitor.addListener( new Listener() {
			@Override
			public void job( final Job job ) {
				
				final long lTime = System.currentTimeMillis();

				final JsonObject joResult = new JsonObject();
				joResult.addProperty( "time", lTime );
				joResult.addProperty( "IP", NetUtil.getIPAddress() );
				joResult.addProperty( "session_seq", Session.getSessionSeq() );

				LOGGER.info( "Processing Job: " + job.toString() );

				final Map<String,String> map = job.getJobDetails();
				
				final String strPort = map.get( "port" );
				final Port port = Port.getPortFor( strPort );

				final TraceMap mapData = new TraceMap( false );
				mapData.addStringMap( mapOptions );
				mapData.putAll( map );
				mapData.put( "job.seq", job.getJobSeq() );
				mapData.put( "job.request", job.getRequest() );
				mapData.putIfAbsent( "initial-event", "incoming (remote) job" );
				mapData.putIfAbsent( "initial-time", lTime );

				joResult.addProperty( "port", "strPort" );

				if ( null!=port && ! port.isInput() ) {
					
					job.setState( JobState.WORKING );
					
					final String strValue = map.get( "value" );
					final Boolean bValue = Boolean.valueOf( strValue );
					
					// map: put( "job", job.getSeq() )
					
					mapData.addFrame();
					setPortValue( port, bValue, lTime, mapData ); // add map here
					
					job.setState( JobState.COMPLETE, joResult.toString() );
				} else {

					joResult.addProperty( "result", "Unknown output port" );
					
					job.setState( JobState.FAILURE, joResult.toString() );
				}
			}
		} );
	}
	
	
	public void processInputEvent(	final Port port,
									final long lTime,
									final TraceMap map
									) {
		System.out.println( "Input event for port: " + port.name() );
		
		map.addFrame();

		final HardwareInput input = hat.getHardwareInputForPort( port );
		
		if ( null==input ) {
			System.err.println( "HardwareInput for port " 
					+ port.name() + " not found. Event not recorded.");
			return;
		}
		
		final JsonPrimitive jsonValue;
		
		final JsonElement jeMap = new Gson().toJsonTree( map );
		final JsonObject jsonMap = jeMap.getAsJsonObject();
		
System.out.println( "Map: " + jeMap.toString() );		
		
		jsonMap.addProperty( "port", port.name() );
		
		final Boolean bValue = hat.getDigitalPortValue( port );
		final Float fValue = hat.getAnalogPortValue( port );
		final String strValue;
		final String strThreshold = "";
		if ( null!=bValue ) {
			jsonMap.addProperty( "type", "boolean" );
			jsonValue = new JsonPrimitive( bValue.booleanValue() );
			strValue = Boolean.toString( bValue );
		} else if ( null!=fValue ) {
			jsonMap.addProperty( "type", "float" );
			jsonValue = new JsonPrimitive( fValue.floatValue() );
			strValue = Float.toString( fValue );
		} else {
			jsonValue = null;
			strValue = "";
		}
		System.out.println( "\t" + port.name() 
					+ " = " + strValue + " (" + input.name() + ")" );
		
		if ( null!=input && null!=jsonValue ) {
			
			jsonMap.add( "value", jsonValue );
			
			final String strSubject = input.name();
			final String strData = jsonMap.toString();
			
			final Event event = Event.add(
					EventType.INPUT, strSubject, strValue, strThreshold, 
					strData, lTime, null, null, null );
			
			System.out.println( "Event created: seq " + event.getEventSeq() );
		} else {
			System.err.print( "Event NOT created" );
//			if ( null==input ) {
//				System.err.print( ", input is null" );
//			} 
			if ( null==jsonValue ) {
				System.err.print( ", jsonValue is null" );
			}
			System.err.println( "." );
			System.out.println( "\tbValue = " + bValue );
			System.out.println( "\tfValue = " + fValue );
			System.out.println( "\tstrValue = " + strValue );
			System.out.println( "\tjsonValue = " + jsonValue );
		}
		
		
	}
	
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );
		
		
//		if ( 150==gc.getClipping().width ) {
		if ( TileType.CONTROL.equals( this.type ) ) {

			gc.setFont( Theme.get().getFont( 11 ) );

//			text.println( "Test Hardware" );
			
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );

			int iY = 16;
			int iX = 14;
			for ( final HardwareTest alert : HardwareTest.values() ) {
				super.addButton( gc, alert.ordinal(), 
					 // iX, iY,  132, 52, alert.strTitle );
						iX, iY,   90, 52, alert.strTitle );
				if ( iY > 50 ) {
					iY = 16;
				 // iX += 148;
					iX += 100;
				} else {
					iY += 70;
				}
			}
			
		} else if ( TileType.GRAPH.equals( this.type ) ) {
			
			//
			
		} else { // type = DISPLAY
		
//			text.println( "Painting.." );
	
			if ( !hat.isActive() ) {
				text.println( "Automation HAT data not available" );
				return;
			}
	
			
			final Double dInterval = hat.getAveragePollingInterval();
			if ( null != dInterval ) {
				text.println( "Polling Automation HAT   -   "
						+ "Polling interval: " 
						+ String.format( "%2.3f", dInterval ) + " seconds" );
			} else {
				text.println( "Automation HAT detected" );
			}
			
			
			final String strIndent = "      ";
			final String strSpacer = "  -  ";
	
			text.println( "Digital Inputs:" );
			for ( final Port port : Port.values() ) {
				if ( port.isInput() && ! port.isAnalog() ) {
					final Boolean value = hat.getDigitalPortValue( port );
					if ( null!=value ) {
						final String strValue = value.toString();
						final HardwareInput hardware = 
										hat.getHardwareInputForPort( port );
						final String strHardware = ( null!=hardware ) 
														? hardware.name() 
														: "<no hw name>";
						text.println( strIndent + strValue 
										+ strSpacer + port.name()
										+ strSpacer + strHardware );
					} else {
						text.println( strIndent + strIndent + "-" + strIndent  
								+ strSpacer + port.name()
								+ strSpacer + "<unmapped>" );
					}
				}
			}
			
			text.println( "Analog Inputs:" );
			for ( final Port port : Port.values() ) {
				final Float fValue;
				if ( port.isInput() && port.isAnalog() ) {
					fValue = hat.getAnalogPortValue( port );
					if ( null!=fValue ) {
	//					final String strValue = value.toString();
						final String strValue = String.format( "%8.4f", fValue );
						final HardwareInput 
								hardware = hat.getHardwareInputForPort( port );
						final String strHardware = null!=hardware 
													? hardware.name() 
													: "<no hw name>";
													
						final String strIRLValue;
						final FunctionBase fb = 
										hat.getAnalogPortStatistics( port );
						if ( null!=fb ) {
							final Double dIRLValue = fb.getParamDouble( 
									FunctionParameter.VAR_VALUE_IRL_LAST_POSTED );
							if ( null!=dIRLValue ) {
								strIRLValue = 
										String.format( "%.2f", dIRLValue ) 
										+ " " + fb.getUnit();
							} else {
								strIRLValue = "(..)";
							}
						} else {
							strIRLValue = "(?)";
						}
								
						text.println( strIndent + strValue 
										+ strSpacer + port.name()
										+ strSpacer + strHardware 
										+ "   = " + strIRLValue );
					} else {
						text.println( strIndent + strIndent + " -" + strIndent  
								+ strSpacer + port.name()
								+ strSpacer + "<unmapped>" );
					}
				} else {
					fValue = null;
				}

				if ( Port.IN_A_1.equals( port ) && ( null!=fValue ) ) {
					final Graph graph = HistogramTile.getGraph( port.name() );
					if ( null!=graph ) {
						graph.add( fValue );
					}
				}
			}
			
			text.println( "Digital Line Outputs:" );
			for ( final Port port : Port.values() ) {
				if ( ! port.isInput() && ! port.isRelay() ) {
					final Boolean value = hat.getDigitalPortValue( port );
//					if ( null!=value ) {
					{
						final String strValue = ( null!=value )
											? value.toString()
											: strIndent + "-" + strIndent;
						final HardwareOutput 
								hardware = hat.getHardwareOutputForPort( port );
						final String strHardware = null!=hardware 
												? hardware.name() 
												: "<no hw name>";
						text.println( strIndent + strValue 
										+ strSpacer + port.name()
										+ strSpacer + strHardware );
//					} else {
//						text.println( strIndent + strIndent + "-" + strIndent  
//								+ strSpacer + port.name()
//								+ strSpacer + "<unmapped>" );
					}
				}
			}

			text.println( "Automation Relays:" );
			for ( final Port port : Port.values() ) {
				if ( ! port.isInput() && port.isRelay() ) {
					final Boolean value = hat.getDigitalPortValue( port );
					{
						final String strValue = ( null!=value )
												? value.toString() 
												: ( strIndent + "-" );
						final HardwareOutput 
								hardware = hat.getHardwareOutputForPort( port );
						final String strHardware = null!=hardware 
												? hardware.name() 
												: "<no hw name>";
						text.println( strIndent + strValue + strIndent 
										+ strSpacer + port.name()
										+ strSpacer + strHardware );
//					} else {
//						text.println( strIndent + strIndent + "-" + strIndent  
//								+ strSpacer + port.name()
//								+ strSpacer + "<unmapped>" );
					}
				}
			}

			// no analog output on this device
		}
		
	}
	
	
	public void setPortValue(	final Port port,
								final boolean bValue,
								final long lTime,
								final TraceMap map
								) {
		if ( null==port ) return;

		hat.setPortValue( port, bValue );

		final HardwareOutput output = hat.getHardwareOutputForPort( port );
		final Boolean bValueVerified = hat.getDigitalPortValue( port );
		
		if ( null!=output && null!=bValueVerified ) {

			final JsonObject jsonMap = new JsonObject();
			jsonMap.addProperty( "port", port.name() );
			jsonMap.addProperty( "type", "boolean" );
			jsonMap.addProperty( "value", bValue );

//			final Map<String,Object> map = new HashMap<>();
//			final TraceMap map = new TraceMap( mapIn );
//			if ( null!=mapIn ) {
//				map.putAll( mapIn );
//			}
			map.put( "port", port.name() );
			map.put( "type", "boolean" );
			map.put( "value", bValue );
//			map.put( "time-initiate", lTime );
//			map.put( "source-initiate", "IOAHAT.setPortValue()" );
			map.put( "data-collection-interval", 
									this.hat.getAveragePollingInterval() );

			final String strSubject = output.name();
//			final JsonPrimitive jsonData = new JsonPrimitive( bValueVerified.booleanValue() );
//			final String strData = jsonData.getAsString();
//			final String strData = jsonMap.toString();
			final String strValue = Boolean.toString( bValue );
			final String strThreshold = "";
			
//			final Event event = Event.add( 
//					EventType.USER, strSubject, strValue, strThreshold, 
//					strData, lTime, null, null, null );

			final Event event = Event.add(
					EventType.USER, strSubject, strValue, strThreshold, 
					map, lTime, null, null, null );
//					EventType.USER, strSubject, strValue, strThreshold, 
//					strData, lTime, null, null, null );

			
			System.out.println( "Event created: seq " + event.getEventSeq() );
		}
	}
	
	

	private void play(	final S2Button button,
						final HardwareTest test,
						final TraceMap map ) {

		System.out.println( "Selected hardware test: " + test.strTitle );

		final long lTime = System.currentTimeMillis();
//		final Map<String,Object> map = new HashMap<>();
//		final TraceMap map = new TraceMap();
//		map.put( "source-initiate", "IOAHT.play()" );

		final Thread thread = new Thread( "Hardware test (IO_AutomationHatTile)" ) {
			public void run() {
				button.setState( ButtonState.WORKING );

				final Job job = null;
				
				setPortValue( test.port, test.bValue, lTime, map );
				
				button.setJob( job );
				button.setState( ButtonState.READY );
			};
		};
		thread.start();
	}
	
	
	@Override
	protected void activateButton( final S2Button button ) {
		for ( final HardwareTest program : HardwareTest.values() ) {
			if ( program.ordinal()==button.getIndex() ) {
				final TraceMap map = new TraceMap();
				map.put( "initial-event", "touchscreen click" );
				play( button, program, map );
			}
		}
	}
	


	@Override
	protected void finalize() throws Throwable {
		System.out.println( "Shutting down GPIO." );
//		gpio.shutdown();
		super.finalize();
	}	
	

}
