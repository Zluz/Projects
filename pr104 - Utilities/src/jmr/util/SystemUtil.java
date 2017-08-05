package jmr.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public abstract class SystemUtil {


	//	final String strPropertiesFile = "H:\\Share\\settings.ini";
	final public static String SYSTEM_PROPERTIES_FILE = "S:\\settings.ini";
	
	private static Properties properties = null;

	
	
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
	
}
