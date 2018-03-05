import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jmr.CompositeSessionManager;
import jmr.FileSessionMap;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;
import jmr.s2fs.FileSession.SessionFile;

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
							final Column col ) {
		if ( mapSessionIndex.containsKey( iRow ) ) {
			final String strMAC = mapSessionIndex.get( iRow );
			
//			final FileSession session = fsm.getFileSession( strMAC );
//			final FileSessionMap map = new FileSessionMap( session );
			
//			final Map<String, String> 
			final FileSessionMap map = 
					new FileSessionMap( csm.getAllSessionData().get( strMAC ) );
			
			switch ( col ) {
				case SESSION_NAME: {
					return map.toString();
				}
				case MAC: {
					return strMAC;
				}
				case DEVICE_INFO: {
					return map.get( SessionFile.DEVICE_INFO.name() );
				}
				case SYSTEM_INFO: {
					return map.get( SessionFile.SYSTEM_INFO.name() );
				}
				case IP: {
					return map.getIP();
				}
				case DESCRIPTION: {
					return map.getDescription();
				}
			}
		}
		return "Row:" + iRow + ", Column:" + col.name();
	}
	
	
}
