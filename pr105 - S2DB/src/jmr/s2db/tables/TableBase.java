package jmr.s2db.tables;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import jmr.s2db.comm.ConnectionProvider;

public abstract class TableBase {

	
	final static SimpleDateFormat DATE_FORMATTER = 
							new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	
	
	public Long get(	final String strTable,
						final String strWhere,
						final String strInsertNames,
						final String strInsertValues ) {
		if ( null==strTable ) return null;
		if ( null==strWhere ) return null;
		if ( null==strInsertNames ) return null;
		if ( null==strInsertValues ) return null;
		
		try ( final Statement 
				stmt = ConnectionProvider.get().getStatement() ) {

			final String strQuery = "SELECT seq FROM " + strTable + " "
					+ "WHERE ( " + strWhere + " );";
			
			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				if ( rs.next() ) {
					final long lSeq = rs.getLong( 1 );
					return lSeq;
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
	
	

	public static String format( final Object value ) {
		if ( null==value ) return "''";
		if ( value instanceof String ) {
			return "'" + value + "'";
		} else if ( value instanceof Number ) {
			return "" + value.toString();
		} else if ( value instanceof Date ) {
			return "'" + DATE_FORMATTER.format( ((Date) value) ) + "'";
		} else {
			return "'" + value.toString() + "'";
		}
//		return "";
	}

	
}
