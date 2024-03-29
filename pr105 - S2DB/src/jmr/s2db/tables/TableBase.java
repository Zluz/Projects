package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.Settings;
import jmr.s2db.comm.ConnectionProvider;

public abstract class TableBase {

	private static final Logger 
			LOGGER = Logger.getLogger( TableBase.class.getName() );
	

	public Long get(	final String strTable,
						final String strWhere,
						final String strInsertNames,
						final String strInsertValues ) {
		if ( ! Settings.SQL_ENABLED ) return null;
		if ( null==strTable ) return null;
		if ( null==strWhere ) return null;
		if ( strTable.isEmpty() ) {
			throw new IllegalStateException( "Missing table name." );
		}
		if ( Character.isLowerCase( strTable.charAt( 0 ) ) ) {
			throw new IllegalStateException( 
							"Table name appears to be lower-case." );
		}
		
		final String strQuery = "SELECT MAX(seq) FROM " + strTable + " "
				+ "WHERE ( " + strWhere + " );";
		
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = null!=conn ? conn.createStatement() : null ) {

			if ( null!=stmt ) {
				try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
					if ( rs.next() && !rs.wasNull() ) {
						final long lSeq = rs.getLong( 1 );
						if ( lSeq > 0 ) {
							return lSeq;
						}
					}
				}
			} else {
				LOGGER.severe( ()-> "Failed to acquire a database connection." );
				return null;
			}
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "SQL Query: " + strQuery, e );
		}

		if ( null==strInsertNames ) return null;
		if ( null==strInsertValues ) return null;

		final String strInsert = "INSERT INTO " + strTable + " "
				+ "( " + strInsertNames + " ) "
				+ "VALUES ( " + strInsertValues + " );";

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeUpdate( strInsert, Statement.RETURN_GENERATED_KEYS );
			try ( final ResultSet rs = stmt.getGeneratedKeys() ) {
				
				if ( rs.next() ) {
					final long lSeq = rs.getLong( 1 );
					return lSeq;
				}
			}
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "SQL Insert: " + strInsert, e );
		}
		return null;
	}
	
	
	
}
