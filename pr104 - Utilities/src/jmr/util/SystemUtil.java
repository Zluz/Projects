package jmr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;


public abstract class SystemUtil {


	private static final Logger 
					LOGGER = Logger.getLogger( SystemUtil.class.getName() );

	final public static String[] SYSTEM_PATH = {
									"S:\\settings.ini",
									"\\\\192.168.6.220\\Share\\settings.ini",
									"/Share/settings.ini"
	};
	
//	final public static String 
//				SYSTEM_PROPERTIES_FILE_WIN_1 = "S:\\settings.ini";
//	final public static String 
//				SYSTEM_PROPERTIES_FILE_UNIX = "/Share/settings.ini";
	
//	final public static String SYSTEM_PROPERTIES_FILE = OSUtil.isWin() 
//											? SYSTEM_PROPERTIES_FILE_WIN 
//											: SYSTEM_PROPERTIES_FILE_UNIX;

	final public static String PROCESS_KILL_SELF = 
								"/bin/kill -9 `pgrep -f pr101_ -U 1000`";

	private static Properties properties = null;
	
	private static boolean bShutdown = false;

	
	
	/* go to pr110:SessionPath instead */ 
	public static Properties getProperties() {
		if ( null==properties ) {
			properties = new Properties();
			try {
				for ( final String strPath : SYSTEM_PATH ) {
					final File file = new File( strPath );
					if ( file.isFile() ) {
						properties.load( new FileInputStream( strPath ) );
						return properties;
					}
				}
//				properties.load( new FileInputStream( SYSTEM_PROPERTIES_FILE ) );
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
						
						System.out.println( "Running: " + PROCESS_KILL_SELF );
						Runtime.getRuntime().exec( PROCESS_KILL_SELF );

						Thread.sleep( 3000 );
						System.out.println( "SystemUtil.shutdown()" );
						Runtime.getRuntime().halt( 1000 );

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


	public static long getPid() {
		final String strName = ManagementFactory.getRuntimeMXBean().getName();
		if ( ( null==strName ) || ( ! strName.contains( "@" ) ) ) {
			LOGGER.warning( "Failed to get the process name." );
			return 0; 
		}
		final String strPid = strName.split( "@" )[0];
		try {
			final long lPid = Long.parseLong( strPid );
			return lPid;
		} catch ( final NumberFormatException e ) {
			LOGGER.warning( ()-> "Failed to get the pid, "
										+ "encountered " + e.toString() );
			return 0;
		}
	}
	
	
	private static File fileTemp = null;
	
	public static File getTempDir() {
		if ( null==fileTemp ) {
			String strTempDir = System.getenv( "TEMP" );
			if ( StringUtils.isBlank( strTempDir ) ) {
				strTempDir = System.getenv( "TMP" );
			}
			if ( null!=strTempDir ) {
				fileTemp = new File( strTempDir );
			} else if ( ! OSUtil.isWin() ) {
				fileTemp = new File( "/tmp" );
			} else {
				fileTemp = new File( "." );
			}
		}
		return fileTemp;
	}
	
	
}
