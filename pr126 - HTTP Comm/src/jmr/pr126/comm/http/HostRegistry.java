package jmr.pr126.comm.http;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class HostRegistry {

	
	private static final Logger 
			LOGGER = Logger.getLogger( HostRegistry.class.getName() );

	private static HostRegistry instance;
	
	private Map<String,URL> mapServers = new HashMap<>();
	
	
	private HostRegistry() {}
	
	
	public static synchronized HostRegistry getInstance() {
		if ( null==instance ) {
			instance = new HostRegistry();
		}
		return instance;
	}
	
	public void register( final String strName, 
						  final URL url ) {
		this.mapServers.put( strName, url );
		
		LOGGER.info( ()-> "New alias registered: \"" + strName + "\"\n" 
								+ print() );
	}
	
	public String print() {
		final StringBuilder sb = new StringBuilder();
		for ( final Entry<String, URL> entry : mapServers.entrySet() ) {
			final URL url = entry.getValue();
			sb.append( "\"" + entry.getKey() + "\": " + url + "\n" );
		}
		return sb.toString();
	}
	
	public URL getHost( final String strName ) {
		final URL url = this.mapServers.get( strName );
		return url;
	}
	
}
