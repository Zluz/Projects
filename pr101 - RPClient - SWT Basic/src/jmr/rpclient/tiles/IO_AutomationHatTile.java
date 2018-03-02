package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.s2db.tables.Job;
import jmr.util.hardware.rpi.Pimoroni_AutomationHAT;
import jmr.util.hardware.rpi.Pimoroni_AutomationHAT.Ports;

public class IO_AutomationHatTile extends TileBase {



	final static List<Job> listing = new LinkedList<>();
	
	final List<String> listStatus = new LinkedList<>();

	public final static int PIN_COUNT = 32;

	private final Pimoroni_AutomationHAT hat = Pimoroni_AutomationHAT.get();
	


	public IO_AutomationHatTile(  final Map<String, String> mapOptions  ) {
		// init ?
	}
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );

		text.println( "Painting.." );

		if ( null==hat.updateData() ) return;

		text.println( "Automation HAT detected" );
		

		text.println( "Digital Inputs:" );
		for ( final Ports port : Ports.values() ) {
			if ( hat.mapDigitalInput.containsKey( port ) ) {
				text.println( port.name() + ": " 
									+ hat.mapDigitalInput.get( port ) );
			}
		}
		
		text.println( "Analog Inputs:" );
		for ( final Ports port : Ports.values() ) {
			if ( hat.mapAnalogInput.containsKey( port ) ) {
				text.println( port.name() + ": " 
									+ hat.mapAnalogInput.get( port ) );
			}
		}
		

//		super.addButton( gc, 1, 10, iY, 125, 60, "Provision" ); iY += 70;
//		super.addButton( gc, 2, 10, iY, 125, 60, "Pin State HIGH" ); iY += 70;
//		super.addButton( gc, 3, 10, iY, 125, 60, "Pin State LOW" ); iY += 70;
//		super.addButton( gc, 4, 10, iY, 125, 60, "Shutdown GPIO" ); iY += 70;
		

	}
	
	
//	1 - pin_03 - gpio02 (sda1)   		GPIO 8
//	2 - pin_11 - gpio17 (gpio_gen0)		GPIO 0
//	3 - pin_12 - gpio18 (gpio_gen1)		GPIO 1
//	4 - pin_13 - gpio27 (gpio_gen2)		GPIO 2

	
//	final static GpioController gpio;
//	static {
//		GpioController instance = null;
//		try {
//			instance = GpioFactory.getInstance();
//		} catch ( final Throwable e ) {
//			System.err.println( "Unable to initialize the Pi4J singleton. "
//					+ "GPIO services will not be available.");
//		}
//		gpio = instance;
//	}

	private void activateButtonThreaded( final S2Button button ) 
									throws InterruptedException {
		if ( null!=button ) {
			button.setState( ButtonState.WORKING );
		}
		Job job = null;
		
//		if ( null==gpio ) {
//			button.setState( ButtonState.READY );
//			System.err.println( "GPIO services not available." );
//			return;
//		}
//		
//		switch ( button.getIndex() ) {
//			case 1: {
////				pins.put( 1, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_08, "wire 1", PinState.LOW ) );
////				pins.put( 2, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_00, "wire 2", PinState.LOW ) );
////				pins.put( 3, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_01, "wire 3", PinState.LOW ) );
////				pins.put( 4, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_02, "wire 4", PinState.LOW ) );
////
////				pins.put( 5, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_09, "wire ?a", PinState.LOW ) );
////				pins.put( 6, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_07, "wire ?b", PinState.LOW ) );
//
//				
////				
////				pins.put(  0, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_00, "wire 2", PinState.LOW ) );
////				pins.put(  1, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_01, "wire 3", PinState.LOW ) );
////				pins.put(  2, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_02, "wire 4", PinState.LOW ) );
////				pins.put(  3, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_03, "GPIO_03", PinState.LOW ) );
////				pins.put(  4, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_04, "GPIO_04", PinState.LOW ) );
////				pins.put(  5, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_05, "GPIO_05", PinState.LOW ) );
////				pins.put(  6, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_06, "GPIO_06", PinState.LOW ) );
////				pins.put(  7, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_07, "GPIO_07", PinState.LOW ) );
////				pins.put(  8, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_08, "wire 1", PinState.LOW ) );
////				pins.put(  9, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_09, "GPIO_09", PinState.LOW ) );
////				pins.put( 10, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_10, "GPIO_10", PinState.LOW ) );
////				pins.put( 11, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_11, "GPIO_11", PinState.LOW ) );
////				pins.put( 12, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_12, "GPIO_12", PinState.LOW ) );
////				pins.put( 13, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_13, "GPIO_13", PinState.LOW ) );
////				pins.put( 14, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_14, "GPIO_14", PinState.LOW ) );
////				pins.put( 15, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_15, "GPIO_15", PinState.LOW ) );
////				pins.put( 16, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_16, "GPIO_16", PinState.LOW ) );
////				pins.put( 17, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_17, "GPIO_17", PinState.LOW ) );
////				pins.put( 18, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_18, "GPIO_18", PinState.LOW ) );
////				pins.put( 19, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_19, "GPIO_19", PinState.LOW ) );
////				pins.put( 20, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_20, "GPIO_20", PinState.LOW ) );
////				pins.put( 21, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_21, "GPIO_21", PinState.LOW ) );
////				pins.put( 22, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_22, "GPIO_22", PinState.LOW ) );
////				pins.put( 23, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_23, "GPIO_23", PinState.LOW ) );
////				pins.put( 24, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_24, "GPIO_24", PinState.LOW ) );
////				pins.put( 25, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_25, "GPIO_25", PinState.LOW ) );
////				pins.put( 26, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_26, "GPIO_26", PinState.LOW ) );
////				pins.put( 27, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_27, "GPIO_27", PinState.LOW ) );
////				pins.put( 28, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_28, "GPIO_28", PinState.LOW ) );
////				pins.put( 29, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_29, "GPIO_29", PinState.LOW ) );
////				pins.put( 30, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_30, "GPIO_30", PinState.LOW ) );
////				pins.put( 31, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_31, "GPIO_31", PinState.LOW ) );
////
////				for ( final Entry<Integer, GpioPinDigitalOutput> 
////											entry : pins.entrySet() ) {
////					final GpioPinDigitalOutput pin = entry.getValue();
////					pin.setShutdownOptions( true, PinState.LOW );
////				}
//
//				break;
//			}
////			
//			case 2: {
//				for ( final Entry<Integer, GpioPinDigitalOutput> 
//												entry : pins.entrySet() ) {
//					final GpioPinDigitalOutput pin = entry.getValue();
//					pin.setState( PinState.HIGH );
//				}
//				break;
//			}
//			case 3: {
//				for ( final Entry<Integer, GpioPinDigitalOutput> 
//												entry : pins.entrySet() ) {
//					final GpioPinDigitalOutput pin = entry.getValue();
//					pin.setState( PinState.LOW );
//				}
//				break;
//			}
//			case 4: {
//				gpio.shutdown();
//				break;
//			}
//		}
		if ( null!=button ) {
			button.setJob( job );
			button.setState( ButtonState.READY );
		}
	}
	
	
	
	@Override
	protected void activateButton( final S2Button button ) {
		final Thread thread = new Thread( "Button action (GPIOTile)" ) {
			public void run() {
				try {
//					synchronized ( pins ) {
						activateButtonThreaded( button );
//					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			};
		};
		thread.start();
	}


	@Override
	protected void finalize() throws Throwable {
		System.out.println( "Shutting down GPIO." );
//		gpio.shutdown();
		super.finalize();
	}	
	

}
