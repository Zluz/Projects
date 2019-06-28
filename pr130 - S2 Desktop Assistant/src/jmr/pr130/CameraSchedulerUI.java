package jmr.pr130;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.sarxos.webcam.Webcam;

import jmr.SessionPath;
import jmr.pr124.ImageCapture;
import jmr.pr125.PostStillsContinuous;
import jmr.pr125.PostStillsContinuous.PostStillsListener;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobSet;
import jmr.util.NetUtil;
import jmr.util.SystemUtil;
import jmr.util.report.TraceMap;

public class CameraSchedulerUI {

//	final static Display display = Display.getCurrent();
	final static Display display = S2TrayIcon.display;

	final private Shell shell;
	
	private static CameraSchedulerUI instance = null;
	
	final private Text textLog;
	
	private PostStillsContinuous post = null; 
	
	final File fileTempDir = SystemUtil.getTempDir();
	final File fileSession = SessionPath.getSessionDir();
	final String strMAC = NetUtil.getMAC();

	private final static int LOG_HISTORY_SIZE = 10 * 1024;

	
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
	
	
	private CameraSchedulerUI() {
		shell = new Shell( display, SWT.TITLE | SWT.MIN | SWT.RESIZE );
		this.shell.setText( CameraSchedulerUI.class.getName() );
		
		shell.setLayout( new FillLayout( SWT.HORIZONTAL ) );

		this.textLog = new Text( shell, SWT.MULTI | SWT.V_SCROLL );
		this.textLog.setText( "textLog" + Text.DELIMITER + Text.DELIMITER );
		
		final Composite compControls = new Composite( shell, SWT.NONE );
		compControls.setLayout( new FillLayout( SWT.VERTICAL ) );
		
		final Text text = new Text( compControls, SWT.MULTI | SWT.V_SCROLL );
		text.setText( "<Configuration text here>" );
		
		final Button btnActivate = new Button( compControls, SWT.CHECK );
		final Text txtSessionDir = new Text( compControls, SWT.READ_ONLY );
		final Text txtTempDir = new Text( compControls, SWT.READ_ONLY );
		
		txtSessionDir.setText( "Session Dir:  " + fileSession.getAbsolutePath().toString() );
		txtTempDir.setText( "Temp Dir:  " + fileTempDir.getAbsolutePath().toString() );
		
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
		
		shell.setSize( 1000, 600 );
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

	
	final PostStillsListener listener = new PostStillsListener() {
		
		File filePrevious = null;
		
		@Override
		public void reportNewFile( final File file ) {
			
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
							fileTarget.getAbsolutePath(), "_" );
			
			final File fileMask = new File( strFilenameBase + "-mask.jpg" );
			
			log( "File posted: " + fileTarget.getAbsolutePath() );
			
			if ( null==filePrevious ) {
				this.filePrevious = fileTarget;
				return;
			}
			
			final TraceMap mapData = new TraceMap();
			mapData.put( "image_current", fileTarget.getAbsoluteFile().toString() );
			mapData.put( "image_previous", filePrevious.getAbsoluteFile().toString() );
			if ( fileMask.isFile() ) {
				mapData.put( "image_mask", fileMask.getAbsoluteFile().toString() );
			}
			
			final String strResult = strFilenameBase;
			final JobSet jobset = null;
			final JobType type = JobType.PROCESSING_IMAGE_DIFF;
			Job.add( type, jobset, strResult, mapData );
			
			this.filePrevious = fileTarget;
			
			
			final String strRequest = type.name() + ":" + strFilenameBase;
			final String strSQLDelete =
//					"DELETE " +
////					"SELECT * \n" + 
//					"FROM s2db.job " + 
////					"WHERE request LIKE \"PROCESSING_IMAGE_DIFF:S:%Sessions%94-C6-91-19-C5-CC%capture_vid0\" " + 
////					"WHERE request LIKE ? " + 
//					"WHERE request LIKE '" + StringUtils.replace( strRequest, "\\", "%" ) + "' " + 
//					"ORDER BY seq DESC " + 
//					"LIMIT 10,10000"
//					+ ""
//					+ ""
					"DELETE j \n" + 
					"FROM s2db.job j \n" + 
					"	JOIN ( SELECT * FROM s2db.job "
								+ "WHERE ( request LIKE 'PROCESSING_IMAGE_DIFF:%' ) "
								+ "ORDER BY seq DESC LIMIT 1 OFFSET 100 ) as oldest \n" + 
					"		ON ( TRUE \n" + 
					"				AND ( j.seq < oldest.seq ) \n" + 
					"				AND ( j.request LIKE '" + StringUtils.replace( strRequest, "\\", "%" ) + "' ) \n" + 
					"			);";
			
			try (	final Connection conn = ConnectionProvider.get().getConnection();
					final PreparedStatement stmt = 
									conn.prepareStatement( strSQLDelete ) ) {

//				stmt.setString( 1, strRequest );
				stmt.executeUpdate();
				
			} catch ( final SQLException e ) {
				e.printStackTrace();
				
				System.err.println( "SQL: " + strSQLDelete );
				
//				LOGGER.log( Level.SEVERE, "Query SQL: " + strQuery, e );
			}
			
		};
		
		@Override
		public void reportProcessEnded() {
			log( "reportProcessEnded()" );
			captureStop();
			captureStart();
		}
	};
	
	
	public void captureStart() {
		if ( null!=this.post ) {
			this.post.setListener( null );
			this.post.stop();
		}
		this.post = new PostStillsContinuous( listener );
		post.start();
	}
	
	public void captureStop() {
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
