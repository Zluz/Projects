package jmr.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SelfDestruct {
	
	public final static long DEFAULT_TIMEOUT = TimeUnit.MINUTES.toMillis( 5L );

	private static final Logger 
					LOGGER = Logger.getLogger( SelfDestruct.class.getName() );

	
	private static SelfDestruct instance;
	
	private Thread threadCountDown = null;
	
	private long lTimeExpire = 0;
	
	private SelfDestruct() {
		lTimeExpire = Math.max( lTimeExpire, DEFAULT_TIMEOUT );
		threadCountDown = new Thread( "Self-Destruct countdown." ) {
			@Override
			public void run() {
				for (;;) {
					try {
						Thread.sleep( 1000L );
					} catch ( final InterruptedException e ) {
						LOGGER.warning( "Self-Destruct thread interrupted. "
									+ "Continuing." );
					}
					final long lNow = System.currentTimeMillis();
					if ( lNow > lTimeExpire ) {
						SelfDestruct.shutdown( "Process timeout elapsed." );
					}
				}
			}
		};
		threadCountDown.start();
	};
	
	private static void shutdown( final String strMessage ) {
		LOGGER.warning( ()-> "Ending process. " + strMessage );
		Runtime.getRuntime().halt( 200 );
	}
	
	public void addTime( final long lTime ) {
		this.lTimeExpire = Math.max( lTimeExpire, 
							System.currentTimeMillis() + 1000 * lTime );
	}
	
	public synchronized static SelfDestruct getInstance() {
		if ( null==instance ) {
			instance = new SelfDestruct();
		}
		return instance;
	}

	public static void setTime( final long lTime ) {
		SelfDestruct.getInstance().addTime( lTime );
	}
	
	
}
