package jmr.util.devices;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public enum IPCamera {
	
	CAM_241,
	CAM_242,
	CAM_244,
	CAM_246,
	CAM_250,
	;
	
	private static Properties properties;
	
	private String strURL;
	private URL url;
	private String strHost;
	private String strTitle;
	
	public static void setProperties( final Properties properties ) {
		IPCamera.properties = properties;
	}
	
	private void load() {
		if ( null == strURL ) {
			this.strURL = properties.getProperty( this.name() + ".url" );
			this.strTitle = properties.getProperty( this.name() + ".title" );
			if ( null == strURL ) return; // special case, debug test ?
			try {
				this.url = new URL( strURL );
				this.strHost = this.url.getHost();
			} catch ( final MalformedURLException e ) {
				System.err.println( "Failed to read from URL: " + strURL );
				e.printStackTrace();
			}
		}
	}
	
	public URL getURL() {
		load();
		return this.url;
	}
	
	public String getHost() {
		load();
		return this.strHost;
	}

	public String getTitle() {
		load();
		return this.strTitle;
	}
	
}
