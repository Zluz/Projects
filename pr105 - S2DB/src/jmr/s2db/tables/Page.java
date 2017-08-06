package jmr.s2db.tables;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jmr.s2db.Client;
import jmr.s2db.comm.ConnectionProvider;

public class Page extends TableBase {

	public static enum PageState {
		ACTIVE,
		PENDING,
		EXPIRED,
		;
	}
	
	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( Page.class.getName() );

		
	
	
	public Long get( final long seqPath ) {
		
		final Long lSession = Client.get().getSessionSeq();
		if ( null==lSession ) return null;
		
		final Long lSeq = super.get(	"page", 
					"seq_path = " + seqPath + " AND seq_session = " + lSession, 
					"seq_path, seq_session", 
					"" + seqPath + ", " + lSession );
		return lSeq;
	}
	
	
	public Map<String,String> getMap( final Long seqPage ) {
		if ( null==seqPage ) throw new IllegalStateException( "Null seqPage" );

		try ( final Statement 
				stmt = ConnectionProvider.get().getStatement() ) {

			final String strQuery = 
			 "SELECT  "
			 + "	* "
			 + "FROM  "
			 + "	prop "
			 + "WHERE "
			 + "	prop.seq_page = " + seqPage + ";";
			 
			stmt.executeQuery( strQuery );

			final Map<String,String> map = new HashMap<>();
			
			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				while ( rs.next() ) {
					final String strName = rs.getString( "name" );
					final String strValue = rs.getString( "value" );
					
					map.put( strName, strValue );
				}
			}
			
			return map;
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	
	public void addMap(	final Long seqPage,
						final Map<String,String> map ) {
		if ( null==seqPage ) return;
		if ( null==map ) return;

		try ( final Statement 
				stmt = ConnectionProvider.get().getStatement() ) {

			String strInsert = "INSERT INTO prop "
					+ "( seq_page, name, value ) "
					+ "VALUES ";
			
			for ( final Entry<String, String> entry : map.entrySet() ) {
				final String strKey = entry.getKey();
				final String strValue = entry.getValue();
				
				strInsert += "( " + seqPage + ","
							+ " '" + strKey + "',"
							+ " '" + strValue + "' ),";
			}
			
			strInsert = strInsert.substring( 0, strInsert.length() - 1 ) + ";";
			
			stmt.executeUpdate( strInsert );

			final Date now = new Date();
			
//			final String strUpdate = "UPDATE page "
//					+ "SET last_modified=" + TableBase.format( now ) + ", "
//							+ "state='A' "
//					+ "WHERE seq=" + seqPage + ";";
//
//			stmt.executeUpdate( strUpdate );
			
			setState( seqPage, now, 'A' );

//			LOGGER.log( Level.INFO, "New page saved (seq=" + seqPage+ ")" );
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void setState(	final long seqPage,
							final Date dateEffective,
							final char cState ) {

		try ( final Statement 
				stmt = ConnectionProvider.get().getStatement() ) {

			final String strUpdate = "UPDATE page "
					+ "SET last_modified=" 
							+ TableBase.format( dateEffective ) + ", "
							+ "state='" + cState + "' "
					+ "WHERE seq=" + seqPage + ";";

			stmt.executeUpdate( strUpdate );
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void expire(	final long seqPage ) {
		final Date now = new Date();
		setState( seqPage, now, 'E' );
		
//		LOGGER.log( Level.INFO, "Existing page expired (seq=" + seqPage+ ")" );
	}
	
	
	
}
