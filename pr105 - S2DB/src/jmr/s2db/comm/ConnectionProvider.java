package jmr.s2db.comm;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

public class ConnectionProvider {


	public final static String 
			MYSQL_CONNECTION = "jdbc:mysql://192.168.1.200:3306/s2db";
	
	public final static String
			MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	

	private final BasicDataSource bds;

	private static ConnectionProvider instance;

//	private Connection conn;
	
	
	private final List<WeakReference<Connection>> 
						listConnections = new LinkedList<>();
	
	
	private ConnectionProvider() throws SQLException, ClassNotFoundException {
		Class.forName( MYSQL_DRIVER );

//		this.conn = DriverManager.getConnection( 
//				MYSQL_CONNECTION, "s2_full", "s2db" );
//		this.conn.setSchema( "s2db" );
		
		bds = new BasicDataSource();
		
		bds.setDriverClassName( MYSQL_DRIVER );
		bds.setUrl( MYSQL_CONNECTION );
		bds.setUsername( "s2_full" );
		bds.setPassword( "s2db" );
		bds.setMaxActive( 10 );
		bds.setMaxOpenPreparedStatements( 10 );
		
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
			public void run() {
				close();
			}
		});
	};
	
	
	public static synchronized ConnectionProvider get() {
		if ( null==instance ) {
			try {
				instance = new ConnectionProvider();
			} catch ( final SQLException | ClassNotFoundException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	

	
	public Connection getConnection() {
		try {
//			https://stackoverflow.com/questions/7592056/am-i-using-jdbc-connection-pooling
				
			final Connection conn = bds.getConnection();
			listConnections.add( new WeakReference<Connection>( conn ) );
			return conn;
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	
	public void close() {

		final String strCaller = 
				Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.print( "Closing lingering connections, "
						+ "called from " + strCaller + "()..." );

		for ( final WeakReference<Connection> ref : listConnections ) {
			if ( null!=ref ) {
				final Connection conn = ref.get();
				if ( null!=conn ) {
					try {
						new Thread() {
							@Override
							public void run() {
								try {
									conn.close();
									ref.clear();
								} catch ( final SQLException e ) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}.start();
					} catch ( final Throwable t ) {
						// ignore
					}
				}
			}
		}
		try {
			Thread.sleep( 100 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for ( final WeakReference<Connection> ref : listConnections ) {
			if ( null!=ref ) {
				final Connection conn = ref.get();
				if ( null!=conn ) {
					try {
						conn.close();
						if ( conn.isClosed() ) {
							System.out.print( "." );
							ref.clear();
						}
					} catch ( final Throwable t ) {
						System.out.print( "X" );
					}
				}
			}
		}
		System.out.println( "Done." );
	}
	
	
	@Override
	protected void finalize() throws Throwable {
//		if ( null!=conn && !conn.isClosed() ) {
//			conn.close();
//		}
		this.close();
		
		super.finalize();
	}
	
	
	public static void main( final String[] args ) throws Exception {
		
		ConnectionProvider.get();
		
//		final Statement stmt = ConnectionProvider.get().getStatement();
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {
		
	//		Class.forName( MYSQL_DRIVER );
	//		final Connection conn = DriverManager.getConnection( 
	//						MYSQL_CONNECTION, "s2_full", "s2db" );
	//		final Statement stmt = conn.createStatement();
			
			final String strSQL = "select * from s2db.device";
			final ResultSet rs = stmt.executeQuery( strSQL );
			
			if ( rs.first() ) {
				final int iSeq = rs.getInt( "seq" );
				System.out.println( "seq = " + iSeq );
			}
			
			rs.close();
			stmt.close();
		}
	}
	
}
