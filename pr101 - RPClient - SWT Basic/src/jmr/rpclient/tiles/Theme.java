package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class Theme {
	
	public enum Colors {
		BACKGROUND,
		TEXT,
		TEXT_LIGHT,
		TEXT_BOLD,
		;
	}

	private static Theme instance;
	
	private Display display;
	
	private Theme() {
		this.display = Display.getCurrent();
	}
	
	private final Map<Integer,Font> mapFonts = new HashMap<Integer,Font>();
	
	
	public static synchronized Theme get() {
		if ( null==instance ) {
			instance = new Theme();
		}
		return instance;
	}
	
	public Color getColor( Colors color ) {
		final int iColor;
		switch ( color ) {
			case BACKGROUND 	: iColor = SWT.COLOR_BLACK; break; 
			case TEXT 			: iColor = SWT.COLOR_GRAY; break;
			case TEXT_LIGHT 	: iColor = SWT.COLOR_GRAY; break;
			case TEXT_BOLD  	: iColor = SWT.COLOR_WHITE; break;
			default				: iColor = SWT.COLOR_GRAY; break;
		}
		return display.getSystemColor( iColor );
	}
	
	/**
	 * RPi:
	 * 		5 is too small
	 * 		6 too small for normal text, barely readable
	 * PC:
	 * 		6 too small 
	 * 
	 * 5 is too small for RPi
	 * 6 is 
	 * 
	 * @param iSize
	 * @return
	 */
	public Font getFont( final int iSize ) {
		if ( !mapFonts.containsKey( iSize ) ) {
		    final FontData fd = display.getSystemFont().getFontData()[0];
		    fd.setHeight( iSize );
			final Font font = new Font( display, fd );
			mapFonts.put( iSize, font );
		}
		return mapFonts.get( iSize );
	}
	
	
	
	
}
