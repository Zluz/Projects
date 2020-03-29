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
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.s2db.DataFormatter;
import jmr.s2db.comm.ConnectionProvider;
import jmr.util.NetUtil;
import jmr.util.transform.DateFormatting;

public class Page extends TableBase {

	public static final String ATTR_STATE = ".state";
	public static final String ATTR_LAST_MODIFIED = ".last_modified";
	public static final String ATTR_LAST_MODIFIED_UXT = ".last_modified_uxt";
	public static final String ATTR_LAST_MODIFIED_ENG = ".last_modified_eng";
	public static final String ATTR_SEQ_SESSION = ".seq_session";
	public static final String ATTR_SEQ_PATH = ".seq_path";
	public static final String ATTR_SEQ_PAGE = ".seq_page";



	public static enum PageState {
		ACTIVE,
		PENDING,
		EXPIRED,
		;
	}
	
	private final static Map<Long,Map<String,String>> 
						CACHE = new HashMap<>();
	
	
	
	private static final Logger 
			LOGGER = Logger.getLogger( Page.class.getName() );

		
	
	public Long create( final long seqPath ) {

		final Long lSession = Client.get().getSessionSeq();
		if ( null==lSession ) return null;

		final String strInsert = "INSERT INTO page "
				+ "( seq_path, seq_session ) "
				+ "VALUES ( " + seqPath + ", " + lSession + " );";
		
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
			LOGGER.log( Level.SEVERE, "Update SQL: " + strInsert, e );
		}
		return null;

	}
	
	public Long get( final long seqPath ) {
		
		final Long lSession = Client.get().getSessionSeq();
		if ( null==lSession ) return null;
		
		final Long lSeq = super.get(	"page", 
//					"seq_path = " + seqPath + " AND seq_session = " + lSession, 
					"seq_path = " + seqPath + " AND page.state = 'A'", 
					"seq_path, seq_session", 
					"" + seqPath + ", " + lSession );
		return lSeq;
	}

	
	public Long getNoSession( final long seqPath ) {
		
		final Long lSeq = super.get(	"Page", 
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

		final String strQueryProperties = 
		 "SELECT  "
		 + "	* "
		 + "FROM  "
		 + "	prop "
		 + "WHERE "
		 + "	prop.seq_page = " + seqPage + ";";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final Map<String,String> map = new HashMap<>();
			
			try ( final ResultSet rs = stmt.executeQuery( strQueryProperties ) ) {
				while ( rs.next() ) {
					final String strName = rs.getString( "name" );
					final String strValue = rs.getString( "value" );
					
					map.put( strName, strValue );
				}
			}

			final String strQueryAttributes = 
					 "SELECT  "
					 + "	* "
					 + "FROM  "
					 + "	page "
					 + "WHERE "
					 + "	seq = " + seqPage + ";";
					 
			try ( final ResultSet rs = stmt.executeQuery( strQueryAttributes ) ) {
				if ( rs.next() ) {
					final Date dateModified = rs.getTimestamp( "last_modified" );
					final String strState = rs.getString( "state" );
//					final long seqPage = rs.getLong( "seq" );
					final long seqSession = rs.getLong( "seq_session" );
					final long seqPath = rs.getLong( "seq_path" );
					
					if ( null!=dateModified ) {
						final String strDateTime = 
								DateFormatting.getDateTime( dateModified );
						map.put( ATTR_LAST_MODIFIED, strDateTime );
						map.put( ATTR_LAST_MODIFIED_UXT, ""+dateModified.getTime() );
						map.put( ATTR_LAST_MODIFIED_ENG, strDateTime );
						
					}
					map.put( ATTR_SEQ_PAGE, "" + seqPage );
					map.put( ATTR_SEQ_PATH, "" + seqPath );
					map.put( ATTR_SEQ_SESSION, "" + seqSession );
					map.put( ATTR_STATE, strState );
				}
			}
			
			if ( CACHE.size() > 20 ) {
				CACHE.clear();
			}
			
			CACHE.put( seqPage, map );
			
			return map;
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query SQL: " + strQueryProperties, e );
		}

		return null;
	}
	
	
	public void addMap(	final Long seqPage,
						final Map<String,String> map,
						final boolean bActivate ) {
		if ( null==seqPage ) return;
		if ( null==map ) return;

		String strSQL = null;

		
		String strInsert = "INSERT INTO Prop "
				+ "( seq_page, name, value ) "
				+ "VALUES ";
		
		for ( final Entry<String, String> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();
			
			strInsert += "( " + seqPage + ","
//						+ " '" + strKey + "',"
//						+ " '" + strValue + "' ),";
//						+ " " + strKey + ","
//						+ " " + strValue + " ),";
						+ " " + DataFormatter.format( strKey ) + ","
						+ " " + DataFormatter.format( strValue ) + " ),";
		}
		
		strSQL = strInsert.substring( 0, strInsert.length() - 1 ) + ";";
		
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

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
//			System.err.println( "SQL EXCEPTION - " + e.toString() );
//			System.err.println( "SQL: " + strSQL );
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Insert SQL: " + strSQL, e );
		}
	}
	
	
	public void setState(	final long seqPage,
							final Date dateEffective,
							final char cState ) {
		
		final String strUpdate;
		if ( null!=dateEffective ) {
			strUpdate = "UPDATE Page "
					+ "SET last_modified=" 
							+ DataFormatter.format( dateEffective ) + ", "
							+ "state='" + cState + "' "
					+ "WHERE seq=" + seqPage + ";";
		} else {
			strUpdate = "UPDATE Page "
					+ "SET state='" + cState + "' "
					+ "WHERE seq=" + seqPage + ";";
		}

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeUpdate( strUpdate );
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Update SQL: " + strUpdate, e );
		}
	}
	
	
	public void expireAll(	final String strPageRegex ) {
		
		final List<Long> listToExpire = new LinkedList<>();

		final String strQuery;
		strQuery = "SELECT page.seq, path.name "
				+ "FROM Page page, Path path "
				+ "WHERE ( page.seq_path = path.seq ) "
					+ "AND ( page.state = 'A' ) "
				+ "ORDER BY seq ASC;";

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmtQuery = conn.createStatement(); ) {
		
			stmtQuery.executeQuery( strQuery );
			
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

		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query SQL: " + strQuery, e );
		}
		
		if ( !listToExpire.isEmpty() ) {

			String strUpdate = "UPDATE page "
					+ "SET state='E' "
					+ "WHERE FALSE";
			for ( final Long seq : listToExpire ) {
				strUpdate += " OR ( seq = " + seq + " )";
			}

			try (	final Connection conn = ConnectionProvider.get().getConnection();
					final Statement stmtUpdate = conn.createStatement() ) {
			
				stmtUpdate.executeUpdate( strUpdate );

			} catch ( final SQLException e ) {
				e.printStackTrace();
				LOGGER.log( Level.SEVERE, "Update SQL: " + strUpdate, e );
			}		
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
		Client.get().register( ClientType.TEST, strSession, strClass );
		
		new Page().expireAll( "/tmp/jmr.s2db.comm.JsonIngest_1502256041720/forecast/simpleforecast/forecastday/08/qpf_day" );
	}
	
	
	
}
