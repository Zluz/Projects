package jmr.pr130;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.sarxos.webcam.Webcam;

import jmr.SessionPath;
import jmr.pr124.ImageCapture;
import jmr.pr125.PostStillsContinuous;
import jmr.pr125.PostStillsContinuous.PostStillsListener;
import jmr.s2db.Client;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobSet;
import jmr.util.NetUtil;
import jmr.util.OSUtil;
import jmr.util.SystemUtil;
import jmr.util.report.TraceMap;
import jmr.util.transform.TextUtils;

public class CameraSchedulerUI {

//	final static Display display = Display.getCurrent();
	final static Display display = S2TrayIcon.display;

	final private Shell shell;
	
	private static CameraSchedulerUI instance = null;
	
	final private Text textLog;
	final private Text textTrace;
	
	final private Text textPoolThreads;
	final private Text textCameras;
	
	private PostStillsContinuous post = null; 
	
	final File fileTempDir = SystemUtil.getTempDir();
	final File fileSession = SessionPath.getSessionDir();
	final String strMAC = NetUtil.getMAC();

	private final static int LOG_HISTORY_SIZE = 10 * 1024;

	
	final Map<Character,File> mapPreviousFile = new HashMap<>();
	final Map<String,Map<String,String>> mapConfig = new HashMap<>();
	final Map<Character,String> mapBaseFilenames = new HashMap<>();
	
	boolean bActive;


	public static final int POOL_SIZE = 4;
	
	final static ThreadPoolExecutor pool = 
			(ThreadPoolExecutor)Executors.newFixedThreadPool( POOL_SIZE );
	

	
	private static void log( final String strText ) {
		if ( null==display || display.isDisposed() ) return;
		if ( null==instance ) return;
		if ( null==instance.textLog || instance.textLog.isDisposed() ) return;
		
		display.asyncExec( new Runnable() {
			@Override
			public void run() {
				final Text text = instance.textLog;
				
				String strWidgetText = text.getText();
				if ( strWidgetText.length() > LOG_HISTORY_SIZE ) {
					strWidgetText = StringUtils.right( 
										strWidgetText, LOG_HISTORY_SIZE );
					strWidgetText = StringUtils.substringAfter( 
										strWidgetText, Text.DELIMITER );
				}
				strWidgetText = strWidgetText + Text.DELIMITER + strText;
				
				text.setText( strWidgetText );
				text.setSelection( strWidgetText.length() );
				
				System.out.println( "log> " + strText );
			}
		});
	}
	
	
	private void refreshControls() {
		display.asyncExec( new Runnable() {
			@Override
			public void run() {
				
				final List<String> list = 
								new LinkedList<>( mapBaseFilenames.values() );
				Collections.sort( list );
				final String strCameraFilenames = 
								String.join( Text.DELIMITER, list );
				textCameras.setText( 
							"Cameras (base filenames)" + Text.DELIMITER 
							+ strCameraFilenames );
				
				textPoolThreads.setText( "Thread Pool: " + Text.DELIMITER 
							+ pool.getActiveCount() + " of " + POOL_SIZE 
								+ " active threads" ); 
			}
		});
	}
	

