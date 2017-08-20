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
	

	public static final String HEADER_AUTHORIZATION = "Authorization";
	
	public static final String KEY_TOKEN_TYPE = "token_type";
	public static final String KEY_ACCESS_TOKEN = "access_token";

	
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
	
	// these can be used to avoid having to request a new key every time.
	// they will apply if the username or the password is missing.
	public static final String DUMMY_AUTH_TOKEN_VALUE = "";
	public static final String DUMMY_AUTH_TOKEN_TYPE = "bearer";
	public static final String DUMMY_VEHICLE_ID = "";
	
	public static final String PROPERTY_NAME_USERNAME = "teslamotors.username";
	public static final String PROPERTY_NAME_PASSWORD = "teslamotors.password";
	
}
