package jmr.s2db.job;

import java.util.LinkedList;
import java.util.List;

import jmr.s2db.tables.Job;

public class RemoteJobMonitor {

	private static final List<Listener> LISTENERS = new LinkedList<>();
	
	private static RemoteJobMonitor instance;
	
	public static synchronized RemoteJobMonitor get() {
		if ( null==instance ) {
			instance = new RemoteJobMonitor();
		}
		return instance;
	}
	
	public static interface Listener {
		public void job( final Job job );
	}
	
	public void addListener( final Listener listener ) {
		LISTENERS.add( listener );
		JobMonitor.get().check();
	}
	
	public void post( final Job job ) {
		
		System.out.println( "RemoteJobMonitor.post()" );
		
		for ( final Listener listener : LISTENERS ) {
			listener.job( job );
		}
	}
	
	
	
}
