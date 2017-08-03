package jmr.s2db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

public class Client {

	public final static String 
			MYSQL_CONNECTION = "jdbc:mysql://192.168.1.200:3306/s2db";
	
	public final static String
			MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	

	private static Client instance;
	
	private Client() {};
	
	
	public static synchronized Client get() {
		if ( null==instance ) {
			instance = new Client();
		}
		return instance;
	}
	
	
	public long register(	final String strMAC,
							final Date now ) {
		return 0l;
	}
	
	public static void main( final String[] args ) throws Exception {
		
		Class.forName( MYSQL_DRIVER );
		final Connection conn = DriverManager.getConnection( 
						MYSQL_CONNECTION, "s2_full", "s2db" );
		final Statement stmt = conn.createStatement();
		
		final String strSQL = "select * from s2db.device";
		final ResultSet rs = stmt.executeQuery( strSQL );
		
		if ( rs.first() ) {
			final int iSeq = rs.getInt( "seq" );
			System.out.println( "seq = " + iSeq );
		}
		
		rs.close();
		stmt.close();
		conn.close();
	}
	
}
