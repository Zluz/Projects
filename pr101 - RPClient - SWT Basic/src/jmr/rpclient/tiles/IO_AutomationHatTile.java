package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.event.EventType;
import jmr.s2db.tables.Event;
import jmr.s2db.tables.Job;
import jmr.util.hardware.HardwareInput;
import jmr.util.hardware.HardwareOutput;
import jmr.util.hardware.rpi.Pimoroni_AutomationHAT;
import jmr.util.hardware.rpi.Pimoroni_AutomationHAT.Port;

public class IO_AutomationHatTile extends TileBase {

	public static enum TileType {
		DISPLAY, // full display
		GRAPH,
		CONTROL,
		;
	}

	public static enum HardwareTest {
		RELAY_1_ON( "Relay 1 ON", "/Local/scripts/test_email.sh" ),
		RELAY_1_OFF( "Relay 1 OFF", "/Local/scripts/test_sms.sh" ),
		;
		
		public final String strTitle;
		public final String strAction;
		
		HardwareTest(	final String strTitle,
						final String strScript ) {
			this.strTitle = strTitle;
			this.strAction = strScript;
		}
	}
	
	final TileType type;
	
	final static List<Job> listing = new LinkedList<>();
	
	final List<String> listStatus = new LinkedList<>();

	public final static int PIN_COUNT = 32;

	private final Pimoroni_AutomationHAT hat;
	


	public IO_AutomationHatTile(	final TileType type,
									final Map<String, String> mapOptions  ) {
		this.type = type;
		
		hat = Pimoroni_AutomationHAT.get();
		hat.initialize( mapOptions );
		
		hat.registerChangeExec( HardwareInput.TEST_DIGITAL_INPUT_2, 
				new Runnable() {
			@Override
			public void run() {
				System.out.println( "Runnable - test-input-2 fired." );
			}
		} );
		
		for ( final Port port : Port.values() ) {
			if ( port.isInput() ) {
				hat.registerChangeExec( port, new Runnable() {
					@Override
					public void run() {
						final long lTime = System.currentTimeMillis();
						processInputEvent( port, lTime );
					}
				} );
			}
		}
	}
	
	
	public void processInputEvent(	final Port port,
									final long lTime ) {
		System.out.println( "Input event for port: " + port.name() );

		final HardwareInput input = hat.getHardwareInputForPort( port );
		
		if ( null==input ) {
			System.err.println( "HardwareInput for port " 
					+ port.name() + " not found. Event not recorded.");
			return;
		}
		
		final JsonPrimitive jsonValue;
		final JsonObject jsonMap = new JsonObject();
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
//			final String strData = jsonValue.getAsString();
			final String strData = jsonMap.toString();
			
			final Event event = Event.add(
					EventType.INPUT, strSubject, strValue, strThreshold, 
					strData, lTime, null, null, null );
			
			System.out.println( "Event created: seq " + event.getEventSeq() );
		} else {
			System.err.print( "Event NOT created" );
			if ( null==input ) {
				System.err.print( ", input is null" );
			} 
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
						iX, iY,  132, 52, alert.strTitle );
				if ( iY > 50 ) {
					iY = 16;
					iX += 148;
				} else {
					iY += 70;
				}
			}
			
		} else if ( TileType.GRAPH.equals( this.type ) ) {
			
			//
			
		} else { // type = DISPLAY
		
			text.println( "Painting.." );
	
			if ( !hat.isActive() ) {
				text.println( "Automation HAT data not available" );
				return;
			}
	
			text.println( "Automation HAT detected" );
			
			
			final String strIndent = "      ";
			final String strSpacer = "   -   ";
	
			text.println( "Digital Inputs:" );
			for ( final Port port : Port.values() ) {
				if ( port.isInput() ) {
					final Boolean value = hat.getDigitalPortValue( port );
					if ( null!=value ) {
						final String strValue = value.toString();
						final HardwareInput hardware = hat.getHardwareInputForPort( port );
						final String strHardware = null!=hardware ? hardware.name() : "<no hw name>";
						text.println( strIndent + strValue 
										+ strSpacer + port.name()
										+ strSpacer + strHardware );
					}
				}
			}
			
			text.println( "Analog Inputs:" );
			for ( final Port port : Port.values() ) {
				final Float value = hat.getAnalogPortValue( port );
				if ( null!=value ) {
					final String strValue = value.toString();
					final HardwareInput hardware = hat.getHardwareInputForPort( port );
					final String strHardware = null!=hardware ? hardware.name() : "<no hw name>";
					text.println( strIndent + strValue 
									+ strSpacer + port.name()
									+ strSpacer + strHardware );
				}
			}
			
			text.println( "Digital Outputs:" );
			for ( final Port port : Port.values() ) {
				if ( !port.isInput() ) {
					final Boolean value = hat.getDigitalPortValue( port );
					if ( null!=value ) {
						final String strValue = value.toString();
						final HardwareOutput hardware = hat.getHardwareOutputForPort( port );
						final String strHardware = null!=hardware ? hardware.name() : "<no hw name>";
						text.println( strIndent + strValue 
										+ strSpacer + port.name()
										+ strSpacer + strHardware );
					}
				}
			}

			// no analog output on this device
		}
		
	}
	
	
	public void setPortValue(	final Port port,
								final boolean bValue,
								final long lTime ) {
		if ( null==port ) return;

		hat.setPortValue( port, bValue );

		final HardwareOutput output = hat.getHardwareOutputForPort( port );
		final Boolean bValueVerified = hat.getDigitalPortValue( port );
		
		if ( null!=output && null!=bValueVerified ) {
			
			final JsonObject jsonMap = new JsonObject();
			jsonMap.addProperty( "port", port.name() );
			jsonMap.addProperty( "type", "boolean" );
			jsonMap.addProperty( "value", bValue );

			final String strSubject = output.name();
//			final JsonPrimitive jsonData = new JsonPrimitive( bValueVerified.booleanValue() );
//			final String strData = jsonData.getAsString();
			final String strData = jsonMap.toString();
			final String strValue = Boolean.toString( bValue );
			final String strThreshold = "";
			
			final Event event = Event.add( 
					EventType.USER, strSubject, strValue, strThreshold, 
					strData, lTime, null, null, null );
			
			System.out.println( "Event created: seq " + event.getEventSeq() );
		}
	}
	
	

	private void play(	final S2Button button,
						final HardwareTest test ) {

		System.out.println( "Selected hardware test: " + test.strTitle );

		final long lTime = System.currentTimeMillis();


		final Thread thread = new Thread( "Hardware test (IO_AutomationHatTile)" ) {
			public void run() {
				button.setState( ButtonState.WORKING );

				final Job job = null;
				
				if ( HardwareTest.RELAY_1_ON.equals( test ) ) {

					setPortValue( Port.OUT_R_1, true, lTime );

				} else if ( HardwareTest.RELAY_1_OFF.equals( test ) ) {

					setPortValue( Port.OUT_R_1, false, lTime );

				} else {
//					job = null;
				}
				
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
				play( button, program );
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
