package jmr.rpclient.tiles;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
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
import jmr.util.RunProcess;

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
		DISABLED,
		;
	}
	
	private final static Set<String> MONITOR_FILES = new HashSet<>();
	
	
	private final Thread threadUpdater;

	private final String strName;
	private final String strSourceImage;
	private final String strIndex;
	
	private State state = State.IDLE;
	private int iCountTotal;
	private int iCountCompleted;
	private boolean bUsingMask;
	private boolean bWarmup;
	
	
	
	public ProcImageTile( final String strIndex,
						  final Map<String, String> mapOptions ) {
		if ( null==mapOptions || mapOptions.isEmpty() ) {
			LOGGER.warning( "Missing configuration map" );
			strSourceImage = null;
			threadUpdater = null;
			this.strIndex = null;
			this.strName = null;
			return;
		}
		
		this.strIndex = strIndex;
		this.bWarmup = true;
		
		final String strPrefix = "proc_image." + strIndex;
		this.strSourceImage = mapOptions.get( strPrefix + ".file" );
		this.strName = mapOptions.get( strPrefix + ".name" );
		
		if ( StringUtils.isBlank( this.strSourceImage ) ) {
			this.state = State.DISABLED;
		} else if ( MONITOR_FILES.contains( this.strSourceImage ) ) {
			this.state = State.DISABLED;
		} else {
			MONITOR_FILES.add( this.strSourceImage );
			this.state = State.IDLE;
			
			clearOldFiles();
		}
		
		this.threadUpdater = createThread();
	}

	
	String strData = "uninitialized";
	
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
	
	
	long lLastUpdate = System.currentTimeMillis();
	long lCycleCount = 0;

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
		
//		System.out.println( "Running command: " 
//						+ strCommand[0] + " " + strCommand[1] + " " 
//						+ strCommand[2] + " " + strCommand[3] );
		
		final RunProcess run = new RunProcess( strCommand );
		final Integer iResult = run.run();
		final String strOut = run.getStdOut();
		if ( null==iResult || iResult != 0 ) {
			LOGGER.warning( strLogPrefix + "Non-zero result from process." );
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
		
//		try {
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
//		} catch ( final IOException e ) {
//			LOGGER.warning( "Failed to clear old files. " + e.toString() );
//		}
	}
	
	
	public boolean scan() {

		this.iCountTotal++;
		final String strPrefix = "[" + this.strIndex + "] ";

		this.state = State.WAITING_FOR_FILE;
		boolean bReady = doWaitForFileToMove( strSourceImage );
		this.state = State.PREPARING_FILES;
		final long lTimeNow = System.currentTimeMillis();
		
//		System.out.println( "Source image file updated.." );
		
		lCycleCount++;
		final long lElapsed = lTimeNow - lLastUpdate;
		lLastUpdate = lTimeNow;

		if ( bReady ) {

//			System.out.println( "001" );

			final Graph graph = HistogramTile.getGraph( 
									"FILE_UPDATE_INTERVAL_" + strIndex );
			if ( null!=graph && ( lCycleCount > 1 ) ) {
				graph.add( ( (float) lElapsed ) / 1000 );
			}
			
			try {
//				System.out.println( "001.01" );
				
				final File filePrevious = new File( getFilenamePrevious() );
				if ( filePrevious.exists() ) {
					FileUtils.forceDelete( filePrevious );
				}

//				final File fileMask = new File( getFilenameMask() );
//				if ( fileMask.exists() ) {
//					FileUtils.forceDelete( fileMask );
//				}

//				System.out.println( "001.02" );

				final File fileCurrent1 = new File( getFilenameCurrent() );
				if ( fileCurrent1.isFile() ) {
					fileCurrent1.renameTo( filePrevious );
				}

//				System.out.println( "001.03" );

			} catch ( final IOException e ) {
				this.state = State.FAULT;
				e.printStackTrace();
				bReady = false;
			}
		} else {
			this.state = State.FAULT;
		}
		
		if ( bReady ) {
			
//			System.out.println( "002" );
			
			try {
			
				final File fileSource = new File( strSourceImage );
				final File fileCurrent = new File( getFilenameCurrent() );

				if ( fileSource.exists() ) {
					FileUtils.copyFile( fileSource, fileCurrent );
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
					final Graph graph = HistogramTile.getGraph( 
										"IMAGE_CHANGE_VALUE_" + strIndex );
					graph.add( fDiff );
					
					// potentially fire event here
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
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		final boolean bEnabled = ! State.DISABLED.equals( this.state );
		
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
			case DISABLED:
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
		}
		text.println( "State: " + this.state.name() );
		if ( bEnabled ) {
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		} else {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		}
		gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );

		text.addSpace( 10 );
		text.println( "Total: " + this.iCountTotal + ",  "
				+ "Completed: " + this.iCountCompleted ); 
//				+ "Fault: " + this.iCountFault ); 

		text.addSpace( 10 );
		
		gc.setFont( Theme.get().getFont( 9 ) );
		if ( this.bUsingMask ) {
			text.println( "[mask]" );
		} else {
			text.setRightAligned( true );
			text.println( "<no-mask>" );
		}

//		text.println( "Source Data:" );
//		text.println( this.strData );

	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
