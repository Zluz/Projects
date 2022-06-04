package jmr.rpclient.swt;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Resource;

import jmr.rpclient.ModalMessage;

public class Tracker {

	final public static int ALERT_THRESHOLD = 10;
	
	private static Tracker instance;
	
	private final List<WeakReference<Resource>> list = new LinkedList<>();
	private final Map<Resource,StackTraceElement> map = new HashMap<>();
	private final Thread thread;
	private boolean bActive = true;
	
	private Tracker() {
		thread = new Thread( ()-> {
			try {
				while ( bActive ) {
					Thread.sleep( 10000 );
					prune();
					check();
				}
			} catch ( final InterruptedException e ) {
				// just exit
			}
		} );
		thread.start();
	}
	
	public static Tracker get() {
		if ( null == instance ) {
			instance = new Tracker();
		}
		return instance;
	}
	
	
	public void prune() {
		list.removeIf( wr-> {
			final Resource resource = wr.get();
			if ( null == resource || resource.isDisposed() ) {
				map.remove( resource );
				return true;
			} else {
				return false;
			}
		} );
	}
	
	public void check() {
		if ( map.size() < ALERT_THRESHOLD ) return;
		
		int iMax[] = { 0 };
		StackTraceElement steMax[] = { null };
		final Map<String,Integer> mapCounts = new HashMap<>();
		map.values().forEach( ste-> {
			final String strFrame = ste.toString();
			Integer iCount = mapCounts.get( strFrame );
			if ( null != iCount ) {
				iCount+= 1;
			} else {
				iCount = 1;
			}
			mapCounts.put( strFrame, iCount );
			if ( iCount > iMax[0] ) {
				iMax[0] = iCount;
				steMax[0] = ste;
			}
		} );
		
		System.out.println( "Current max tracked SWT Resource allocation:" );
		System.out.println( "  " + iMax[0] + " instances from: " 
								+ steMax[0].toString() );
		
		if ( iMax[0] > ALERT_THRESHOLD ) {
			final String strFrame = steMax[0].toString();
			final String strBody = 
					"Excessive SWT resources allocated from:\n"
					+ "\t" + strFrame;
			System.err.println( "WARNING: " + strBody );
			
			final String strTitle = "Excessive SWT resource usage";
			final ModalMessage message = new ModalMessage( 
								strTitle, strBody, 0 );
			ModalMessage.add( message );
		}
	}
	
	public void add( final Resource resource ) {
		synchronized ( list ) {
			prune();
			boolean bFound[] = { false };
			list.forEach( wr-> {
				if ( resource == wr.get() ) {
					bFound[0] = true;
				}
			});
			if ( bFound[0] ) {
				return;
			}

			list.add( new WeakReference<Resource>( resource ) );
			final StackTraceElement frame = 
					Thread.currentThread().getStackTrace()[2];
			map.put( resource, frame );
		}
	}
	
}
