package jmr.pr125;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import jmr.SessionPath;
import jmr.util.SelfDestruct;
import jmr.util.SystemUtil;

public class PostStills {


	private static final Logger 
					LOGGER = Logger.getLogger( PostStills.class.getName() );

	
	public final static long TIME_TIMEOUT = 120L;
	
	public final static long TIME_DELAY = 10L;
	
	
	
	private final File fileSession;
	private final File filePID;
	private final ImageCaptureS2 capture;
	private final long lStartTime;
	
	public PostStills() {
		
		this.lStartTime = System.currentTimeMillis();
		
		SelfDestruct.setTime( TIME_TIMEOUT );

		this.capture = new ImageCaptureS2();
		this.fileSession = SessionPath.getSessionDir();
		this.filePID = recordPIDFile();
	}
	
	
	private File recordPIDFile() {
		final long lPid = SystemUtil.getPid();
		final String strPidFilename = "pr125_" + lPid + ".pid";
		
		final File filePID = new File( fileSession, strPidFilename );
		
		BufferedWriter bw = null;
		try {
			final FileWriter fw = new FileWriter( filePID );
			bw = new BufferedWriter( fw );
			bw.write( "pid: " + lPid + "\n" );
			bw.write( "class: " + PostStills.class.getName() + "\n" );
			bw.write( "project: pr125\n" );
		} catch ( final IOException e ) {
			LOGGER.warning( "Failed to write PID file. "
									+ "Encountered " + e.toString() );
		} finally {
			if ( null!=bw ) {
				try {
					bw.close();
				} catch ( final IOException e ) {
					LOGGER.warning( "Failed to close file. " + e.toString() );
				}
			}
		}
		
		filePID.deleteOnExit();
		return filePID;
	}

	
	private File checkForLaterPIDFile() {
		final File[] listFiles = fileSession.listFiles();
		for ( final File file : listFiles ) {
			if ( ! filePID.equals( file ) 
					&& ( file.getName().endsWith( ".pid" ) ) ) {
				if ( file.lastModified() > this.lStartTime ) {
					return file;
				}
			}
		}
		return null;
	}
	
	
	
	private void exit( final int iExitCode,
					   final String strMessage ) {
		LOGGER.info( strMessage );
		
		final ProcessCleanup cleanup = new ProcessCleanup();
		final long lCount = cleanup.markForDeletion();
		LOGGER.info( "Temporary files found to delete: " + lCount );
		
		Runtime.getRuntime().exit( iExitCode );
	}
	
	
	
	public void start() throws InterruptedException {

		recordPIDFile();
		
		for (;;) {
			
			final File fileLaterPID = checkForLaterPIDFile();
			if ( null!=fileLaterPID ) {
				this.exit( 0, "Exiting. Later PID file found: " 
									+ fileLaterPID.getName() );
			}
			
			try {
				FileUtils.touch( filePID );
			} catch ( final IOException e ) {
				LOGGER.warning( "Failed to update PID file. " + e.toString() );
			}
			
			try {

				System.out.println( new Date().toString() );
				System.out.println( "Capturing.." );
				capture.runCapture( true );
	//			System.out.println( "Done." );
				
				int i = 0;
				for ( final File fileSrc : capture.getImageFiles() ) {
					
					final String strFileFull = "capture_vid" + i + ".jpg";
					final File fileFullDest = new File( fileSession, strFileFull );
	
					final String strFileThumb = "capture_vid" + i + "-thumb.jpg";
					final File fileThumbDest = new File( fileSession, strFileThumb );
	
					try {
						
						System.out.println( "\tCreating file \"" + strFileFull + "\" "
								+ "from \"" + fileSrc.getAbsolutePath() + "\"" );
						
						FileUtils.copyFile( fileSrc, fileFullDest );
						FileUtils.copyFile( fileSrc, fileThumbDest );
	
						fileSrc.delete();
						
					} catch ( final IOException e ) {
						LOGGER.warning( ()-> "Failed to copy file, "
										+ "encountered " + e.toString() );
					}
					
					i++;
				}
				
			} catch ( final Exception e ) {
				LOGGER.warning( ()-> "Problem encountered during capture: " 
							+ e.toString() );
			}
			
			System.out.print( "Pausing..." );
			Thread.sleep( TimeUnit.SECONDS.toMillis( TIME_DELAY ) );
			System.out.println( "Done." );
			
			SelfDestruct.setTime( TIME_TIMEOUT );
		}
		
	}
	
	
	
	
	
	
	
	public static void main( final String[] args ) throws InterruptedException {
		System.out.println( "Starting " + PostStills.class.getName()  + ".." );
		
		final PostStills post = new PostStills();
		post.start();
	}
	
}
