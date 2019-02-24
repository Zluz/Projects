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
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;

public class ImageCapture {
	
	public static void main(String[] args) throws IOException {
		
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
			final Webcam webcam = Webcam.getWebcamByName( strName );
			System.out.println( "---\tgetName(): " + webcam.getName() );
			final Dimension[] arrSizes = webcam.getViewSizes();
//			final Dimension[] arrSizes = webcam.getCustomViewSizes();
			
			Dimension dimBest = null;
//			Dimension dimBest = new Dimension( 1920, 1080 );
			for ( final Dimension dim : arrSizes ) {
				if ( null==dimBest ) {
					dimBest = dim; 
				} else if ( dim.getWidth() > dimBest.getWidth() ) {
					dimBest = dim;
				}
				System.out.println( "\t\t" + dim.toString() );
			}
			
			webcam.setViewSize( dimBest );
			
//			WebcamImageTransformer transformer = new WebcamImageTransformer() {
//				@Override
//				public BufferedImage transform( final BufferedImage image ) {
//					return image;
//				}
//			};
//			webcam.setImageTransformer( transformer );
			
			try {
				webcam.open();
				
				final BufferedImage image = webcam.getImage();
				
//				final File file = new File("Capture_" + i + ".jpg");
				final File file = new File( strName + ".jpg" );
				System.out.println( "--- Saving to \"" 
									+ file.getAbsolutePath() + "\"" );
				ImageIO.write(image, "JPG", file);
			} catch ( final WebcamException e ) {
				System.err.println( "WARNING: Failed to access camera \"" 
										+ webcam.getName() + "\", "
										+ "encountered " + e.toString() );
				Webcam.resetDriver();
			}
			
		}
		
		new OpenIMAJGrabber();
	}
	
}
