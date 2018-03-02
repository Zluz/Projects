package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.tables.Job;

public class IO_GPIOTile extends TileBase {



	public final String strGPIOId;

	final static List<Job> listing = new LinkedList<>();
	
	final List<String> listStatus = new LinkedList<>();

	public final static int PIN_COUNT = 32;
	
	final static Map<Integer,GpioPinDigitalOutput> pins = new HashMap<>();
	



	public IO_GPIOTile(  final Map<String, String> mapOptions  ) {
		this.strGPIOId = mapOptions.get( "gpio" );
		final Thread threadUpdateStatus = new Thread( "Update GPIO Status" ) {
			@Override
			public void run() {
				try {
					for (;;) {
						final List<String> list = new LinkedList<String>();
						list.add( "Update time: " + System.currentTimeMillis() );
						list.add( "" );
						if ( null!=gpio ) {
							
							synchronized ( pins ) {
								for ( final Entry<Integer, GpioPinDigitalOutput> 
													entry : pins.entrySet() ) {
									final GpioPinDigitalOutput pin = entry.getValue();
	
									String strLine = "";
									
									final String strName = pin.getName();
									strLine += "pin " + strName + ": ";
									final String strMode = pin.getMode().getName();
									strLine += " mode: " + strMode + ", ";
									final String strState = pin.getState().getName();
									strLine += " state: " + strState;
									
									list.add( strLine );
								}
							}
							
						} else {
							list.add( "GPIO singleton is null." );
							list.add( "GPIO services not available." );
						}
						
						synchronized ( listStatus ) {
							listStatus.clear();
							listStatus.addAll( list );
						}
						Thread.sleep( 200 );
					}
				} catch ( final InterruptedException e ) {
//					listStatus.add( "Interrupted. Status not available." );
					System.err.println( "GPIO status thread interrupted." );
				}
			}
		};
		threadUpdateStatus.start();
	}
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {

//		final long lNow = System.currentTimeMillis();
		
//		String strText = "";
//		for ( final String strSession : map.keySet() ) {
//			strText += strSession + "\n";
//		}
		
		int iY = 10;

//		final int iX_RequestText;
////		final int iX_MAC;
//		final int iX_RequestTime;
//		final int iX_RequestResult;
//		final char iX_State;
//		
////		if ( 450 == rect.width ) {
////			iX_MAC = 10;	iX_Exec = 10;	iX_Desc = 290;
////		} else {
//			iX_State = 5;	
//			iX_RequestTime = 20;	
////			iX_MAC = 10;	
//			iX_RequestText = 80;
//			iX_RequestResult = 450;
////		}
			
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		gc.setFont( Theme.get().getFont( 10 ) );
		if ( null!=strGPIOId && !strGPIOId.isEmpty() ) {
			gc.drawText( "Local GPIO Id: " + this.strGPIOId, 15, 4 );
			iY = iY + 20;
		}
		

		final GCTextUtils text = new GCTextUtils( gc );
		final Rectangle rect = gc.getClipping();
		text.setRect( new Rectangle( 150, iY, rect.width, rect.height ) );
		synchronized ( listStatus ) {
			for ( final String line : listStatus ) {
				text.println( line );
			}
		}
		

		super.addButton( gc, 1, 10, iY, 125, 60, "Provision" ); iY += 70;
		super.addButton( gc, 2, 10, iY, 125, 60, "Pin State HIGH" ); iY += 70;
		super.addButton( gc, 3, 10, iY, 125, 60, "Pin State LOW" ); iY += 70;
		super.addButton( gc, 4, 10, iY, 125, 60, "Shutdown GPIO" ); iY += 70;
		

//		synchronized ( listing ) {
//			for ( final Job job : listing ) {
////			for ( final Map<String, String> map : map2.values() ) {
//
//				final JobState state = job.getState();
//
//				final Color color;
//				if ( JobState.REQUEST.equals( state ) ) {
//					color = Theme.get().getColor( Colors.TEXT_BOLD );
//				} else {
//					color = Theme.get().getColor( Colors.TEXT_LIGHT );
//				}
//				gc.setForeground( color );
//				
//				final long lRequestTime = job.getRequestTime();
//				final long lElapsed = lNow - lRequestTime;
////				final String strElapsed = String.format( "%.2f s", lElapsed );
//				final String strElapsed = DateFormatting.getSmallTime( lElapsed );
//				gc.setFont( Theme.get().getFont( 8 ) );
//				gc.drawText( ""+state.getChar(), iX_State, iY );
//				gc.drawText( strElapsed, iX_RequestTime, iY );
//
//				final String strRequest = job.getRequest();
//				gc.drawText( strRequest, iX_RequestText, iY );
//				
//				if ( rect.width > 400 ) {
//					final String strResult = job.getResult();
//					final String strPrintable = null!=strResult ? strResult : "-";
//					gc.drawText( "   " + strPrintable, iX_RequestResult, iY );
//				}
//				
//	//			strText += strIPFit + "   " + strExecFit + "   " + strName + "\n";
//				iY += 18;
//			}
//		}


	}
	
	
//	1 - pin_03 - gpio02 (sda1)   		GPIO 8
//	2 - pin_11 - gpio17 (gpio_gen0)		GPIO 0
//	3 - pin_12 - gpio18 (gpio_gen1)		GPIO 1
//	4 - pin_13 - gpio27 (gpio_gen2)		GPIO 2

	
	final static GpioController gpio;
	static {
		GpioController instance = null;
		try {
			instance = GpioFactory.getInstance();
		} catch ( final Throwable e ) {
			System.err.println( "Unable to initialize the Pi4J singleton. "
					+ "GPIO services will not be available.");
		}
		gpio = instance;
	}

