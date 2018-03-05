package jmr.s2db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jmr.S2FSUtil;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;

public class DBSessionManager {


	final static Map<String,Map<String,String>> 
									MAP_SESSIONS = new HashMap<>();
	
	
	public DBSessionManager() {
		this.scan();
	}
	
	
	private boolean scan() {
		
		final String strPath = "/Sessions/";

		final Map<String, Long> mapPageSeqs = 
						new Path().getChildPages( strPath, true );
		if ( null==mapPageSeqs ) {
			return false; // something bad happened, just quit.
		}
		
//		map.clear();
//		map.putAll( mapSessions );

		final Page page = new Page();
		
		synchronized ( MAP_SESSIONS ) {
			MAP_SESSIONS.clear();
			
			for ( final Entry<String, Long> entry : mapPageSeqs.entrySet() ) {
	
	//			final String strSession = entry.getKey();
				final long lPageSeq = entry.getValue();
				
				final Map<String, String> map = 
	//						Client.get().loadPage( strPath );
							page.getMap( lPageSeq );
				
				final String strMAC = map.get( "device.mac" );
				final String strNorm = S2FSUtil.normalizeMAC( strMAC );
				
				MAP_SESSIONS.put( strNorm, map );
			}
		}
		
		return true;
	}
	
	
	/**
	 * Return map of normalized MAC to active session page data 
	 * from the database.
	 * @return
	 */
	public Map<String,Map<String,String>> getSessionMap() {
		synchronized ( MAP_SESSIONS ) {
			return Collections.unmodifiableMap( MAP_SESSIONS );
		}
	}
	
	public boolean hasSession( final String strMAC ) {
		if ( null==strMAC ) return false;
		
		final String strNorm = S2FSUtil.normalizeMAC( strMAC );
		if ( MAP_SESSIONS.containsKey( strNorm ) ) {
			return true;
		}
		return false;
	}
	
	
}
