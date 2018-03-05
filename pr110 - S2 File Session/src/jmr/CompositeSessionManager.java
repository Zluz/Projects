package jmr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jmr.s2db.DBSessionManager;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;

public class CompositeSessionManager {

	
	public final static Map<String,Map<String,String>> MAP = new HashMap<>();
	
	private final DBSessionManager dbsm;
	private final FileSessionManager s2sm;
	
	
	public CompositeSessionManager() {
		dbsm = new DBSessionManager();
		s2sm = FileSessionManager.getInstance();
		this.collect();
	}
	
	
	private void collect() {
		
		MAP.clear();
		
		for ( final Entry<String, Map<String, String>> 
								entry : dbsm.getSessionMap().entrySet() ) {
			final String strMAC = entry.getKey();
			final Map<String,String> mapData = entry.getValue();
			MAP.put( strMAC, mapData );
		}
		
		for ( final Entry<String, FileSession> 
								entry : s2sm.getSessionMap().entrySet() ) {
			final String strMAC = entry.getKey();
			
			final Map<String,String> map;
			if ( MAP.containsKey( strMAC ) ) {
				map = MAP.get( strMAC );
			} else {
				map = new HashMap<String,String>();
				MAP.put( strMAC, map );
			}
			
			final FileSessionMap fsm = new FileSessionMap( entry.getValue() );
			map.putAll( fsm );
		}
	}
	
	
	public Map<String,Map<String,String>> getAllSessionData() {
		return Collections.unmodifiableMap( MAP );
	}
	
	
}
