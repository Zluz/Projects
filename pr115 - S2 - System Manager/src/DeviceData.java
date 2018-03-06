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

	public String getValue(	final int iRow, 
							final Field col ) {
		if ( mapSessionIndex.containsKey( iRow ) ) {
			final String strMAC = mapSessionIndex.get( iRow );
			
//			final FileSession session = fsm.getFileSession( strMAC );
//			final FileSessionMap map = new FileSessionMap( session );
			
//			final Map<String, String> 
			final SessionMap map = 
//					new SessionMap( csm.getAllSessionData().get( strMAC ) );
					csm.getAllSessionData().get( strMAC );
			
//			System.out.println( "getValue(), iRow:" + iRow + ", col:" + col );
//			System.out.println( "\tmap = " + map.toString() );
			
			if ( null!=col ) {
				final String strValue = map.get( col );
//				System.out.println( "\tstrValue = " + strValue );
				
				if ( null!=strValue ) {
					return strValue;
				}
			}
			
//			switch ( col ) {
//				case SESSION_NAME: {
//					return map.toString();
//				}
//				case MAC: {
//					return strMAC;
//				}
//				case DEVICE_INFO: {
//					return map.get( Field.DEVICE_INFO );
//				}
//				case SYSTEM_INFO: {
//					return map.get( Field.SYSTEM_INFO );
//				}
//				case IP: {
////					return map.getIP();
//					return map.get( Field.IP );
//				}
//				case DESCRIPTION: {
////					return map.getDescription();
//					return map.get( Field.DESCRIPTION );
//				}
//			}
		}
		return "Row:" + iRow + ", Column:" + col.name();
	}
	
	
}
