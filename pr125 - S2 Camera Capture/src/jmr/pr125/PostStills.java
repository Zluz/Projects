package jmr.pr125;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import jmr.SessionPath;
import jmr.util.SelfDestruct;

public class PostStills {


	private static final Logger 
					LOGGER = Logger.getLogger( PostStills.class.getName() );

	
	public final static long TIME_TIMEOUT = 120L;
	
	public final static long TIME_DELAY = 10L;
	
	
	
	public static void main( final String[] args ) throws InterruptedException {
		System.out.println( "Starting " + PostStills.class.getName()  + ".." );

		SelfDestruct.setTime( TIME_TIMEOUT );

		final ImageCaptureS2 capture = new ImageCaptureS2();
		
		final File fileSession = SessionPath.getSessionDir();
		
		for (;;) {
			
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
	
}