	private static void showTrace( final TraceMap mapData ) {
		if ( null==display || display.isDisposed() ) return;
		if ( null==instance ) return;
		if ( null==instance.textTrace || instance.textTrace.isDisposed() ) return;
		
		display.asyncExec( new Runnable() {
			@Override
			public void run() {
				final Text text = instance.textTrace;
				
				final StringBuilder sb = new StringBuilder();
				
				final List<String> list = new LinkedList<>( mapData.keySet() );
				Collections.sort( list );
				for ( final String strKey : list ) {
					final Object objValue = mapData.get( strKey );
					sb.append( strKey );
					sb.append( " = " );
					if ( objValue instanceof String ) {
						sb.append( "\"" + objValue.toString() + "\"" );
					} else {
						sb.append( objValue.toString() );
					}
					sb.append( Text.DELIMITER );
				}
				
				text.setText( sb.toString() );
			}
		});
	}
	
	
	private CameraSchedulerUI() {
		shell = new Shell( display, SWT.TITLE | SWT.MIN | SWT.RESIZE );
		this.shell.setText( OSUtil.getProgramName() + "  -  " 
							+ CameraSchedulerUI.class.getName() );
		
		shell.setLayout( new FillLayout( SWT.HORIZONTAL ) );

		this.textLog = new Text( shell, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP );
		this.textLog.setText( "textLog" + Text.DELIMITER + Text.DELIMITER );

		final Composite compRight = new Composite( shell, SWT.NONE );
		compRight.setLayout( new FillLayout( SWT.VERTICAL ) );

		this.textTrace = new Text( compRight, SWT.MULTI | SWT.V_SCROLL );
		this.textTrace.setText( "<Last Trace Map here>" );

		final Composite compControls = new Composite( compRight, SWT.NONE );
		compControls.setLayout( new FillLayout( SWT.VERTICAL ) );
		
		this.textCameras = new Text( compControls, SWT.MULTI | SWT.READ_ONLY );
		this.textPoolThreads = new Text( compControls, SWT.READ_ONLY );
		
		final Button btnActivate = new Button( compControls, SWT.CHECK );
		final Text txtSessionDir = new Text( compControls, SWT.READ_ONLY );
		final Text txtTempDir = new Text( compControls, SWT.READ_ONLY );
		
		final String strSessionDir = fileSession.getAbsolutePath().toString();
		txtSessionDir.setText( "Session Dir: " + Text.DELIMITER + strSessionDir );
		if ( ! strSessionDir.contains( "Sessions" ) ) {
			txtSessionDir.setBackground( display.getSystemColor( SWT.COLOR_YELLOW ) );
		}

		final String strTempDir = fileTempDir.getAbsolutePath().toString();
		txtTempDir.setText( "Temp Dir:  " + Text.DELIMITER + strTempDir );
		if ( strTempDir.contains( "C:\\Users" ) ) {
			txtTempDir.setBackground( display.getSystemColor( SWT.COLOR_YELLOW ) );
		}
		
		
		btnActivate.setText( "Active" );

		btnActivate.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected( final SelectionEvent e ) {
				if ( btnActivate.getSelection() ) {
					captureStart();
				} else {
					captureStop();
				}
			}
		});
		
		shell.setSize( 1100, 800 );
		shell.layout();
		
		final Image image = S2TrayIcon.getS2Icon();
		shell.setImage( image );
		
		shell.addListener( SWT.Close, new Listener() {
			@Override
			public void handleEvent( final Event event ) {
				shell.setVisible( false );
				event.doit = false;
			}
		});
		
		shell.addControlListener( new ControlAdapter() {
			@Override
			public void controlResized( final ControlEvent e ) {
				final Point pt = shell.getSize();
				log( "Shell resized to " + pt.x + ", " + pt.y );
			}
		});
		
		CameraSchedulerUI.log( 
					CameraSchedulerUI.class.getSimpleName() + " started." );
	}
	
	
	public static boolean capture( final Webcam camera,
								   final File fileTempDir,
								   final File fileSession,
								   final int iCameraIndex,
								   final File fileDest ) {

		final long lTimeNow = System.currentTimeMillis();
		

//		final File fileSrc = File.createTempFile( "Capture_", ".jpg" );
		final File fileSrc = new File( fileTempDir, "Capture_" + lTimeNow + ".jpg" );

//		final String strFileThumb = "capture_vid" + i + "-thumb.jpg";
//		final File fileThumbDest = new File( fileSession, strFileThumb );

		log( "Capturing image from camera: " + camera.getName() );
		log( "Capturing to: " + fileSrc.getAbsolutePath() );
		
//		final String strName = camera.getName();
		ImageCapture.capture( camera, fileSrc );

		final String strFileFull = "capture_vid" + iCameraIndex + ".jpg";
		final File fileFullDest = new File( fileSession, strFileFull );
		try {
			
			log( "Copying to: " + fileFullDest.getAbsolutePath() );
			FileUtils.copyFile( fileSrc, fileFullDest );
			return true;
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	
	final Map<String,String> getConfigForCamera( final File file ) {
		if ( null==file ) return Collections.emptyMap();
		
		final String strFilename = file.getAbsolutePath();
		
		if ( ! mapConfig.containsKey( strFilename ) ) {
			try {
				final String strConfig = FileUtils.readFileToString( 
									file, Charset.defaultCharset() );
				final Map<String,String> map = 
									TextUtils.getMapFromIni( strConfig );
				
				mapConfig.put( strFilename, map );
				return map;
			} catch ( final IOException e ) {
				log( "ERROR (" + e.toString() + ") - "
									+ "Failed to read configuration file: " 
									+ strFilename );
				return Collections.emptyMap();
			}

		}
		return mapConfig.get( strFilename );
	}
	
	
	private void processNewFile( final File file,
								 final TraceMap mapData ) {
		
		mapData.addFrame();
		refreshControls();
		
		final String strFilename = file.getName();
		final File fileTransfer = 
				new File( SessionPath.getSessionDir(), strFilename + "_" );
		final File fileTarget = 
				new File( SessionPath.getSessionDir(), strFilename );
		try {
			FileUtils.copyFile( file, fileTransfer );
			FileUtils.moveFile( fileTransfer, fileTarget );
		} catch ( final IOException e ) {
			log( "Failed to copy file, encountered " + e.toString() );
			e.printStackTrace();
		}
		
		final String strFilenameBase = 
						StringUtils.substringBeforeLast( 
						fileTarget.getAbsolutePath(), "-t" );
		
		final File fileMask = new File( strFilenameBase + "-mask.jpg" );
		final File fileConfig = new File( strFilenameBase + "-config.ini" );
		final File fileLive = new File( strFilenameBase + "-live.ini" );
		
		final char cCameraIndex = strFilenameBase.charAt( 
										strFilenameBase.length() - 1 );
		
		mapBaseFilenames.put( cCameraIndex, strFilenameBase );
		
		if ( ! fileConfig.isFile() ) {
			log( "ERROR - Missing configuration file: " 
									+ fileConfig.getAbsolutePath() );
			return;
		}
		
		log( "File posted: " + fileTarget.getAbsolutePath() );
		
		final File filePrevious = mapPreviousFile.get( cCameraIndex );
		if ( null==filePrevious ) {
			mapPreviousFile.put( cCameraIndex, fileTarget );
			return;
		}
		
		mapData.putAllUnder( "01", Client.get().getDetails() );
		
		mapData.put( "image_current", fileTarget.getAbsoluteFile().toString() );
		mapData.put( "image_previous", filePrevious.getAbsoluteFile().toString() );
		mapData.put( "image_mask", fileMask.getAbsoluteFile().toString() );
		mapData.put( "file_config", fileConfig.getAbsoluteFile().toString() );
		mapData.put( "file_live", fileLive.getAbsoluteFile().toString() );
		
		mapData.putAllUnder( "config", getConfigForCamera( fileConfig ) );

		try {
			if ( fileLive.isFile() ) {
				
				final String strLive = FileUtils.readFileToString( 
									fileLive, Charset.defaultCharset() );
				final Map<String,String> mapLive = 
									TextUtils.getMapFromIni( strLive );
				mapData.putAllUnder( "live", mapLive );
			}
		} catch ( final IOException e ) {
			log( "WARNING (" + e.toString() + ") - "
								+ "Failed to read live data file: " 
								+ fileLive.getAbsolutePath() );
			return;
		}
		

		mapData.addFrame();

		final String strResult = strFilenameBase;
		final JobSet jobset = null;
		final JobType type = JobType.PROCESSING_IMAGE_DIFF;
		Job.add( type, jobset, 1, strResult, mapData );
		
		mapPreviousFile.put( cCameraIndex, fileTarget );
		
		showTrace( mapData );
	}
	
	
	final PostStillsListener listener = new PostStillsListener() {
		
//		File filePrevious = null;
		
		@Override
		public void reportNewFile( final File file ) {
			if ( null==file ) return;
			if ( ! file.isFile() ) return;
			
			final TraceMap mapData = new TraceMap();
			
			final Runnable runnable = new Runnable() {
				public void run() {
					processNewFile( file, mapData );
				};
			};
			pool.execute( runnable );
		};
		
		@Override
		public void reportProcessEnded() {
			log( "reportProcessEnded()" );
			captureStop();
			captureStart();
		}
	};

	private Thread threadDeletion;
	
	
	final void startJobDeletionThead() {
		threadDeletion = new Thread( "Job Deletion" ) {
			public void run() {
				try {
					while ( bActive ) {
						TimeUnit.SECONDS.sleep( 5 );
						final Collection<String> list = mapBaseFilenames.values();
						for ( final String strFilenameBase : list ) {
							deleteOldProcessingJobs( strFilenameBase );
						}
					}
				} catch ( final InterruptedException e ) {
					log( "Job deletion interrupted" );
				}
			};
		};
		threadDeletion.start();
	}
	
	
	public void deleteOldProcessingJobs( final String strFilenameBase ) {

		final JobType type = JobType.PROCESSING_IMAGE_DIFF;
		final String strRequest = type.name() + ":" + strFilenameBase;
		final String strSqlSafeRequest = StringUtils.replace( strRequest, "\\", "%" );
		final String strSQLDelete =
				"DELETE j \n" + 
				"FROM s2db.job j \n" + 
				"	JOIN ( SELECT * FROM s2db.job "
							+ "WHERE ( request LIKE 'PROCESSING_IMAGE_DIFF:%' ) "
							+ "ORDER BY seq DESC LIMIT 1 OFFSET 100 ) as oldest \n" + 
				"		ON ( TRUE \n" + 
				"				AND ( j.seq < oldest.seq ) \n" + 
				"				AND ( j.request LIKE '" + strSqlSafeRequest + "' ) \n" + 
				"			);";
		
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final PreparedStatement stmt = 
								conn.prepareStatement( strSQLDelete ) ) {

//			stmt.setString( 1, strRequest );
			stmt.executeUpdate();
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			
			System.err.println( "SQL: " + strSQLDelete );
			
//			LOGGER.log( Level.SEVERE, "Query SQL: " + strQuery, e );
		}
	}
	
	
	public void captureStart() {
		this.bActive = true;
		if ( null!=this.post ) {
			this.post.setListener( null );
			this.post.stop();
		}
		this.post = new PostStillsContinuous( listener );
		post.start();
		startJobDeletionThead();
	}
	
	public void captureStop() {
		this.bActive = false;
		CameraSchedulerUI.log( "--- captureStop()" );
	}
	
	
	public static synchronized CameraSchedulerUI get() {
		if ( null==instance ) {
			instance = new CameraSchedulerUI();
		}
		return instance;
	}
	

	public void open() {
		shell.setVisible( true );
		shell.setActive();
	}
	
	
	public Shell getShell() {
		return this.shell;
	}

	
	public void close() {
		if ( null!=this.post ) {
			this.post.setListener( null );
			this.post.stop();
		}
		this.shell.close();
		this.shell.dispose();
	}
	

	public static void main( final String[] args ) {

		final CameraSchedulerUI gui = new CameraSchedulerUI();
		gui.open();

	    while ( ! gui.getShell().isDisposed()) {
		      if ( display.readAndDispatch()) {
		    	  display.sleep();
		      }
		}
		display.dispose(); 
	}
	
}
