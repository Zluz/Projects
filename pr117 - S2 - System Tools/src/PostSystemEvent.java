import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jmr.s2db.Client;
import jmr.s2db.event.EventType;
import jmr.s2db.event.SystemEvent;
import jmr.s2db.tables.Event;
import jmr.util.NetUtil;

public class PostSystemEvent {
	
	
	public static void main( final String[] args ) {
		if ( null==args || args.length != 1 ) {
			System.out.println( "Usage: " 
						+ PostSystemEvent.class.getSimpleName() 
						+ " <SystemEvent>" );
			System.out.println( "Where <SystemEvent> may be one of:" );
			for ( final SystemEvent event : SystemEvent.values() ) {
				System.out.println( "\t" + event.name() );
			}
			System.exit( 100 );
		}
		
		final long lStart = System.currentTimeMillis();
		final SystemEvent se = SystemEvent.valueOf( args[0] );
		
		final Client client = Client.get();
		try {
		    final String strIP = NetUtil.getIPAddress();
		    final String strClass = PostSystemEvent.class.getName();
		    final String strMAC = NetUtil.getMAC();
		    final String strSessionID = NetUtil.getSessionID();
			client.register( strSessionID, strClass );
			

			final String strDeviceName = client.getThisDevice().getName();
//			final Map<String,String> mapOptions = client.getThisDevice().getOptions();
			
			
			
			final JsonObject jo = new JsonObject();
			jo.addProperty( "IP", strIP );
			jo.addProperty( "MAC", strMAC );
			jo.addProperty( "device.name", strDeviceName );
			jo.addProperty( "time", lStart );
			final String strData = jo.toString();
			
			final Event event = Event.add( EventType.SYSTEM, se.name(),
					strMAC, "", strData, lStart, null, null, null );
			
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
