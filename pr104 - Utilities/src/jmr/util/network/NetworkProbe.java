package jmr.util.network;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class NetworkProbe {

	private final static Logger 
				LOGGER = Logger.getLogger( NetworkProbe.class.getName() );
	
	public enum Distance {
		INTERNET( "yahoo.com", "google.com" ),
		NET_DNS( "wiki", "s112" ),
		NET_IP( "192.168.6.1" ),
		LOCAL( "127.0.0.1" ),
		NO_NETWORKING(),
		;
		
		final String[] strHostTargets;
		
		Distance( final String... strHostTargets ) {
			this.strHostTargets = strHostTargets;
		}
	}

	
	public boolean testPing( final String strDestination ) {
		final String strCommand = "ping -n 1 " + strDestination;
		try {
			final Process process = Runtime.getRuntime().exec( strCommand );
			process.waitFor( 4L, TimeUnit.SECONDS );
			final int iExitCode = process.exitValue();
			return ( 0==iExitCode );
		} catch ( final Exception e ) {
			LOGGER.warning( ()-> "Failed to ping " + strDestination + ". "
					+ "Encountered " + e.toString() );
		}
		return false;
	}
	
	
	public boolean testDistance( final Distance distance ) {
		if ( null==distance ) return false;
		if ( 0==distance.strHostTargets.length ) return false;
		
		for ( final String strDestination : distance.strHostTargets ) {
			final boolean bPing = testPing( strDestination );
			if ( bPing ) {
				return true;
			}
		}
		return false;
	}
	
	
	public Distance verifyNetworking() {
		for ( final Distance distance : Distance.values() ) {
			if ( testDistance( distance ) ) {
				return distance;
			}
		}
		return Distance.NO_NETWORKING;
	}
	
	public static void main( final String[] args ) {
		System.out.print( "Testing networking..." );
		final Distance distance = new NetworkProbe().verifyNetworking();
		System.out.println( "Done." );
		System.out.println( "Distance: " + distance );
	}
	
}
