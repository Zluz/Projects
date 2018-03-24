package jmr.s2db.tables;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.DataFormatter;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.event.EventType;
import jmr.s2db.trigger.TriggerDetail;


/*
 * trigger types:
		I - Hardware input port (digital or analog)
		O - Hardware output port (digital or analog)
		P - Page updated (may be imported content)
		T - Time event
		U - User input event (pressing a button)

	detail:
		PLAY_AUDIO_PROGRAM


 */
public class Trigger extends TableBase {

	
	private static final Logger 
			LOGGER = Logger.getLogger( Trigger.class.getName() );

	
	private final Long seqTrigger;
	private final Long seqSession;
	private final Long seqLog;
	private final Long time;
	private final EventType type;
	private final TriggerDetail detail;
	private final String strMatch;
	private final URI uri;
	
	
	
//	private Trigger() {};
	
	public static Trigger get( final long seqTrigger ) {
		final String strWhere = "trigger.seq = " + seqTrigger;
		final List<Trigger> listTrigger = Trigger.get( strWhere, 1 );
		
		if ( null==listTrigger || listTrigger.isEmpty() ) {
			LOGGER.log( Level.WARNING, 
					"Requested Trigger record not found "
					+ "(seq=" + seqTrigger + "). " );
			return null;
		} else {
			return listTrigger.get( 0 );
		}
	}
	
	public static List<Trigger> getTriggersLike(
										final EventType event,
										final String strDetailLike ) {
		final String strWhere = 
				"( ( trigger.type.state = \"" + event.getChar() + "\" ) "
				+ "&& ( trigger.detail LIKE \"" + strDetailLike + "\" ) )";
		final List<Trigger> list = Trigger.get( strWhere, 100 );
		return list;
	}
	
	private Trigger( final Long lSeq,
					final EventType event,
					final TriggerDetail detail,
					final long time,
					final String strMatch,
					final URI uri,
					final long seqSession,
					final Long seqLog ) {
		this.seqTrigger = lSeq;
		this.seqSession = seqSession;
		this.seqLog = seqLog;
		this.type = event;
		this.time = time;
		this.uri = uri;
		this.detail = detail;
		this.strMatch = strMatch;
	}
					
	
	public static List<Trigger> get( 	final String strWhere,
										final int iLimit ) {

		final String strQueryProperties = 
						 "SELECT * "
						 + " FROM trigger "
						 + " WHERE " + strWhere + " "
				 		 + " ORDER BY time DESC "
				 		 + " LIMIT " + iLimit + ";";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final List<Trigger> listJob = new LinkedList<>();
			
			try ( final ResultSet rs = stmt.executeQuery( strQueryProperties ) ) {
				while ( rs.next() ) {

					final char cType = rs.getString( "type" ).charAt( 0 );
					
					URI uri = null;
					final String strURI = rs.getString( "url" );
					try {
						uri = URI.create( strURI );
					} catch ( final Exception e ) {
						uri = null;
					}

					final Trigger trigger = new Trigger(	
									rs.getLong( "seq" ),
									EventType.getTriggerEventFor( cType ),
									TriggerDetail.valueOf( rs.getString( "detail" ) ),
									rs.getLong( "time" ),
									rs.getString( "match" ),
									uri,
									rs.getLong( "seq_session" ),
									rs.getLong( "seq_log" ) );
					
//					trigger.type = TriggerEvent.getTriggerEventFor( cType );
//					
//					trigger.time = rs.getLong( "time" );
//					trigger.seqTrigger = rs.getLong( "seq" );
//					trigger.seqSession = rs.getLong( "seq_session" );
//					trigger.seqLog = rs.getLong( "seq_log" );
//					
//					trigger.strDetail = rs.getString( "detail" );
//					trigger.strMatch = rs.getString( "match" );
//					final String strURI = rs.getString( "url" );
//					try {
//						trigger.uri = URI.create( strURI );
//					} catch ( final Exception e ) {
//						trigger.uri = null;
//					}
					
					listJob.add( trigger );
				}
			}
			
			return listJob;
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query SQL: " + strQueryProperties, e );
		}

