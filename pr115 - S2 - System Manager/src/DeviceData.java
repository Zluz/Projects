import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jmr.CompositeSessionManager;
import jmr.Field;
import jmr.SessionMap;

public class DeviceData {


//	final FileSessionManager fsm = FileSessionManager.getInstance();
	final CompositeSessionManager csm;
	
	final public Map<Integer,String> mapSessionIndex = new HashMap<>();
	
	
	
	public DeviceData() {
		this.csm = new CompositeSessionManager();
		
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
	
	public String getValue(	final int iRow, 
							final Field col ) {
		if ( null!=col ) {
			final SessionMap sm = this.getSessionMapForRow( iRow );
			if ( null!=sm ) {
				final String strValue = sm.get( col );
	//				System.out.println( "\tstrValue = " + strValue );
				
				if ( null!=strValue ) {
					return strValue;
				}
			}
		}

		//return "Row:" + iRow + ", Column:" + col.name();
		return "-";
	}


	
}
