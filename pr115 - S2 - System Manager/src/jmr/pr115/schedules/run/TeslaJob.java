package jmr.pr115.schedules.run;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.S2Properties;
import jmr.SettingKey;
import jmr.pr102.Command;
import jmr.pr102.DataRequest;
import jmr.pr102.TeslaVehicleInterface;
import jmr.pr102.comm.TeslaLogin;
import jmr.pr115.actions.ReportTeslaNotCharging;
import jmr.s2.ingest.S2TeslaLogin;
import jmr.s2db.event.EventType;
import jmr.s2db.event.TimeEvent;
import jmr.s2db.imprt.WebImport;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;

public class TeslaJob extends JobWorker {
	
	private int iLowBatteryWarning = 70;

	public final static JsonParser PARSER = new JsonParser();
	
	private static TeslaVehicleInterface tvi = null;
	
	public final Set<DataRequest> setRequest = new HashSet<>();
	public final Map<Command,String> mapCommand = new HashMap<>();
	
	private final boolean bCheckLowCharge;
	
	
	public TeslaJob(	final boolean bCheckLowCharge,
						DataRequest... requests ) {
		this.bCheckLowCharge = bCheckLowCharge;
		for ( final DataRequest request : requests ) {
			setRequest.add( request );
		}
		
		new ReportTeslaNotCharging(); // instantiate to register action
	}
	
	public TeslaJob() {
		this( true, DataRequest.VEHICLE_STATE, DataRequest.CHARGE_STATE );
	}
	
	public void addJob( final Job job ) {
		if ( null==job ) return;
		
		final String strRequest = job.getRequest();
		final JobType type = job.getJobType();
		final boolean bRead = JobType.TESLA_READ.equals( type );
		final boolean bWrite = JobType.TESLA_WRITE.equals( type );

		System.out.println( "Adding job to TeslaJob: " + strRequest );
		
		if ( bRead || bWrite ) {
			final int iPos = strRequest.indexOf(":");
			final String strSub = strRequest.substring( iPos+1 ).trim();
			
			if ( bRead ) {
				final DataRequest request = DataRequest.getDataRequest( strSub );
				if ( null!=request ) {
					setRequest.add( request );
				}
			} else if ( bWrite ) {
				final Command command = Command.getCommand( strSub );
				final String strPost = null; //future: get this from the job
				if ( null!=command ) {
					mapCommand.put( command, strPost );
				}
			}
		}
	}
	
	
	private synchronized TeslaVehicleInterface getTVI() {
		if ( null==tvi ) {
			
		    /* make sure S2DB is initialized */
//		    Client.get();
		    
			final TeslaLogin login = new S2TeslaLogin();

			// see   jmr.s2.ingest.TeslaIngestManager
			tvi = new TeslaVehicleInterface( login );

			final Integer iLevel = S2Properties.get().getValueAsInt( 
									SettingKey.TESLA_LOW_BATTERY_THRESHOLD );
			if ( null!=iLevel ) {
				this.iLowBatteryWarning = iLevel;
			}
		}
		return tvi;
	}
	
