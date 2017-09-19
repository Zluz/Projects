package jmr.rpclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
	
	final public static Color COLOR_GREEN = 
					display.getSystemColor( SWT.COLOR_GREEN );

	final public static Color COLOR_BLUE = 
					display.getSystemColor( SWT.COLOR_BLUE );

	final public static Color COLOR_DARK_BLUE = 
					display.getSystemColor( SWT.COLOR_DARK_BLUE );

	final public static Color COLOR_RED = 
					display.getSystemColor( SWT.COLOR_RED );

	final public static Color COLOR_GRAY = 
					display.getSystemColor( SWT.COLOR_GRAY );
		
	final public static Color COLOR_DARK_GRAY = 
//					display.getSystemColor( SWT.COLOR_DARK_GRAY );
					new Color( display, 30, 30, 30 );

  

    
	public static final Font FONT_HUGE;
	public static final Font FONT_LARGE;
	public static final Font FONT_MEDIUM;
	public static final Font FONT_SMALL;

	
    static {
    	final FontData fdHuge = display.getSystemFont().getFontData()[0];
        fdHuge.setHeight( 140 );
    	FONT_HUGE = new Font( display, fdHuge );

    	final FontData fdLarge = display.getSystemFont().getFontData()[0];
        fdLarge.setHeight( 40 );
    	FONT_LARGE = new Font( display, fdLarge );

    	final FontData fdMedium = display.getSystemFont().getFontData()[0];
        fdMedium.setHeight( 25 );
    	FONT_MEDIUM = new Font( display, fdMedium );

    	final FontData fdSmall = display.getSystemFont().getFontData()[0];
        fdSmall.setHeight( 15 );
    	FONT_SMALL = new Font( display, fdSmall );
    }
	

	
	
	
	public final static Cursor CURSOR_HIDE;

  
	static {
	    final PaletteData palette = new PaletteData(
	    		new RGB[] { UI.COLOR_WHITE.getRGB(), UI.COLOR_BLACK.getRGB() } );
	    final ImageData idHide = new ImageData( 16, 16, 1, palette );
	    idHide.transparentPixel = 0;
	    CURSOR_HIDE = new Cursor( UI.display, idHide, 0, 0 );
	}
    
  
}
