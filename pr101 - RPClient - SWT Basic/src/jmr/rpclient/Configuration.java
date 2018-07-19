package jmr.rpclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import jmr.util.NetUtil;
import jmr.util.OSUtil;

@Deprecated // use S2Properties
public class Configuration {

	final public static String PATH_SHARE = 
			OSUtil.isWin() 
					? "S:\\" 
					: "/Share";
	
	final public static String PATH_PROPERTIES =
			OSUtil.isWin() 
					? "S:\\settings.ini" 
//						"\\\\192.168.6.200\\Share\\settings.ini";
//						"H:\\Share\\settings.ini";
					: "/Share/settings.ini";
								
	
	
	final private Properties propSettings;
	
	
	private static Configuration instance;


	private Configuration() {
		propSettings = new Properties();
		
		try {
			final FileInputStream fis = new FileInputStream( PATH_PROPERTIES );
			propSettings.load( fis );
			
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	public static Configuration get() {
		if ( null==instance ) {
			instance = new Configuration();
		}
		return instance;
	}
	
	public String get( String strKey ) {
		return propSettings.getProperty( strKey );
	}
	
	
//	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
//		System.out.println( "Fake MAC: " + createFakeMAC() );
		
		System.out.println( "Process Name: " + NetUtil.getProcessName() );
		
		
		final Date now = new Date();
		final long lNow = now.getTime();
		System.out.println( "Time now: " + lNow );
		// 1500764595269
		System.out.println( "Time, GMT   : " + now.toGMTString() );
		
//		final long lTarget = 1500764595269l;
		final long lTarget = 1500764595269l;
		final Date dateTarget = new Date( lTarget );
		System.out.println( "Target, GMT : " + dateTarget.toGMTString() );
		
//		int iDays = 365 * 10; // 10 years
//		final long lDistance = TimeUnit.DAYS.toMillis( iDays );
//		final long lDistance = 1000000000000l;
		final long lDistance = 10000000000l;
							//  123456789012
		System.out.println( "Distance, long: " + lDistance );
		final Date dateBack = new Date( lNow - lDistance );
		System.out.println( "Past, GMT   : " + dateBack.toGMTString() );
		
		
		System.out.println( "Loading configuration.." );
		final Configuration config = Configuration.get();
		
		final String strUsername = config.get( "teslamotors.username" );
		System.out.println( "Username = " + strUsername );

//		final String strMID = config.getSessionID();
//		System.out.println( "Machine ID: " + strMID );
	}
	
	
}
