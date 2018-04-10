package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jmr.s2db.DataFormatter;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.job.JobType;
import jmr.util.transform.JsonUtils;


/*
 * state values:
 * 		R - request
 * 		W - working
 * 		C - complete
 * 		F - failure
 */
public class Job extends TableBase {

	public static enum JobState {
		REQUEST( true ),
		WORKING( true ),
		COMPLETE,
		FAILURE,
		UNKNOWN,
		;
		
		final boolean bActive;
		
		JobState( final boolean bActive ) {
			this.bActive = bActive;
		}
		
		JobState() {
			this( false );
		}
		
		public char getChar() {
			return this.name().charAt( 0 );
		}
		
		public static JobState getJobStateFor( final char c ) {
			for ( final JobState state : JobState.values() ) {
				if ( c==state.getChar() ) {
					return state;
				}
			}
			return JobState.UNKNOWN;
		}

		public boolean isActive() {
			return this.bActive;
		}
	}
	
	
	private static final Logger 
			LOGGER = Logger.getLogger( Page.class.getName() );

	
	public StatusListener listener = null;
	
	public Thread threadMonitorStatus = null;

	
	
	private Long seqJob = null;
	
	private Long seqSession = null;
	private Long seqDeviceTarget = null;
	private JobState state = JobState.UNKNOWN;
	private String strRequest = null;
	private Long lRequestTime = null;
	private Long lCompleteTime = null;
	private String strResult;
	
	private long lLastRefresh = 0;
	
	private String strMAC = null;

	
	
	private Job() {};
	
	public static Job get( final long seqJob ) {
		final String strWhere = "job.seq = " + seqJob;
		final List<Job> listJob = Job.get( strWhere, 100 );
		
		if ( null==listJob || listJob.isEmpty() ) {
			LOGGER.log( Level.WARNING, 
					"Requested Job record not found "
					+ "(seq=" + seqJob + "). " );
			return null;
		} else {
			return listJob.get( 0 );
		}
	}
	
	public static List<Job> getNewJobsContaining( 
										final String strRequestContains ) {
		final String strWhere = 
				"( ( job.state = \"R\" ) "
				+ "&& ( job.request LIKE \"" + strRequestContains + "\" ) )";
		final List<Job> listJob = Job.get( strWhere, 100 );
		return listJob;
	}
	
