import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jmr.s2db.Client;
import jmr.s2db.event.EventType;
import jmr.s2db.event.SystemEvent;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Event;
import jmr.s2db.tables.Job;
import jmr.util.NetUtil;

/**
 * Utility for requesting the call stack from its Client UI or 
 * requesting that the Client UI shutdown.
 * This may be useful for debugging or a client gets stuck.
 */
public class SendRemoteRequest {
	
	public static void main( final String[] args ) {
		
		final String strRemoteIP = "192.168.1.11";
		final JobType jobRemoteCommand = JobType.REMOTE_SHUTDOWN;
		
		
		

//		final long lStart = System.currentTimeMillis();
//		final SystemEvent se = SystemEvent.valueOf( args[0] );
		
		final Client client = Client.get();
		try {
//		    final String strIP = NetUtil.getIPAddress();
		    final String strClass = PostSystemEvent.class.getName();
//		    final String strMAC = NetUtil.getMAC();
		    final String strSessionID = NetUtil.getSessionID();
			client.register( strSessionID, strClass );
			

//			final String strDeviceName = client.getThisDevice().getName();
//			final Map<String,String> mapOptions = client.getThisDevice().getOptions();
			
			
			final Map<String,String> map = new HashMap<>();
			map.put( "remote", strRemoteIP );
			
			final Job job = Job.add( jobRemoteCommand, map );
			
			System.out.println( "Job created: " + job.getJobSeq() );
			
			System.exit( 0 );
		} finally {
			client.close();
		}
		
	}
}
