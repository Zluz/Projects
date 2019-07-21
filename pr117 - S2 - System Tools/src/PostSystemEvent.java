import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.s2db.event.EventType;
import jmr.s2db.event.SystemEvent;
import jmr.s2db.tables.Event;
import jmr.util.NetUtil;
import jmr.util.report.Reporting;

public class PostSystemEvent {
	
	private static final boolean REGISTER_QUIET = true;
	
	public final static long MAX_PROCESS_TIME = TimeUnit.SECONDS.toMillis( 120 );


	public static void printHelp() {
		System.out.println( "Usage: " 
				+ PostSystemEvent.class.getSimpleName() 
				+ " <SystemEvent>" );
		System.out.println( "Where <SystemEvent> may be one of:" );
		for ( final SystemEvent event : SystemEvent.values() ) {
			System.out.println( "\t" + event.name() );
		}
	}
	
	
	public static void startProcessTimeoutThread( final long lTime ) {
		
		/*
		 * in the shell this works:
		 *     /usr/bin/killall -u root -s 6 java
		 */
		
		final Thread threadShutdown = new Thread( "Process Timeout Shutdown" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( lTime );
					System.out.println( "Process timeout elapsed. Exiting." );
					
					System.out.println( "Threads:" );
					System.out.println( Reporting.reportAllThreads() );
					
//					Runtime.getRuntime().exit( 200 );
					Runtime.getRuntime().halt( 200 );
				} catch ( final InterruptedException e ) {
					System.out.println( "Timeout thread interrupted. Exiting." );
//					Runtime.getRuntime().exit( 210 );
					Runtime.getRuntime().halt( 210 );
				}
			}
		};
		threadShutdown.start();
	}
	
	
	public static void main( final String[] args ) {
		if ( null==args || args.length != 1 ) {
			printHelp();
			System.exit( 100 );
		}
		
		startProcessTimeoutThread( MAX_PROCESS_TIME );
		
		final boolean bVerbose;
		final SystemEvent se;
		final String strParam = args[0].trim().toUpperCase();
		if ( "VERBOSE".equalsIgnoreCase( strParam ) ) {
			bVerbose = true;
			se = SystemEvent.TEST_SYSTEM_EVENT;
	    	System.out.println( "Enabled verbose output." );
		} else {
			bVerbose = false;
			se = SystemEvent.getSystemEvent( strParam );
		}
		final long lStart = System.currentTimeMillis();
		
		if ( null==se ) {
			System.out.println( "Invalid <SystemEvent>: " + strParam );
			printHelp();
			System.exit( 100 );
		}
		
	    if ( bVerbose ) {
	    	System.out.print( "Getting Client singleton..." );
	    }
		final Client client = Client.get();
	    if ( bVerbose ) {
	    	System.out.println( "Done." );
	    }
		try {
		    final Map<String, String> mapNICs = NetUtil.getIPAddresses();
		    final String strIP = NetUtil.getIPAddress();
		    final String strMAC = NetUtil.getMAC();
		    final String strClass = PostSystemEvent.class.getName();
		    final String strSessionID = NetUtil.getSessionID();
		    if ( bVerbose ) {
		    	System.out.print( "Registering client..." );
		    }
			client.register( ClientType.TEST, strSessionID, 
										strClass, REGISTER_QUIET );
		    if ( bVerbose ) {
		    	System.out.println( "Done." );
		    }

			final String strDeviceName = client.getThisDevice().getName();
//			final Map<String,String> mapOptions = client.getThisDevice().getOptions();
			
			final JsonObject jo = new JsonObject();
			jo.addProperty( "IP", strIP );
			jo.addProperty( "MAC", strMAC );
			jo.addProperty( "device.name", strDeviceName );
			jo.addProperty( "time", lStart );
		    if ( null!=mapNICs ) {
		    	for ( final Entry<String, String> entry : mapNICs.entrySet() ) {
		    		jo.addProperty( "IP." + entry.getKey(), entry.getValue() );
		    	}
		    }
			
			final String strData = jo.toString();
			
		    if ( bVerbose ) {
		    	System.out.print( "Posting Event..." );
		    }
			final Event event = Event.add( EventType.SYSTEM, se.name(),
					strMAC, "", strData, lStart, null, null, null );
		    if ( bVerbose ) {
		    	System.out.println( "Done." );
		    }
			
			System.out.println( "Event generated" );
			System.out.println( "\tseq\t" + event.getEventSeq() );
			System.out.println( "\tsubject\t" + event.getSubject() );
			System.out.println( "\tvalue\t" + event.getValue() );
			System.out.println( "\ttime\t" + event.getTime() );
			System.out.println( "\tdata (JsonObject)" );
			for ( final Entry<String, JsonElement> 
							entry : jo.getAsJsonObject().entrySet() ) {
				System.out.println( "\t\t" + entry.getKey() 
							+ " = " + entry.getValue().toString() );
			}
			
			System.exit( 0 );
		} finally {
			client.close();
		}
	}

}
