package jmr.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorIcon {
	
	private final Display display;
	
	public ColorIcon( final Display display ) {
		this.display = display;
		
		//TODO add color-following-thread
	}
	
	public Image getIcon( final RGB rgb ) {
		final Image image = new Image( display, 16, 16 );
		final GC gc = new GC( image );
		gc.setAntialias( SWT.ON );
		gc.setAdvanced( true );
		
		gc.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
		gc.setBackground( display.getSystemColor( SWT.COLOR_BLACK ) );
		gc.fillOval( 0, 0, 16, 16 );
		
		final RGB rgbDark = new RGB( rgb.red / 2, rgb.green / 2, rgb.blue / 2 );
		final Color colorDark = new Color( display, rgbDark );
		gc.setBackground( colorDark );
		gc.fillOval( 2, 2, 12, 12 );
		colorDark.dispose();

		final Color colorHere = new Color( display, rgb );
		gc.setBackground( colorHere );
		gc.fillOval( 3, 3, 10, 10 );
		colorHere.dispose();
		
		//TODO cache
		return image;
	}

}
