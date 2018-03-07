package jmr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jmr.s2db.DBSessionManager;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;

public class CompositeSessionManager {

	
//	public final static Map<String,Map<String,String>> MAP = new HashMap<>();
	public final static Map<String,SessionMap> MAP = new HashMap<>();
	
	private final DBSessionManager dbsm;
	private final FileSessionManager s2sm;
	
	
	public CompositeSessionManager() {
		dbsm = new DBSessionManager();
		s2sm = FileSessionManager.getInstance();
		this.collect();
	}
	
	
	private void collect() {
		
		MAP.clear();
		
		
		
		for ( final Entry<String, FileSession> 
								entry : s2sm.getSessionMap().entrySet() ) {
			final String strMAC = entry.getKey();

			final SessionMap fsm = new SessionMap( entry.getValue() );

			MAP.put( strMAC, fsm );

//			final Map<String,String> map;
			final SessionMap map;
			if ( MAP.containsKey( strMAC ) ) {
				map = MAP.get( strMAC );
			} else {
//				map = new HashMap<String,String>();
				map = new SessionMap();
				MAP.put( strMAC, map );
			}
			
			
//			map.putAll( fsm );
			for ( final Entry<Field, String> e : fsm.entrySet() ) {
				fsm.put( e.getKey(), e.getValue() );
			}
		}
		
		

		for ( final Entry<String, Map<String, String>> 
								entry : dbsm.getSessionMap().entrySet() ) {
			
			final String strMAC = entry.getKey();
			final Map<String,String> mapData = entry.getValue();
			
			final SessionMap sm = new SessionMap( mapData );
			
			
//			final Map<String,String> map;
			
			final SessionMap map;
			
			if ( MAP.containsKey( strMAC ) ) {
				map = MAP.get( strMAC );
			} else {
				map = new SessionMap();
				MAP.put( strMAC, map );
			}
			
			for ( final Entry<Field, String> e : sm.entrySet() ) {
				map.put( e.getKey(), e.getValue() );
			}
			
//			MAP.put( strMAC, sm );
		}
		
		
		
		
	}
	
	
	public Map<String,SessionMap> getAllSessionData() {
		return Collections.unmodifiableMap( MAP );
	}
	
	
}