	public boolean process(	final long lTime,
							final JsonObject joCombined ) {
		if ( null==joCombined ) return false;
		

		// useful:  https://www.freeformatter.com/json-formatter.html
		
//		for ( final Entry<String, JsonElement> entry : joResponse.entrySet() ) {
//			System.out.println( entry.getKey() + " = " + entry.getValue() );
//		}

		final boolean bHasChargeState;
		final boolean bHasVehicleState;

		int iBatteryLevel = 0;
		int iChargeRate = 0;
		boolean bHomelinkNearby = false;
//		String strAutoparkState = "";

		if ( joCombined.has( "battery_level" ) ) {
			bHasChargeState = true;
			iBatteryLevel = joCombined.get( "battery_level" ).getAsInt();
			iChargeRate = joCombined.get( "charge_rate" ).getAsInt();
		} else {
			bHasChargeState = false;
		}
		
		if ( joCombined.has( "homelink_nearby" ) ) {
			bHasVehicleState = true;
			bHomelinkNearby = joCombined.get( "homelink_nearby" ).getAsBoolean();
//			strAutoparkState = joCombined.get( "autopark_state_v2" ).getAsString();
		} else {
			bHasVehicleState = false;
		}
		
		
		if ( bCheckLowCharge && bHasVehicleState && bHasChargeState ) {
			if ( bHomelinkNearby
					&& ( iBatteryLevel < iLowBatteryWarning )
					&& ( 0 == iChargeRate ) ) {
				jmr.s2db.tables.Event.add( 
						EventType.TIME, 
						TimeEvent.TESLA_LOW_BATTERY.name(), 
						Integer.toString( iBatteryLevel ),
						Integer.toString( iLowBatteryWarning ),
						joCombined.toString(), 
						lTime, 
						null, null, null );
			}
		}
		
		return true;
	}
	
