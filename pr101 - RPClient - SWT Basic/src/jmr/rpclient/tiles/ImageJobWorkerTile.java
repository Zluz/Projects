package jmr.rpclient.tiles;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;
import jmr.rpclient.tiles.HistogramTile.Graph;
import jmr.s2db.Client;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.event.EventType;
import jmr.s2db.job.JobManager;
import jmr.s2db.tables.Event;
import jmr.s2db.tables.Job;
import jmr.util.FileUtil;
import jmr.util.NetUtil;
import jmr.util.RunProcess;
import jmr.util.report.TraceMap;
import jmr.util.transform.DateFormatting;
import jmr.util.transform.JsonUtils;

public class ImageJobWorkerTile extends TileBase {


//	private static final float CHOKE_CLOSE_SLOW = 0.0001f;
//	private static final float CHOKE_OPEN_SLOW  = 0.0100f;
//	private static final float CHOKE_OPEN_FAST  = 0.5000f;


	private static final Logger 
			LOGGER = Logger.getLogger( ImageJobWorkerTile.class.getName() );


	private static enum State {
		IDLE,
		WAITING_FOR_FILE,
		PREPARING_FILES,
		EXECUTING_COMPARISON,
		POST_COMPARISON,
		FAULT,
		INFO__DISABLED,
		WARNING__SHARE_READONLY,
		WARNING__MISSING_SOURCE_DIR,
		;
	}
	
//	private final static Set<String> MONITOR_FILES = new HashSet<>();
	
	
	private RunProcess run;
	
	private final Thread threadUpdater;

//	private final String strName;
//	private final String strSourceImage;
	private final String strIndex;
	private final String strExpectedObjects;
//	private final float fThreshold;
//	private final File fileSourcePath;
	
	private State state = State.IDLE;
//	private float fChoke;
	private int iCountTotal;
	private int iCountCompleted;
	private int iCountChanged;
	private boolean bUsingMask;
	private boolean bWarmup = true;
//	private int iCyclesSinceLastChange = 10;
	
	String strData = "uninitialized";
	
	
//	public String getBaseFilename() {
//		final int iPosSlash = this.strSourceImage.lastIndexOf( '/' );
//		final String strNoPath = this.strSourceImage.substring( iPosSlash + 1 );
//		final int iPosDot = strNoPath.lastIndexOf( '.' );
//		final String strNoExt = strNoPath.substring( 0, iPosDot );
////		final int iPosDash = strNoExt.lastIndexOf( '-' );
////		final String strNoDash = strNoExt.substring( 0, iPosDash );
//		return strNoExt;
//	}
	
	
	public ImageJobWorkerTile( final String strIndex,
						  final Map<String, String> mapOptions ) {
		if ( null==mapOptions || mapOptions.isEmpty() ) {
			LOGGER.warning( "Missing configuration map" );
//			strSourceImage = null;
			threadUpdater = null;
			this.strIndex = null;
//			this.strName = null;
//			this.fThreshold = 0.0f;
//			this.fChoke = 0.0f;
//			this.fileSourcePath = null;
			this.strExpectedObjects = null;
			return;
		}
		
		this.strIndex = strIndex;
		this.bWarmup = true;
		
		final String strPrefix = "proc_image." + strIndex;

//		this.strSourceImage = mapOptions.get( strPrefix + ".file" );
//		this.strName = mapOptions.get( strPrefix + ".name" );
		final String strValue = mapOptions.get( strPrefix + ".expected.objects" );
		if ( StringUtils.isNotBlank( strValue ) ) {
			this.strExpectedObjects = strValue;
		} else {
			this.strExpectedObjects = "(not-specified)";
		}
//		final String strThreshold = mapOptions.get( strPrefix + ".threshold" );
//		float fThresholdParsed = 10.0f; 
//		try {
//			fThresholdParsed = Float.parseFloat( strThreshold );
//		} catch ( final Exception e ) {
//			fThresholdParsed = 10.0f;
//			LOGGER.warning( "Failed to parse threshold value: " + strThreshold );
//		}
//		this.fThreshold = fThresholdParsed;
//		this.fChoke = 0.0f;
		
//		if ( StringUtils.isBlank( this.strSourceImage ) ) {
//			this.state = State.INFO__DISABLED;
//		} else if ( MONITOR_FILES.contains( this.strSourceImage ) ) {
//			this.state = State.INFO__DISABLED;
//		} else {
//			MONITOR_FILES.add( this.strSourceImage );
//			this.state = State.IDLE;
//
////			clearOldFiles();
//		}
		
//		if ( State.IDLE.equals( this.state ) ) {
//			final File fileSource = new File( this.strSourceImage );
//			this.fileSourcePath = fileSource.getParentFile();
//			
//			if ( ! this.fileSourcePath.isDirectory() ) {
//				this.state = State.WARNING__MISSING_SOURCE_DIR;
//			} else if ( ! this.fileSourcePath.canWrite() ) {
//				this.state = State.WARNING__SHARE_READONLY;
//			}
//		} else {
//			this.fileSourcePath = null;
//		}
		
		this.threadUpdater = createThread();
		this.threadUpdater.start();
	}
	
	
	
