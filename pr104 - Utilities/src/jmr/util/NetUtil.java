package jmr.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;

public abstract class NetUtil {


	private static String strMAC = null;
	private static String strIP = null;
	
	
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
		if ( null==strIP ) {
			try {
				final InetAddress ip = InetAddress.getLocalHost();
				strIP = ip.getHostAddress();
			} catch ( final UnknownHostException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return strIP;
	}
	
	
	public static String getProcessName() {
		final String strName = ManagementFactory.getRuntimeMXBean().getName();
		return strName;
	}
	
	
	
//	@SuppressWarnings("deprecation")
//	public static void main(String[] args) {
//		
////		System.out.println( "Fake MAC: " + createFakeMAC() );
//		
//		System.out.println( "Process Name: " + getProcessName() );
//		
//		
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
//		
//		
//		System.out.println( "Loading configuration.." );
//		final Configuration config = Configuration.get();
//		
//		final String strUsername = config.get( "teslamotors.username" );
//		System.out.println( "Username = " + strUsername );
//
////		final String strMID = config.getSessionID();
////		System.out.println( "Machine ID: " + strMID );
//	}
	
	
}
