package jmr.s2db.event;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jmr.s2db.tables.Event;


/**
 * This monitor will watch the event table and report to registered listeners
 * when new events appear.
 * 
 * This is unlike the JobMonitor which keeps a listing of recent jobs.
 */
public class EventMonitor {
	
	public final static long MONITOR_INTERVAL = TimeUnit.SECONDS.toMillis( 1 );

	
	private final static Set<Long> setPostedEventSeqs = new HashSet<>();

	private static final List<EventListener> listeners = new LinkedList<>();

	private static Thread threadUpdater;
	
	
	private static long seqLastEventScanned = 0;

	
	
	
	public static interface EventListener {
		public void process( final Event event );
	}
	
	
	private static EventMonitor instance;
	
	private EventMonitor() {};
	
	public static EventMonitor get() {
		if ( null==instance ) {
			instance = new EventMonitor();
		}
		return instance;
	}
	

	public void initializeEventMonitorThread() {
		if ( null!=threadUpdater ) return;
		
		threadUpdater = new Thread( "Event Monitor" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( MONITOR_INTERVAL );
		
					for (;;) {
						try {
							scanNewEvents();
						} catch ( final Exception e ) {
							// ignore.. 
							// JDBC connection may have been dropped..
							e.printStackTrace();
						}
		
						Thread.sleep( MONITOR_INTERVAL );
					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
		threadUpdater.start();
		
		System.out.println( "Event Monitor thread started." );
	}


	
	private static void scanNewEvents() {
		
		final List<Event> listNewEvents = 
				Event.get( "seq>" + seqLastEventScanned, 10 );
		
		for ( final Event event : listNewEvents ) {
			if ( null!=event ) {
				final long seq = event.getEventSeq();
				seqLastEventScanned = Math.max( seqLastEventScanned, seq );
		
				postNewEvent( event );
			}
		}
		
		// see if theres a very old seq. delete if any are found.
		if ( setPostedEventSeqs.size() > 100 ) {
			synchronized ( setPostedEventSeqs ) {
				long seqToDelete = 0;
				for ( final long seq : setPostedEventSeqs ) {
					if ( seq + 100 < seqLastEventScanned ) {
						seqToDelete = seq;
					}
				}
				setPostedEventSeqs.remove( seqToDelete );
			}
		}
	}
	
	
	public static void postNewEvent( final Event event ) {
		if ( null==event ) return;
		
//		System.out.println( "--- postNewEvent(), event " + event.getEventSeq() );
		
		final long seq = event.getEventSeq();
		synchronized ( setPostedEventSeqs ) {
			if ( !setPostedEventSeqs.contains( seq ) ) {
				
				for ( final EventListener listener : listeners ) {
					if ( null!=listener ) {
						listener.process( event );
					}
				}
			}
			setPostedEventSeqs.add( seq );
		}
	}
	
	
	public void addListener( final EventListener listener ) {
		if ( null==listener ) return;
		
		synchronized ( setPostedEventSeqs ) {
			EventMonitor.listeners.add( listener );
		}
		
		this.initializeEventMonitorThread();
		
		System.out.println( "EventListener registered: " + listener.toString() );
	}

	
	
	
}
