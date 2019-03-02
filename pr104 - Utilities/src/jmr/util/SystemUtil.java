package jmr.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


public abstract class SystemUtil {


	//	final String strPropertiesFile = "H:\\Share\\settings.ini";
	final public static String SYSTEM_PROPERTIES_FILE_WIN = "S:\\settings.ini";
	final public static String SYSTEM_PROPERTIES_FILE_UNIX = "/Share/settings.ini";
	final public static String SYSTEM_PROPERTIES_FILE = OSUtil.isWin() 
											? SYSTEM_PROPERTIES_FILE_WIN 
											: SYSTEM_PROPERTIES_FILE_UNIX;

	final public static String PROCESS_KILL_SELF = 
								"/bin/kill -9 `pgrep -f pr101_ -U 1000`";

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
	

	public static List<String> getProperties( final SUProperty property ) {
		final List<String> list = new LinkedList<>();
		if ( null==property ) return list;
		
		int i = 1;
		boolean bFound = true;
		while ( bFound ) {
			final String strName = 
						property.getName() + "." + String.format("%03d", i );
			final String strValue = getProperties().getProperty( strName );
			if ( null!=strValue && !strValue.isEmpty() ) {
				bFound = true;
				list.add( strValue );
			} else {
				bFound = false;
			}
			i++;
		}
		return list;
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
			new Thread( "Call HALT, Run KILL" ) {
				@Override
				public void run() {
					try {
						Thread.sleep( 3000 );
						Runtime.getRuntime().halt( 1000 );
						
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
