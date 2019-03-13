package jmr.pr124;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import jmr.util.SelfDestruct;

public class ImageCapture {
	
	public final static long TIME_TIMEOUT = 10L;

	
	private static void capture( final String strName ) {

		Webcam webcam = null;
		
		try {
			webcam = Webcam.getWebcamByName( strName );
			System.out.println( "---\tgetName(): " + webcam.getName() );
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
				final File file = new File( strName + ".jpg" );
				System.out.println( "--- Saving to \"" 
									+ file.getAbsolutePath() + "\"" );
				ImageIO.write(image, "JPG", file);
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
	}
	
	
	public static void main( final String[] args ) 
							throws IOException, InterruptedException {
		
//		com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber.class.getName();
//		new OpenIMAJGrabber();
		
//		final Webcam webcam = Webcam.getDefault();
		final List<Webcam> list = Webcam.getWebcams();
		System.out.println( "Camera system:");
		System.out.println( "\tgetDriver(): " + Webcam.getDriver().toString() );
		System.out.println( "Detected cameras:" );
		
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

		System.out.println( "Capturing stills:" );
		for ( final String strName : listNames ) {
			
			SelfDestruct.setTime( TIME_TIMEOUT, 
							"Timeout on still capture of \"" + strName + "\"" );
			
			capture( strName );
			
			Thread.sleep( 1000L );
		}
		
//		new OpenIMAJGrabber();
	}
	
}
