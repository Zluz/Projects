package jmr.pr124;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

//import com.github.sarxos.webcam.Webcam;
//import com.github.sarxos.webcam.WebcamException;

import jmr.util.SelfDestruct;
import jmr.util.SystemUtil;

public class ImageCapture {
	
	public final static long TIME_TIMEOUT_STARTUP = 60L;
	public final static long TIME_TIMEOUT_CAPTURE = 20L;


	public static void capture( final String strName ) {
		
		final File file = new File( strName + ".jpg" );

//		final Webcam webcam = Webcam.getWebcamByName( strName );
//		capture( webcam, file );
	}
	
	
	public static void init() {
//		final String strClassName = WebcamDefaultDriver.class.getName();
//		final String strClassPackage = WebcamDefaultDriver.class.getPackage().toString();
//		org.slf4j.Logger loggerWebcam = org.slf4j.LoggerFactory.getLogger(
//									WebcamDefaultDriver.class );
//		
//		LoggerContext lc =  (LoggerContext)LoggerFactory.getILoggerFactory();
//		final org.slf4j.Logger logger = lc.getLogger( strClassPackage );
//		logger.setl
//		
//		if ( loggerWebcam instanceof org.slf4j.Logger ) {
//			loggerWebcam.
//		}
	}
	

	public static void capture( // final Webcam webcam,
								final File fileTarget ) {
/*
		try {
			final String strName = webcam.getName();
			System.out.println( "---\tgetName(): " + strName );
			final Dimension[] arrSizes = webcam.getViewSizes();
			
			Dimension dimBest = null;
			for ( final Dimension dim : arrSizes ) {
				if ( null==dimBest ) {
					dimBest = dim; 
				} else if ( dim.getWidth() > dimBest.getWidth() ) {
					dimBest = dim;
				}
				System.out.println( "\t\t" + dim.toString() );
			}
			
			webcam.setViewSize( dimBest );
			
			try {
				webcam.open();
				
				final BufferedImage image = webcam.getImage();
				
	//			final File file = new File("Capture_" + i + ".jpg");
//				final File file = new File( strName + ".jpg" );
				
				final File fileWrite = new File( 
									fileTarget.getAbsoluteFile() + "_" );
				
//				System.out.println( "--- Saving to \"" 
//									+ fileTarget.getAbsolutePath() + "\"" );

				System.out.print( "Writing to:   " 
							+ fileWrite.getAbsolutePath() + "  ... " );				
				ImageIO.write( image, "JPG", fileWrite );
				System.out.println( "Done." );

				System.out.print( "Renaming to:  " 
							+ fileTarget.getAbsolutePath() + "   ... " );				
				FileUtils.moveFile( fileWrite, fileTarget );
				System.out.println( "Done." );

////				Thread.sleep( 700 );
//				System.out.println( "Saving to BAOS.." );
//				
//				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				
//				ImageIO.write( image, "JPG", baos );
//				baos.flush();
//
////				Thread.sleep( 700 );
//				System.out.println( "Saving to byte array.." );
//
//				final byte[] buffer = baos.toByteArray();
//				baos.close();
//
////				Thread.sleep( 700 );
//				System.out.println( "Saving to File.." );
//
//				final OutputStream os = new FileOutputStream( fileTarget );
//				os.write( buffer );
//				os.close();
				
//				Thread.sleep( 700 );
//				System.out.println( "(saved)" );
				
			} catch ( final WebcamException e ) {
				System.err.println( "WARNING: Failed to access camera \"" 
										+ webcam.getName() + "\", "
										+ "encountered " + e.toString() );
//				Webcam.resetDriver();
			}
			
		} catch ( final Exception e ) {
			
		} finally {
			webcam.close();
		}
		*/
	}
	
	/*
	public static void main( final String[] args ) 
							throws IOException, InterruptedException {
		
		final File fileTempDir = SystemUtil.getTempDir();
		
		final boolean bContinuous;
		if ( args.length > 0 && "continuous".equals( args[0].toLowerCase() ) ) {
			bContinuous = true;
			System.out.println( "Running in continuous mode." );
		} else {
			bContinuous = false;
			System.out.println( "Running in a single pass." );
		}

		SelfDestruct.setTime( TIME_TIMEOUT_STARTUP, "Timeout while starting up." );

//		com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber.class.getName();
//		new OpenIMAJGrabber();
		
//		final Webcam webcam = Webcam.getDefault();
		final List<Webcam> list = Webcam.getWebcams();
		System.out.println( "Camera system:");
		System.out.println( "\tgetDriver(): " + Webcam.getDriver().toString() );
		System.out.println( "Detected cameras:" );
		
		if ( list.isEmpty() ) {
			System.out.println( "WARNING: No cameras found. Exiting." );
			System.exit( 100 );
		}
		
		final List<String> listNames = new LinkedList<>();
		
		for ( final Webcam webcam : list ) {
			listNames.add( webcam.getName() );
			System.out.println( "---\tgetName(): " + webcam.getName() );
			System.out.println( "\tgetDevice(): " + webcam.getDevice().getName() );
			System.out.println( "\tgetViewSize(): " + webcam.getViewSize() );
			System.out.println( "\tgetFPS(): " + webcam.getFPS() );
			System.out.println( "\ttoString(): " + webcam.toString() );
			System.out.println( "\tgetViewSizes():" );
		}

		System.out.println( "Capturing stills.." );
		for (;;) {
			int i=0;
			for ( final Webcam webcam : list ) {
				
				final long lTimeNow = System.currentTimeMillis();
				final File file = new File( fileTempDir, 
						"capture_vid" + i + "-t" + lTimeNow + ".jpg" );

				final String strName = webcam.getName();
				SelfDestruct.setTime( TIME_TIMEOUT_CAPTURE, 
						"Timeout on still capture of \"" + strName + "\"." );
				
				capture( webcam, file );
				
//				Thread.sleep( 100L );
				i++;
			}
			if ( ! bContinuous ) {
				System.out.println( "Capture complete. Exiting." );
				System.exit( 0 );
			}
		}
		
//		new OpenIMAJGrabber();
	}
*/	
}
