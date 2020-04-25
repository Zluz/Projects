package jmr.rpclient.swt;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import jmr.pr134.fonts.FontProvider;
import jmr.pr134.fonts.FontProvider.FontResource;

public class Theme {
	
	public enum Colors {
		BACKGROUND( 0,0,0 ),
		LINE_FAINT( 0,0,70 ),
		LINE_BOLD( 255, 255, 255 ),
		TEXT( 180, 180, 180 ),
		TEXT_LIGHT( 100,100,100 ),
		TEXT_BOLD( 255, 255, 255 ),
		BACK_ALERT( 160, 0, 0 ),
		;
		
		final RGB rgb;
		
		private Colors( final int r, final int g, final int b ) {
			this.rgb = new RGB( r,g,b );
		}
	}

	private static Theme instance;
	
	private final static EnumMap< Colors, Color > 
				COLORMAP = new EnumMap<>( Colors.class );
	
	private Display display;
	
	private final FontProvider fontprovider;
	
	private Theme() {
		this.display = Display.getCurrent();
		for ( final Colors c : Colors.values() ) {
			final Color color = ColorCache.getColor( c.rgb );
//			final Color color = new Color( display, c.rgb );
			COLORMAP.put( c, color );
		}
		fontprovider = new FontProvider( display );
	}
	
	private final Map<Integer,Font> mapFontNormal = new HashMap<Integer,Font>();
	private final Map<Integer,Font> mapFontBold = new HashMap<Integer,Font>();
	
	
	public static synchronized Theme get() {
		if ( null==instance ) {
			instance = new Theme();
		}
		return instance;
	}
	
	public Color getColor_( Colors color ) {
		final int iColor;
		switch ( color ) {
			case BACKGROUND 	: iColor = SWT.COLOR_BLACK; break; 
			case TEXT 			: iColor = SWT.COLOR_GRAY; break;
			case TEXT_LIGHT 	: iColor = SWT.COLOR_GRAY; break;
			case TEXT_BOLD  	: iColor = SWT.COLOR_WHITE; break;
			case LINE_FAINT		: iColor = SWT.COLOR_DARK_BLUE; break;
			case LINE_BOLD		: iColor = SWT.COLOR_GREEN; break;
			case BACK_ALERT		: iColor = SWT.COLOR_DARK_RED; break;
			default				: iColor = SWT.COLOR_GRAY; break;
		}
		return display.getSystemColor( iColor );
	}

	public Color getColor( Colors color ) {
		return COLORMAP.get( color );
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
		if ( !mapFontNormal.containsKey( iSize ) ) {
//		    final FontData fd = display.getSystemFont().getFontData()[0];
			final FontResource fr;
			if ( iSize < 10 ) {
				fr = FONT_TINY;
			} else if ( iSize > 14 ){
				fr = FONT_LARGE;
			} else {
				fr = FONT_NORMAL;
			}
//			final Font fontSource = fontprovider.get( fr );
//			final FontData fd = fontSource.getFontData()[ 0 ];
//		    fd.setHeight( iSize );
//			final Font font = new Font( display, fd );
			final Font font = fontprovider.get( fr, iSize, SWT.NORMAL );
			mapFontNormal.put( iSize, font );
		}
		return mapFontNormal.get( iSize );
	}
	
	public final static FontResource 
					FONT_TINY = FontResource.CABIN_CONDENSED; 
	public final static FontResource 
					FONT_NORMAL = FontResource.ARCHIVO_NARROW; 
	public final static FontResource 
					FONT_LARGE = FontResource.BARLOW_CONDENSED_MEDIUM; 
	
	
	
	public Font getBoldFont( final int iSize ) {
		if ( !mapFontBold.containsKey( iSize ) ) {
		    final FontData fd = display.getSystemFont().getFontData()[0];
		    fd.setHeight( iSize );
		    fd.setStyle( SWT.BOLD );
			final Font font = new Font( display, fd );
			mapFontBold.put( iSize, font );
		}
		return mapFontBold.get( iSize );
	}
	
	
	
	
}
