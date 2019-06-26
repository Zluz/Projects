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

import org.apache.commons.io.FileUtils;

import com.github.sarxos.webcam.Webcam;

import jmr.SessionPath;
import jmr.pr124.ImageCapture;
import jmr.pr125.PostStillsContinuous;
import jmr.pr125.PostStillsContinuous.PostStillsListener;
import jmr.util.NetUtil;
import jmr.util.SystemUtil;

public class CameraSchedulerUI {

//	final static Display display = Display.getCurrent();
	final static Display display = S2TrayIcon.display;

	final private Shell shell;
	
	private static CameraSchedulerUI instance = null;
	
	final private Text textLog;
	
	private boolean bActive = false;

	private Thread threadCapture = null;
	
	final File fileTempDir = SystemUtil.getTempDir();
	final File fileSession = SessionPath.getSessionDir();
	final String strMAC = NetUtil.getMAC();


	
	private static void log( final String strText ) {
		if ( null==display || display.isDisposed() ) return;
		if ( null==instance ) return;
		if ( null==instance.textLog || instance.textLog.isDisposed() ) return;
		
		display.asyncExec( new Runnable() {
			@Override
			public void run() {
				final String strNewText = 
						instance.textLog.getText() + Text.DELIMITER 
						+ strText;
				instance.textLog.setText( strNewText );
			}
		});
	}
	
	
	private CameraSchedulerUI() {
		shell = new Shell( display, SWT.TITLE | SWT.MIN | SWT.RESIZE );
		this.shell.setText( CameraSchedulerUI.class.getName() );
		
		shell.setLayout( new FillLayout( SWT.HORIZONTAL ) );

		this.textLog = new Text( shell, SWT.MULTI );
		
		
		final Composite compControls = new Composite( shell, SWT.NONE );
		compControls.setLayout( new FillLayout( SWT.VERTICAL ) );
		
		final Text text = new Text( compControls, SWT.MULTI );
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
		
		shell.setSize( 500, 300 );
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
			
			log( "File posted: " + fileTarget.getAbsolutePath() );
		};
		
		@Override
		public void reportProcessEnded() {
			log( "reportProcessEnded()" );
			captureStop();
			captureStart();
		}
	};
	
	
	public void captureStart() {
		CameraSchedulerUI.log( "--- captureStart()" );

		this.threadCapture = new Thread( "Camera Capture" ) {
			@Override
			public void run() {
				log( "--- captureStart - capture thread started" );
				try {
					PostStillsContinuous post = null; 
					while ( bActive ) {
						
						Thread.sleep( 1000 );
		
						System.out.println( "Starting post-stills process." );
						
						post = new PostStillsContinuous( listener );
						post.start();
						
						while ( post.isRunning() && bActive ) {
							Thread.sleep( 1000 );
						}

						System.out.println( "Post-stills process ended." );

						Thread.sleep( 5000 );

					}
				} catch ( final InterruptedException e ) {
					bActive = false;
				}
			}
		};
		threadCapture.start();
		
		this.bActive = true;
	}
		
	
	public void captureStop() {
		CameraSchedulerUI.log( "--- captureStop()" );
		this.threadCapture.interrupt();
		this.bActive = false;
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
