package jmr.sharedb.api;

import java.util.Map;

public interface ShareDB {


	public abstract static class Listener {
		public abstract void event();
	}
	
	public static interface Path {
		
		public String getPath();
		
		public long getLastModified();
		
	}
	
	
	public void register( final String strID );
	

	public Map<String,String> read( final Path path );
	
	
	
	
}
