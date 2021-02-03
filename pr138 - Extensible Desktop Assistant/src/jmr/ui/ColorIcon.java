package jmr.ui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorIcon {
	
	private final Display display;
	
	private final static boolean IS_WINDOWS = '\\' == File.pathSeparatorChar; 
	
	public ColorIcon( final Display display ) {
		this.display = display;
		
		//TODO add color-following-thread
	}
	
	public Image getIcon( final RGB rgb ) {
		final int iSize;
		// should actually attempt to estimate the taskbar size
		// for now look at OS and used canned values
		if ( IS_WINDOWS ) {  
			iSize = 16;
		} else {
			iSize = 24;
		}
		final Image image = new Image( display, iSize, iSize );
		final GC gc = new GC( image );
		gc.setAntialias( SWT.ON );
		gc.setAdvanced( true );
		
		gc.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
		gc.setBackground( display.getSystemColor( SWT.COLOR_BLACK ) );
		gc.fillOval( 0, 0, iSize, iSize );
		
		final RGB rgbDark = new RGB( rgb.red / 2, rgb.green / 2, rgb.blue / 2 );
		final Color colorDark = new Color( display, rgbDark );
		gc.setBackground( colorDark );
		gc.fillOval( 3, 3, iSize - 6, iSize - 6 );
		colorDark.dispose();

		final Color colorHere = new Color( display, rgb );
		gc.setBackground( colorHere );
		gc.fillOval( 4, 4, iSize - 8, iSize - 8 );
		colorHere.dispose();
		
		//TODO cache
		return image;
	}

}
