package jmr.rpclient.tiles;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;
import jmr.s2db.tables.Event;

public class EventListingTile extends TileBase {


	final static long SUBJECT_REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis( 1 );
	final static long EVENTS_REFRESH_INTERVAL = 500;

	final static Map<String,Event> events = new HashMap<>();

	static long lLastSubjectUpdate = 0;
	final static List<String> listSubjects = new LinkedList<>();

	static Thread threadUpdate = null;
	
	
	public static void updateEvents_simple() {
		final List<Event> list = Event.get( "seq>0", 100 );
		
		synchronized ( events ) {
			events.clear();
			for ( final Event event : list ) {
				final String strSubject = event.getSubject();
				if ( !events.containsKey( strSubject ) ) {
					events.put( strSubject, event );
				}
			}
		}
	}
	
	
	public static void updateEvents() {
		
		final long lNow = System.currentTimeMillis();
		if ( lNow - lLastSubjectUpdate > SUBJECT_REFRESH_INTERVAL ) {
			synchronized ( listSubjects ) {
				listSubjects.clear();
				listSubjects.addAll( Event.getSubjects() );
				lLastSubjectUpdate = lNow;
			}
		}
		
		final Map<String,Event> map = new HashMap<>();
		synchronized ( listSubjects ) {
			for ( final String strSubject : listSubjects ) {
				final Event event = Event.getLatestEventFor( strSubject );
				map.put( strSubject, event );
			}
		}

		synchronized ( events ) {
			events.clear();
			events.putAll( map );
		}
	}
	
	
	public EventListingTile() {
		if ( null==threadUpdate ) {
			threadUpdate = new Thread( "EventListingTile updater" ) {
				@Override
				public void run() {
					try {
						while ( !UI.display.isDisposed() ) {
							Thread.sleep( EVENTS_REFRESH_INTERVAL );
						
							updateEvents();
						}
					} catch ( final InterruptedException e ) {
						// just quit.
					}
				}				
			};
			threadUpdate.start();
		}
	}
	
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		int iY = 2;
		
		synchronized ( events ) {
			
			final List<String> list = new LinkedList<>( events.keySet() );
			Collections.sort( list );
			for ( final String strSubject : list ) {
				
				final Event event = events.get( strSubject );
				if ( null!=event ) {
	
					final Color color;
//					if ( JobState.REQUEST.equals( state ) ) {
//						color = Theme.get().getColor( Colors.TEXT_BOLD );
//					} else {
//						color = Theme.get().getColor( Colors.TEXT_LIGHT );
//					}
					color = Theme.get().getColor( Colors.TEXT );
					gc.setForeground( color );
					
					gc.setFont( Theme.get().getFont( 8 ) );
					
					gc.drawText( event.getSubject(), 26, iY );

					gc.setFont( Theme.get().getFont( 12 ) );

					final String strType = event.getTriggerType().name();
					gc.drawText( strType.substring( 0, 1 ), 10, iY );

					gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
					gc.drawText( event.getValue(), 80, iY + 12 );
					
					iY += 38;
				}
			}
		}
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
