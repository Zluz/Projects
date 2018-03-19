package jmr.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public abstract class SystemUtil {


	//	final String strPropertiesFile = "H:\\Share\\settings.ini";
	final public static String SYSTEM_PROPERTIES_FILE = "S:\\settings.ini";

	final public static String PROCESS_KILL_SELF = "/bin/kill -9 `pgrep -f pr101_ -U 1000`";

	private static Properties properties = null;
	
	private static boolean bShutdown = false;

	
	
	public static Properties getProperties() {
		if ( null==properties ) {
			properties = new Properties();
			try {
				properties.load( new FileInputStream( SYSTEM_PROPERTIES_FILE ) );
			} catch ( final IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return properties;
	}

	
	public static String getProperty( final SUProperty property ) {
		if ( null==property ) return "";
		
		final String strName = property.getName();
		final String strValue = getProperties().getProperty( strName );
		return strValue;
	}
	
	
	public static void shutdown(	final int iExitCode,
									final String strMessage ) {
		if ( bShutdown ) {
			System.out.print( "Shutdown requested" );
			if ( null!=strMessage && !strMessage.isEmpty() ) {
				System.out.print( "(" + strMessage + ")" );
			}
			System.out.println();
			return;
		}
		System.out.print( "Shutting down" );
		if ( null!=strMessage && !strMessage.isEmpty() ) {
			System.out.print( " - " + strMessage );
		}
		System.out.println();
		
		if ( !OSUtil.isWin() ) {
			new Thread( "External process KILL thread" ) {
				@Override
				public void run() {
					try {
						Thread.sleep( 3000 );
						System.out.println( "Running: " + PROCESS_KILL_SELF );
						Runtime.getRuntime().exec( PROCESS_KILL_SELF );
					} catch ( final IOException | InterruptedException e ) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		
		new Thread( "Shutdown request" ) {
			@Override
			public void run() {
				System.exit( iExitCode );
			}
		}.start();
	}
	
	
}
