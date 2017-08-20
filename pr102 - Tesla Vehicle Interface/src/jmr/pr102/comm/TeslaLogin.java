package jmr.pr102.comm;

import java.util.Map;

import jmr.pr102.TeslaConstants;

public interface TeslaLogin extends TeslaConstants {

	public Map<String,String> login() throws Exception;
		
	public Map<String,String> getLoginDetails();
	
	public void invalidate();

	public String getTokenValue() throws Exception;
	
	public String getTokenType() throws Exception;
	
	public boolean isAuthenticating();
	
}
