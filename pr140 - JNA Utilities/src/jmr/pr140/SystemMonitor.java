package jmr.pr140;

import java.util.LinkedList;
import java.util.List;

public class SystemMonitor {

	public interface Listener {
		void changedScreensaver( final boolean bActivated );
	}

	private final List<Listener> listListeners = new LinkedList<>();
	
	private static SystemMonitor instance = null;
	
	private Thread thread;
	
	private SystemMonitor() {
		this.start();
	}
	
	public static SystemMonitor get() {
		if ( null == instance ) {
			instance = new SystemMonitor();
		}
		return instance;
	}

	public void addListener( final Listener listener ) {
		this.listListeners.add( listener );
	}
	
	public void start() {
		this.thread = new Thread() {
			@Override
			public void run() {
				System.out.println( "test" );
			}
		};
		this.thread.start();
	}
	
	
	
	
}
