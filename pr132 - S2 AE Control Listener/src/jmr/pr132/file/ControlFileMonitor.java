package jmr.pr132.file;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ControlFileMonitor {

	public static interface Listener {
		public void invoke( final Operation operation );
	}
	
	
	private Thread threadMonitor = null;
	
	private boolean bActive = false;
	
	private final Handler handler = new Handler();
	
	private final Listener listener;
	
	public ControlFileMonitor( final Listener listener ) {
		this.listener = listener;
	}

	
	public Thread start() {
		if ( null==threadMonitor ) {
			threadMonitor = new Thread( "ControlFileMonitor" ) {
				@Override
				public void run() {
					try {
						while ( bActive ) {
							Thread.sleep( TimeUnit.MINUTES.toMillis( 1 ) );
							
							final List<Operation> events = handler.checkForNewWork();
							
							for ( final Operation op : events ) {
								listener.invoke( op );
							}
						}
					} catch ( final InterruptedException e ) {
						// just quit
						bActive = false;
					}
				}
			};
			threadMonitor.start();
			bActive = true;
		}
		return threadMonitor;
	}
	
	
	
}
