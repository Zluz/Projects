package jmr.rpclient.swt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Resource;

import jmr.rpclient.ModalMessage;

public class Tracker {

	final public static int ALERT_THRESHOLD = 50;
	
	// Resources younger than this will not be auto-disposed
	final public static long AGE_THRESHOLD = TimeUnit.MINUTES.toMillis( 10 );
	
	private static Tracker instance;
	
//	private final List<WeakReference<Resource>> list = new LinkedList<>();
//	private final Map<Resource,StackTraceElement> map = new HashMap<>();
	
	private final TreeMap<Integer,ResourceRecord> map = new TreeMap<>();
	
	private final List<String> listFrameReported = new ArrayList<String>( 8 );
	
	private final Thread thread;
	private boolean bActive = true;
	
	final static class ResourceRecord {
		WeakReference<Resource> wr;
		StackTraceElement frame;
		final long lTime = System.currentTimeMillis();
	}
	
	private Tracker() {
		thread = new Thread( ()-> {
			try {
				while ( bActive ) {
					Thread.sleep( 30 * 1000 );
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
		final List<Integer> listToDelete = new LinkedList<>();
		
		for ( final Entry<Integer, ResourceRecord> entry: map.entrySet() ) {
			final Resource resource = entry.getValue().wr.get();
			if ( ( null == resource ) || ( resource.isDisposed() ) ) {
				listToDelete.add( entry.getKey() );
			}
		}
		
		listToDelete.forEach( iHash-> map.remove( iHash ) );
	}
	
	public void check() {
		if ( map.size() < ALERT_THRESHOLD ) return;
		
		int iMax[] = { 0 };
		StackTraceElement steMax[] = { null };
		final Map<String,Integer> mapCounts = new HashMap<>();
		map.values().forEach( record-> {
			final String strFrame = record.frame.toString();
			Integer iCount = mapCounts.get( strFrame );
			if ( null != iCount ) {
				iCount+= 1;
			} else {
				iCount = 1;
			}
			mapCounts.put( strFrame, iCount );
			if ( iCount > iMax[0] ) {
				iMax[0] = iCount;
				steMax[0] = record.frame;
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
								strTitle, strBody, 4 );
			ModalMessage.add( message );
		}
	}
	
	public void addInternal( final Resource resource,
							 final StackTraceElement frame ) {
		if ( null == resource ) return;
		if ( resource.isDisposed() ) return;
		
		if ( map.containsKey( resource.hashCode() ) ) {
			return;
		}

		final ResourceRecord record = new ResourceRecord();
		record.frame = frame;
		record.wr = new WeakReference<Resource>( resource );
		
		map.put( resource.hashCode(), record );
	}

	public void add( final Resource resource ) {
		final StackTraceElement frame = 
				Thread.currentThread().getStackTrace()[2];
		synchronized ( map ) {
			prune();
			addInternal( resource, frame );
		}
	}

	
	public static boolean equal( final StackTraceElement lhs,
								 final StackTraceElement rhs ) {
		if ( null == lhs ) return false; 
		if ( null == rhs ) return false;
		
		if ( ! lhs.getClass().equals( rhs.getClass() ) ) return false;
		if ( lhs.getLineNumber() != rhs.getLineNumber() ) return false;
		
		return true;
	}
	
	/**
	 * This should not have to be used.
	 * Use add() instead.
	 * @param resource
	 */
	public void addAutoDispose( final Resource resource ) {
		final StackTraceElement frame = 
				Thread.currentThread().getStackTrace()[2];
		final long lNow = System.currentTimeMillis();
		synchronized ( map ) {
			prune();
			int iCount[] = { 0 };
			final long lCutoff = lNow - AGE_THRESHOLD;
			map.values().forEach( record-> {
				if ( equal( record.frame, frame ) ) {
					final Resource resourceFound = record.wr.get();
					if ( null != resourceFound 
							&& record.lTime < lCutoff ) {
						resourceFound.dispose();
						iCount[0]++;
					}
				}
			});
			if ( iCount[0] > 0 ) {
				final String strFrame = frame.toString();
				if ( ! listFrameReported.contains( strFrame ) ) {
					System.out.println( "Auto-disposed " 
								+ iCount[0] + " resource(s) "
								+ "from " + frame.toString() );
					System.out.println( "(further events will be hidden)" );
					listFrameReported.add( strFrame );
				}
				prune();
			}
			
			addInternal( resource, frame );
		}
	}
	
	
	
}
