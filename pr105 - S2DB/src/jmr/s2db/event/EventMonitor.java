package jmr.s2db.event;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jmr.pr126.comm.http.HttpListener;
import jmr.s2db.comm.Notifier;
import jmr.s2db.tables.Event;

import static jmr.pr126.comm.http.HttpListener.Listener;


/**
 * This monitor will watch the event table and report to registered listeners
 * when new events appear.
 * 
 * This is unlike the JobMonitor which keeps a listing of recent jobs.
 */
public class EventMonitor {
	
	public final static long MONITOR_INTERVAL = TimeUnit.SECONDS.toMillis( 2 );


	
	private static final Logger 
			LOGGER = Logger.getLogger( EventMonitor.class.getName() );

		
	private final static Set<Long> setPostedEventSeqs = new HashSet<>();

//	private static final SynchronousQueue<WeakReference<EventListener>> 
//						listeners = new SynchronousQueue<>();
//	private static final ConcurrentLinkedQueue<WeakReference<EventListener>>
//						LISTENERS = new ConcurrentLinkedQueue<>();
	
	private static final ConcurrentHashMap<EventListener, String>
						LISTENERS = new ConcurrentHashMap<>();

	private static Thread threadUpdater;
	
//	private static HttpListener httplistener = new HttpListener();
	
	/* similar to JobMonistor.listener */ 
	private static Listener listener = new Listener() {
		@Override
		public void received( final Map<String, Object> map ) {
			System.out.println( "--- EventMonitor HttpListener.received()" );
			if ( Notifier.EVENT_TABLE_UPDATE.equals( map.get( "event" ) ) ) {
				if ( "event".equals( map.get( "table" ) ) ) {
					System.out.print( "\tScanning for new events..." );
					scanNewEvents();
					System.out.println( "Done." );
				}
			}
		}
	};
	
	
	static {
//		httplistener.registerListener( listener );
		HttpListener.getInstance().registerListener( listener );
	}
	
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
						
						clearOldListeners();
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

	
	private static void clearOldListeners() {
		
		if ( 1==1 ) return; // disable for now..
		
		List<WeakReference<EventListener>> listDelete = null;
//		synchronized ( listeners ) {
//			for ( final WeakReference<EventListener> ref : LISTENERS ) {
//				final EventListener listener = ref.get();
//				if ( null==listener ) {
//					if ( null==listDelete ) {
//						listDelete = new LinkedList<>();
//					}
//					listDelete.add( ref );
//				}
//			}
//		}
		if ( null!=listDelete ) {
			for ( final WeakReference<EventListener> ref : listDelete ) {
				LISTENERS.remove( ref );
			}
		}
	}
	
	
	public static void postNewEvent( final Event event ) {
		if ( null==event ) return;
		
		System.out.println( "--- postNewEvent(), event " + event.toString() );

		final long seq = event.getEventSeq();
		final boolean bPost;
		synchronized ( setPostedEventSeqs ) {
			if ( ! setPostedEventSeqs.contains( seq ) ) {
				setPostedEventSeqs.add( seq );
				bPost = true;
			} else {
				bPost = false;
			}
		}

		if ( bPost ) {
//			synchronized ( listeners ) {
			if ( LISTENERS.isEmpty() ) {
				LOGGER.warning( "No listeners registered with EventMonitor." );
			} else {
//				for ( final WeakReference<EventListener> ref : LISTENERS ) {
				for ( final EventListener listener : LISTENERS.keySet() ) {
//					final EventListener listener = ref.get();
					if ( null!=listener ) {
						System.out.println( "--- postNewEvent(), "
											+ "listener " + listener );
						listener.process( event );
					}
				}
			}
//			}
		}
	}
	
	
	public void addListener( final EventListener listener,
							 final String strName ) {
		if ( null==listener ) return;
		
//		EventMonitor.clearOldListeners();
		
		synchronized ( setPostedEventSeqs ) {
//			final WeakReference<EventListener> 
//						ref = new WeakReference<EventListener>( listener );
			
			LOGGER.info( "EventListener added." );
			
//			EventMonitor.LISTENERS.add( ref );
			EventMonitor.LISTENERS.put( listener, strName );
//			if ( ! EventMonitor.listeners.offer( ref ) ) {
//				LOGGER.severe( "Failed to add EventListener " 
//									+ listener.toString() );
//			}
		}
		
		this.initializeEventMonitorThread();
		
		System.out.println( "EventListener registered: " + listener.toString() );
	}

	
}
