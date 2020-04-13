package jmr.pr136.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class CompositeRefresher {

	private final Composite composite;
	private final long lRefreshRate;
	private final Thread thread;
	
	public CompositeRefresher( final Composite composite,
							   final long lRefreshRate ) {
		this.composite = composite;
		this.lRefreshRate = lRefreshRate;
		final Display display = composite.getDisplay();
		
		this.thread = new Thread( "Refresher" ) {
			@Override
			public void run() {
				try {
					while ( ! display.isDisposed() 
							&& ! composite.isDisposed() ) {
						display.syncExec( ()-> {
								if ( ! composite.isDisposed() ) {
									composite.redraw();
								}
							} );
						Thread.sleep( lRefreshRate );
					}
				} catch ( final Throwable t ) {
					t.printStackTrace();
					Runtime.getRuntime().exit( 101 );
				}
			}
		};

		this.thread.start();
	}
	
}
