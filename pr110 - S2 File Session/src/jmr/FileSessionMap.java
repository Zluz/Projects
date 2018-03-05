package jmr;

import java.util.HashMap;
import java.util.Map;

import jmr.s2fs.FileSession;

@SuppressWarnings("serial")
public class FileSessionMap extends HashMap<String,String> {

	public static enum Keys {
		UNAME,		// getAllSystemInfo()
		CONKY,		// getDeviceInfo()
		IFCONFIG,	// getNetworkInterfaceInfo()
		;
	}
	
	
	public FileSessionMap( final FileSession session ) {
		if ( null!=session ) {
			this.put( Keys.UNAME.name(), session.getAllSystemInfo() );
			this.put( Keys.CONKY.name(), session.getDeviceInfo() );
			this.put( Keys.IFCONFIG.name(), session.getNetworkInterfaceInfo() );
		}
	}
	
	public FileSessionMap( final Map<String,String> map ) {
		this.putAll( map );
	}

	public String getIP() {
		return FileSessionMap.getIP( this );
	}
	
	public String getDescription() {
		return FileSessionMap.getDescription( this );
	}


	public static String getIP( final Map<String,String> map ) {
		if ( null==map ) return "<null>";
		
		final String strDeviceIP = map.get( "device.ip" );
		if ( null!=strDeviceIP ) return strDeviceIP;
		
		final String str_ifconfig = Keys.IFCONFIG.name();
		if ( null!=str_ifconfig ) {
			final String[] strs = str_ifconfig.split( "\n" );
			for ( final String str : strs ) {
				if ( str.contains( "inet addr:192.168" ) ) {
					int iStart = str.indexOf( "addr:192.168" ) + 5;
					int iEnd = str.indexOf( " ", iStart );
					final String strsub = str.substring( iStart, iEnd );
					return strsub;
				}
				if ( str.contains( "inet 192.168" ) ) {
					int iStart = str.indexOf( "inet 192.168" ) + 5;
					int iEnd = str.indexOf( " ", iStart );
					final String strsub = str.substring( iStart, iEnd );
					return strsub;
				}
			}
		}
		return "<unknown>";
	}
	
	

	public static String[] getMAC( final Map<String,String> map ) {
		if ( null==map ) return new String[]{ "<null>", "<?>" };
		
		final String str_ifconfig = map.get( Keys.IFCONFIG.name() );
		String strNIC = "<?>";
		if ( null!=str_ifconfig ) {
			final String[] strs = str_ifconfig.split( "\n" );
			for ( final String str : strs ) {
				if ( !str.isEmpty() && !str.startsWith( " " ) ) {
					final String strsub = str.substring( 0, 8 );
					final int iPos = strsub.indexOf( ":" );
					if ( iPos > 0 ) {
						strNIC = strsub.substring( 0, iPos );
					} else {
						strNIC = strsub.trim();
					}
				}
				if ( str.contains( "HWaddr " ) ) {
					int iStart = str.indexOf( "HWaddr " ) + 7;
					int iEnd = str.length();
					final String strsub = str.substring( iStart, iEnd ).trim();
					final String strMAC = S2FSUtil.normalizeMAC( strsub );
					return new String[]{ strMAC, strNIC };
				}
				if ( str.trim().startsWith( "ether " ) ) {
					int iStart = str.indexOf( "ether " ) + 6;
					int iEnd = str.indexOf( "  ", iStart );
					final String strsub = str.substring( iStart, iEnd ).trim();
					final String strMAC = S2FSUtil.normalizeMAC( strsub );
					return new String[] { strMAC, strNIC };
				}
			}
		}
		
		strNIC = "<session>";
		final String strSession = map.get( "session.id" );
		if ( null!=strSession ) {
			final String strsub = strSession.substring( 5, 22 );
			final String strMAC = S2FSUtil.normalizeMAC( strsub );
			return new String[] { strMAC, strNIC };
		}
		
		return new String[]{ "<unknown>", "<?>" };
	}
	
	


	public static String getDescription( final Map<String,String> map ) {
		if ( null==map ) return "<null>";
		
		final String strDeviceName = map.get( "device.name" );
		if ( null!=strDeviceName ) return strDeviceName;
		
		final String str_ifconfig = map.get( Keys.CONKY.name() );
		if ( null!=str_ifconfig && !str_ifconfig.isEmpty() ) {
			return str_ifconfig;
		}
		return "<unknown>";
	}


	
	
	
	
	
}
