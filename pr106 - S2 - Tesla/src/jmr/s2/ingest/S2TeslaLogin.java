package jmr.s2.ingest;

import java.util.Map;

import jmr.S2Properties;
import jmr.SettingKey;
import jmr.pr102.comm.TeslaLoginSimple;
import jmr.s2db.Client;

public class S2TeslaLogin extends TeslaLoginSimple {

	final private Client s2db;
	
//	final static public String PATH_LOGIN = "/External/Ingest/Tesla/Login";
	final static public String PATH_LOGIN = "/var/Tesla/Login";
	
	public S2TeslaLogin() {
//		super(	SystemUtil.getProperty( SUProperty.TESLA_USERNAME ),
//				SystemUtil.getProperty( SUProperty.TESLA_PASSWORD ).toCharArray() );
		super(	S2Properties.get().getValue( SettingKey.TESLAMOTORS_USERNAME ),
				S2Properties.get().getValue( SettingKey.TESLAMOTORS_PASSWORD ).toCharArray() );
		s2db = Client.get();
	
//		final S2Properties props = S2Properties.get();
//		final char[] cUsername = props.getValue( SettingKey.NEST_USERNAME ).toCharArray();
//		final char[] cPassword = props.getValue( SettingKey.NEST_PASSWORD ).toCharArray();

		
		final Map<String, String> map = s2db.loadPage( PATH_LOGIN );
		this.strTokenValue = map.get( "access_token" );
		this.strTokenType = map.get( "token_type" );
	}

	
	@Override
	public Map<String, String> login() throws Exception {
		final Map<String, String> map = super.login();

		map.put( "timestamp", ""+System.currentTimeMillis() );

		if ( map.containsKey( KEY_ACCESS_TOKEN ) ) {
			s2db.savePage( PATH_LOGIN, map );
		}
			
		return map;
	}
	
	
	
}
