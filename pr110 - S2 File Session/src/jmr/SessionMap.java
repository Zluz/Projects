package jmr;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import jmr.s2fs.FileSession;

@SuppressWarnings("serial")
//public class SessionMap extends HashMap<String,String> {
public class SessionMap extends EnumMap<Field,Element> {

//	public static enum Keys {
//		UNAME,		// getAllSystemInfo()
//		CONKY,		// getDeviceInfo()
//		IFCONFIG,	// getNetworkInterfaceInfo()
//		;
//	}
	
	final long lSnapshotTime;
	
	public SessionMap( final long lSnapshotTime ) {
		super( Field.class );
		this.lSnapshotTime = lSnapshotTime;
		this.fs = null;
	}
	
	
	final FileSession fs;

	
	
	public SessionMap(	final FileSession session,
						final long lSnapshotTime ) {
		super( Field.class );
		this.lSnapshotTime = lSnapshotTime;
		this.fs = session;
		if ( null!=session ) {
			this.put( Field.UNAME, session.getAllSystemInfo() );
			this.put( Field.CONKY, session.getDeviceInfo() );
			this.put( Field.IFCONFIG, session.getNetworkInterfaceInfo() );
		}
	}
	

	public SessionMap(	final Map<String,String> map,
						final long lSnapshotTime ) {
		super( Field.class );
		this.lSnapshotTime = lSnapshotTime;
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
	

	public Element put(	final Field key, 
						final String strValue ) {
		return super.put( key, new Element( strValue ) );
	}
	
	@Override
	public Element get( final Object key ) {
		try {
			iDepth++;
			if ( 5==iDepth ) return null;
			
			final Element strSuper = super.get( key );
			if ( null!=strSuper ) {
				return strSuper;
			} else {
				final Field field;
				Element strValue = null;
				if ( key instanceof Field ) {
					field = (Field) key;
				} else {
					field = Field.get( key.toString() );
				}
				strValue = evaluate( field );
				
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
	
	
	
	private Element evaluate( final Field field ) {
		if ( null==field ) return null;
		
		final String strValue;
		switch ( field ) {
			case IP: {
				return new Element( getIP( this ) );
			}
			case MAC: {
				final String[] values = getMAC( this );
				return new Element( values[0] );
			}
			case NIC: {
				final String[] values = getMAC( this );
				return new Element( values[1] );
			}
			case DESCRIPTION: {
				return new Element( getDescription( this ) );
			}
			case UNAME_FORMATTED: {
				final Element strUname = this.get( Field.UNAME );
				if ( null!=strUname && ( null!=strUname.get() ) ) {
					strValue = strUname.get().split( "\n" )[0];
				} else {
					strValue = "<no uname output>";
				}
				return new Element( strValue );
			}
			case TIMESTR_PAGE: {
				final Long lValue = this.getLong( Field.LAST_MODIFIED );
				return new Element( getISOTime( lValue ) );
			}
			case TIMEUXT_SCREENSHOT: {
				final File file = this.getScreenshots()[0];
				final long lModified;
				if ( null!=file ) {
					lModified = file.lastModified();
					return new Element( lModified );
				} else {
					strValue = "<no file>";
				}
				return new Element( strValue );
			}
			case FILE_SCREENSHOT: {
				final File file = this.getScreenshots()[0];
				return new Element( file );
			}
			case IMAGE_SCREENSHOT: {
				final File file = this.getScreenshots()[0];
				return new Element( file );
			}
			case OS_VERSION: {
				final Element eUname = this.get( Field.UNAME );
				if ( null==eUname ) return new Element( "<unknown>" );
				final String[] strParts = eUname.getAsString().split( " " );
				if ( "raspberrypi".equals( strParts[1] ) ) {
					final String strVersion = "Linux RPi " + strParts[2];
					return new Element( strVersion );
				}
				return new Element( "<unknown>" );
			}
			case TIMEELP_SESSION: {
				final Element eAge = this.get( Field.LAST_MODIFIED );
				if ( null==eAge ) return new Element( Long.MAX_VALUE );
				final Long lSessionAge = eAge.getAsLong();
				if ( null==lSessionAge ) return new Element( Long.MAX_VALUE );
				final long lElapsed = this.lSnapshotTime - lSessionAge;
				return new Element( lElapsed );
			}
			case TIMEELP_SCREENSHOT: {
				final File file = this.getScreenshots()[0];
				if ( null!=file ) {
					final long lModified = file.lastModified();
					final long lElapsed = this.lSnapshotTime - lModified;
					return new Element( lElapsed );
				}
				return new Element( Long.MAX_VALUE );
			}
			case SESSION_STATE: {
				final Long lElapsedScreenshot = 
						this.get( Field.TIMEELP_SCREENSHOT ).getAsLong();
				if ( lElapsedScreenshot > TimeUnit.HOURS.toMillis( 1 ) ) {
					return new Element( "Lost contact" );
				}
				final Long lElapsedSession = 
						this.get( Field.TIMEELP_SESSION ).getAsLong();
				if ( lElapsedSession > TimeUnit.DAYS.toMillis( 5 ) ) {
					return new Element( "Outdated" );
				}
				return new Element( "Current" );
			}
			default: {
				return null;
			}
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
		
		final Element element = this.get( field );
		if ( null==element ) return null;
		
		if ( null!=element.getAsLong() ) {
			return element.getAsLong();
		}
		
		final String strValue = element.getAsString();
		if ( null==strValue ) return null;
		
		try {
			final Long lValue = Long.parseLong( strValue );
			return lValue;
		} catch ( final NumberFormatException e ) {
			return null;
		}
	}
	
	
	public File[] getScreenshots() {
		if ( null!=fs ) {
			return fs.getScreenshotImageFiles();
		}
		return new File[] { null, null };
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
	
	
	public Map<String,Element> asMap() {
		final Map<String,Element> map = new HashMap<>();
		for ( final Entry<Field, Element> entry : this.entrySet() ) {
			map.put( entry.getKey().name(), entry.getValue() );
		}
		return map;
	}
	

//	public static String getIP( final Map<String,String> map ) {
	public static String getIP( final SessionMap map ) {
		if ( null==map ) return "<null>";
		
//		final String strDeviceIP = map.get( "device.ip" );
		final Element elementDeviceIP = map.get( Field.IP );
		final String strDeviceIP = 
				null!=elementDeviceIP ? elementDeviceIP.getAsString() : null;
		if ( null!=strDeviceIP ) return strDeviceIP;
		
//		final String str_ifconfig = Keys.IFCONFIG.name();
		final Element elementIfConfig = map.get( Field.IFCONFIG );
		final String str_ifconfig = 
				null!=elementIfConfig ? elementIfConfig.getAsString() : null;
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
		
		final Element eIfConfig = map.get( Field.IFCONFIG );
		final String str_ifconfig = 
							null!=eIfConfig ? eIfConfig.getAsString() : null;
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
		final Element eSession = map.get( Field.SESSION_ID );
		final String strSession = null!=eSession ? eSession.getAsString() : null;
		if ( null!=strSession ) {
			final String strsub = strSession.substring( 5, 22 );
			final String strMAC = S2FSUtil.normalizeMAC( strsub );
			return new String[] { strMAC, strNIC };
		}
		
		return new String[]{ "<unknown>", "<?>" };
	}
	
	


	public static String getDescription( final SessionMap map ) {
		if ( null==map ) return "<null>";
		
		final Element eDeviceName = map.get( Field.DEVICE_NAME );
		final String strDeviceName = 
						null!=eDeviceName ? eDeviceName.getAsString() : null;
		if ( null!=strDeviceName ) return strDeviceName;
		
		final Element eIfConfig = map.get( Field.CONKY );
		final String str_ifconfig = 
						null!=eIfConfig ? eIfConfig.getAsString() : null;
		if ( null!=str_ifconfig && !str_ifconfig.isEmpty() ) {
			return str_ifconfig;
		}
		return "<unknown>";
	}


	
	
	
	
	
}
