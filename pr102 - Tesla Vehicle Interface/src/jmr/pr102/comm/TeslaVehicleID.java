package jmr.pr102.comm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.pr102.TeslaConstants;
import jmr.util.http.ContentRetriever;
import jmr.util.http.ContentType;
import jmr.util.transform.JsonUtils;

public class TeslaVehicleID {


	public static final TeslaVehicleID 
				DUMMY_VEHICLE_ID = new TeslaVehicleID( null, 0 );
	
	private final TeslaLogin login;
	
	private final int iVehicleIndex;
	
	private final Map<String,String> map = new HashMap<>();
	
	private JsonObject joVehicleData = null;
	
	
	public TeslaVehicleID(	final TeslaLogin login,
							final int iVehicleIndex ) {
		this.login = login;
		this.iVehicleIndex = iVehicleIndex;
	}
	
	
	
	private synchronized void loadVehicleData() {
		map.clear();

		final String strURL = TeslaConstants.URL_BASE_TESLA_API_PROD 
						+ "api/1/vehicles/";
		
		final ContentRetriever get = new ContentRetriever( strURL );
		
		try {
			final String strTokenValue = login.getTokenValue();
			final String strTokenType = login.getTokenType();
			get.addProperty( 
					"Authorization", strTokenType + " " + strTokenValue );

			final String strResponse = get.getContent( ContentType.APP_JSON );
			
			// response may look something like (except for 99999 patterns)
			// {"response":[{"id":17999993559999989,"vehicle_id":1199999195,"vin":"599991E23H9999994","display_name":"Nameless Midnight","option_codes":"RENA,AF02,APF1,APH2,APPB,AU01,BCMB,BP00,BR03,BS00,BX60,CDM0,CH05,PPSW,CW00,DCF0,DRLH,DSH7,DV4W,FG02,FR04,HC00,HP00,IDBA,INPTB,IX01,LP01,ME02,MI01,PF00,PI01,PK00,PS01,PX00,QTTP,RFP2,SC05,SP00,SR01,SU01,TM00,TP03,TR00,UTPB,WTAS,X001,X003,X007,X011,X013,X021,X025,X027,X028,X031,X037,X040,X044,YFFC,MDLS,BTX5,COUS","color":null,"tokens":["189999974999969f","9d9999992999999f"],"state":"online","in_service":null,"id_s":"17799993559599999","remote_start_enabled":true,"calendar_enabled":true,"notifications_enabled":true,"backseat_token":null,"backseat_token_updated_at":null}],"count":1}
			
			final JsonElement je = new JsonParser().parse( strResponse );
			if ( ! je.isJsonObject() ) {
				throw new IllegalStateException( 
						"Unexpected JSON structure (expected JsonObject): " 
								+ strResponse );
			}
			final JsonElement response = je.getAsJsonObject().get( "response" );
			if ( ! response.isJsonArray() ) {
				throw new IllegalStateException( 
						"Unexpected JSON structure (expected JsonArray): " 
								+ strResponse );
			}
			final JsonElement details = 
					response.getAsJsonArray().get( this.iVehicleIndex );
			
			if ( ! details.isJsonObject() ) {
				throw new IllegalStateException( 
						"Unexpected JSON structure (expected JsonObject): " 
								+ strResponse );
			}
			this.joVehicleData = details.getAsJsonObject();
			
			final Map<String,String> mapAdd = 
					JsonUtils.transformJsonToMap( joVehicleData );
			
	        this.map.putAll( mapAdd );

		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			System.err.println( "Exception while trying to load vehicle data." );
			for ( final Entry<String, String> entry : 
									get.getProperties().entrySet() ) {
				System.err.println( "\t" 
							+ entry.getKey() + " = " + entry.getValue() );
			}
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

	
	public JsonObject getVehicleDataResponse() {
		return this.joVehicleData;
	}
	
	
	
}
