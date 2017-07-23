package jmr.sharedb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node extends HashMap<String,String> {

	private final String strPath;
	
	private final Server server;
	
	private final List<Listener> listeners = new LinkedList<Listener>();
	
	
	
	
	public Node(	final Server server,
					final String strPath ) {
		this.server = server;
		this.strPath = strPath;
	}
	
	
	public static interface Listener {
		public void changed();
	}
	
	
	public long getLastUpdated() {
		return 0;
	}
	
	public void addListener( final Listener listener ) {
		this.listeners.add( listener );
	}
	
	private void notifyListeners() {
		for ( final Listener listener : listeners ) {
			listener.changed();
		}
	}
	
	@Override
	public String put( final String key, final String value ) {
		final String result = super.put(key, value);
		notifyListeners();
		return result;
	}
	
	@Override
	public void putAll( final Map<? extends String, ? extends String> map ) {
		super.putAll( map );
		notifyListeners();
	}
	
	
	
	
}
