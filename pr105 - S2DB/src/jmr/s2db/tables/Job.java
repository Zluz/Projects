package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.DataFormatter;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.job.JobType;


/*
 * state values:
 * 		R - request
 * 		W - working
 * 		C - complete
 * 		F - failure
 */
public class Job extends TableBase {

	private static final Logger 
			LOGGER = Logger.getLogger( Page.class.getName() );

	private Long seqJob = null;
	
	private Long seqSession = null;
	private Long seqDeviceTarget = null;
	private char cState = 0;
	private String strRequest = null;
	private Long lRequestTime = null;
	private Long lCompleteTime = null;
	
	private String strMAC = null;
	
	
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
					job.cState = rs.getString( "state" ).charAt( 0 );
					job.lRequestTime = rs.getLong( "request_time" );
					job.lCompleteTime = rs.getLong( "complete_time" );
					job.seqSession = rs.getLong( "seq_session" );
					job.seqJob = rs.getLong( "seq" );
					
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
		job.cState = 'R';
		job.seqDeviceTarget = seqDeviceTarget;

		final String strInsert;
		
		final String strFormatted = DataFormatter.format( job.strRequest );

		if ( null==seqDeviceTarget ) {
			strInsert = 
					"INSERT INTO job "
					+ "( seq_session, state, request, request_time ) "
					+ "VALUES ( " 
							+ job.seqSession.longValue() + ", "
							+ "\"" + job.cState + "\", "
							+ strFormatted + ", " 
							+ job.lRequestTime + " );";
		} else {
			strInsert = 
					"INSERT INTO job "
					+ "( seq_session, state, request, request_time, "
					+ "			seq_device_target ) "
					+ "VALUES ( " 
							+ job.seqSession.longValue() + ", "
							+ "\"" + job.cState + "\", "
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
	
	public char getState() {
		return this.cState;
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
	

	public boolean setState( final char state ) {
		return setState( state, null );
	}
	
	public boolean setState(	final char state,
								final String strResult ) {
		if ( null==this.getJobSeq() ) return false;
		
		final String strUpdate;
		strUpdate = "UPDATE job "
				+ "SET state=\"" + state + "\" " 
				+ ( null!=strResult 
					? ", result=" + DataFormatter.format( strResult ) + " " 
					: "" )	
				+ "WHERE seq=" + this.getJobSeq() + ";";

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeUpdate( strUpdate );
			
			return true;
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Update SQL: " + strUpdate, e );
			return false;
		}
	}
	
}