	private List<Job> getJobList() {
		final JobManager manager = Client.get().getJobManager();

		List<Job> list;
		boolean bActive = true;

		do {
			try {
				TimeUnit.SECONDS.sleep( 2 );
			} catch ( final InterruptedException e ) {
				//TODO handle interruption
				e.printStackTrace();
				bActive = false;
			}

			list = manager.getJobListing( 
					"( ( job.state = \"R\" ) AND ( job.step = \"1\" ) )", 200 );

			System.out.println( "Query returned " + list.size() + " rows" );
			
		} while ( bActive && list.isEmpty() );

		System.out.println( "Non-empty list ready." );

		if ( list.isEmpty() ) return null;
		
		//TODO this is currently taking the latest.. should it take the oldest?
		
		Collections.sort( list, new Comparator<Job>() {
			@Override
			public int compare( final Job lhs, final Job rhs ) {
				return rhs.getJobSeq().compareTo( lhs.getJobSeq() );
			}
		});
		
		System.out.println( "Returning list." );
		
		return list;
	}

	
	private boolean acquireJob( final Job job ) {

		final long seqSession = Client.get().getSessionSeq();
		
		final String strUpdate = 
				"UPDATE job SET "
						+ "seq_session_working = " + seqSession 
						+ ", state = 'W' "
				+ "WHERE ( "
						+ "seq = " + job.getJobSeq() 
						+ " AND state = 'R' );";
		
		final String strQuery =
				"SELECT seq_session_working "
				+ "FROM job "
				+ "WHERE seq = " + job.getJobSeq();
		
		try ( final Connection conn = ConnectionProvider.get().getConnection() ) {
			if ( null==conn ) return false;
			
			try ( final PreparedStatement stmt =  
			  							conn.prepareStatement( strUpdate ) ) {
				stmt.executeUpdate();
			}

			try ( final PreparedStatement stmt =  
										conn.prepareStatement( strQuery ) ) {
				final ResultSet rs = stmt.executeQuery();
				
				if ( ! rs.first() ) return false;
				
				final long seqWorking = rs.getLong( "seq_session_working" );
				if ( seqWorking == seqSession ) {
					System.out.println( "Job acquired: " + seqSession );
					return true;
				} else {
					System.out.println( "Job NOT acquired, "
										+ "taken by session " + seqWorking );
					return false;
				}
			}

		} catch ( final SQLException e ) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	private Job doPopNextJob() {

		Job job = null;
		do {
			try {
				TimeUnit.SECONDS.sleep( 2 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
				//TODO handle interruption
			}
			
			final List<Job> list = getJobList();
			
			System.out.println( "Job list pulled, " + list.size() + " rows." );
			
			while ( null==job && ! list.isEmpty() ) {
				job = list.remove( 0 );
				if ( acquireJob( job ) ) {
					System.out.println( "Job acquired: " + job.getJobSeq() );
				} else {
					job = null;
				}
			}
		} while ( null==job );
			
		System.out.println( "Job identified to work: " + job.getJobSeq() );
		return job;
	}
	
	
	
	private Thread createThread() {
		
		final Thread thread = new Thread( "Image Job Monitor" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );

					lLastUpdate = System.currentTimeMillis();

					while ( scan() ) {}

				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
		return thread;
	}
	
	
	long lLastUpdate = 0;
	long lLastChange = 0;
	long lCycleCount = 0;


	private final List<String> listTags = new LinkedList<>();


	final public static String FILENAME_CURRENT = "/tmp/compare-current";
	final public static String FILENAME_PREVIOUS = "/tmp/compare-previous";
	final public static String FILENAME_MASK = "/tmp/compare-mask";
	
	final public static String COMMAND = "/Local/scripts/compare_images.sh";
	
	
	private String getFilenameCurrent() {
		return FILENAME_CURRENT + "_" + this.strIndex + ".jpg";
	}

	private String getFilenamePrevious() {
		return FILENAME_PREVIOUS + "_" + this.strIndex + ".jpg";
	}

	private String getFilenameMask() {
		return FILENAME_MASK + "_" + this.strIndex + ".jpg";
	}

//	private String getFilenameSourceMask() {
//		final String strSourceMask = 
//						this.strSourceImage.replace( ".", "-mask." );
//		return strSourceMask;
//	}


	
	/*
	 * Get a numeric difference between the files.
	 * From the external process this is a float between 0 and 1.
	 * The returned number is 100 x this number (a percentage).
	 */
	public Float getImageDifference( final File fileLHS,
										    final File fileRHS,
										    final File fileMask,
										    final String strLogPrefix ) {
		
		final String strFileMask = ( null!=fileMask ) 
										? fileMask.getAbsolutePath()
										: null;
		final String[] strCommand = { "/bin/bash", 
									  COMMAND, 
									  fileLHS.getAbsolutePath(), 
									  fileRHS.getAbsolutePath(),
									  strFileMask };
		
		run = new RunProcess( strCommand );
		final Integer iResult = run.run();
		if ( null==iResult || iResult != 0 ) {
			LOGGER.warning( strLogPrefix + "Non-zero result from process ("
					+ "exit code = " + iResult + ")." ); // + "\n"
//					+ "Full process output:\n" + strOut );
			return null;
		} else {

			final Float fValue = run.getOutputFloat();
			return fValue;
		}
	}
	

	public void clearOldFiles() {
		
		final File fileCurrent = new File( getFilenameCurrent() );
		if ( fileCurrent.exists() ) {
			if ( ! fileCurrent.delete() ) {
				LOGGER.warning( "Failed to delete file: " 
									+ fileCurrent.getAbsolutePath() );
			}
		}
		
		final File filePrevious = new File( getFilenamePrevious() );
		if ( filePrevious.exists() ) {
			if ( ! filePrevious.delete() ) {
				LOGGER.warning( "Failed to delete file: " 
									+ filePrevious.getAbsolutePath() );
			}
		}
		
		final File fileMask = new File( getFilenameMask() );
		if ( fileMask.exists() ) {
			if ( ! fileMask.delete() ) {
				LOGGER.warning( "Failed to delete file: " 
									+ fileMask.getAbsolutePath() );
			}
		}
	}
	
	
	public boolean scan() {

		this.iCountTotal++;
		final String strPrefix = "[" + this.strIndex + "] ";

		this.state = State.WAITING_FOR_FILE;
//		boolean bReady = doWaitForFileToMove( strSourceImage );
		final Job job = doPopNextJob();
		boolean bReady = ( null != job );
		this.state = State.PREPARING_FILES;
		final long lTimeNow = System.currentTimeMillis();
		

		lCycleCount++;
		final long lElapsed = ( lLastUpdate > 0 ) ? lTimeNow - lLastUpdate : 0;
		lLastUpdate = lTimeNow;
		
		listTags.clear();
		final TraceMap trace;
//		final Map<String, Object> mapData;
		
		final String strName;
		final String strSourceImage;
		final String strImagePrevious;
		final String strImageMask;
		final String strWorkDir;

		System.out.println( "--- scan() - 1.0 - bReady = " + bReady );

		trace = bReady ? new TraceMap( job.getJobData() ) : null;

		if ( bReady ) {

			System.out.println( trace );
			System.out.println( JsonUtils.report( trace ) );
			
			bReady = bReady 
				&& trace.containsKey( "config.name" )
				&& trace.containsKey( "image_previous" )
				&& trace.containsKey( "image_current" )
				&& trace.containsKey( "image_mask" )
				&& trace.containsKey( "config.threshold" )
				&& trace.containsKey( "path_work" );
		}	

		System.out.println( "--- scan() - 2.0 - bReady = " + bReady );

		if ( bReady ) {

//			trace = new TraceMap();

			strSourceImage = trace.get( "image_current" ).toString();
			strImagePrevious = trace.get( "image_previous" ).toString();
			strImageMask = trace.get( "image_mask" ).toString();
			strWorkDir = trace.get( "path_work" ).toString();
			strName = trace.get( "config.name" ).toString();

		} else {
			this.state = State.FAULT;
			strSourceImage = null;
			strImagePrevious = null;
			strImageMask = null;
			strWorkDir = null;
			strName = null;
//			trace = null;
		}

		System.out.println( "--- scan() - 3.0 - bReady = " + bReady );

		float fThreshold = 0;
		try {
			final Object objValue = trace.get( "config.threshold" );
			if ( null!=objValue ) {
				fThreshold = Float.parseFloat( objValue.toString() );
			}
		} catch ( final NumberFormatException e ) {
			System.err.println( "Failed to read config.threshold" );
			bReady = false;
		}
		

		if ( bReady ) {
			
			final Graph graph = 
						HistogramTile.getGraph( "FILE_INTERVAL_" + strIndex );
			
			if ( null!=graph && ( lCycleCount > 1 ) && ( lElapsed > 0 ) ) {
				
				graph.add( ( (float) lElapsed ) / 1000 );
			}
			
//			try {
//				
//				final File filePrevious = new File( getFilenamePrevious() );
//				if ( filePrevious.exists() ) {
//					FileUtils.forceDelete( filePrevious );
//				}
//
//				final File fileCurrent1 = new File( getFilenameCurrent() );
//				if ( fileCurrent1.isFile() ) {
//					fileCurrent1.renameTo( filePrevious );
//				}
//
//			} catch ( final IOException e ) {
//				this.state = State.FAULT;
//				e.printStackTrace();
//				bReady = false;
//			}
		} else {
			this.state = State.FAULT;
		}

		System.out.println( "--- scan() - 4.0 - bReady = " + bReady );

		long lSourceFileTimestamp = 0;
		
		if ( bReady ) {
			
//			System.out.println( "002" );
			System.out.println( "--- scan() - 4.1 - bReady = " + bReady );

			try {
			
				final File fileSource = new File( strSourceImage );
				final File filePrevious = new File( strImagePrevious );
				final File fileTmpCurrent = new File( getFilenameCurrent() );
				final File fileTmpPrevious = new File( getFilenamePrevious() );

				if ( fileSource.exists() ) {
					FileUtils.copyFile( fileSource, fileTmpCurrent );
					lSourceFileTimestamp = fileSource.lastModified();
				} else {
					this.state = State.FAULT;
					// file probably just moved
					bReady = false;
				}

				System.out.println( "--- scan() - 4.2 - bReady = " + bReady );

				if ( filePrevious.exists() ) {
					FileUtils.copyFile( filePrevious, fileTmpPrevious );
				} else {
					this.state = State.FAULT;
					// file probably just moved
					bReady = false;
				}

				System.out.println( "--- scan() - 4.3 - bReady = " + bReady );

				final File fileSourceMask = new File( strImageMask );
				
				if ( fileSourceMask.exists() ) {
					final File fileMask = new File( getFilenameMask() );
					if ( ! fileMask.exists() ) {
						FileUtils.copyFile( fileSourceMask, fileMask );
					}
					bUsingMask = true;
				} else {
					bUsingMask = false;
				}
				
				System.out.println( "--- scan() - 4.4 - bReady = " + bReady );

			} catch ( final IOException e ) {
				this.state = State.FAULT;
				e.printStackTrace();
				bReady = false;
			}
		}

		System.out.println( "--- scan() - 5.0 - bReady = " + bReady );

		if ( bReady ) {
			
//			System.out.println( "003" );

			final File filePrevious = new File( getFilenamePrevious() );
			final File fileCurrent = new File( getFilenameCurrent() );
			final File fileMask = new File( getFilenameMask() );
			
			this.state = State.EXECUTING_COMPARISON;
			final Float fOutputDiff = getImageDifference( 
							filePrevious, fileCurrent, fileMask, strPrefix );
			this.state = State.POST_COMPARISON;

			trace.put( "diff-output", fOutputDiff );
			
			this.iCountCompleted++;

			System.out.println( "--- scan() - 5.1 - bReady = " + bReady );

			if ( null != fOutputDiff ) {
				
				final float fDiff = fOutputDiff * 100;

				trace.put( "diff-value", fDiff );

				System.out.println( strPrefix + "Comparison result: " + fDiff );
				
//				if ( ! this.bWarmup ) {

					System.out.println( "--- scan() - 5.2 - bReady = " + bReady );
					
					final float fChoke;
					long lDuration = TimeUnit.HOURS.toMillis( 2 );
					try {
						final Object objValue = trace.get( "live.time_last_change" );
						if ( null!=objValue ) {
							lLastChange = Long.parseLong( objValue.toString() );
						}
						lDuration = lTimeNow - lLastChange;
					} catch ( final NumberFormatException e ) {
						// ignore
					}
					fChoke = (float)lDuration / 1000000000;
					

					trace.put( "duration-to-last", lDuration );
					trace.put( "duration-to-last-minutes", 
							TimeUnit.MILLISECONDS.toMinutes( lDuration ) );
					trace.put( "threshold-choke", fChoke );

					final float fThresholdAdjusted = (float)fThreshold - fChoke;
					
					trace.put( "threshold-adjusted", fThresholdAdjusted );

					{
						final Graph graph = HistogramTile.getGraph( 
										"IMAGE_CHANGE_VALUE_" + strIndex );
						graph.add( fDiff );
						graph.setThresholdMax( new Double( fThresholdAdjusted ) );
					}

					System.out.println( "--- scan() - 5.3 - bReady = " + bReady );
					
					if ( fDiff >= fThresholdAdjusted ) {
						
						System.out.println( "Change above threshold.   "
								+ String.format( 
										"(diff) %.3f  >=  (threshold) %.3f", 
										fDiff, fThresholdAdjusted ) );
						
						trace.put( "live.time_last_change", lTimeNow );
						trace.put( "live.last-threshold-adjusted", fThresholdAdjusted );
						
						// image changed
						
//						if ( this.iCyclesSinceLastChange < 2 ) {
//							this.fChoke = this.fChoke - CHOKE_OPEN_FAST;
//							this.listTags.add( "{+change+}" );
//						} else {
//							this.fChoke = this.fChoke - CHOKE_OPEN_SLOW;
//							this.listTags.add( "{change}" );
//						}
//						this.iCyclesSinceLastChange = 0;
						
						this.iCountChanged++;
//						final long lChangeElapsed = lTimeNow - lLastChange;
						
						System.out.println( "Change above threshold. Reporting.." );
						this.reportChangeDetected( strName,
									fileCurrent, filePrevious, strWorkDir,
									lSourceFileTimestamp, 
									fThreshold, fDiff, 
									lTimeNow, trace );
						
						System.out.print( "lLastChange = " + lLastChange + ", "
									+ "lTimeNow = " + lTimeNow );

						if ( lLastChange > 0 ) {
							final Graph graph = HistogramTile.getGraph( 
											"CHANGE_INTERVAL_" + strIndex );
							
							System.out.print( ", graph: " + graph.hashCode() );
							
//							final float fChangeMinutes = 
//											(float)lChangeElapsed / 1000 / 60;
//
//							System.out.print( ", fChangeMinutes: " + fChangeMinutes );
//
//							graph.add( fChangeMinutes );
						}
						lLastChange = lTimeNow;
						
//					} else {
//						this.fChoke = this.fChoke + CHOKE_CLOSE_SLOW;
//						this.iCyclesSinceLastChange++;
					}

//				} else {
//					System.out.println( strPrefix + "(still in warmup)" );
//					this.bWarmup = false;
//				}
					
					
				System.out.println( JsonUtils.report( trace ) );

					
					
				
			} else {
				System.out.println( strPrefix + "Comparison process failed." );
				System.out.println( strPrefix + "Output:\n" + run.getStdOut() );
			}
			this.run = null;
		}

		System.out.println( "--- scan() - 6.0 - bReady = " + bReady );

		this.state = State.IDLE;

		return true;
	}
	
	
	private void reportChangeDetected( final String strName,
									   final File fileChanged,
									   final File filePrevious,
									   final String strWorkPath,
									   final long lFileTimestamp,
									   final float fThreshold,
									   final float fDiffValue,
									   final long lTimeDetect,
									   final TraceMap map ) {
		if ( null == fileChanged ) return;
//		if ( null == this.fileSourcePath ) return;
		
		final Date dateFile = new Date( lFileTimestamp );
		
		final String strTimestamp = 
				DateFormatting.getTimestamp( dateFile ).substring( 0, 15 );
		
//		final String strBaseFilename = getBaseFilename();
//		final String strChangeDir = strTimestamp + "_" + strBaseFilename;
		
		final File fileChangeDir = 
//						new File( this.fileSourcePath, ""+ lFileTimestamp );
//						new File( "/tmp", strTimestamp );
//						new File( this.fileSourcePath, ""+ lFileTimestamp );
//						new File( this.fileSourcePath, strChangeDir );
						new File( strWorkPath );
		
		final String strChangePath = fileChangeDir.getAbsolutePath();

		try {
			FileUtils.forceMkdir( fileChangeDir );
			
			final File fileDestChangedImage = new File( fileChangeDir, "changed.jpg" );
			final File fileDestPrevImage = new File( fileChangeDir, "previous.jpg" );
			final File fileDestText = new File( fileChangeDir, "info.txt" );
			
			FileUtils.copyFile( fileChanged, fileDestChangedImage );
			FileUtils.copyFile( filePrevious, fileDestPrevImage );
			
			final StringBuffer sb = new StringBuffer();

			sb.append( String.format( 
					"Name: %s\n", strName ) );
//			sb.append( String.format( 
//					"Source image file: %s\n", this.strSourceImage ) );
			sb.append( String.format( 
					"Source image timestamp: %s\n", strTimestamp ) );
			sb.append( String.format( 
					"Change directory: %s\n", strChangePath ) );
			
			sb.append( String.format( 
					"Comparison value: %.6f\n", fDiffValue ) );
			sb.append( String.format( 
					"Comparison threshold base: %.6f\n", fThreshold ) );
//			sb.append( String.format( 
//					"Comparison threshold choke: %.6f\n", this.fChoke ) );
			
			sb.append( String.format( 
					"Count, total: %d\n", this.iCountTotal ) );
			sb.append( String.format( 
					"Count, completed: %d\n", this.iCountCompleted ) );
			sb.append( String.format( 
					"Count, changed: %d\n", this.iCountChanged ) );

			sb.append( "\nTrace" );
//			for ( final Entry<String, Object> entry : map.entrySet() ) {
//				sb.append( "\t\"" + entry.getKey() + "\" "
//						+ "= " + entry.getValue() + "\n" );
//			}
			sb.append( JsonUtils.reportMap( map ) );
			
			sb.append( "\n\n" );
			
			sb.append( "\nComparison process output:\n" + this.run.getStdOut() );
			
			
			map.put( "name", strName );
			map.put( "change-directory", strChangePath );
			map.put( "source-image-timestamp", strTimestamp );
			map.put( "expected-objects", this.strExpectedObjects );
			
			map.put( "diff-value", fDiffValue );
			map.put( "diff-threshold", fThreshold );
//			map.put( "diff-choke", fChoke );
			
			map.put( "file-info", fileDestText.getAbsolutePath() );
//			map.put( "file-source", this.strSourceImage );
			map.put( "file-previous", fileDestPrevImage.getAbsolutePath() );
			map.put( "file-changed", fileDestChangedImage.getAbsolutePath() );

//			map.put( "identity-camera", strBaseFilename );
			map.put( "identity-timestamp", strTimestamp );
			map.put( "identity-mac", NetUtil.getMAC() );

			FileUtil.saveToFile( fileDestText, sb.toString() );

			
			final String strSubject = "IMAGE_CHANGE";
//			final String strValue = ""+ fDiffValue; 
			final String strValue = strName; 
			
			map.addFrame();
			
			final Event event = Event.add( 
					EventType.ENVIRONMENT, strSubject, strValue, 
//					""+ ( this.fThreshold - this.fChoke ), 
					""+  fThreshold, 
					map, lTimeDetect, 
					null, null, null ); 
			
			System.out.println( "Event created: seq " + event.getEventSeq() );
			
		} catch ( final IOException e ) {
			LOGGER.warning( "Failed to record change data. " + e.toString() );
		}
	}
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		final boolean bEnabled = ( ! this.state.name().contains( "__" ) );
		
//		if ( bEnabled && ! threadUpdater.isAlive() ) {
//			threadUpdater.start();
//		}
		
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );

		if ( bEnabled ) {
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		} else {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		}

		gc.setFont( Theme.get().getFont( 12 ) );

		text.addSpace( 10 );
		text.println( "<name>" );

		gc.setFont( Theme.get().getFont( 8 ) );
		
		text.addSpace( 4 );
//		text.println( "Source Image File:" );
//		text.println( this.strSourceImage );
		text.println( "Index: " + this.strIndex );
		text.addSpace( 10 );

		gc.setFont( Theme.get().getFont( 10 ) );

		switch ( this.state ) {
			case IDLE:
			case INFO__DISABLED:
			case WAITING_FOR_FILE: {
				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
				break;
			}
			case EXECUTING_COMPARISON:
			case FAULT:
			case POST_COMPARISON:
			case PREPARING_FILES:
				gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
				gc.setBackground( UI.COLOR_BLUE );
				break;
			case WARNING__MISSING_SOURCE_DIR:
			case WARNING__SHARE_READONLY:
				gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
				gc.setBackground( Theme.get().getColor( Colors.BACK_ALERT ) );
				break;
		}
		text.println( "State:  " + this.state.name() );
		if ( bEnabled ) {
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		} else {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		}
		gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );

