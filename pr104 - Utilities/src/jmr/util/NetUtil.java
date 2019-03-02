package jmr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public abstract class NetUtil {


	private static final Logger 
					LOGGER = Logger.getLogger( NetUtil.class.getName() );

	
//	private final static ConcurrentHashMap<String, String[]> MAP_INTERFACES = 
//					new ConcurrentHashMap<String, String[]>();
					
    private final static Map<String,String[]> 
				    		MAP_INTERFACES = new TreeMap<>(
								( Comparator<String> ) ( lhs, rhs ) -> {
									return rhs.compareTo( lhs );
								}
				    		);
				    		
    private static long lScanTime = 0;
    
    private final static long SCAN_EXPIRE = TimeUnit.HOURS.toMillis( 1 );
	
	
	
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
		final Map<String, String[]> map = getNetworkInterfaces();
		if ( null!=map && ! map.isEmpty() ) {
			for ( final Entry<String, String[]> entry : map.entrySet() ) {
				final String strIP = entry.getValue()[0];
				if ( strIP.contains( "192.168." ) ) {
					return strIP;
				}
			}
		}
		return "127.0.0.1";
	}
	

	public static String getMAC() {
		final Map<String, String[]> map = getNetworkInterfaces();
		if ( null!=map && ! map.isEmpty() ) {
			final String[] arrValues = map.values().iterator().next();
			return arrValues[1];
		} else {
			return "00-00-00-00-00-00-00-00";
		}
	}
	
	
	
	public static Map<String,String> getIPAddresses() {
		final Map<String,String> mapResult = new HashMap<>();
		
		final Map<String, String[]> map = getNetworkInterfaces();
		if ( null!=map && ! map.isEmpty() ) {
			for ( final Entry<String, String[]> entry : map.entrySet() ) {
				final String[] arrValues = entry.getValue();
				final String strIP = arrValues[0];
				if ( strIP.contains( "192.168." ) ) {
					mapResult.put( arrValues[3], strIP );
				}
			}
		}
		return mapResult;
	}
		
	

	public synchronized static Map<String,String[]> getNetworkInterfaces() {
		
		final long lNow = System.currentTimeMillis();
		if ( lScanTime + SCAN_EXPIRE > lNow ) {
			return MAP_INTERFACES;
		}
		
		final Map<String,String[]> map = new HashMap<>();

		boolean bHas_eth0 = false;
		
		try {
			// LOG.info("Full list of Network Interfaces:");
			final Enumeration<NetworkInterface> 
						enNIC = NetworkInterface.getNetworkInterfaces();
			while ( enNIC.hasMoreElements() ) {
				final NetworkInterface nic = enNIC.nextElement();

				final String strName = nic.getName();

				if ( "eth0".equals( strName ) ) {
					bHas_eth0 = true;
				}
				
				final byte mac[] = nic.getHardwareAddress();
				final StringBuilder sbMAC = new StringBuilder();
				if ( null != mac && mac.length > 0 ) {
					for (int i = 0; i < mac.length; i++) {
						sbMAC.append( String.format( "%02X%s", mac[i], 
								(i < mac.length - 1) ? "-" : "") );		
					}
				}
				if ( 0 == sbMAC.length() ) {
					sbMAC.append( "<no_mac>" );
				}
				
				final Enumeration<InetAddress> enAddr = nic.getInetAddresses();
				while ( enAddr.hasMoreElements() ) {
					final InetAddress addr = enAddr.nextElement();
					
					final String strHostAddr = addr.getHostAddress();
					
					/* position ('1'=true, '0'=false)
					 * 0 - is not-loopback
					 * 1 - has mac
					 * 2 - is not-wlan
					 * 3 - has IPv4 address
					 */
					final StringBuilder sbRank = new StringBuilder();
					sbRank.append( nic.isLoopback() ? "0" : "1" );
					sbRank.append( sbMAC.length()>0 ? "1" : "0" );
					sbRank.append( strName.contains( "wlan" ) ? "0" : "1" );
					sbRank.append( strHostAddr.contains( "." ) ? "1" : "0" );
					
					final String[] arrValues = new String[] { 
											strHostAddr,  		// 1
											sbMAC.toString(), 	// 2
											strName, 			// 3
											nic.getDisplayName() // 4
										};
					map.put( sbRank.toString() + ":" + strName, arrValues );
				}
			}
		} catch (SocketException e) {
			// LOG.info(" (error retrieving network interface list)");
		}
		
		if ( ! bHas_eth0 && ! OSUtil.isWin() ) {
			try {
				final String strCommand = 
//								"/sbin/ifconfig eth0 | /bin/grep -i \"B8:\"";
								"/sbin/ifconfig eth0";
				final Process process = Runtime.getRuntime().exec( strCommand );
				process.waitFor( 1000L, TimeUnit.MILLISECONDS );
				if ( process.isAlive() ) {
					LOGGER.warning( "Process ifconfig is taking too long." );
					process.waitFor( 2000L, TimeUnit.MILLISECONDS );
				}
				final InputStreamReader isr = 
							new InputStreamReader( process.getInputStream() );
				try ( final BufferedReader br = new BufferedReader( isr ) ) {
					final String strOutput = br.lines().collect( 
									Collectors.joining( "\n" ) );
					
//					System.out.println( "IFCONFIG: " + strOutput );

					final String strMAC = getMAC( strOutput );

//					System.out.println( "MAC: \"" + strMAC + "\"" );

					if ( StringUtils.isNotBlank( strMAC ) ) {
						final String[] arrValues = new String[] {
											"<no_ip>",
											strMAC,
											"eth0",
											"eth0 (not connected)"
										};
						map.put( "1110:eth0", arrValues );
					}
				}
			} catch (IOException | InterruptedException e) {
				LOGGER.warning( "Failed to run process ifconfig." );
			}
		}

		final Map<String,String[]> mapSorted = new TreeMap<>(
				( Comparator<String> ) ( lhs, rhs ) -> {
					return rhs.compareTo( lhs );
				}
		);

		mapSorted.putAll( map );
		
//		System.out.println( "Network Interfaces:" );
//		for ( final Entry<String, String[]> entry : mapSorted.entrySet() ) {
//			final String strKey = entry.getKey();
//			final String[] arrValues = entry.getValue();
//			System.out.println( "\t" + strKey );
//			for ( final String strValue : arrValues ) {
//				System.out.println( "\t\t" + strValue );
//			}
//		}
		
		synchronized ( MAP_INTERFACES ) {
			MAP_INTERFACES.clear();
			MAP_INTERFACES.putAll( mapSorted );
			lScanTime = System.currentTimeMillis();
		}
		
		return mapSorted;
	}

	
	public static boolean isMACChar( final char c ) {
		if ( Character.isDigit( c ) ) return true;
		if ( Character.isAlphabetic( c ) ) return true; // well, almost.
		if ( '-' == c ) return true;
		if ( ':' == c ) return true;
		return false;
	}
	
	
	public static String getMAC( final String strInput ) {
		if ( StringUtils.isBlank( strInput ) ) return null;

		final String strUpper = " " + strInput.trim().toUpperCase() + " ";
		
		final int iPos = strUpper.indexOf( "B8:" );
		int iStart = iPos;
		int iEnd = iPos;
		while ( iStart > 0 && isMACChar( strUpper.charAt( iStart ) ) ) {
			iStart--;
		}
		while ( iEnd < strUpper.length() 
							&& isMACChar( strUpper.charAt( iEnd ) ) ) {
			iEnd++;
		}
		final String strMAC = strUpper.substring( iStart + 1, iEnd )
											.replaceAll( ":", "-" );
		return strMAC;
	}
	

	
	
	public static String getProcessName() {
		final String strName = ManagementFactory.getRuntimeMXBean().getName();
		return strName;
	}
	
	
	
//	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws SocketException {
		
		getNetworkInterfaces();
		
		
		
//		System.out.println( "Fake MAC: " + createFakeMAC() );
		
		System.out.println( "Process Name: " + getProcessName() );
		
		System.out.println( "IP Address: " + getIPAddress() );

		System.out.println( "MAC Address: " + getMAC() );

		
//		final Enumeration<NetworkInterface> nis = 
//					NetworkInterface.getNetworkInterfaces();
//		for ( final NetworkInterface nic : Collections.list( nis ) ) {
//			System.out.println( "NIC: " + nic.toString() );
//		}
//		
//		for ( int i = 0; i<10; i++ ) {
//			final NetworkInterface nic = NetworkInterface.getByIndex( i );
//			if ( null!=nic ) {
//				System.out.println( "NIC (" + i + "): " + nic.toString() );
//			}
//		}
		
//		if (1==1) return;
		
		
//		final Map<String, String> map = getIPAddresses( true );
//		System.out.println( "All addresses:" );
//		for ( final Entry<String, String> entry : map.entrySet() ) {
//			System.out.println( "\t" + entry.getKey() + " : " + entry.getValue() );
//		}
		
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
