package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jmr.s2db.Client;
import jmr.s2db.DataFormatter;
import jmr.s2db.comm.ConnectionProvider;
import jmr.util.NetUtil;

public class Page extends TableBase {

	public static enum PageState {
		ACTIVE,
		PENDING,
		EXPIRED,
		;
	}
	
	private final static Map<Long,Map<String,String>> CACHE = new HashMap<>();
	
	
	
	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( Page.class.getName() );

		
	
	
	
	public Long get( final long seqPath ) {
		
		final Long lSession = Client.get().getSessionSeq();
		if ( null==lSession ) return null;
		
		final Long lSeq = super.get(	"page", 
//					"seq_path = " + seqPath + " AND seq_session = " + lSession, 
					"seq_path = " + seqPath, 
					"seq_path, seq_session", 
					"" + seqPath + ", " + lSession );
		return lSeq;
	}

	
	public Long getNoSession( final long seqPath ) {
		
		final Long lSeq = super.get(	"page", 
//					"seq_path = " + seqPath + " AND seq_session = " + lSession, 
					"seq_path = " + seqPath, 
					"seq_path", 
					"" + seqPath );
		return lSeq;
	}
	
	
	
	public Map<String,String> getMap( final Long seqPage ) {
		if ( null==seqPage ) throw new IllegalStateException( "Null seqPage" );

		if ( CACHE.containsKey( seqPage ) ) {
			return CACHE.get( seqPage );
		}
		
//		try ( final Statement 
//				stmt = ConnectionProvider.get().getStatement() ) {
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

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
			
			if ( CACHE.size() > 20 ) {
				CACHE.clear();
			}
			
			CACHE.put( seqPage, map );
			
			return map;
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	
	public void addMap(	final Long seqPage,
						final Map<String,String> map,
						final boolean bActivate ) {
		if ( null==seqPage ) return;
		if ( null==map ) return;

		String strSQL = null;
		
//		try ( final Statement 
//				stmt = ConnectionProvider.get().getStatement() ) {
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			String strInsert = "INSERT INTO prop "
					+ "( seq_page, name, value ) "
					+ "VALUES ";
			
			for ( final Entry<String, String> entry : map.entrySet() ) {
				final String strKey = entry.getKey();
				final String strValue = entry.getValue();
				
				strInsert += "( " + seqPage + ","
//							+ " '" + strKey + "',"
//							+ " '" + strValue + "' ),";
//							+ " " + strKey + ","
//							+ " " + strValue + " ),";
							+ " " + DataFormatter.format( strKey ) + ","
							+ " " + DataFormatter.format( strValue ) + " ),";
			}
			
			strSQL = strInsert.substring( 0, strInsert.length() - 1 ) + ";";
			
			stmt.executeUpdate( strSQL );

			final Date now = new Date();
			
//			final String strUpdate = "UPDATE page "
//					+ "SET last_modified=" + TableBase.format( now ) + ", "
//							+ "state='A' "
//					+ "WHERE seq=" + seqPage + ";";
//
//			stmt.executeUpdate( strUpdate );
			
			if ( bActivate ) {
				setState( seqPage, now, 'A' );
			}

//			LOGGER.log( Level.INFO, "New page saved (seq=" + seqPage+ ")" );
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			System.err.println( "SQL EXCEPTION - " + e.toString() );
			System.err.println( "SQL: " + strSQL );
			e.printStackTrace();
		}
		
	}
	
	
	public void setState(	final long seqPage,
							final Date dateEffective,
							final char cState ) {

//		try ( final Statement 
//				stmt = ConnectionProvider.get().getStatement() ) {
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final String strUpdate;
			if ( null!=dateEffective ) {
				strUpdate = "UPDATE page "
						+ "SET last_modified=" 
								+ DataFormatter.format( dateEffective ) + ", "
								+ "state='" + cState + "' "
						+ "WHERE seq=" + seqPage + ";";
			} else {
				strUpdate = "UPDATE page "
						+ "SET state='" + cState + "' "
						+ "WHERE seq=" + seqPage + ";";
			}

			stmt.executeUpdate( strUpdate );
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void expireAll(	final String strPageRegex ) {
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmtQuery = conn.createStatement();
				final Statement stmtUpdate = conn.createStatement() ) {
		
			final String strQuery;
			strQuery = "SELECT page.seq, path.name "
					+ "FROM Page page, Path path "
					+ "WHERE ( page.seq_path = path.seq ) "
						+ "AND ( page.state = 'A' ) "
					+ "ORDER BY seq ASC;";
			stmtQuery.executeQuery( strQuery );
			
//			final Map<String,String> map = new HashMap<>();
			final List<Long> listToExpire = new LinkedList<>();
			
			try ( final ResultSet rs = stmtQuery.executeQuery( strQuery ) ) {
				while ( rs.next() ) {
					final String strName = rs.getString( "name" );
					//TODO optimize
					if ( strName.matches( strPageRegex ) ) {
						final long seq = rs.getLong( "seq" );
						listToExpire.add( seq );
					}
				}
			}
			
			if ( !listToExpire.isEmpty() ) {

				String strUpdate = "UPDATE page "
						+ "SET state='E' "
						+ "WHERE FALSE";
				for ( final Long seq : listToExpire ) {
					strUpdate += " OR ( seq = " + seq + " )";
				}

				stmtUpdate.executeUpdate( strUpdate );
			}
			
			
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
	
	
	
	public static void main( final String[] args ) {

		final String strSession = NetUtil.getSessionID();
		final String strClass = Page.class.getName();
		Client.get().register( strSession, strClass );
		
		new Page().expireAll( "/tmp/jmr.s2db.comm.JsonIngest_1502256041720/forecast/simpleforecast/forecastday/08/qpf_day" );
	}
	
	
	
}