		text.addSpace( 10 );
		text.println( "Total: " + this.iCountTotal + ",  "
				+ "Completed: " + this.iCountCompleted + ",  "
				+ "Changes: " + this.iCountChanged ); 
//				+ "Fault: " + this.iCountFault ); 

		text.addSpace( 4 );
		
		gc.setFont( Theme.get().getFont( 9 ) );
		
//		text.println( String.format( 
////					"Threshold:   %1$.3f   -   %2$.3f   =   %3$.3f", 
////							this.fThreshold, 
////							this.fChoke, 
////							this.fThreshold - this.fChoke ) );
//					"Threshold:  %1$.3f", this.fThreshold ) );

		
		final StringBuilder sbEnabled = new StringBuilder();
		final StringBuilder sbDisabled = new StringBuilder();
		
		if ( this.bUsingMask ) {
			sbEnabled.append( "[mask] " );
		} else {
			sbDisabled.append( " <no-mask>" );
		}
		if ( this.bWarmup ) {
			sbDisabled.append( " <warmup>" );
		} else {
			sbEnabled.append( "[ready] " );
		}
		
		for ( final String strTag : listTags ) {
			sbEnabled.append( strTag + " " );
		}
		
		text.println( sbEnabled.toString() );
		text.setRightAligned( true );
		text.addSpace( -14 );
		text.println( sbDisabled.toString() );

		gc.setFont( Theme.get().getFont( 8 ) );
		text.setRightAligned( false );
		text.println( "Expected objects: " + this.strExpectedObjects );

//		text.println( "Source Data:" );
//		text.println( this.strData );
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

	public static void main(String[] args) {
		final String strValue = "value";
		System.out.println( String.format( "String: %1$s eol", strValue ) );
	}
	
}
