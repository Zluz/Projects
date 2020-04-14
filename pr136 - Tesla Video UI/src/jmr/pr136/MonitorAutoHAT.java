package jmr.pr136;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jmr.util.hardware.rpi.pimoroni.AutomationHAT;
import jmr.util.hardware.rpi.pimoroni.Port;

public class MonitorAutoHAT {

	private final static Logger 
				LOGGER = Logger.getLogger( MonitorAutoHAT.class.getName() );

	private final Monitor monitor_1_Batt = new Monitor( "12V Battery Voltage" );
	private final Monitor monitor_2_Accy = new Monitor( "Accessory Voltage" );
	
	private final AutomationHAT hat = AutomationHAT.get();

	private boolean bActive;

	public Monitor getMonitor_1_12VBatt() {
		return monitor_1_Batt;
	}
	
	public Monitor getMonitor_2_Accy() {
		return monitor_2_Accy;
	}
	
	public MonitorAutoHAT() {
		
		final Map<String,String> map = new HashMap<>();
		//TODO default generic config 
		map.put( "IN_A_1", "GENERIC_ANALOG_IN_1:20,0.0,1.0,3.0,volts" );
		map.put( "IN_A_2", "GENERIC_ANALOG_IN_2:20,0.0,1.0,3.0,volts" );
		map.put( "IN_A_3", "GENERIC_ANALOG_IN_3:20,0.0,1.0,3.0,volts" );
		map.put( "IN_D_1", "GENERIC_DIGITAL_IN_1" );
		map.put( "IN_D_2", "GENERIC_DIGITAL_IN_2" );
		map.put( "IN_D_3", "GENERIC_DIGITAL_IN_3" );
		map.put( "OUT_R_1", "GENERIC_RELAY_1" );
		map.put( "OUT_R_2", "GENERIC_RELAY_2" );
		map.put( "OUT_R_3", "GENERIC_RELAY_3" );
		map.put( "OUT_D_1", "GENERIC_DIGITAL_OUT_1" );
		map.put( "OUT_D_2", "GENERIC_DIGITAL_OUT_1" );
		map.put( "OUT_D_3", "GENERIC_DIGITAL_OUT_1" );
		hat.initialize( map );
		
		bActive = true;
		
		
		final Thread thread = new Thread( ()-> {
			try {
				while ( bActive ) {
					
					Thread.sleep( 200 );
					final long lTime = System.currentTimeMillis();
					
					final Float fValue1 = hat.getAnalogPortValue( Port.IN_A_1 );
					if ( null != fValue1 ) {
						monitor_1_Batt.add( lTime, fValue1 );
					}

					final Float fValue2 = hat.getAnalogPortValue( Port.IN_A_2 );
					if ( null != fValue2 ) {
						monitor_2_Accy.add( lTime, fValue2 );
					}
				

				}
			} catch ( final Exception e ) {
				// just quit
			}
		} );
		thread.start();
		
//		hat.registerChangeExec( port, ( map, lTime )-> {
//			final Float fValue = hat.getAnalogPortValue( port );
//			if ( null != fValue ) {
//				monitor_AccyVolts.add( lTime, fValue );
//			}
//		} );
	}
	
	
	public void setRelayState( final boolean bEnergized ) {
		hat.setPortValue( Port.OUT_R_1, bEnergized );
	}
	
	
	public Boolean getDigitalValue( final Port port ) {
		final Boolean bValue = hat.getDigitalPortValue( port );
		return bValue;
	}

	public Float getAnalogValue( final Port port ) {
		final Float fValue = hat.getAnalogPortValue( port );
		return fValue;
	}

	
	
	public void shutdown() {
//		LOGGER.info( ()-> "Shutting down..." );
		this.bActive = false;
		hat.close(); // note: control thread does not end
//		LOGGER.info( ()-> "Shutting down...Done." );
	}
	

}
