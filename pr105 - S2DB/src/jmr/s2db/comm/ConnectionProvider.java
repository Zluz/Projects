package jmr.s2db.comm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionProvider {


	public final static String 
			MYSQL_CONNECTION = "jdbc:mysql://192.168.1.200:3306/s2db";
	
	public final static String
			MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	


	private static ConnectionProvider instance;

	private Connection conn;
	
	private ConnectionProvider() throws SQLException, ClassNotFoundException {
		Class.forName( MYSQL_DRIVER );
		this.conn = DriverManager.getConnection( 
				MYSQL_CONNECTION, "s2_full", "s2db" );
		this.conn.setSchema( "s2db" );
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
	
	public Statement getStatement() {
		try {
			final Statement result = this.conn.createStatement();
			return result;
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		if ( null!=conn && !conn.isClosed() ) {
			conn.close();
		}
		super.finalize();
	}
	
	
	public static void main( final String[] args ) throws Exception {
		
		ConnectionProvider.get();
		
		final Statement stmt = ConnectionProvider.get().getStatement();
		
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
