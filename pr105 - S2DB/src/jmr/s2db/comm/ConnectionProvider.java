package jmr.s2db.comm;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.DelegatingDatabaseMetaData;

import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class ConnectionProvider {


//	public final static String 
//			MYSQL_CONNECTION = "jdbc:mysql://192.168.6.200:3306/s2db"
//							+ "?autoReconnect=true&useSSL=false";
	
	public final static String
//			MYSQL_DRIVER = "com.mysql.jdbc.Driver";
			MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	

	private static final Logger 
			LOGGER = Logger.getLogger( ConnectionProvider.class.getName() );


	private final BasicDataSource bds;

	private static ConnectionProvider instance;
	
	private static boolean bShutdown = false;

	

	private final List<ConnectionReference> 
						listConnections = new LinkedList<>();
	
	public static class ConnectionReference {
		final WeakReference<Connection> reference;
		final StackTraceElement[] stack; 
		
		public ConnectionReference( final Connection connection ) {
			this.reference = new WeakReference<>( connection );
			this.stack = Thread.currentThread().getStackTrace();
		}
	}
	
	
	private ConnectionProvider() throws SQLException, ClassNotFoundException {
		Class.forName( MYSQL_DRIVER );

		final char[] cUsername = SystemUtil.getProperty( 
						SUProperty.S2DB_USERNAME ).toCharArray(); 
		final char[] cPassword = SystemUtil.getProperty( 
						SUProperty.S2DB_PASSWORD ).toCharArray(); 
		final String strURL = SystemUtil.getProperty( 
						SUProperty.S2DB_CONNECTION ); 

		
//		this.conn = DriverManager.getConnection( 
//				MYSQL_CONNECTION, "s2_full", "s2db" );
//		this.conn.setSchema( "s2db" );
		
		bds = new BasicDataSource();
		
		bds.setDriverClassName( MYSQL_DRIVER );
		bds.setUrl( strURL );
		bds.setUsername( new String( cUsername ) );
		bds.setPassword( new String( cPassword ) );
		bds.setMaxActive( 10 );
		bds.setMaxOpenPreparedStatements( 10 );
		
		bds.setRemoveAbandoned( true );
//		bds.setLogAbandoned( true );
		bds.setRemoveAbandonedTimeout( 60 );
		
		// test connection here ?
		
		Runtime.getRuntime().addShutdownHook( new Thread( 
							"Shutdown Hook - ConnectionProvider.close()" ) {
			@Override
			public void run() {
				bShutdown = true;
				close();
			}
		});
		
		final Thread threadCleanup = new Thread( "Connection Cleaner" ) {
			@Override
			public void run() {
				try {
					while ( ! bShutdown ) {
						Thread.sleep( 1000 );
						final List<ConnectionReference> 
									listDelete = new LinkedList<>();
						synchronized ( listConnections ) { 
							try {
								for ( final ConnectionReference ref : listConnections ) {
									if ( null==ref ) {
										// just ignore
									} else if ( null==ref.reference 
													|| null==ref.reference.get() ) {
										listDelete.add( ref );
									}
								}
							} catch ( final ConcurrentModificationException e ) {
//							}} catch ( final Exception e ) {
								// ignore, quit
							}
							for ( final ConnectionReference ref : listDelete ) {
								listConnections.remove( ref );
							}
						}
						
					}
				} catch ( final InterruptedException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		};
		threadCleanup.start();
	};
	
	
	public static synchronized ConnectionProvider get() {
		if ( null==instance ) {
			try {
				instance = new ConnectionProvider();
			} catch ( final SQLException | ClassNotFoundException e ) {
				
				System.err.println( "Exception encountered while trying to "
						+ "instantiate a ConnectionProvider: " + e.toString() );
				
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	

	public static boolean bLockOut = false;
	public static int iSequentialFailed = 0;
	
	public static boolean isInLockout() {
		return bLockOut;
	}
	
	
	/**
	 * Attempt to retrieve a database connection.
	 * May return null.
	 * @return
	 */
	public Connection getConnection() {
		
		if ( iSequentialFailed > 8 ) {
			bLockOut = true;
//			LOGGER.warning( ()-> "Too many failed connection attempts. "
//					+ "Going into lockout." );
			LOGGER.severe( ()-> "Too many failed connection attempts. "
					+ "Shutting down." );
//			throw new IllegalStateException( 
//					"Failed to acquire a database connection (in lockout)." );
			SystemUtil.shutdown( 200, "Lost connection to the database." );
//			return null;
		}
		
		if ( bLockOut ) {
			// just stay here forever .. (for now)
		}

		if ( bds.getNumActive() > 10 ) {
			LOGGER.warning( ()-> "Too many active connections. "
					+ "Denying further requests." );
			throw new IllegalStateException( "Too many active connections." );
		}
		
		try {
//			https://stackoverflow.com/questions/7592056/am-i-using-jdbc-connection-pooling
				
			int i=10;
			Connection conn = null;
			do {
				conn = bds.getConnection();
				i--;
			} while ( i>0 && ( null==conn || !conn.isValid( 200 ) ) );
			
			if ( null==conn ) return null;
			
			synchronized ( listConnections ) { 
				listConnections.add( new ConnectionReference( conn ) );
			}
			iSequentialFailed = 0;
			return conn;
			
		} catch ( final SQLException e ) {
			iSequentialFailed++;
			LOGGER.log( Level.WARNING, 
					"Exception while getting a connection", e );
			LOGGER.warning( "BasicDataSource: " + bds.toString() ); 
			LOGGER.warning( "Database URL: " + bds.getUrl() ); 
			e.printStackTrace();
		}

		return null;
	}
	
	
	public void close() {
		
		bShutdown = true;

		final StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		final String strCaller = 
					ste.getClassName() + "." + ste.getMethodName() + "()";
		final String strLeader = "Closing S2DB "
				+ "from " + strCaller + ". ";
		
		synchronized ( listConnections ) {
			if ( listConnections.isEmpty() ) {
				System.out.println( strLeader + "All connections already closed." );
				return;
			}
			System.out.println( strLeader 
					+ "Checking " + listConnections.size() + " connections.." );
	
			final List<ConnectionReference> listRemove = new LinkedList<>();
			for ( final ConnectionReference ref : listConnections ) {
				final WeakReference<Connection> wr = ref.reference;
				if ( null!=wr ) {
					final Connection conn = wr.get();
					if ( null!=conn ) {
						try {
							new Thread() {
								@Override
								public void run() {
									try {
										conn.close();
	//									ref.clear();
										listRemove.add( ref );
									} catch ( final SQLException e ) {
										if ( !e.getMessage().contains( 
												"\" is closed.") ) {
											e.printStackTrace();
										}
									}
								}
							}.start();
						} catch ( final Throwable t ) {
							// ignore
						}
					}
				}
			}
			if ( !listRemove.isEmpty() ) {
				System.out.println( strLeader 
						+ "Cleaning up " + listRemove.size() + " references.." );
				while ( !listRemove.isEmpty() ) {
					try {
						final ConnectionReference conn = listRemove.remove( 0 );
						if ( null!=conn ) {
							listConnections.remove( conn );
						}
					} catch ( final Exception e ) {
						// ignore
					}
				}
			}
		}
		System.out.println( strLeader + "Done." );
		
		try {
			Thread.sleep( 100 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( listConnections.isEmpty() ) {
			System.out.println( "All connections closed." );
			return;
		}
		
//		System.out.println();
//		System.out.println( "Lingering connections detected:" );
		final List<String> list = new LinkedList<>();
		synchronized ( listConnections ) {
			for ( final ConnectionReference ref : listConnections ) {
				final WeakReference<Connection> wr = ref.reference;
				final Connection conn = wr.get();
				try {
					if ( null!=conn && !conn.isClosed() ) {
	//					System.out.println( "\t" + conn.toString() );
						System.out.println( "\t" 
									+ conn.getClass().getSimpleName() 
									+ "-" + conn.hashCode() );
						list.add( conn.getClass().getSimpleName() 
									+ "-" + conn.hashCode() );
					}
				} catch ( final SQLException e ) {
					// TODO Auto-generated catch block
	//				e.printStackTrace();
				}
			}
		}
		if ( !list.isEmpty() ) {
			System.out.println();
			System.out.println( "Lingering connections detected:" );
			for ( final String line : list ) {
				System.out.println( "\t" + line );
			}
			System.out.println();
		}
		
//		System.out.print( "Closing lingering connections..." );

		// skip for now..
//		for ( final WeakReference<Connection> ref : listConnections ) {
//			if ( null!=ref ) {
//				final Connection conn = ref.get();
//				if ( null!=conn ) {
//					try {
//						if ( !conn.isClosed() ) {
//							conn.close();
//						}
//						if ( conn.isClosed() ) {
//							System.out.print( "." );
//						}
//						ref.clear();
//					} catch ( final Throwable t ) {
//						if ( !t.getMessage().contains( 
//								"\" is closed.") ) {
//							System.out.print( "X" );
//							t.printStackTrace();
//						} else {
//							System.out.print( "." );
//						}
//					}
//				}
//			}
//		}
//		System.out.println( "Done." );
	}
	
	
	@Override
	protected void finalize() throws Throwable {
//		if ( null!=conn && !conn.isClosed() ) {
//			conn.close();
//		}
		this.close();
		
		super.finalize();
	}
	
	
	public static void testBasicConnection() 
					throws SQLException, ClassNotFoundException {
		
		final String strUsername = SystemUtil.getProperty( 
						SUProperty.S2DB_USERNAME ); 
		final String strPassword = SystemUtil.getProperty( 
						SUProperty.S2DB_PASSWORD ); 
		final String strURL = SystemUtil.getProperty( 
						SUProperty.S2DB_CONNECTION ); 
		
		System.out.println( "Connection URL:\n" + strURL );
		
		Class.forName( MYSQL_DRIVER );
//		Class.forName("com.mysql.cj.jdbc.Driver");
//		Class.forName("com.mysql.jdbc.Driver");
		
		final Connection conn = DriverManager.getConnection(
									strURL, strUsername, strPassword );
		
		System.out.println( "Connection established." );
		
//		final String strSQL = "SELECT * FROM Session LIMIT 5;";
		final String strSQL = "SELECT NOW();";
		System.out.println( "\tQuery: " + strSQL );
		
		final Statement stmt = conn.createStatement();
		final ResultSet rs = stmt.executeQuery( strSQL );
		while ( rs.next() ) {
			System.out.println( "\tResult: " + rs.getString( 1 ) );
		}
		
	}

	public static void testAdvancedConnection() throws Exception {
		
		ConnectionProvider.get();
		
//		final Statement stmt = ConnectionProvider.get().getStatement();
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {
			
			final DatabaseMetaData metadata = conn.getMetaData();
			final DelegatingDatabaseMetaData 
					ddmd = (DelegatingDatabaseMetaData)metadata;
			final String strURL = ddmd.getURL();
			
			System.out.println( "Connection URL: " + strURL );
		
	//		Class.forName( MYSQL_DRIVER );
	//		final Connection conn = DriverManager.getConnection( 
	//						MYSQL_CONNECTION, "s2_full", "s2db" );
	//		final Statement stmt = conn.createStatement();
			
//			final String strSQL = "select * from s2db.device";
			final String strSQL = 
								"SELECT host "
								+ "from information_schema.processlist "
								+ "WHERE ID=connection_id();";
			final ResultSet rs = stmt.executeQuery( strSQL );
			
			if ( rs.first() ) {
//				final int iSeq = rs.getInt( "seq" );
//				System.out.println( "seq = " + iSeq );
				final String strValue = rs.getString( 1 );
				System.out.println( "value = " + strValue );
			}
			
			rs.close();
			stmt.close();
		}
	}

	
	public static void main( final String[] args ) throws Exception {
		testBasicConnection();
	}

	
}
