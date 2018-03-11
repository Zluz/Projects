package jmr.pr115.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class SWTUtil {

	final static Display display = Display.getDefault();
	
	final static public Map<String,Image> 
							MAP_IMAGES = new HashMap<String,Image>();
	
	
	
	
	public static Image loadImage( final File file ) {
		if ( null==file ) return null;
		if ( !file.isFile() ) return null;
		
//		System.out.println( "--- SWTUtil.loadImage()" );
		
		final String strFilename = file.getAbsolutePath();
		if ( MAP_IMAGES.containsKey( strFilename ) ) {
			return MAP_IMAGES.get( strFilename );
		}
		
		try {
			final Image image = new Image( display, strFilename );
			MAP_IMAGES.put( strFilename, image );
//			System.out.println( "    SWTUtil.loadImage() - Image saved to MAP_IMAGES" );
			return image;
		} catch ( final Exception e ) {
			return null;
		}
	}
	
	
}