	private void activateButtonThreaded( final S2Button button ) 
									throws InterruptedException {
		if ( null!=button ) {
			button.setState( ButtonState.WORKING );
		}
		Job job = null;
		
		if ( null==gpio ) {
			button.setState( ButtonState.READY );
			System.err.println( "GPIO services not available." );
			return;
		}
		
		switch ( button.getIndex() ) {
			case 1: {
//				pins.put( 1, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_08, "wire 1", PinState.LOW ) );
//				pins.put( 2, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_00, "wire 2", PinState.LOW ) );
//				pins.put( 3, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_01, "wire 3", PinState.LOW ) );
//				pins.put( 4, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_02, "wire 4", PinState.LOW ) );
//
//				pins.put( 5, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_09, "wire ?a", PinState.LOW ) );
//				pins.put( 6, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_07, "wire ?b", PinState.LOW ) );

				
				
				pins.put(  0, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_00, "wire 2", PinState.LOW ) );
				pins.put(  1, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_01, "wire 3", PinState.LOW ) );
				pins.put(  2, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_02, "wire 4", PinState.LOW ) );
				pins.put(  3, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_03, "GPIO_03", PinState.LOW ) );
				pins.put(  4, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_04, "GPIO_04", PinState.LOW ) );
				pins.put(  5, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_05, "GPIO_05", PinState.LOW ) );
				pins.put(  6, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_06, "GPIO_06", PinState.LOW ) );
				pins.put(  7, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_07, "GPIO_07", PinState.LOW ) );
				pins.put(  8, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_08, "wire 1", PinState.LOW ) );
				pins.put(  9, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_09, "GPIO_09", PinState.LOW ) );
				pins.put( 10, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_10, "GPIO_10", PinState.LOW ) );
				pins.put( 11, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_11, "GPIO_11", PinState.LOW ) );
				pins.put( 12, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_12, "GPIO_12", PinState.LOW ) );
				pins.put( 13, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_13, "GPIO_13", PinState.LOW ) );
				pins.put( 14, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_14, "GPIO_14", PinState.LOW ) );
				pins.put( 15, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_15, "GPIO_15", PinState.LOW ) );
				pins.put( 16, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_16, "GPIO_16", PinState.LOW ) );
				pins.put( 17, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_17, "GPIO_17", PinState.LOW ) );
				pins.put( 18, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_18, "GPIO_18", PinState.LOW ) );
				pins.put( 19, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_19, "GPIO_19", PinState.LOW ) );
				pins.put( 20, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_20, "GPIO_20", PinState.LOW ) );
				pins.put( 21, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_21, "GPIO_21", PinState.LOW ) );
				pins.put( 22, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_22, "GPIO_22", PinState.LOW ) );
				pins.put( 23, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_23, "GPIO_23", PinState.LOW ) );
				pins.put( 24, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_24, "GPIO_24", PinState.LOW ) );
				pins.put( 25, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_25, "GPIO_25", PinState.LOW ) );
				pins.put( 26, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_26, "GPIO_26", PinState.LOW ) );
				pins.put( 27, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_27, "GPIO_27", PinState.LOW ) );
				pins.put( 28, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_28, "GPIO_28", PinState.LOW ) );
				pins.put( 29, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_29, "GPIO_29", PinState.LOW ) );
				pins.put( 30, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_30, "GPIO_30", PinState.LOW ) );
				pins.put( 31, gpio.provisionDigitalOutputPin( RaspiPin.GPIO_31, "GPIO_31", PinState.LOW ) );

				for ( final Entry<Integer, GpioPinDigitalOutput> 
											entry : pins.entrySet() ) {
					final GpioPinDigitalOutput pin = entry.getValue();
					pin.setShutdownOptions( true, PinState.LOW );
				}

				break;
			}
			
			case 2: {
				for ( final Entry<Integer, GpioPinDigitalOutput> 
												entry : pins.entrySet() ) {
					final GpioPinDigitalOutput pin = entry.getValue();
					pin.setState( PinState.HIGH );
				}
				break;
			}
			case 3: {
				for ( final Entry<Integer, GpioPinDigitalOutput> 
												entry : pins.entrySet() ) {
					final GpioPinDigitalOutput pin = entry.getValue();
					pin.setState( PinState.LOW );
				}
				break;
			}
			case 4: {
				gpio.shutdown();
				break;
			}
		}
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
					synchronized ( pins ) {
						activateButtonThreaded( button );
					}
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
		gpio.shutdown();
		super.finalize();
	}	
	

}
