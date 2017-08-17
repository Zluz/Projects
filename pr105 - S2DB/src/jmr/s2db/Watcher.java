package jmr.s2db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import jmr.s2db.comm.ConnectionProvider;

public class Watcher {

	final static int SEQ_COUNT = 3;
	
	final static int POLLING_INTERVAL = 500; // 500ms polling

	
	public static interface Listener {
		public void addedPage();
		public void addedSession();
		public void updatedPage();
	}
	
	public final List<Listener> listListeners = new LinkedList<Listener>();

	
	private static Watcher instance;
	
	private Watcher() {};
	
	public static synchronized Watcher get() {
		if ( null==instance ) {
			instance = new Watcher();
		}
		return instance;
	}
	
	
	public synchronized void addListener( final Listener listener ) {
		if ( !DATABASE_CHANGE_MONITOR.isAlive() ) {
			DATABASE_CHANGE_MONITOR.start();
		}
		listListeners.add( listener );
	}
	
	
	public Long[] readLastRows() {
//		try ( final Statement 
//				stmt = ConnectionProvider.get().getStatement() ) {
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final String strQuery = 
			 "SELECT "
			 + "   max( ps.seq ), " 
			 + "   max( session.seq ), "
			 + "   unix_timestamp( max( pd.last_modified ) ) " 
			 + "FROM " 
			 + "	page as ps, " 
			 + "	page as pd, " 
			 + "    session " 
			 + "WHERE " 
			 + "	ps.state = 'A';"; 
			 
			stmt.executeQuery( strQuery );

			int iIndex = 0;
			final Long[] arr = new Long[ SEQ_COUNT ];
			
			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				if ( rs.next() ) {
					while ( iIndex<SEQ_COUNT ) {
						arr[ iIndex ] = rs.getLong( iIndex + 1 );
						iIndex++;
					}
				}
			}
			
			return arr;
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	private void notifyListeners( final int iType ) {
		for ( final Listener listener : listListeners ) {
			if ( null!=listener ) {
				switch ( iType ) {
					case 0 : listener.addedPage(); break;
					case 1 : listener.addedSession(); break;
					case 2 : listener.updatedPage(); break;
					default: break;
				}
			}
		}
	}
	
	
	final Thread DATABASE_CHANGE_MONITOR = new Thread() {
		public void run() {
			try {

				final Long[] seqLastRows = new Long[ SEQ_COUNT ];

				for (;;) {
					Thread.sleep( POLLING_INTERVAL );
					
					final Long[] seqNowRows = readLastRows();

					if ( null!=seqNowRows ) {
						for ( int i=0; i<SEQ_COUNT; i++ ) {
							if ( !seqNowRows[i].equals( seqLastRows[i] ) ) {
								if ( null!=seqLastRows[i] ) {
									notifyListeners( i );
								}
								seqLastRows[i] = seqNowRows[i];
							}
						}
					}
				}
			} catch ( final InterruptedException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
	};
	
	
	
	
	

	public static void main( final String[] args ) throws InterruptedException {

		ConnectionProvider.get();
		
		Watcher.get().addListener( new Listener() {
			@Override
			public void addedPage() {
				System.out.println( "Page added." );
			}
			@Override
			public void addedSession() {
				System.out.println( "Session added." );
			}
			@Override
			public void updatedPage() {
				System.out.println( "Page updated." );
			}
		});
		
		Thread.sleep( Long.MAX_VALUE );
	}
	
	
}
