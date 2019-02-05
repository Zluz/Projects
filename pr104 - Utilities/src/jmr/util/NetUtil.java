package jmr.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public abstract class NetUtil {


	private static String strMAC = null;
	private static String strIP = null;
	private static int iIPConfidence = 0;
	
	
	public static String getMAC() {
		if ( null!=strMAC ) {
			return strMAC;
		}

		String strEvaluatedMAC = null;
		
		try {
			final Enumeration<NetworkInterface> nis = 
							NetworkInterface.getNetworkInterfaces();
			
			String strNIC_eth0 = null;
			String strNIC_any = null;
			
			for ( ; nis.hasMoreElements(); ) {
				final NetworkInterface ni = nis.nextElement();
				
				if ( !ni.isLoopback() ) {
					
					final byte mac[] = ni.getHardwareAddress();
					if ( null!=mac && mac.length>0 ) {
						final StringBuilder sb = new StringBuilder();
						for (int i = 0; i < mac.length; i++) {
							sb.append( String.format( "%02X%s", mac[i], 
									(i < mac.length - 1) ? "-" : "") );		
						}
						
						final String strDisplayName = ni.getDisplayName();
						
						strNIC_any = sb.toString();
						if ( strDisplayName.startsWith( "eth0" ) ) {
							strNIC_eth0 = sb.toString();
						}

//						final InetAddress address =
//								ni.getInetAddresses().nextElement();
						final Enumeration<InetAddress> 
										addresses = ni.getInetAddresses();

//						strIP = address.getHostAddress();
//						strIP = address.
						for ( ; addresses.hasMoreElements(); ) {
							final InetAddress next = addresses.nextElement();
							final String strAddrText = next.toString();
//							System.out.println( "IP Address candidate: " + strAddrText );							
							final int iConfidence;
//							if ( strAddrText.contains( "eth0" ) ) {
							if ( strAddrText.contains( "192.168." ) ) {
								final String strLastOct = strAddrText.split("\\.")[3];
//								System.out.println( "\tlast oct = " + strLastOct );							
								iConfidence = 100 
										+ Integer.parseInt( strLastOct );
							} else {
								iConfidence = 10;
							}
//System.out.println( "\tconfidence = " + iConfidence );							
							final String strAddress = strAddrText.split("/")[1];
							if ( !strAddress.contains(":") ) {
								if ( iConfidence>iIPConfidence ) {
									strIP = strAddress;
									iIPConfidence = iConfidence;
								}
//								System.out.println( "IP Address resolved to: " + strIP );
							}
						}
						
//						System.out.println( "mac: " + sb.toString() 
//								+ ", display name: \"" + strDisplayName + "\""
////								+ ", loopback: " + ni.isLoopback() 
//								);
					} else {
//						final String strDisplayName = ni.getDisplayName();
//						
//						System.out.println( "(NO MAC)" 
//								+ ", display name: \"" + strDisplayName + "\""
////								+ ", loopback: " + ni.isLoopback() 
//								);
						
					}
				}
			}
			
			if ( null!=strNIC_eth0 ) {
				strEvaluatedMAC = strNIC_eth0;
			} else if ( null!=strNIC_any ) {
				strEvaluatedMAC = strNIC_any;
			}

		} catch ( final SocketException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( null!=strEvaluatedMAC ) {
			strMAC = strEvaluatedMAC;
			return strEvaluatedMAC;
		} else {
			return createFakeMAC();
		}
	}
	
	
	private static String strMachineID;
	
	public static String getSessionID() {
		if ( null==strMachineID ) {
			
			final long lTime = new Date().getTime();
//				final String strTimeNow = Long.toString( lTime );
//				final int iLen = strTimeNow.length();
//				final String strMark = strTimeNow.substring( iLen - 10 );
			final String strMark = String.format( "%011X", lTime );
			
			final String strProcessName = getProcessName();
			final String strPID = strProcessName.split( "@" )[0];
			final Long lPID = Long.parseLong( strPID );
			final String strPIDx = String.format( "%05X", lPID );
			
			final String strOS = 
					( OSUtil.isWin() ? "W" : "L" ) 
					+ OSUtil.getArch();

			strMachineID = 
					strOS + "--" 
					+ getMAC() + "--" 
					+ strPIDx + "--" 
					+ strMark;
		}
		return strMachineID;
	}
	
	
	public static String createFakeMAC() {
		final StringBuilder sb = new StringBuilder( "ZZ" );
		for ( int i=0; i<5; i++ ) {
			sb.append( '-' );
			sb.append( (char)( ('Z'-'A') * Math.random() + 'A' ) );
			sb.append( (char)( ('Z'-'A') * Math.random() + 'A' ) );
		}
		return sb.toString();
	}
	

	public static String getIPAddress() {
		final Map<String, String> map = getIPAddresses( true );
		if ( null!=map && ! map.isEmpty() ) {
			return map.values().iterator().next();
		} else {
			return "127.0.0.1";
		}
	}

	
	public static String getIPAddress_() {
		if ( null==strIP ) {
			getMAC();
			if ( null==strIP ) {
				try {
					final InetAddress ip = InetAddress.getLocalHost();
					strIP = ip.getHostAddress();
					iIPConfidence = 1;
				} catch ( final UnknownHostException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return strIP;
	}
	
	
	
	
	
	// from: https://stackoverflow.com/questions/494465/how-to-enumerate-ip-addresses-of-all-enabled-nic-cards-from-java
	public static Map<String,String> getIPAddresses( final boolean bIncludeLocal ) {
		
		final Map<String,String> map = new HashMap<>();
		
//		try {
//			final InetAddress localhost = InetAddress.getLocalHost();
//			// LOG.info(" IP Addr: " + localhost.getHostAddress());
//			// Just in case this host has multiple IP addresses....
//			final InetAddress[] arrAddresses = InetAddress.getAllByName(
//							localhost.getCanonicalHostName() );
//			
//			if ( arrAddresses != null && arrAddresses.length > 0 ) {
//				// LOG.info(" Full list of IP addresses:");
//				for ( final InetAddress address : arrAddresses ) {
////				for (int i = 0; i < allMyIps.length; i++) {
//					// LOG.info(" " + allMyIps[i]);
////					list.add( address.getHostAddress() );
////					address.get
//					String strNIC = null;
//					try {
//						final NetworkInterface nic = 
//								NetworkInterface.getByInetAddress( address );
//						if ( null!=nic ) {
//							strNIC = nic.getName();
//						} else {
//							strNIC = "<Null NIC>";
//						}
//					} catch ( final Exception e ) {
//						strNIC = "<Unknown NIC," + e.toString() + ">";
//					}
//					map.put( strNIC, address.getHostAddress() );
//				}
//			}
//		} catch ( final UnknownHostException e ) {
//			// LOG.info(" (error retrieving server host name)");
//		}
		
//		return map;

		try {
			// LOG.info("Full list of Network Interfaces:");
			for (	final Enumeration<NetworkInterface> 
							enNIC = NetworkInterface.getNetworkInterfaces(); 
					enNIC.hasMoreElements(); ) {
				final NetworkInterface nic = enNIC.nextElement();
				// LOG.info(" " + intf.getName() + " " + intf.getDisplayName());
				for (	final Enumeration<InetAddress> 
								enAddr = nic.getInetAddresses(); 
						enAddr.hasMoreElements(); ) {
					// LOG.info(" " + enumIpAddr.nextElement().toString());
					
					boolean bGood = true;

					final String strName = nic.getName();
					if ( ! bIncludeLocal && strName.startsWith( "lo" ) ) {
						bGood = false;
					}

					final InetAddress addr = enAddr.nextElement();
					final String strHostAddr = addr.getHostAddress();
					if ( ! strHostAddr.contains( "." ) ) { // only ipv4
						bGood = false;
					}
					
					if ( bGood ) {
						map.put( nic.getName(), strHostAddr );
					}
				}
			}
		} catch (SocketException e) {
			// LOG.info(" (error retrieving network interface list)");
		}

		final Map<String,String> mapSorted = new TreeMap<>( 
//				( Comparator<String> ) ( lhs, rhs ) -> rhs.compareTo( lhs ) );
				( Comparator<String> ) ( lhs, rhs ) -> {  

//					lhs = lhs.replace( "eth", "0" ).replace( "wlan", "1" ).replace( "lo", "9" );
//					rhs = rhs.replace( "eth", "0" ).replace( "wlan", "1" ).replace( "lo", "9" );
					
					lhs = sortableNICName( lhs );
					rhs = sortableNICName( rhs );
//					
//					return rhs.compareTo( lhs );
					return lhs.compareTo( rhs );
					
//					rhs.replace( "eth", "0" ).replace( "wlan", "1" ).compareTo(
//							lhs.replace( "eth", "0" ).replace( "wlan", "1" ) )
					
//					return strR.compareTo( strL );
				} );
		
		mapSorted.putAll( map );
		
		return mapSorted;
	}
	
	
	private static String sortableNICName( final String strNICName ) {
		final String strSortable = strNICName
										.replace( "eth", "0" )
										.replace( "wlan", "1" )
										.replace( "lo", "9" );
		return strSortable;
	}
	
	
	public static String getProcessName() {
		final String strName = ManagementFactory.getRuntimeMXBean().getName();
		return strName;
	}
	
	
	
//	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
//		System.out.println( "Fake MAC: " + createFakeMAC() );
		
		System.out.println( "Process Name: " + getProcessName() );
		
		System.out.println( "IP Address: " + getIPAddress() );
		
		final Map<String, String> map = getIPAddresses( true );
		System.out.println( "All addresses:" );
		for ( final Entry<String, String> entry : map.entrySet() ) {
			System.out.println( "\t" + entry.getKey() + " : " + entry.getValue() );
		}
		
//		final Date now = new Date();
//		final long lNow = now.getTime();
//		System.out.println( "Time now: " + lNow );
//		// 1500764595269
//		System.out.println( "Time, GMT   : " + now.toGMTString() );
//		
////		final long lTarget = 1500764595269l;
//		final long lTarget = 1500764595269l;
//		final Date dateTarget = new Date( lTarget );
//		System.out.println( "Target, GMT : " + dateTarget.toGMTString() );
//		
////		int iDays = 365 * 10; // 10 years
////		final long lDistance = TimeUnit.DAYS.toMillis( iDays );
////		final long lDistance = 1000000000000l;
//		final long lDistance = 10000000000l;
//							//  123456789012
//		System.out.println( "Distance, long: " + lDistance );
//		final Date dateBack = new Date( lNow - lDistance );
//		System.out.println( "Past, GMT   : " + dateBack.toGMTString() );
		
		
//		System.out.println( "Loading configuration.." );
//		final Configuration config = Configuration.get();
//		
//		final String strUsername = config.get( "teslamotors.username" );
//		System.out.println( "Username = " + strUsername );

//		final String strMID = config.getSessionID();
//		System.out.println( "Machine ID: " + strMID );
	}
	
	
}
