package jmr.rpclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public abstract class UI {

	final public static Display display = new Display();

	final public static Color COLOR_WHITE = 
					display.getSystemColor( SWT.COLOR_WHITE );
	
	final public static Color COLOR_BLACK = 
					display.getSystemColor( SWT.COLOR_BLACK );

  
	public final static Cursor CURSOR_HIDE;

  
	static {
	    final PaletteData palette = new PaletteData(
	    		new RGB[] { UI.COLOR_WHITE.getRGB(), UI.COLOR_BLACK.getRGB() } );
	    final ImageData idHide = new ImageData( 16, 16, 1, palette );
	    idHide.transparentPixel = 0;
	    CURSOR_HIDE = new Cursor( UI.display, idHide, 0, 0 );
	}
    
  
}
