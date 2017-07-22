package jmr.pr102.comm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.pr102.TeslaConstants;

public class TeslaVehicleID {


	public static final TeslaVehicleID 
				DUMMY_VEHICLE_ID = new TeslaVehicleID( null, 0 );
	
	private final TeslaLogin login;
	
	private final int iVehicleIndex;
	
	private final Map<String,String> map = new HashMap<>();
	
	public TeslaVehicleID(	final TeslaLogin login,
							final int iVehicleIndex ) {
		this.login = login;
		this.iVehicleIndex = iVehicleIndex;
	}
	
	
	
	private synchronized void loadVehicleData() {
		map.clear();

		final String strURL = TeslaConstants.URL_BASE_TESLA_API_PROD 
						+ "api/1/vehicles/";
		
		final HttpGet get = new HttpGet( strURL, this.login );
		
		try {
			final String strResponse = get.getContent();
			
			final JsonElement element = new JsonParser().parse( strResponse );
			final JsonElement response = element.getAsJsonObject().get( "response" );
			final JsonElement details = response.getAsJsonArray().get( this.iVehicleIndex );
			final JsonObject jo = details.getAsJsonObject();
			final Map<String,String> mapAdd = JsonUtils.transformJsonToMap( jo );
			
	        this.map.putAll( mapAdd );

		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String getVehicleID() {
		if ( DUMMY_VEHICLE_ID==this ) {
			return TeslaConstants.DUMMY_VEHICLE_ID;
		}
		if ( map.isEmpty() ) {
			this.loadVehicleData();
		}
		return map.get( "id" );
	}
	
	public synchronized void invalidate() {
		map.clear();
	}
	
	
	public Map<String,String> getVehicleData() {
		
		if ( map.isEmpty() ) {
			this.loadVehicleData();
		}
		return Collections.unmodifiableMap( map );
	}
	
	
	
}
