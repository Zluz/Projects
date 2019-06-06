package jmr.rpclient.tiles;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;
import jmr.rpclient.tiles.HistogramTile.Graph;
import jmr.s2db.event.EventType;
import jmr.s2db.tables.Event;
import jmr.util.FileUtil;
import jmr.util.NetUtil;
import jmr.util.RunProcess;
import jmr.util.report.TraceMap;
import jmr.util.transform.DateFormatting;

public class ProcImageTile extends TileBase {


	private static final Logger 
			LOGGER = Logger.getLogger( ProcImageTile.class.getName() );


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
	
	private final static Set<String> MONITOR_FILES = new HashSet<>();
	
	
	private final Thread threadUpdater;

	private final String strName;
	private final String strSourceImage;
	private final String strIndex;
	private final float fThreshold;
	private final File fileSourcePath;
	
	private State state = State.IDLE;
	private float fChoke;
	private int iCountTotal;
	private int iCountCompleted;
	private int iCountChanged;
	private boolean bUsingMask;
	private boolean bWarmup = true;
	private int iCyclesSinceLastChange = 10;
	
	String strData = "uninitialized";
	
	
	public String getBaseFilename() {
		final int iPosSlash = this.strSourceImage.lastIndexOf( '/' );
		final String strNoPath = this.strSourceImage.substring( iPosSlash + 1 );
		final int iPosDot = strNoPath.lastIndexOf( '.' );
		final String strNoExt = strNoPath.substring( 0, iPosDot );
//		final int iPosDash = strNoExt.lastIndexOf( '-' );
//		final String strNoDash = strNoExt.substring( 0, iPosDash );
		return strNoExt;
	}
	
	
	public ProcImageTile( final String strIndex,
						  final Map<String, String> mapOptions ) {
		if ( null==mapOptions || mapOptions.isEmpty() ) {
			LOGGER.warning( "Missing configuration map" );
			strSourceImage = null;
			threadUpdater = null;
			this.strIndex = null;
			this.strName = null;
			this.fThreshold = 0.0f;
			this.fChoke = 0.0f;
			this.fileSourcePath = null;
			return;
		}
		
		this.strIndex = strIndex;
		this.bWarmup = true;
		
		final String strPrefix = "proc_image." + strIndex;

		this.strSourceImage = mapOptions.get( strPrefix + ".file" );
		this.strName = mapOptions.get( strPrefix + ".name" );
		final String strThreshold = mapOptions.get( strPrefix + ".threshold" );
		float fThresholdParsed = 10.0f; 
		try {
			fThresholdParsed = Float.parseFloat( strThreshold );
		} catch ( final Exception e ) {
			fThresholdParsed = 10.0f;
			LOGGER.warning( "Failed to parse threshold value: " + strThreshold );
		}
		this.fThreshold = fThresholdParsed;
		this.fChoke = 0.0f;
		
		if ( StringUtils.isBlank( this.strSourceImage ) ) {
			this.state = State.INFO__DISABLED;
		} else if ( MONITOR_FILES.contains( this.strSourceImage ) ) {
			this.state = State.INFO__DISABLED;
		} else {
			MONITOR_FILES.add( this.strSourceImage );
			this.state = State.IDLE;

			clearOldFiles();
		}
		
		if ( State.IDLE.equals( this.state ) ) {
			final File fileSource = new File( this.strSourceImage );
			this.fileSourcePath = fileSource.getParentFile();
			
			if ( ! this.fileSourcePath.isDirectory() ) {
				this.state = State.WARNING__MISSING_SOURCE_DIR;
			} else if ( ! this.fileSourcePath.canWrite() ) {
				this.state = State.WARNING__SHARE_READONLY;
			}
		} else {
			this.fileSourcePath = null;
		}
		
		this.threadUpdater = createThread();
	}

	
	private boolean doWaitForFileToMove( final String strFilename ) {
		if ( StringUtils.isBlank( strFilename ) ) return false;
		
		final File file = new File( strFilename );
		try {
			if ( ! file.isFile() ) {
				Thread.sleep( 1000 );
			}
			if ( file.isFile() ) {
				long lModified = file.lastModified();
				while ( file.lastModified() == lModified ) {
					strData = "hash: " + file.hashCode() + ", " 
								+ file.lastModified();
					Thread.sleep( 1000 );
				}
				return true;
			} else {
				return false;
			}
		} catch ( final InterruptedException e ) {
			return false;
		}
	}
	
	
	private Thread createThread() {
		
		final Thread thread = new Thread( "Image File Monitor" ) {
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

	private String getFilenameSourceMask() {
		final String strSourceMask = 
						this.strSourceImage.replace( ".", "-mask." );
		return strSourceMask;
	}


	
	/*
	 * Get a numeric difference between the files.
	 * From the external process this is a float between 0 and 1.
	 * The returned number is 100 x this number (a percentage).
	 */
	public static Float getImageDifference( final File fileLHS,
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
		
		final RunProcess run = new RunProcess( strCommand );
		final Integer iResult = run.run();
		final String strOut = run.getStdOut();
		if ( null==iResult || iResult != 0 ) {
			LOGGER.warning( strLogPrefix + "Non-zero result from process. "
					+ "Exit code = " + iResult + "\n"
					+ "Full process output:\n" + strOut );
			return null;
		} else if ( StringUtils.isNotBlank( strOut ) ) {
			
//			System.out.println( "Output: " + strOut );
			
			try {
				final String strValue = strOut.split( "\\n" )[0];
				final float fOutput = Float.parseFloat( strValue ) * 100;
				return fOutput;
			} catch ( final NumberFormatException e ) {
				
				LOGGER.warning( strLogPrefix + "Failed to parse float. "
						+ "Full process output:\n" + strOut );
				return null;
			}
			
		} else {
			LOGGER.warning( strLogPrefix + "Empty output from process." );
			return null;
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
		boolean bReady = doWaitForFileToMove( strSourceImage );
		this.state = State.PREPARING_FILES;
		final long lTimeNow = System.currentTimeMillis();
		

		lCycleCount++;
		final long lElapsed = ( lLastUpdate > 0 ) ? lTimeNow - lLastUpdate : 0;
		lLastUpdate = lTimeNow;
		
		listTags.clear();
		final TraceMap map;

		if ( bReady ) {

			map = new TraceMap();

			final Graph graph = 
						HistogramTile.getGraph( "FILE_INTERVAL_" + strIndex );
			
			if ( null!=graph && ( lCycleCount > 1 ) && ( lElapsed > 0 ) ) {
				
				graph.add( ( (float) lElapsed ) / 1000 );
			}
			
			try {
				
				final File filePrevious = new File( getFilenamePrevious() );
				if ( filePrevious.exists() ) {
					FileUtils.forceDelete( filePrevious );
				}

				final File fileCurrent1 = new File( getFilenameCurrent() );
				if ( fileCurrent1.isFile() ) {
					fileCurrent1.renameTo( filePrevious );
				}

			} catch ( final IOException e ) {
				this.state = State.FAULT;
				e.printStackTrace();
				bReady = false;
			}
		} else {
			this.state = State.FAULT;
			map = null;
		}
		
		long lSourceFileTimestamp = 0;
		
		if ( bReady ) {
			
//			System.out.println( "002" );
			
			try {
			
				final File fileSource = new File( strSourceImage );
				final File fileCurrent = new File( getFilenameCurrent() );

				if ( fileSource.exists() ) {
					FileUtils.copyFile( fileSource, fileCurrent );
					lSourceFileTimestamp = fileSource.lastModified();
				} else {
					this.state = State.FAULT;
					// file probably just moved
					bReady = false;
				}

				final File fileSourceMask = new File( getFilenameSourceMask() );
				
				if ( fileSourceMask.exists() ) {
					final File fileMask = new File( getFilenameMask() );
					if ( ! fileMask.exists() ) {
						FileUtils.copyFile( fileSourceMask, fileMask );
					}
					bUsingMask = true;
				} else {
					bUsingMask = false;
				}
				
			} catch ( final IOException e ) {
				this.state = State.FAULT;
				e.printStackTrace();
				bReady = false;
			}
		}
		
		if ( bReady ) {
			
//			System.out.println( "003" );

			final File filePrevious = new File( getFilenamePrevious() );
			final File fileCurrent = new File( getFilenameCurrent() );
			final File fileMask = new File( getFilenameMask() );
			
			this.state = State.EXECUTING_COMPARISON;
			final Float fDiff = getImageDifference( 
							filePrevious, fileCurrent, fileMask, strPrefix );
			this.state = State.POST_COMPARISON;

			this.iCountCompleted++;

			if ( null != fDiff ) {
			
				System.out.println( strPrefix + "Comparison result: " + fDiff );
			
				if ( ! this.bWarmup ) {
					
					final float fThresholdAdjusted = this.fThreshold - this.fChoke;
					
					{
						final Graph graph = HistogramTile.getGraph( 
										"IMAGE_CHANGE_VALUE_" + strIndex );
						graph.add( fDiff );
						graph.setThresholdMax( new Double( fThresholdAdjusted ) );
					}

					
					if ( fDiff >= fThresholdAdjusted ) {
						
						// image changed
						
						if ( this.iCyclesSinceLastChange < 2 ) {
							this.fChoke = this.fChoke - 0.5f;
							this.listTags.add( "{+change+}" );
						} else {
							this.fChoke = this.fChoke - 0.1f;
							this.listTags.add( "{change}" );
						}
						this.iCyclesSinceLastChange = 0;
						
						this.iCountChanged++;
						final long lChangeElapsed = lTimeNow - lLastChange;
						
						System.out.println( "Change above threshold. Reporting.." );
						this.reportChangeDetected( fileCurrent, filePrevious, 
										lSourceFileTimestamp, fDiff, 
										lTimeNow, map );
						
						System.out.print( "lLastChange = " + lLastChange + ", "
								+ "lTimeNow = " + lTimeNow );
						
						if ( lLastChange > 0 ) {
							final Graph graph = HistogramTile.getGraph( 
											"CHANGE_INTERVAL_" + strIndex );
							
							System.out.print( ", graph: " + graph.hashCode() );
							
							final float fChangeMinutes = 
											(float)lChangeElapsed / 1000 / 60;

							System.out.print( ", fChangeMinutes: " + fChangeMinutes );

							graph.add( fChangeMinutes );
						}
						lLastChange = lTimeNow;
						
					} else {
						this.fChoke = this.fChoke + 0.02f;
						this.iCyclesSinceLastChange++;
					}

				} else {
					System.out.println( strPrefix + "(still in warmup)" );
					this.bWarmup = false;
				}
				
			} else {
				System.out.println( strPrefix + "Comparison returned null result." );
			}
		}

		this.state = State.IDLE;

		return true;
	}
	
	
	private void reportChangeDetected( final File fileChanged,
									   final File filePrevious,
									   final long lFileTimestamp,
									   final float fDiffValue,
									   final long lTimeDetect,
									   final TraceMap map ) {
		if ( null == fileChanged ) return;
		if ( null == this.fileSourcePath ) return;
		
		final Date dateFile = new Date( lFileTimestamp );
		
		final String strTimestamp = 
				DateFormatting.getTimestamp( dateFile ).substring( 0, 15 );
		
		final String strBaseFilename = getBaseFilename();
		final String strChangeDir = strTimestamp + "_" + strBaseFilename;
		
		final File fileChangeDir = 
//						new File( this.fileSourcePath, ""+ lFileTimestamp );
//						new File( "/tmp", strTimestamp );
//						new File( this.fileSourcePath, ""+ lFileTimestamp );
						new File( this.fileSourcePath, strChangeDir );
		
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
					"Name: %s\n", this.strName ) );
			sb.append( String.format( 
					"Source image file: %s\n", this.strSourceImage ) );
			sb.append( String.format( 
					"Source image timestamp: %s\n", strTimestamp ) );
			sb.append( String.format( 
					"Change directory: %s\n", strChangePath ) );
			
			sb.append( String.format( 
					"Comparison value: %.6f\n", fDiffValue ) );
			sb.append( String.format( 
					"Comparison threshold base: %.6f\n", this.fThreshold ) );
			sb.append( String.format( 
					"Comparison threshold choke: %.6f\n", this.fChoke ) );
			
			sb.append( String.format( 
					"Count, total: %d\n", this.iCountTotal ) );
			sb.append( String.format( 
					"Count, completed: %d\n", this.iCountCompleted ) );
			sb.append( String.format( 
					"Count, changed: %d\n", this.iCountChanged ) );
			
			
			map.put( "name", this.strName );
			map.put( "change-directory", strChangePath );
			map.put( "source-image-timestamp", strTimestamp );
			map.put( "diff-value", fDiffValue );
			map.put( "diff-threshold", fThreshold );
			map.put( "diff-choke", fChoke );
			
			map.put( "file-info", fileDestText.getAbsolutePath() );
			map.put( "file-source", this.strSourceImage );
			map.put( "file-previous", fileDestPrevImage.getAbsolutePath() );
			map.put( "file-changed", fileDestChangedImage.getAbsolutePath() );

			map.put( "identity-camera", strBaseFilename );
			map.put( "identity-timestamp", strTimestamp );
			map.put( "identity-mac", NetUtil.getMAC() );

			sb.append( "\nTraceMap:\n" );
			for ( final Entry<String, Object> entry : map.entrySet() ) {
				sb.append( "\t\"" + entry.getKey() + "\" "
						+ "= " + entry.getValue() + "\n" );
			}
			
			FileUtil.saveToFile( fileDestText, sb.toString() );

			
			final String strSubject = "IMAGE_CHANGE";
//			final String strValue = ""+ fDiffValue; 
			final String strValue = this.strName; 
			
			map.addFrame();
			
			final Event event = Event.add( 
					EventType.ENVIRONMENT, strSubject, strValue, 
					""+ ( this.fThreshold - this.fChoke ), map, lTimeDetect, 
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
		
		if ( bEnabled && ! threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );

		if ( bEnabled ) {
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		} else {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		}

		gc.setFont( Theme.get().getFont( 12 ) );

		text.addSpace( 10 );
		text.println( this.strName );

		gc.setFont( Theme.get().getFont( 8 ) );
		
		text.addSpace( 4 );
//		text.println( "Source Image File:" );
		text.println( this.strSourceImage );
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
		
		text.println( String.format( 
					"Threshold:   %1$.3f   -   %2$.3f   =   %3$.3f", 
							this.fThreshold, this.fChoke, 
							this.fThreshold - this.fChoke ) );

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