		return null;
	}
	
	
//	public static Job add( final String strRequest ) {
//		return Job.add( null, strRequest );
//	}
	
//	public static Trigger add(	final JobType type,
//							final String strOptions ) {
//		return Trigger.add( null, type, strOptions );
//	}
//	
//
//	public static Trigger add(	final JobType type,
//								final Map<String,String> map ) {
//		final StringBuilder strOptions = new StringBuilder();
//		if ( null!=map ) {
//			for ( final Entry<String, String> entry : map.entrySet() ) {
//				final String strKey = entry.getKey();
//				final String strValue = entry.getValue();
//				
//				strOptions.append( strKey + "=" + strValue + "\\" );
//			}
//		}
//		return Trigger.add( null, type, strOptions.toString() );
//	}
	
	
	public static Trigger add(	final EventType type,
								final TriggerDetail detail,
								final String strMatch,
								final String strURL ) {
		if ( null==type ) return null;
		
		final Long lSession = Session.getSessionSeq();
		if ( null==lSession ) {
			LOGGER.log( Level.SEVERE, 
					"Session not initialized, cannot create Triggers." );
			return null;
		}
		
		final URI uri;
		try {
			uri = new URI( strURL );
		} catch ( final URISyntaxException e ) {
			LOGGER.log( Level.SEVERE, 
					"Cannot create trigger; URL not recognized: " + strURL );
			return null;
		}

		final long lTime = System.currentTimeMillis();
		
//		final Trigger trigger = new Trigger(	
//						null, // assign later
//						type,
//						lTime,
//						strDetail,
//						strMatch,
//						uri,
//						lSession,
//						null );
		
		final String strInsert;
		
//		final String strFormatted = DataFormatter.format( trigger.strRequest );

//		if ( null==seqDeviceTarget ) {
		strInsert = 
				"INSERT INTO trigger "
				+ "( seq_session, seq_log, time, type, detail, match, url ) "
				+ "VALUES ( " 
						+ lSession + ", "
						+ "null, "
						+ lTime + ", "
						+ DataFormatter.format( type.getChar() ) + ", "
						+ DataFormatter.format( detail.name() ) + ", "
						+ DataFormatter.format( strMatch ) + ", "
						+ DataFormatter.format( uri.toString() ) + " );";
//		} else {
//			strInsert = 
//					"INSERT INTO job "
//					+ "( seq_session, state, request, request_time, "
//					+ "			seq_device_target ) "
//					+ "VALUES ( " 
//							+ trigger.seqSession.longValue() + ", "
//							+ "\"" + trigger.state.getChar() + "\", "
//							+ strFormatted + ", " 
//							+ trigger.lRequestTime + ","
//							+ trigger.seqDeviceTarget + " );";
//		}

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeUpdate( strInsert, Statement.RETURN_GENERATED_KEYS );
			try ( final ResultSet rs = stmt.getGeneratedKeys() ) {
				
				if ( rs.next() ) {
					final long lSeq = rs.getLong( 1 );

					final Trigger trigger = new Trigger(
									lSeq,
									type,
									detail,
									lTime,
									strMatch,
									uri,
									lSession,
									null );
					return trigger;
				}
			}

		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Update SQL: " + strInsert, e );
		}
		return null;
	}
	
	
	
	public Long getTriggerSeq() {
		return this.seqTrigger;
	}
	
	public TriggerDetail getDetail() {
		return this.detail;
	}
	
	public String getMatch() {
		return this.strMatch;
	}

	public URI getURI() {
		return this.uri;
	}
	
	public EventType getTriggerType() {
		return this.type;
	}

	public boolean delete() {
		return Trigger.delete( this.seqTrigger );
	}
	
	public static boolean delete( final long lSeq ) {
		
		final String strDelete = 
						"DELETE FROM trigger WHERE seq=" + lSeq + ";";

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final boolean bResult = stmt.execute( strDelete );
			return bResult;

		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Delete SQL: " + strDelete, e );
			
			return false;
		}
	}

}
