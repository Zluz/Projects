package jmr.pr124;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class ImageCapture {
	
	public static void main(String[] args) throws IOException {
		Webcam webcam = Webcam.getDefault();
		webcam.open();
		BufferedImage image = webcam.getImage();
		final File file = new File("test.jpg");
		System.out.println( "Saving to " + file.getAbsolutePath() );
		ImageIO.write(image, "JPG", file);
	}
	
}
