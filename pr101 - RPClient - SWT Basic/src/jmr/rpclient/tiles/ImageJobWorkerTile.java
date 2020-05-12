package jmr.rpclient.tiles;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.google.gson.JsonSyntaxException;

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
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Event;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobSet;
import jmr.util.FileUtil;
import jmr.util.NetUtil;
import jmr.util.RunProcess;
import jmr.util.report.TraceMap;
import jmr.util.transform.DateFormatting;
import jmr.util.transform.JsonUtils;

public class ImageJobWorkerTile extends TileBase {


	private static final Logger 
			LOGGER = Logger.getLogger( ImageJobWorkerTile.class.getName() );

	private static final long START_TIME = System.currentTimeMillis();
	
	

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
	
	

	final static private Map<String,Long> 
					FILES_PROCESSED = new TreeMap<String,Long>();
	
	
	private RunProcess run;
	
	private static final Set<Long> setAcquiredJobs = new HashSet<>();
	
	private final Thread threadUpdater;

	private final String strIndex;
	private final String strExpectedObjects;
	
	private State state = State.IDLE;
	private int iCountTotal;
	private int iCountCompleted;
	private int iCountChanged;
	private boolean bUsingMask;
	private boolean bWarmup = true;
	
	private final String strTileName;
	private final File fileMonitorPath;
	
	private final String strFileConfig;
	private final Map<String,Object> mapConfig;
	private final String strFileMask;
	private final String strFileLive;
	
	String strData = "uninitialized";
	
	String strName = null;
	String strFilename = null;
	Job job = null;
	

	
	
	public ImageJobWorkerTile( final String strIndex,
						  final Map<String, String> mapOptions ) {
		if ( null==mapOptions || mapOptions.isEmpty() ) {
			LOGGER.warning( "Missing configuration map" );
			threadUpdater = null;
			this.strIndex = null;
			this.strExpectedObjects = null;
			this.strTileName = null;
			this.fileMonitorPath = null;
			this.mapConfig = Collections.emptyMap();
			this.strFileMask = null;
			this.strFileConfig = null;
			this.strFileLive = null;
			return;
		}
		
		this.strIndex = strIndex;
		this.bWarmup = true;
		
		final String strPrefix = "proc_image." + strIndex;

		final String strValue = mapOptions.get( strPrefix + ".expected.objects" );
		if ( StringUtils.isNotBlank( strValue ) ) {
			this.strExpectedObjects = strValue;
		} else {
			this.strExpectedObjects = "(not-specified)";
		}

		this.strTileName = mapOptions.get( strPrefix + ".name" );
		final String strMonitorPath = mapOptions.get( strPrefix + ".monitor_path" );
		if ( null!=strMonitorPath ) {
			final File file = new File( strMonitorPath );
			if ( file.isDirectory() ) {
				this.fileMonitorPath = file;
			} else {
				this.fileMonitorPath = null;
			}
		} else {
			this.fileMonitorPath = null;
		}
		
		if ( null != this.fileMonitorPath ) {
			this.strFileLive = 
					this.fileMonitorPath.getAbsolutePath() + "/live.ini";
		} else {
			this.strFileLive = null;
		}

		this.threadUpdater = createThread();
		this.threadUpdater.start();
		
		synchronized ( ImageJobWorkerTile.threadJobList ) {
			if ( ! ImageJobWorkerTile.threadJobList.isAlive() ) {
				ImageJobWorkerTile.threadJobList.start();
			}
		}

		this.strFileConfig = mapOptions.get( strPrefix + ".file_config" );
		Map<String,Object> map = Collections.emptyMap();
		
		if ( StringUtils.isNotBlank( strFileConfig ) ) {
			final File file = new File( strFileConfig );
			if ( file.isFile() ) {
				try {
					final String strContents = FileUtil.readFromFile( file );
					map = JsonUtils.transformJsonToMap( strContents );
				} catch ( final JsonSyntaxException e ) {
					final String strMessage =  
							"Failed to read config file: " + strFileConfig;
					System.err.println( strMessage );
					throw new IllegalStateException( strMessage, e );
				}
			}
		}
		this.mapConfig = map;
		
		if ( ! map.isEmpty() ) {
			this.strFileMask = ""+ map.get( "image_mask" );
		} else {
			this.strFileMask = "";
		}

		if ( StringUtils.isNotBlank( this.strTileName ) ) {
			System.out.println( "Image monitoring tile initialized: " 
								+ this.strTileName );
		}
		
	}

	
	
