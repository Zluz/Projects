package jmr.pr102;

import java.nio.charset.StandardCharsets;

public interface TeslaConstants {

	final static String URL_BASE_TESLA_API_PROD = 
			"https://owner-api.teslamotors.com/";
	

	final static String REQUEST_CLIENT_ID = 
			"81527cff06843c8634fdc09e8ac0abefb46ac849f38fe1e431c2ef2106796384";
	
	final static String REQUEST_CLIENT_SECRET = 
			"c7257eb71a564034f9419ee651c7d0e5f7aa6bfbd18bafb5c5c033b093bb2fa3";

	final static String UTF8 = StandardCharsets.UTF_8.name();
	
	
	/*
	 * Controls through:
	 * 		DataRequest,
	 * 		Command,
	 * 		(ListVehicles)
	 * 
	 * other:
	 *		mobile_enabled 
	 * 	
	 */
	
	
}
