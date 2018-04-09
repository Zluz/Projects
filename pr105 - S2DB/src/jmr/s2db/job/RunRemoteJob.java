package jmr.s2db.job;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jmr.s2db.job.JobMonitor.InputStreamHandler;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.util.NetUtil;
import jmr.util.OSUtil;

public class RunRemoteJob {

	private final String strName;
	private final String strIP;
	
	public RunRemoteJob( final String strName ) {
		this.strName = strName;
		this.strIP = NetUtil.getIPAddress();
	}
	
	public boolean isIntendedHere( final Job job ) {
		if ( null==job ) return false;

		final Map<String,String> map = job.getJobDetails();
		if ( null==map ) return false;

		System.out.println( "        RRJ.strIP = " + this.strIP );
		System.out.println( "        map.get(\"remote\") = " + map.get( "remote" ) );
		
		final boolean bMatchIP = this.strIP.equals( map.get( "remote" ) );
		if ( bMatchIP ) return true;

		final boolean bMatchName = null!=this.strName 
				&& this.strName.equals( map.get( "remote" ) );
		if ( bMatchName ) return true;
		
		
		return false;
	}
	

	
	public void runRemoteExecute( final Job job ) {
		
		System.out.println( "--- RunRemoteJob.runRemoteExecute()" );

		final Map<String,String> map = job.getJobDetails();
	
		job.setState( JobState.WORKING );

		final String strCommand = map.get( "command" );
		
		System.out.println( "Running command: " + strCommand );
		
		try {
			final Process process = 
							Runtime.getRuntime().exec( strCommand );

			final StringBuffer inBuffer = new StringBuffer();
			final InputStream inStream = process.getInputStream();
			new InputStreamHandler( job, inBuffer, inStream, System.out );

			final StringBuffer errBuffer = new StringBuffer();
			final InputStream errStream = process.getErrorStream();
			new InputStreamHandler( job, errBuffer , errStream, System.err );

			process.waitFor();
			
			
//				final String strResult = "Exit value = " + process.exitValue();
			final String strOutput = inBuffer.toString();
			final String strError = errBuffer.toString();
			
			final Map<String,String> mapResult = new HashMap<>();
			mapResult.put( "exit_code", ""+process.exitValue() );
			mapResult.put( "std_out", strOutput );
			mapResult.put( "std_err", strError );
			
			final Gson GSON = new Gson();
			final JsonElement jsonResult = GSON.toJsonTree( mapResult );
			final String strResult = jsonResult.toString();
			
			job.setState( JobState.COMPLETE, strResult );
		} catch ( final Exception e ) {
			job.setState( JobState.FAILURE, e.toString() );
		}
	}

	
	public void runGetCallStack( final Job job ) {
		
		System.out.println( "--- RunRemoteJob.runGetCallStack()" );

		job.setState( JobState.WORKING );

		final JsonObject jo = new JsonObject();

		final Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		for ( final Entry<Thread, StackTraceElement[]> entry : map.entrySet() ) {
			
			if ( null!=entry 
					&& null!=entry.getKey() 
					&& null!=entry.getValue() ) {
				
				final JsonArray ja = new JsonArray();
				
				final StackTraceElement[] stack = entry.getValue();
				for ( final StackTraceElement frame : stack ) {
					final String strFrame = frame.getClassName() + "." 
											+ frame.getMethodName() + "(), " 
											+ frame.getFileName() + ":" 
											+ frame.getLineNumber();
					ja.add( strFrame );
				}
			
				final String strThread = entry.getKey().getName();
				jo.add( strThread, ja );
			}
			
			final String strResult = jo.toString();
			
			job.setState( JobState.COMPLETE, strResult );
		}
	}
	

	public void runShutdown( final Job job ) {
		
		System.out.println( "--- RunRemoteJob.runShutdown()" );

		job.setState( JobState.WORKING );

		final Map<String,String> mapResult = new HashMap<>();
		
		mapResult.put( "acknowledged", ""+System.currentTimeMillis() );
		mapResult.put( "program", OSUtil.getProgramName() );

		final Gson GSON = new Gson();
		final JsonElement jsonResult = GSON.toJsonTree( mapResult );
		final String strResult = jsonResult.toString();
		
		job.setState( JobState.COMPLETE, strResult );
		Runtime.getRuntime().exit( 100 );
	}
	
	
	
	
}
