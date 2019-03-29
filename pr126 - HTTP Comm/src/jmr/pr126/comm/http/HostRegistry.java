package jmr.pr126.comm.http;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HostRegistry {

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
	}
	
	public URL getHost( final String strName ) {
		final URL url = this.mapServers.get( strName );
		return url;
	}
	
}
