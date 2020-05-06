package jmr.s2.ingest;

import java.util.HashMap;
import java.util.Map;

import jmr.s2db.imprt.Summarizer;
import jmr.s2db.imprt.SummaryRegistry;

public class TeslaSummarizers {

	private final static Summarizer SUM_TESLA_CHARGE_STATE;
	
	
	private final static String VALUE_FULL_LEVEL = "90";
	
	
	public static void register() {
		SummaryRegistry.get().add( SUM_TESLA_CHARGE_STATE );
	}
	
	
	
	static {
		SUM_TESLA_CHARGE_STATE = new Summarizer() {

			@Override
			public boolean isMatch( final String strNodePath ) {
//				return "/External/Ingest/Tesla - CHARGE_STATE/data/response".
//								equals( strNodePath );
				final String strMatch = "/External/Ingest/Tesla - CHARGE_STATE";
				return strNodePath.contains( strMatch );
			}

			public Map<String,String> summarize( Object objData ) {
				// not implemented.
				return null;
			};
			
			@Override
			public Map<String, String> summarize( 
										final Map<String, String> input ) {
				if ( null==input ) return null;
				if ( input.isEmpty() ) return null;
				
				final Map<String,String> map = new HashMap<String,String>();
				
				
				// "Engaged"/"Disengaged"/ ?
				final String strPortLatch = input.get( "charge_port_latch" );
				final boolean bPortEngaged = 
									"Engaged".equalsIgnoreCase( strPortLatch );
				
//				// "true"/ ?
//				final String strPortOpen = input.get( "charge_port_door_open" );
//				final boolean bPortOpen = "true".equalsIgnoreCase( strPortOpen );
				
				// "0.0"/ ?
				final String strChargeRate = input.get( "charge_rate" );
				final boolean bRateZero = "0.0".equals( strChargeRate );
				
				// "90"/ ?
				final String strBatteryLevel = input.get( "battery_level" );
				final boolean bBatteryFull = 
									VALUE_FULL_LEVEL.equals( strBatteryLevel );
				
//				// "(null)"/"Complete"/ ?
//				final String strChargingState = input.get( "charging_state" );
				
				
				final String strStatus;
				if ( bPortEngaged ) {
					if ( bRateZero ) {
						if ( bBatteryFull ) {
							strStatus = "Port engaged, battery full";
						} else {
							strStatus = "Port engaged, not charging";
						}
					} else {
//						strStatus = "Charging, rate: " + strChargeRate;
						strStatus = "Charging, "
								+ "currently: " + strBatteryLevel + "%";
					}
				} else {
					strStatus = "Not connected. "
							+ "Battery: " + strBatteryLevel + "%";
				}
				
				map.put( "status", strStatus );
				
				return map;
			}
			
		};
	}
	
	
	
}
