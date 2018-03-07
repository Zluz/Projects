package jmr;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import jmr.s2fs.FileSession;

@SuppressWarnings("serial")
//public class SessionMap extends HashMap<String,String> {
public class SessionMap extends EnumMap<Field,String> {

//	public static enum Keys {
//		UNAME,		// getAllSystemInfo()
//		CONKY,		// getDeviceInfo()
//		IFCONFIG,	// getNetworkInterfaceInfo()
//		;
//	}
	
	
	public SessionMap() {
		super( Field.class );
		this.fs = null;
	}
	
	
	final FileSession fs;

	
	
	public SessionMap( final FileSession session ) {
		super( Field.class );
		this.fs = session;
		if ( null!=session ) {
			this.put( Field.UNAME, session.getAllSystemInfo() );
			this.put( Field.CONKY, session.getDeviceInfo() );
			this.put( Field.IFCONFIG, session.getNetworkInterfaceInfo() );
		}
	}
	

	public SessionMap( final Map<String,String> map ) {
		super( Field.class );
		this.fs = null;
//		this.putAll( map );
		this.loadMap( map );
	}

//	@Override
//	public String get( final Field field ) {
//		// TODO Auto-generated method stub
//		return super.get(key);
//	}
	
	
	private int iDepth = 0;
	
	@Override
	public String get( final Object key ) {
		try {
			iDepth++;
			if ( 5==iDepth ) return null;
			
			final String strSuper = super.get(key);
			if ( null!=strSuper ) {
				return strSuper;
			} else {
				final Field field;
				String strValue = null;
				if ( key instanceof Field ) {
					field = (Field) key;
				} else {
					field = Field.get( key.toString() );
				}
				if ( null!=field ) {
					switch ( field ) {
						case IP: {
							strValue = getIP( this );
							break;
						}
						case MAC: {
							final String[] values = getMAC( this );
							strValue = values[0];
							break;
						}
						case NIC: {
							final String[] values = getMAC( this );
							strValue = values[1];
							break;
						}
						case DESCRIPTION: {
							strValue = getDescription( this );
							break;
						}
						case UNAME_FORMATTED: {
							final String strUname = this.get( Field.UNAME );
							if ( null!=strUname ) {
								strValue = strUname.split( "\n" )[0];
							} else {
								strValue = "<no uname output>";
							}
							break;
						}
						case TIMESTR_PAGE: {
							final Long lValue = 
									this.getLong( Field.LAST_MODIFIED );
							strValue = getISOTime( lValue );
							break;
						}
						case TIMEE_SCREENSHOT: {
							final File file = this.getScreenshot();
							final Long lModified;
							if ( null!=file ) {
								lModified = file.lastModified();
								strValue = Long.toString( lModified );
							} else {
								lModified = null;
								strValue = "<no file>";
							}
							break;
						}
						case TIMESTR_SCREENSHOT: {
							final File file = this.getScreenshot();
							final Long lModified;
							if ( null!=file ) {
								lModified = file.lastModified();
							} else {
								lModified = null;
							}
							strValue = getISOTime( lModified );
							break;
						}
						default: {
							
							break;
						}
					}
				}
				if ( null!=strValue ) {
					this.put( field, strValue );
					return strValue;
				}
			}
			return null;
		} finally {
			iDepth--;
		}
	}
	
	
	public static String getISOTime( final Long lEpoch ) {
		final String strValue;
		if ( null!=lEpoch ) {
			final LocalDateTime date = 
					LocalDateTime.ofInstant(
							Instant.ofEpochMilli( lEpoch ), 
                    TimeZone.getDefault().toZoneId() );
			strValue = date.toString();
		} else {
			strValue = "<invalid time>";
		}
		return strValue;
	}
	
	
	public Long getLong( final Field field ) {
		if ( null==field ) return null;
		final String strValue = this.get( field );
		if ( null==strValue ) return null;
		try {
			final Long lValue = Long.parseLong( strValue );
			return lValue;
		} catch ( final NumberFormatException e ) {
			return null;
		}
	}
	
	
	public File getScreenshot() {
		if ( null!=fs ) {
			return fs.getScreenshotImageFile();
		}
		return null;
	}
	
	
//	public String getIP() {
//		return SessionMap.getIP( this );
//	}
//	
//	public String getDescription() {
//		return SessionMap.getDescription( this );
//	}

	
	public void loadMap( final Map<String,String> map ) {
		for ( final Entry<String, String> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final Field field = Field.get( strKey );
			if ( null!=field ) {
				final String strValue = entry.getValue();
				this.put( field, strValue );
			}
		}
	}
	
	
	public Map<String,String> asMap() {
		final Map<String,String> map = new HashMap<>();
		for ( final Entry<Field, String> entry : this.entrySet() ) {
			map.put( entry.getKey().name(), entry.getValue() );
		}
		return map;
	}
	

//	public static String getIP( final Map<String,String> map ) {
	public static String getIP( final SessionMap map ) {
		if ( null==map ) return "<null>";
		
//		final String strDeviceIP = map.get( "device.ip" );
		final String strDeviceIP = map.get( Field.IP );
		if ( null!=strDeviceIP ) return strDeviceIP;
		
//		final String str_ifconfig = Keys.IFCONFIG.name();
		final String str_ifconfig = map.get( Field.IFCONFIG );
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
	
	

//	public static String[] getMAC( final Map<String,String> map ) {
	public static String[] getMAC( final SessionMap map ) {
		if ( null==map ) return new String[]{ "<null>", "<?>" };
		
		final String str_ifconfig = map.get( Field.IFCONFIG );
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
		final String strSession = map.get( Field.SESSION_ID );
		if ( null!=strSession ) {
			final String strsub = strSession.substring( 5, 22 );
			final String strMAC = S2FSUtil.normalizeMAC( strsub );
			return new String[] { strMAC, strNIC };
		}
		
		return new String[]{ "<unknown>", "<?>" };
	}
	
	


	public static String getDescription( final SessionMap map ) {
		if ( null==map ) return "<null>";
		
		final String strDeviceName = map.get( Field.DEVICE_NAME );
		if ( null!=strDeviceName ) return strDeviceName;
		
		final String str_ifconfig = map.get( Field.CONKY );
		if ( null!=str_ifconfig && !str_ifconfig.isEmpty() ) {
			return str_ifconfig;
		}
		return "<unknown>";
	}


	
	
	
	
	
}