	public JsonObject getJsonObject( final String strResponse ) {
		if ( null==strResponse ) return new JsonObject();
		
		final JsonElement je = PARSER.parse( strResponse );
		final JsonObject jo = je.getAsJsonObject();
		
		final JsonElement jeResponse = jo.get( "response" );
		final JsonObject joResponse = jeResponse.getAsJsonObject();

		return joResponse;
	}
	
//	public boolean process( final String strResponse ) {
//		if ( null==strResponse ) return false;
//		
//		final JsonElement je = PARSER.parse( strResponse );
//		final JsonObject jo = je.getAsJsonObject();
//		
//		final JsonElement jeResponse = jo.get( "response" );
//		final JsonObject joResponse = jeResponse.getAsJsonObject();
//
//		return true;
//	}
	
	
	public JsonObject request() {

		final JsonObject joCombined = new JsonObject();

		try {
			
			System.out.println( "Calling Tesla REST"
						+ " (" + mapCommand.size() + " commands, " 
						+ setRequest.size() + " requests).." );
			
			for ( final Entry<Command, String> entry : mapCommand.entrySet() ) {
				final Command command = entry.getKey();
				final String strPost = entry.getValue();
				
				System.out.println( "Sending command to Tesla: " + command );
				final Map<String,String> map = 
								this.getTVI().command( command, strPost );
				
				System.out.println( "Result of command from Tesla: " 
						+ map.size() + " elements." ); 
			}
			
			for ( final DataRequest request : setRequest ) {
				
				// Call Tesla REST..
				System.out.println( "Requesting from Tesla: " + request );
				final String strResponse = this.getTVI().request( request );
				System.out.println( "Requested from Tesla " + request 
						+ ", response is " + strResponse.length() + " bytes long." ); 
				// an exception will pop out here, caught below..
				
				
				// Save to S2DB..  
				//   this was borrowed from:
				//     jmr.s2.ingest.TeslaIngestManager.requestToWebService()
				final WebImport ingest = new WebImport( 
								"Tesla - " + request.name(),
								tvi.getURL( request ),
								strResponse );
				final Long seq = ingest.save();
//				final String strResult = ingest.getResponse();
				System.out.println( "Requested from Tesla " + request 
						+ ", saved to page " + seq ); 
				
				
				// Add the response to the combined JSON
				final JsonObject joResponse = getJsonObject( strResponse );
				for ( final Entry<String, JsonElement> 
										entry : joResponse.entrySet() ) {
					joCombined.add( entry.getKey(), entry.getValue() );
				}
			}
			
			// add the vehicle data (sanitized)
			final JsonObject joVehicle = this.getTVI().getVehicleData();
			for ( final Entry<String, JsonElement> 
											entry : joVehicle.entrySet() ) {
				final String strKey = entry.getKey();
				final boolean bSensitive = 
						strKey.contains( "id" ) 
						|| strKey.contains( "vin" ) 
						|| strKey.contains( "token" );
				if ( !bSensitive ) {
					joCombined.add( entry.getKey(), entry.getValue() );
				}
			}
			
			
			System.out.println( "Combined Tesla response: "
					+ joCombined.size() + " entries" );

			return joCombined;
			
		} catch ( final Exception e ) {
			System.err.println( e.toString() + " encountered "
					+ "while communicating with Tesla." );
			e.printStackTrace();
			//TODO report error in an Event
			return null;
		}
	}
	
	
	@Override
	public boolean run() {
		
		final long lNow = System.currentTimeMillis();
		
		final JsonObject joCombined = request();

		if ( null!=joCombined ) {
			
			final boolean bResult = process( lNow, joCombined );
			
			ReportTeslaNotCharging.report( joCombined.toString() );
			
			return bResult;
			
		} else {
			//TODO report error in an Event
			return false;
		}
	}
	
	
	
	
	
	
	@SuppressWarnings("unused")
	public static void main( final String[] args ) {

		final TeslaJob tesla = new TeslaJob( false, DataRequest.VEHICLE_STATE );
//		final CallTesla tesla = new CallTesla( DataRequest.CHARGE_STATE );

//	    Client.get().register( "test", CallTesla.class.getName() );
//		tesla.run();
		
//		final String strExampleChargeState = "{\"response\":{\"charging_state\":\"Stopped\",\"fast_charger_type\":\"ACSingleWireCAN\",\"fast_charger_brand\":\"\\u003cinvalid\\u003e\",\"charge_limit_soc\":84,\"charge_limit_soc_std\":90,\"charge_limit_soc_min\":50,\"charge_limit_soc_max\":100,\"charge_to_max_range\":false,\"max_range_charge_counter\":0,\"fast_charger_present\":false,\"battery_range\":132.22,\"est_battery_range\":92.85,\"ideal_battery_range\":165.13,\"battery_level\":53,\"usable_battery_level\":53,\"charge_energy_added\":0.0,\"charge_miles_added_rated\":0.0,\"charge_miles_added_ideal\":0.0,\"charger_voltage\":null,\"charger_pilot_current\":null,\"charger_actual_current\":null,\"charger_power\":null,\"time_to_full_charge\":0.0,\"trip_charging\":null,\"charge_rate\":0.0,\"charge_port_door_open\":null,\"conn_charge_cable\":\"\\u003cinvalid\\u003e\",\"scheduled_charging_start_time\":null,\"scheduled_charging_pending\":false,\"user_charge_enable_request\":false,\"charge_enable_request\":false,\"charger_phases\":null,\"charge_port_latch\":\"\\u003cinvalid\\u003e\",\"charge_current_request\":48,\"charge_current_request_max\":48,\"managed_charging_active\":false,\"managed_charging_user_canceled\":false,\"managed_charging_start_time\":null,\"battery_heater_on\":false,\"not_enough_power_to_heat\":false,\"timestamp\":1521924536395}}";
//		final String strExampleVehicleState = "{\"response\":{\"api_version\":3,\"autopark_state\":\"unavailable\",\"autopark_state_v2\":\"standby\",\"autopark_style\":\"dead_man\",\"calendar_supported\":true,\"car_version\":\"2018.10.4 8bbdc66\",\"center_display_state\":0,\"df\":0,\"dr\":0,\"ft\":0,\"homelink_nearby\":true,\"last_autopark_error\":\"no_error\",\"locked\":true,\"notifications_supported\":true,\"odometer\":9243.997559,\"parsed_calendar_supported\":true,\"pf\":0,\"pr\":0,\"remote_start\":false,\"remote_start_supported\":true,\"rt\":0,\"sun_roof_percent_open\":0,\"sun_roof_state\":\"unknown\",\"timestamp\":1521925605446,\"valet_mode\":false,\"vehicle_name\":\"Nameless Midnight\"}}";
//		tesla.process( strExampleVehicleState );
	}
}
