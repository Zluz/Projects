package jmr.pr115.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jmr.CompositeSessionManager;
import jmr.Element;
import jmr.Field;
import jmr.SessionMap;

public class DeviceData {

	final long lSnapshotTime;

//	final FileSessionManager fsm = FileSessionManager.getInstance();
	final CompositeSessionManager csm;
	
	final public Map<Integer,String> mapSessionIndex = new HashMap<>();
	
	
	
	public DeviceData() {
		this.lSnapshotTime = System.currentTimeMillis();
		this.csm = new CompositeSessionManager( lSnapshotTime );
		
//		final Set<String> keys = fsm.getSessionKeys();
		final Set<String> keys = csm.getAllSessionData().keySet();
		int i = 0;
		for ( final String key : keys ) {
			mapSessionIndex.put( i, key );
			i++;
		}
	}
	
	public SessionMap getSessionMapForRow( final int iRow ) {
		if ( mapSessionIndex.containsKey( iRow ) ) {
			final String strMAC = mapSessionIndex.get( iRow );
			final SessionMap map = 
					csm.getAllSessionData().get( strMAC );
			return map;
		};
		return null;
	}
	
	public Element getValue(	final int iRow, 
							final Field col ) {
		if ( null!=col ) {
			final SessionMap sm = this.getSessionMapForRow( iRow );
			if ( null!=sm ) {
				return sm.get( col );
//				if ( null!=eValue ) {
//					final String strValue = eValue.getAsString();
//					if ( null!=strValue ) {
//						return strValue;
//					}
//				}
	//				System.out.println( "\tstrValue = " + strValue );
			}
		}

		//return "Row:" + iRow + ", Column:" + col.name();
		return new Element( "-" );
	}


	
	public static void main( final String[] args ) {
		final String strFile = "capture_vid0-t1565756724066.jpg";
		final String strRegex = "capture_vid[0-9]\\-t[0-9]+\\.jpg";
		System.out.println( "match: " + strFile.matches( strRegex ) );
	}
	
}