	public static List<Job> get( 	final String strWhere,
									final int iLimit ) {

		final String strQueryProperties = 
						 "SELECT * "
						 + " FROM job "
						 + " WHERE " + strWhere + " "
				 		 + " ORDER BY request_time DESC "
				 		 + " LIMIT " + iLimit + ";";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final List<Job> listJob = new LinkedList<>();
			
			final long lQueryTime = System.currentTimeMillis();
			
			try ( final ResultSet rs = stmt.executeQuery( strQueryProperties ) ) {
				while ( rs.next() ) {
					
					final Job job = new Job();
					
					job.strRequest = rs.getString( "request" );
					final Object objDeviceTarget 
							= rs.getObject( "seq_device_target" );
					if ( objDeviceTarget instanceof Number ) {
						job.seqDeviceTarget 
								= ((Number)objDeviceTarget).longValue();
					}
					final char cState = rs.getString( "state" ).charAt( 0 );
					job.state = JobState.getJobStateFor( cState );
					job.lRequestTime = rs.getLong( "request_time" );
					job.lCompleteTime = rs.getLong( "complete_time" );
					job.seqSession = rs.getLong( "seq_session" );
					job.seqJob = rs.getLong( "seq" );
					job.strResult = rs.getString( "result" );
					job.lLastRefresh = lQueryTime;
					
					listJob.add( job );
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
	
	public static Job add(	final JobType type,
							final String strOptions ) {
		return Job.add( null, type, strOptions );
	}
	

	public static Job add(	final JobType type,
							final Map<String,String> map ) {
		final StringBuilder strOptions = new StringBuilder();
		if ( null!=map ) {
			for ( final Entry<String, String> entry : map.entrySet() ) {
				final String strKey = entry.getKey();
				final String strValue = entry.getValue();
				
				strOptions.append( strKey + "=" + strValue + "\\" );
			}
		}
		return Job.add( null, type, strOptions.toString() );
	}
	
	
	public static Job add(	final JobType type,
							final String[] options ) {
		final Map<String,String> map = new HashMap<>();
		for ( int i=1; i<options.length; i=i+2 ) {
			map.put( options[i-1], options[i-0] );
		}
		return Job.add( type, map );
	}
	
	
	public static Job add(	final Long seqDeviceTarget,
							final JobType type,
							final String strOptions ) {
		final Long lSession = Session.getSessionSeq();
		if ( null==lSession ) {
			LOGGER.log( Level.SEVERE, 
					"Session not initialized, cannot create Job." );
			return null;
		}
		
		final Job job = new Job();
		job.seqSession = lSession;
		job.lRequestTime = System.currentTimeMillis();
		job.strRequest = type.name() + ":" + strOptions;
		job.state = JobState.REQUEST;
		job.seqDeviceTarget = seqDeviceTarget;

		final String strInsert;
		
		final String strFormatted = DataFormatter.format( job.strRequest );

		if ( null==seqDeviceTarget ) {
			strInsert = 
					"INSERT INTO job "
					+ "( seq_session, state, request, request_time ) "
					+ "VALUES ( " 
							+ job.seqSession.longValue() + ", "
							+ "\"" + job.state.getChar() + "\", "
							+ strFormatted + ", " 
							+ job.lRequestTime + " );";
		} else {
			strInsert = 
					"INSERT INTO job "
					+ "( seq_session, state, request, request_time, "
					+ "			seq_device_target ) "
					+ "VALUES ( " 
							+ job.seqSession.longValue() + ", "
							+ "\"" + job.state.getChar() + "\", "
							+ strFormatted + ", " 
							+ job.lRequestTime + ","
							+ job.seqDeviceTarget + " );";
		}

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeUpdate( strInsert, Statement.RETURN_GENERATED_KEYS );
			try ( final ResultSet rs = stmt.getGeneratedKeys() ) {
				
				if ( rs.next() ) {
					final long lSeq = rs.getLong( 1 );
					job.seqJob = lSeq;
					return job;
				}
			}
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Update SQL: " + strInsert, e );
		}
		return null;
	}
	
	
	
	public Long getJobSeq() {
		return this.seqJob;
	}
	
	public String getRequest() {
		return this.strRequest;
	}
	
	public JobType getJobType() {
		final JobType type = JobType.getType( this.getRequest() );
		return type;
	}
	
	public Map<String,String> getJobDetails() {
		if ( !this.strRequest.contains( ":" ) ) return Collections.emptyMap();
		
		final int iPos = this.strRequest.indexOf( ':' );
		final String strDetails = strRequest.substring( iPos + 1 ).trim();
		final Map<String,String> map = JobType.getDetails( strDetails );
		return map;
	}
	
	public Long getDeviceTargetSeq() {
		return this.seqDeviceTarget;
	}
	
	public JobState getState() {
		return this.state;
	}
	
	public Long getRequestTime() {
		return this.lRequestTime;
	}
	
	public Long getCompleteTime() {
		return this.lCompleteTime;
	}
	
	public String getMAC() {
		if ( null==this.strMAC ) {
//			Client.get().
			this.strMAC = "<?,seqSession=" + this.seqSession + ">";
		}
		return this.strMAC;
	}
	

	public boolean setState( final JobState state ) {
		return setState( state, null );
	}
	
	public boolean setState(	final JobState state,
								final String strResult ) {
		if ( null==this.getJobSeq() ) return false;
		
		final String strNewData;
		if ( null!=strResult ) {
			final JsonObject jo = JsonUtils.getJsonObjectFor( strResult );
			addData( jo );
			strNewData = DataFormatter.format( this.strResult );
		} else {
			strNewData = null;
		}
		
		final String strUpdate;
		strUpdate = "UPDATE job "
				+ "SET "
				+ "seq=" + this.seqJob
				+ ( null!=state 
					? ", state=\"" + state.getChar() + "\" " 
					: "" ) 
				+ ( null!=strNewData 
					? ", result=" + strNewData + " "
					: "" )	
				+ "WHERE seq=" + this.getJobSeq() + ";";

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			if ( null!=state ) {
				this.state = state;
			}
			if ( null!=strNewData ) {
				this.strResult = strResult;
			}

			stmt.executeUpdate( strUpdate );
			
			return true;
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Update SQL: " + strUpdate, e );
			return false;
		}
	}
	
	
	/**
	 * This will add data in the given JsonObject to the existing data
	 * string. This will NOT save it to the database.
	 * @param jo
	 * @return
	 */
	public boolean addData( final JsonObject jo ) {
		if ( null==jo ) return false;
		
		final JsonObject joData = JsonUtils.getJsonObjectFor( strResult );
		
		for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {
			joData.add( entry.getKey(), entry.getValue() );
		}
		
		this.strResult = joData.toString();
		return true;
	}
	

	public boolean updateProgress( final String strLine ) {
		if ( null==strLine ) return false;
		
//		System.out.println( "processing >> \"" + strLine + "\"" );
		
		//						  012345
		if ( strLine.startsWith( "#JSON " ) ) {
			final String strTrimmed = strLine.substring( 5 ).trim();
			
//			final JsonObject joData = JsonUtils.getJsonObjectFor( strResult );
			
			final JsonObject joNew = JsonUtils.getJsonObjectFor( strTrimmed );
			
//			for ( final Entry<String, JsonElement> entry : joNew.entrySet() ) {
//				joData.add( entry.getKey(), entry.getValue() );
//			}
//			
//			final String strData = joData.toString();

			this.addData( joNew );
			
			System.out.println( "Updating data on Job " + this.seqJob + ":" );
			System.out.println( this.strResult );
			this.setState( null, strResult );
		}
		return false;
	}
	
	
	public long getTimeSinceRefresh() {
		final long lNow = System.currentTimeMillis();
		final long lElapsed = lNow - this.lLastRefresh;
		return lElapsed;
	}
	
	public String getResult() {
		return this.strResult;
	}
	
	
	public static interface StatusListener {
		public void updateStatus(	final JobState state,
									final String strData );
	}
	
	
	public boolean checkForUpdatedStatus() {

		final String strQuery = "SELECT state, result "
								+ "FROM job "
								+ "WHERE seq=" + this.seqJob;
		
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				if ( rs.next() ) {
					final char cState = rs.getString( "state" ).charAt( 0 );
					this.state = JobState.getJobStateFor( cState );
					this.strResult = rs.getString( "result" );
					
					return true;
				}
			}
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query SQL: " + strQuery, e );
		}
		return false;
	}
	
	
	public void setStatusListener( final StatusListener listener ) {
		this.listener = listener;
		
		if ( null==threadMonitorStatus ) {
			this.threadMonitorStatus = new Thread( 
					"Job " + this.seqJob + " status monitor " ) {
				@Override
				public void run() {
					try {
						boolean bContinue = true;
						while ( bContinue 
								&& Job.this.state.isActive() ) {
							
							if ( checkForUpdatedStatus() ) {
								Thread.sleep( 200 );
							} else {
								bContinue = false;
							}
						}
					} catch ( final InterruptedException e ) {
						// ignore
					}
				}
			};
			this.threadMonitorStatus.start();
		}
	}
	
	
	public boolean refresh() {
		final Job jobNew = Job.get( this.getJobSeq() );
		if ( null!=jobNew ) {
			this.seqSession = jobNew.seqSession;
			this.seqDeviceTarget = jobNew.seqDeviceTarget;
			this.state = jobNew.state;
			this.strRequest = jobNew.strRequest;
			this.strResult = jobNew.strResult;
			this.lRequestTime = jobNew.lRequestTime;
			this.lCompleteTime = jobNew.lCompleteTime;
			this.lLastRefresh = jobNew.lLastRefresh;
			this.strMAC = jobNew.strMAC;
		}
		return true;
	}
	
	

	
}
