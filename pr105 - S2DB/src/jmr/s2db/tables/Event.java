package jmr.s2db.tables;

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
import jmr.s2db.trigger.TriggerType;


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
public class Event extends TableBase {

	
	private static final Logger 
			LOGGER = Logger.getLogger( Event.class.getName() );

	
	private final Long seqEvent;
	private final Long seqTrigger;
	private final Long seqSession;
	private final Long seqPage;
	private final Long seqLog;
	private final Long time;
	private final TriggerType type;
	private final String strSubject;
	private final String strValue;
	private final String strData;
	
	
	
//	private Trigger() {};
	
	public static Event get( final long seqEvent ) {
		final String strWhere = "event.seq = " + seqEvent;
		final List<Event> listEvent = Event.get( strWhere, 1 );
		
		if ( null==listEvent || listEvent.isEmpty() ) {
			LOGGER.log( Level.WARNING, 
					"Requested Event record not found "
					+ "(seq=" + seqEvent + "). " );
			return null;
		} else {
			return listEvent.get( 0 );
		}
	}
	
	public static List<Event> getTriggersLike(
										final TriggerType event,
										final String strDetailLike ) {
		final String strWhere = 
				"( ( trigger.type.state = \"" + event.getChar() + "\" ) "
				+ "&& ( trigger.detail LIKE \"" + strDetailLike + "\" ) )";
		final List<Event> list = Event.get( strWhere, 100 );
		return list;
	}
	
	private Event( 	final Long lSeq,
					final long time,
					final TriggerType type,
					final String strSubject,
					final long seqSession,
					final Long seqTrigger,
					final Long seqPage,
					final Long seqLog,
					final String strValue,
					final String strData ) {
		this.seqEvent = lSeq;
		this.seqSession = seqSession;
		this.seqLog = seqLog;
		this.seqTrigger = seqTrigger;
		this.seqPage = seqPage;
		this.type = type;
		this.strSubject = strSubject;
		this.time = time;
		this.strValue = strValue;
		this.strData = strData;
	}
					
	
	public static List<Event> get( 	final String strWhere,
									final int iLimit ) {

		final String strQueryProperties = 
						 "SELECT * "
						 + " FROM event "
						 + " WHERE " + strWhere + " "
				 		 + " ORDER BY time DESC "
				 		 + " LIMIT " + iLimit + ";";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final List<Event> listJob = new LinkedList<>();
			
			try ( final ResultSet rs = stmt.executeQuery( strQueryProperties ) ) {
				while ( rs.next() ) {

					final char cType = rs.getString( "type" ).charAt( 0 );
					
					final Event trigger = new Event(	
									rs.getLong( "seq" ),
									rs.getLong( "time" ),
									TriggerType.getTriggerEventFor( cType ),
									rs.getString( "subject" ),
									rs.getLong( "seq_session" ),
									rs.getLong( "seq_trigger" ),
									rs.getLong( "seq_page" ),
									rs.getLong( "seq_log" ),
									rs.getString( "value" ),
									rs.getString( "data" ) );
					
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
	
	
	
	public static Event add(	final TriggerType type,
								final String strSubject,
								final String strValue,
								final String strData,
								final long lTime,
								final Long seqPage,
								final Long seqTrigger,
								final Long seqLog
								) {
		if ( null==type ) return null;
		
		final Long lSession = Session.getSessionSeq();
		if ( null==lSession ) {
			LOGGER.log( Level.SEVERE, 
					"Session not initialized, cannot create Triggers." );
			return null;
		}
		
		final String strInsert;
		
		strInsert = 
				"INSERT INTO event "
				+ "( seq_session, seq_page, seq_trigger, seq_log, "
									+ "time, type, subject, value, data ) "
				+ "VALUES ( " 
						+ lSession + ", "
						+ DataFormatter.format( seqPage ) + ", "
						+ DataFormatter.format( seqTrigger ) + ", "
						+ DataFormatter.format( seqLog ) + ", "
						+ lTime + ", "
						+ DataFormatter.format( type.getChar() ) + ", "
						+ DataFormatter.format( strSubject ) + ", "
						+ DataFormatter.format( strValue ) + ", "
						+ DataFormatter.format( strData ) + " );";

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeUpdate( strInsert, Statement.RETURN_GENERATED_KEYS );
			try ( final ResultSet rs = stmt.getGeneratedKeys() ) {
				
				if ( rs.next() ) {
					final long lSeq = rs.getLong( 1 );

					final Event event = Event.get( lSeq );
					
//					final Event event = new Event(
//									lSeq,
//									type,
//									detail,
//									lTime,
//									strMatch,
//									uri,
//									lSession,
//									null );
					return event;
				}
			}

		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Update SQL: " + strInsert, e );
		}
		return null;
	}
	
	
	
	public Long getEventSeq() {
		return this.seqEvent;
	}
	
	public String getData() {
		return this.strData;
	}

	public String getValue() {
		return this.strValue;
	}
	
	public String getSubject() {
		return this.strSubject;
	}

	public TriggerType getTriggerType() {
		return this.type;
	}

	public boolean delete() {
		return Event.delete( this.seqEvent );
	}
	
	public static boolean delete( final long lSeq ) {
		
		final String strDelete = 
						"DELETE FROM event WHERE seq=" + lSeq + ";";

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
