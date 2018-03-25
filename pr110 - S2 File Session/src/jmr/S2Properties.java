package jmr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@SuppressWarnings("serial")
public class S2Properties extends Properties {

	

	private static S2Properties instance;
	
//	private Properties properties;
	private boolean bInitialized = false;
	
	private S2Properties() {
//		properties = getProperties();
		getProperties();
	}
	
	public static S2Properties get() {
		if ( null==instance ) {
			instance = new S2Properties();
		}
		return instance;
	}

	
	public Properties getProperties() {
//		if ( null==properties ) {
		if ( !this.bInitialized ) {
			this.bInitialized = true;
			final Properties properties = new Properties();
			try {
				final File fileSessionPath = SessionPath.getPath();
				if ( null==fileSessionPath ) {
					return null;
				}
				final File fileBasePath = fileSessionPath.getParentFile();
				final File file = new File( fileBasePath, "settings.ini" ); 
				properties.load( new FileInputStream( file ) );
				
				this.putAll( properties );
				
			} catch ( final IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public String getValue( final SettingKey key ) {
		
		final String strSettingKey = key.name().toUpperCase();
		
		for ( final java.util.Map.Entry<Object, Object> entry : this.entrySet() ) {
			final String strRawEntryKey = entry.getKey().toString().trim();
			final String strEntryKey = strRawEntryKey.toUpperCase().replace( '.', '_' );
			if ( strSettingKey.equals( strEntryKey ) ) {
				return entry.getValue().toString();
			}
		}
		
		return "";
	}
	
	public Integer getValueAsInt( final SettingKey key ) {
		final String strValue = getValue( key );
		try {
			final Integer intValue = Integer.parseInt( strValue );
			return intValue;
		} catch ( final NumberFormatException e ) {
			return null;
		}
	}
	
	
}
