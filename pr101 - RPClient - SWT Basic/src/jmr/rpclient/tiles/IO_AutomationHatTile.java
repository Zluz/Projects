package jmr.rpclient.tiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.tables.Job;
import jmr.util.hardware.HardwareInput;
import jmr.util.hardware.HardwareOutput;
import jmr.util.hardware.rpi.Pimoroni_AutomationHAT;
import jmr.util.hardware.rpi.Pimoroni_AutomationHAT.Port;

public class IO_AutomationHatTile extends TileBase {


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
	
	
	final static List<Job> listing = new LinkedList<>();
	
	final List<String> listStatus = new LinkedList<>();

	public final static int PIN_COUNT = 32;

	private final Pimoroni_AutomationHAT hat;
	


	public IO_AutomationHatTile(  final Map<String, String> mapOptions  ) {
		hat = Pimoroni_AutomationHAT.get();
		hat.initialize( mapOptions );
		
		hat.registerChangeExec( HardwareInput.TEST_DIGITAL_INPUT_2, 
				new Runnable() {
			@Override
			public void run() {
				System.out.println( "Runnable - test-input-2 fired." );
			}
		} );
		
	}
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );
		
		
		if ( 150==gc.getClipping().width ) {

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
			
			return;
		}
		
		

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
	//				text.println( port.name() + ": " + value.toString() );
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
	

	private void play(	final S2Button button,
						final HardwareTest test ) {

		System.out.println( "Selected hardware test: " + test.strTitle );


		final Thread thread = new Thread( "Hardware test (IO_AutomationHatTile)" ) {
			public void run() {
				button.setState( ButtonState.WORKING );

				final Map<String,String> map = new HashMap<String,String>();
				final Job job = null;
				
				final LocalDateTime now = LocalDateTime.now();	
						
				
				if ( HardwareTest.RELAY_1_ON.equals( test ) ) {

					hat.setPortValue( Port.OUT_R_1, true );

				} else if ( HardwareTest.RELAY_1_OFF.equals( test ) ) {

					hat.setPortValue( Port.OUT_R_1, false );

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
