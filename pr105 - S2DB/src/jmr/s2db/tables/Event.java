package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import jmr.s2db.DataFormatter;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.comm.Notifier;
import jmr.s2db.event.EventMonitor;
import jmr.s2db.event.EventType;
import jmr.util.transform.JsonUtils;


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
	private final EventType type;
	private final String strSubject;
	private final String strValue;
	private final String strThreshold;
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
										final EventType event,
										final String strDetailLike ) {
		final String strWhere = 
				"( ( trigger.type.state = \"" + event.getChar() + "\" ) "
				+ "&& ( trigger.detail LIKE \"" + strDetailLike + "\" ) )";
		final List<Event> list = Event.get( strWhere, 100 );
		return list;
	}
	
	private Event( 	final Long lSeq,
					final long time,
					final EventType type,
					final String strSubject,
					final long seqSession,
					final Long seqTrigger,
					final Long seqPage,
					final Long seqLog,
					final String strValue,
					final String strThreshold,
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
		this.strThreshold = strThreshold;
		this.strData = strData;
	}

	
	public static Event getLatestEventFor( final String strSubject ) {

		final String strQuery = "SELECT seq FROM event "
				+ "WHERE subject = \"" + strSubject + "\" "
				+ "ORDER BY time DESC "
				+ "LIMIT 1;";
		 
		try ( final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				while ( rs.next() ) {
					
					final long lSeq = rs.getLong( 1 );
					
					final Event event = Event.get( lSeq );
					return event;
				}
			}
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query SQL: " + strQuery, e );
		}
		return null;
	}
	
	
	public static List<String> getSubjects() {

		final String strQuery = "SELECT DISTINCT( subject ) FROM event;";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final List<String> list = new LinkedList<>();
			
			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				while ( rs.next() ) {
					
					final String strSubject = rs.getString( 1 );
					
					list.add( strSubject );
				}
			}
			
			return list;
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query SQL: " + strQuery, e );
		}
		return null;
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
									EventType.getTriggerEventFor( cType ),
									rs.getString( "subject" ),
									rs.getLong( "seq_session" ),
									rs.getLong( "seq_trigger" ),
									rs.getLong( "seq_page" ),
									rs.getLong( "seq_log" ),
									rs.getString( "value" ),
									rs.getString( "threshold" ),
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
	
	
	private final static Gson GSON = new Gson();
	

	public static Event add(	final EventType type,
								final String strSubject,
								final String strValue,
								final String strThreshold,
								final Map<String,Object> map,
								final long lTime,
								final Long seqPage,
								final Long seqTrigger,
								final Long seqLog
								) {

		final String strJson;
		if ( null==map || map.isEmpty() ) {
			LOGGER.warning( "Event.add(), map is not populated." );
			strJson = "{}";
		} else {
			strJson = GSON.toJson( map );
			if ( ! map.containsKey( "source-initiate" ) ) {
				LOGGER.warning( "Event.add(), map is not populated." );
				final Exception e = new Exception();
				e.printStackTrace();
			}
		}

		final Event event = add( type, strSubject, strValue, strThreshold, 
						strJson, lTime, seqPage, seqTrigger, seqLog );
		return event;
	}

	
	/**
	 * Create and submit an event into the system.
	 * @param type
	 * @param strSubject
	 * @param strValue
	 * @param strThreshold
	 * @param strData
	 * @param lTime
	 * @param seqPage
	 * @param seqTrigger
	 * @param seqLog
	 * @return
	 */
	public static Event add(	final EventType type,
								final String strSubject,
								final String strValue,
								final String strThreshold,
								final String strData,
								final long lTime,
								final Long seqPage,
								final Long seqTrigger,
								final Long seqLog
								) {
//		System.out.println( "--> Event.add()" );
		
		try {
			
			final Event event = add_unchecked( type, 
					strSubject, strValue, strThreshold, strData,
					lTime, seqPage, seqTrigger, seqLog );

//			if ( null!=event ) {
//				System.out.println( "<-- Event.add(), seq=" + event.getEventSeq() );
//			} else {
//				System.out.println( "<-- Event.add(), event is null" );
//			}

			return event;
			
		} catch ( final Throwable t ) {

//			System.err.println( "<-- Event.add() - " +
			System.err.println( 
						"ERROR when creating event: " + t.toString() );
			t.printStackTrace();
			return null;
//		} finally {
//			System.out.println( "<-- Event.add().." );
		}
	}

	
	private static Event add_unchecked(	final EventType type,
										final String strSubject,
										final String strValue,
										final String strThreshold,
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
					"Session not initialized, cannot create Events." );
			return null;
		}
		
		final String strInsert;
		
		strInsert = 
				"INSERT INTO event "
				+ "( seq_session, seq_page, seq_trigger, seq_log, "
									+ "time, type, subject, "
									+ "value, threshold, data ) "
				+ "VALUES ( " 
						+ lSession + ", "
						+ DataFormatter.format( seqPage ) + ", "
						+ DataFormatter.format( seqTrigger ) + ", "
						+ DataFormatter.format( seqLog ) + ", "
						+ lTime + ", "
						+ DataFormatter.format( type.getChar() ) + ", "
						+ DataFormatter.format( strSubject ) + ", "
						+ DataFormatter.format( strValue ) + ", "
						+ DataFormatter.format( strThreshold ) + ", "
						+ DataFormatter.format( strData ) + " );";

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeUpdate( strInsert, Statement.RETURN_GENERATED_KEYS );
			
			Notifier.getInstance().pushTableUpdate( "event" );
			
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
					
					EventMonitor.postNewEvent( event );
					
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
	
//	final private static JsonParser PARSER = new JsonParser(); 
	
	public Map<String,Object> getDataAsMap() {
		final Map<String, Object> map = 
						JsonUtils.transformJsonToMap( this.strData );
		return map;
//		final Map<String,Object> map = new HashMap<>();
//		if ( StringUtils.isNotBlank( this.strData ) ) {
//			try {
//				final JsonElement je = PARSER.parse( this.strData );
//				if ( je.isJsonObject() ) {
//					final JsonObject jo = je.getAsJsonObject();
//					
//				} else {
//					LOGGER.warning( ()-> "Data field is not a JSON Object"
//							+ " '" + strData + "'" );
//				}
//			} catch ( final JsonException e ) {
//				LOGGER.warning( ()-> "Failed to parse data field"
//						+ " '" + strData + "'" );
//			}
//		}
//		return map;
	}

	public String getValue() {
		return this.strValue;
	}

	public String getThreshold() {
		return this.strThreshold;
	}
	
	public String getSubject() {
		return this.strSubject;
	}

	public EventType getEventType() {
		return this.type;
	}
	
	public long getTime() {
		return this.time;
	}

	public Long getTriggerSeq() {
		return seqTrigger;
	}

	public Long getSessionSeq() {
		return seqSession;
	}

	public Long getPageSeq() {
		return seqPage;
	}

	public Long getLogSeq() {
		return seqLog;
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
	
	
	@Override
	public String toString() {
		final String strResult = super.toString() 
				+ " (type:" + this.getEventType().name() + ", "
				+ "subject:" + this.getSubject() + ", "
				+ "seq=" + this.getEventSeq() + ", "
//				+ "data:\"" + StringUtils.substring( this.getData(), 0, 20 ) 
				+ "data:\"" + StringUtils.abbreviate( this.getData(), 20 ) 
				+ "\")";
		return strResult;
	}

}
