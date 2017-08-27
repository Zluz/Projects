package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jmr.s2db.comm.ConnectionProvider;

public abstract class TableBase {

	
	public Long get(	final String strTable,
						final String strWhere,
						final String strInsertNames,
						final String strInsertValues ) {
		if ( null==strTable ) return null;
		if ( null==strWhere ) return null;
		if ( null==strInsertNames ) return null;
		if ( null==strInsertValues ) return null;
		
//		try ( final Statement 
//				stmt = ConnectionProvider.get().getStatement() ) {
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final String strQuery = "SELECT MAX(seq) FROM " + strTable + " "
					+ "WHERE ( " + strWhere + " );";
			
			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				if ( rs.next() && !rs.wasNull() ) {
					final long lSeq = rs.getLong( 1 );
					if ( lSeq > 0 ) {
						return lSeq;
					}
				}
			}
			
			final String strInsert = "INSERT INTO " + strTable + " "
					+ "( " + strInsertNames + " ) "
					+ "VALUES ( " + strInsertValues + " );";
			
			stmt.executeUpdate( strInsert, Statement.RETURN_GENERATED_KEYS );
			try ( final ResultSet rs = stmt.getGeneratedKeys() ) {
				
				if ( rs.next() ) {
					final long lSeq = rs.getLong( 1 );
					return lSeq;
				}
			}
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