	private static List<Job> list = new LinkedList<Job>();
	
	public static Thread threadJobList = 
						new Thread( "ImageJobWorker - Job List" ) {
		public void run() {
			
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
				
				synchronized ( ImageJobWorkerTile.list ) {
					ImageJobWorkerTile.list.clear();
					ImageJobWorkerTile.list.addAll( list );
				}

			} while ( bActive );
		};
	};
	
	
	
	private List<Job> getJobList() {

		final List<Job> list = new LinkedList<>();
		boolean bActive = true;

		do {
			try {
				TimeUnit.SECONDS.sleep( 2 );
				list.clear();
				synchronized ( ImageJobWorkerTile.list ) {
					list.addAll( ImageJobWorkerTile.list );
				}
			} catch ( final InterruptedException e ) {
				//TODO handle interruption
				e.printStackTrace();
				bActive = false;
			}

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

	
	private synchronized static boolean acquireJob( final Job job ) {

		final long seqJob = job.getJobSeq();
		if ( ImageJobWorkerTile.setAcquiredJobs.contains( seqJob ) ) {
			return false;
		}
		
		final long seqSession = Client.get().getSessionSeq();
		
		final String strUpdate = 
				"UPDATE job SET "
						+ "seq_session_working = " + seqSession 
						+ ", state = 'W' "
				+ "WHERE ( "
						+ "seq = " + seqJob 
						+ " AND state = 'R' );";
		
		final String strQuery =
				"SELECT seq_session_working "
				+ "FROM job "
				+ "WHERE seq = " + seqJob;
		
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
					
					ImageJobWorkerTile.setAcquiredJobs.add( seqJob );
					
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
	
	
	
	private Job doPopNextJob_FromDB() {
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
	
	
	
	private Job doPopNextJob_FromPath() {
		if ( null==this.fileMonitorPath ) return null;
		if ( ! this.fileMonitorPath.isDirectory() ) return null;
		
		final long lNow = System.currentTimeMillis();

		final File[] files = this.fileMonitorPath.listFiles( 
											new FileFilter() {
			@Override
			public boolean accept( final File file ) {
				final String strShort = file.getName().toLowerCase();
				if ( ! strShort.startsWith( "capture_cam") ) {
					return false;
				} else if ( strShort.contains( "thumb" ) ) {
					return false;
				} else if ( strShort.contains( "mask" ) ) {
					return false;
				} else if ( ! strShort.endsWith( ".jpg" ) ) {
					return false;
				} else if ( file.lastModified() < START_TIME ) {
					return false;
				} else if ( FILES_PROCESSED.containsKey( strShort ) ) {
					return false;
				} else {
					return true;
				}
			}
		} );
				
		final List<File> listFiles = new LinkedList<>( Arrays.asList( files ) );
	
		Collections.sort( listFiles, new Comparator<File>() {
			public int compare( final File fileLHS, 
								final File fileRHS ) {
				return Long.compare( 
							fileRHS.lastModified(), fileLHS.lastModified() );

			};
		});
		
		if ( listFiles.size() > 1 ) { // need 2
			final File file = listFiles.get( 0 );
			
			if ( file.lastModified() > START_TIME ) {
				System.out.println( "File detected." ); // ends running dots
				System.out.println( 
						"Candidate file: " + file.getAbsolutePath() );
				
				// process

				final File filePrev = listFiles.get( 1 );

				// much of this was taken from pr130:CameraSchedulerUI.java
				
				final TraceMap trace = new TraceMap();

				trace.putAllUnder( "01", Client.get().getDetails() );
				
				final String strFileCurrent = file.getAbsoluteFile().toString();
//				final String strWorkDir = StringUtils.removeEnd( strFileCurrent, ".jpg" ); 
				final String strWorkDir = 
									StringUtils.substringBeforeLast( 
									file.getAbsolutePath(), "." );
				
				trace.put( "config.name", this.strTileName );
				trace.put( "image_current", strFileCurrent );
				trace.put( "image_previous", filePrev.getAbsoluteFile().toString() );
				trace.put( "image_mask", this.strFileMask );
				trace.put( "file_config", this.strFileConfig );
				trace.put( "file_live", this.strFileLive );
				trace.put( "path_work", strWorkDir );
				trace.put( "config.threshold", this.mapConfig.get( "threshold" ) );
				
				
//				trace.putAllUnder( "config", getConfigForCamera( fileConfig ) );

//				final String strResult = S2Utils.strUnixPathOf( strFilenameBase );
				final String strResult = file.getAbsolutePath();
				final JobSet jobset = null;
				final JobType type = JobType.PROCESSING_IMAGE_DIFF;
				
				Job.add( type, jobset, 1, strResult, trace );
								
			} else {
				System.out.println( 
						"File predates process: " + file.getAbsolutePath() );
			}
			
			final String strFilename = file.getName().toLowerCase();
			FILES_PROCESSED.put( strFilename, lNow );
			
		} else {
//			System.out.println( "No files ready." );
			System.out.print( "." );
		}
		
		//TODO implement
		return null;
	}

	
	
	private Job doPopNextJob() {
		if ( null == this.fileMonitorPath ) {
			return doPopNextJob_FromDB();
		} else {
			Job job = null;
			while ( null==job ) {
				job = doPopNextJob_FromPath();
				if ( null == job ) {
					try {
						Thread.sleep( 200 );
					} catch ( final InterruptedException e ) {
						e.printStackTrace();
						return null;
					}
				}
			}
			return job;
		}
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
		
		System.out.println( "Running command:" );
		for ( final String strLine : strCommand ) {
			System.out.println( "\t" + strLine );
		}
		
		run = new RunProcess( strCommand );
		final Integer iResult = run.run();
		
		System.out.println( "  Exit code: " + iResult );
		
		if ( null==iResult || iResult != 0 ) {
			LOGGER.warning( strLogPrefix + "Non-zero result from process ("
					+ "exit code = " + iResult + ")." ); // + "\n"
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
		this.job = doPopNextJob();
		boolean bReady = ( null != job );
		this.state = State.PREPARING_FILES;
		final long lTimeNow = System.currentTimeMillis();
		
		final Long seqJob = null!=job ? job.getJobSeq() : null;
		

		lCycleCount++;
		final long lElapsed = ( lLastUpdate > 0 ) ? lTimeNow - lLastUpdate : 0;
		lLastUpdate = lTimeNow;
		
		listTags.clear();
		final TraceMap trace;
		
//		final String strName;
		final String strSourceImage;
		final String strImagePrevious;
		final String strImageMask;
		final String strWorkDir;

		System.out.println( "--- scan() - 1.0 - bReady = " + bReady );

		if ( bReady ) {
			Map<String, Object> map = null;
			try {
				map = job.getJobData();
			} catch ( final Exception e ) {
				System.err.println( "Failed to read JSON data for "
						+ "Job " + job.getJobSeq() );
				System.err.println( "Job will not be worked, "
						+ "image will not be processed." );
			}
			if ( null!=map ) {
				trace = new TraceMap( map );
			} else {
				trace = null;
			}
		} else {
			trace = null;
		}

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
		}
		
		this.strFilename = strSourceImage;

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
			
			if ( null!=graph && ( lCycleCount > 1 )
					// only show on graph if recent
					&& ( lElapsed < TimeUnit.MINUTES.toMillis( 4 ) )
					&& ( lElapsed > 0 ) ) {
				
				graph.add( ( (float) lElapsed ) / 1000 );
			}
			
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
					System.out.println( "Source image file is missing." );
					this.state = State.FAULT;
					// file probably just moved
					bReady = false;
				}

				System.out.println( "--- scan() - 4.2 - bReady = " + bReady );

				if ( filePrevious.exists() ) {
					FileUtils.copyFile( filePrevious, fileTmpPrevious );
				} else {
					System.out.println( "Previous image file is missing." );
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

			System.out.println( "--- scan() - 5.1 - bReady = " + bReady );

			final File filePrevious = new File( getFilenamePrevious() );
			final File fileCurrent = new File( getFilenameCurrent() );
			final File fileMask = new File( getFilenameMask() );

			System.out.println( "--- scan() - 5.1.1" );

			this.state = State.EXECUTING_COMPARISON;
			final Float fDiff = getImageDifference( 
							filePrevious, fileCurrent, fileMask, strPrefix );
			this.state = State.POST_COMPARISON;

			System.out.println( "--- scan() - 5.1.3 - fDiff = " + fDiff );

			trace.put( "diff-value", fDiff );

			this.iCountCompleted++;

			System.out.println( "--- scan() - 5.1.6" );

			if ( null != fDiff ) {
				
//				System.out.println( strPrefix + "Comparison result: " + fDiff );

				System.out.println( "--- scan() - 5.2 - bReady = " + bReady );

				Double dAvgMultiplier = 1.8;
				try {
					final Object objValue = trace.get( 
										"config.change_average_multiplier" );
					if ( null!=objValue ) {
						dAvgMultiplier = Double.parseDouble( objValue.toString() );
					}
				} catch ( final NumberFormatException e ) {
					// ignore
				}

				Double dAvgOffset = 100.0;
				try {
					final Object objValue = trace.get( 
										"config.change_average_offset" );
					if ( null!=objValue ) {
						dAvgOffset = Double.parseDouble( objValue.toString() );
					}
				} catch ( final NumberFormatException e ) {
					// ignore
				}
				
				final double dDiffAdjusted = dAvgOffset + fDiff;
				
//				final float fChoke;
//				long lDuration = TimeUnit.HOURS.toMillis( 2 );
//				try {
//					final Object objValue = trace.get( "live.time_last_change" );
//					if ( null!=objValue ) {
//						lLastChange = Long.parseLong( objValue.toString() );
//					}
//					lDuration = lTimeNow - lLastChange;
//				} catch ( final NumberFormatException e ) {
//					// ignore
//				}
//				fChoke = (float)lDuration / 1000000;
//				
//
//				trace.put( "duration-to-last", lDuration );
//				trace.put( "duration-to-last-minutes", 
//						TimeUnit.MILLISECONDS.toMinutes( lDuration ) );
//				trace.put( "threshold-choke", fChoke );

//				final float fThresholdAdjusted = (float)fThreshold - fChoke;
//				final float fThresholdAdjusted = fThreshold;
				
				final Double dRecentAverage = 
						evaluateRecentAverage( trace ) + dAvgOffset;
				
				trace.put( "recent-average", dRecentAverage );
				
				final Double fThresholdAdjusted = null!=dRecentAverage 
										? ( dRecentAverage * dAvgMultiplier ) 
										: null;
				
				System.out.println( strPrefix + "Recent Average    : " + dRecentAverage 
									+ "   multiplier: " + dAvgMultiplier );
				System.out.println( strPrefix + "Threshold         : " + fThresholdAdjusted );
				System.out.println( strPrefix + "Comparison result : " + dDiffAdjusted );

				trace.put( "threshold-adjusted", fThresholdAdjusted );

				if ( null!=fThresholdAdjusted ) {
					final Graph graph = HistogramTile.getGraph( 
									"IMAGE_CHANGE_VALUE_" + strIndex );
					graph.add( (float) dDiffAdjusted );
					graph.setThresholdMax( new Double( fThresholdAdjusted ) );
				}

				System.out.println( "--- scan() - 5.3 - bReady = " + bReady );
				
				if ( null!=fThresholdAdjusted && dDiffAdjusted >= fThresholdAdjusted ) {
					
					System.out.println( "Change above threshold.   "
							+ String.format( 
									"(diff) %.3f  >=  (threshold) %.3f", 
									dDiffAdjusted, fThresholdAdjusted ) );
					
					trace.put( "live.time_last_change", lTimeNow );
					trace.put( "live.last-threshold-adjusted", fThresholdAdjusted );
					
					// image changed
					
					this.iCountChanged++;
//						final long lChangeElapsed = lTimeNow - lLastChange;
					
					System.out.println( ">>>>> Change above threshold. Reporting.." );
					this.reportChangeDetected( strName,
								fileCurrent, filePrevious, strWorkDir,
								lSourceFileTimestamp, 
								fThreshold, fDiff, 
								lTimeNow, trace );
					
					System.out.print( "lLastChange = " + lLastChange + ", "
								+ "lTimeNow = " + lTimeNow );

					lLastChange = lTimeNow;
					
				}
					
				System.out.println( JsonUtils.report( trace ) );

				saveLiveData( strSourceImage, fDiff, trace );
				
			} else {
				System.out.println( strPrefix + "Comparison process failed." );
				System.out.println( strPrefix + "Output:\n" + run.getStdOut() );
			}
			this.run = null;
		}

		System.out.println( "--- scan() - 6.0 - bReady = " + bReady );

		this.state = State.IDLE;

		ImageJobWorkerTile.setAcquiredJobs.remove( seqJob );
		
		this.strFilename = null;
		this.job = null;
		return true;
	}
	
	
	
	private Double evaluateRecentAverage( final TraceMap trace ) {
		if ( null==trace ) return null;
		if ( trace.isEmpty() ) return null;
		
//		System.out.println( "--- evaluateRecentAverage()" );
		
		final TreeMap<String,Float> map = new TreeMap<>();
		for ( final Entry<String, Object> entry : trace.entrySet() ) {
			final String strKey = entry.getKey();
			if ( strKey.startsWith( "live.t" ) ) {
				String strValue = entry.getValue().toString();
				
//				System.out.println( "\tstrValue (1) = " + strValue );
				
//				strValue = StringUtils.substringBetween( strValue, "\"" );
//
//				System.out.println( "\tstrValue (2) = " + strValue );
				
				if ( StringUtils.isNotBlank( strValue ) ) {
					try {
						final Float fValue = Float.valueOf( strValue );
						final String strNewKey = 
								StringUtils.substringAfter( strKey, "t" );
						map.put( strNewKey, fValue );
					} catch ( final NumberFormatException e ) {
						// just skip for now..
//						System.out.println( "NFE: " + strValue );
					}
				} else {
//					System.out.println( "Blank: " + strValue );
				}
			}
		}

//		System.out.println( "TreeMap size: " + map.size() );

		// max and min are sanity limits
		double fMax = 0;
		for ( final Entry<String, Float> entry : map.entrySet() ) {
			final float fValue = entry.getValue();
			fMax = Math.max( fMax, fValue );
		}
		double fMin = fMax / 2;
		
		
		double fSum = 0.0;
		double fDiv = 0.0;
		double fWeight = 1.0;
		for ( final Entry<String, Float> entry : map.entrySet() ) {
			final float fValue = entry.getValue();
			if ( fValue > fMin ) {
				fSum = fSum + ( fWeight * fValue );
				fDiv = fDiv + fWeight;
				fWeight = fWeight * 0.8;
			}
		}
		
		final double dAverage = fSum / fDiv;
		System.out.println( "dAverage: " + dAverage );
		return dAverage;
	}


	
	
	private void saveLiveData( final String strSourceImage,
							   final float fDiff,
							   final TraceMap trace ) {

		final String strFilename = trace.get( "file_live" ).toString();
//		final String strLastReport = trace.get( "live.last_report" ).toString(); 
		
//		final Map<String,String> mapLive = new HashMap<>();
//		for ( final Entry<String, Object> entry : trace.entrySet() ) {
//			final String strKey = entry.getKey();
//			if ( strKey.startsWith( "live." ) ) {
//				mapLive.put( strKey.substring( 5 ), entry.getValue().toString() );
//			}
//		}
//		
//		final String strContent = TextUtils.convertMapToString( mapLive );
//		FileUtil.saveToFile( fileLive, strContent );

//		final String strLine = "" + lNow + "=" + fDiff;

		String strIndex = strSourceImage;
		strIndex = StringUtils.substringAfterLast( strIndex, "-" );
		strIndex = StringUtils.removeEnd( strIndex, ".jpg" );
		final String strLine = strIndex + "=" + fDiff;
		final long lMinLen = strLine.length();
		
		boolean bSaved = false;
		do {
			final File fileLive = new File( strFilename );
			final long lModOriginal = fileLive.lastModified();
			final String strOriginal = FileUtil.readFromFile( fileLive );

			final String strUpdated;

			if ( null!=strOriginal ) {
				final List<String> list;
				list = new LinkedList<>( 
							Arrays.asList( strOriginal.split( "\\n" ) ) );
				list.add( strLine );
				Collections.sort( list );
				while ( list.size() > 10 ) {
					list.remove( 0 );
				}
				strUpdated = String.join( "\n", list );
			} else {
				strUpdated = strLine;
			}
			
			final long lModNow = new File( strFilename ).lastModified();
			if ( lModOriginal == lModNow ) {
				do {
					bSaved = FileUtil.saveToFile( fileLive, strUpdated );
				} while ( new File( strFilename ).length() < lMinLen );
			}
			
		} while ( ! bSaved );
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
		
		final File fileChangeDir = new File( strWorkPath );
		
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
			sb.append( String.format( 
					"Source image timestamp: %s\n", strTimestamp ) );
			sb.append( String.format( 
					"Change directory: %s\n", strChangePath ) );
			
			sb.append( String.format( 
					"Comparison value: %.6f\n", fDiffValue ) );
			sb.append( String.format( 
					"Comparison threshold base: %.6f\n", fThreshold ) );
			
			sb.append( String.format( 
					"Count, total: %d\n", this.iCountTotal ) );
			sb.append( String.format( 
					"Count, completed: %d\n", this.iCountCompleted ) );
			sb.append( String.format( 
					"Count, changed: %d\n", this.iCountChanged ) );

			sb.append( "\nTrace" );
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
		
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );

		if ( bEnabled ) {
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		} else {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		}


		if ( null!=this.strTileName ) {
			gc.setFont( Theme.get().getFont( 10 ) );
			text.println( this.strTileName );
		} else {
			text.addSpace( 10 );
		}
		final StringBuilder sbName = new StringBuilder();
		final Job jobLocal = this.job;
		if ( null != jobLocal ) {
			sbName.append( ""+ jobLocal.getJobSeq() + ": " );
			if ( null!=strName ) {
				sbName.append( strName );
			}
		} else {
			sbName.append( "<none>" );
		}
		gc.setFont( Theme.get().getFont( 12 ) );
		text.println( sbName.toString() );
		
		gc.setFont( Theme.get().getFont( 8 ) );
		final StringBuilder sbFile = new StringBuilder();
		final String strFilenameLocal = this.strFilename;
		if ( null != strFilenameLocal ) {
			sbFile.append( "../" + strFilenameLocal.substring( 16 ) );
		} else if ( null!=fileMonitorPath ){
//			sbFile.append( "<none>" );
			sbFile.append( "Monitoring: " + this.fileMonitorPath.getName() );
		} else {
			sbFile.append( "<nothing>" );
		}
		text.println( sbFile.toString() );

		gc.setFont( Theme.get().getFont( 8 ) );
		
//		text.addSpace( 4 );
////		text.println( "Source Image File:" );
////		text.println( this.strSourceImage );
//		text.println( "Index: " + this.strIndex );
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
				gc.setBackground( Theme.get().getColor( 
											Colors.BACKGROUND_FLASH_ALERT ) );
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

		text.addSpace( 4 );
		
		gc.setFont( Theme.get().getFont( 9 ) );
		
		
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
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

	public static void main(String[] args) {
		final String strValue = "value";
		System.out.println( String.format( "String: %1$s eol", strValue ) );
	}
	
}
