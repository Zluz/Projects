package jmr.pr136;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;

public class FontProvider {


	public final static String FONT_BASE_PATH; 

	static {
		if ( '/' == File.separatorChar ) {
			FONT_BASE_PATH = "/Share/Resources/fonts/truetype/";
		} else {
			FONT_BASE_PATH = "T:\\Resources\\fonts\\truetype\\";
		}
	}
	
	
	public static enum FontResource {
		
		// good at very small sizes (ie, micro display)
		CABIN_CONDENSED( 				"Cabin Condensed", 
				"CabinCondensed-Regular.ttf" ),

		ROBOTO_CONDENSED( 				"Roboto Condensed", 
				"RobotoCondensed-Regular.ttf" ),
		ROBOTO_CONDENSED_LIGHT( 		"Roboto Condensed Light", 
				"RobotoCondensed-Light.ttf" ),
		BARLOW_CONDENSED_MEDIUM(		"Barlow Condensed Medium", 
				"BarlowCondensed-Medium.ttf" ),
		
		MILFORD( 						"Milford", 
				"MILF____.ttf" ),
		
		ARCHIVO_NARROW( 				"Archivo Narrow", 
				"ArchivoNarrow-Regular.ttf" ),
		
		MERRIWEATHER_SANS( 				"Merriweather Sans", 
				"MerriweatherSans-Regular.ttf" ),
		;
		
		private final String strFontName;
		private final String strFilename;
		
		private FontResource( 	final String strFontName,
								final String strFilename ) {
			this.strFontName = strFontName;
			this.strFilename = strFilename;
		}
		
	}
	
//	private static final Map<String,Font> FONT_CACHE = new HashMap<>();
	private final Map<String,Font> FONT_CACHE = new HashMap<>();
	
	
	
	public static String getFontKey(	final FontResource fontres,
										final int iSize,
										final int iAttrs ) {
		final String strKey = 
					"" + fontres.ordinal() + "/" + iSize + "/" + iAttrs;
		return strKey;
	}

	public Font get(	final Device device,
							final FontResource fontres,
							final int iSize,
							final int iAttrs ) {
		if ( null != fontres ) {
			
			final String strKey = getFontKey( fontres, iSize, iAttrs );
			if ( FONT_CACHE.containsKey( strKey ) ) {
				return FONT_CACHE.get( strKey );
			}
			
//			final FontData[] arrFonts = display.getFontList( null, true );
//			System.out.println( "Available fonts (" + arrFonts.length + "):" );
//			for ( final FontData fd : arrFonts ) {
//				System.out.println( "\t" + fd.getName() );
//			}

			final String strFilename = FONT_BASE_PATH + fontres.strFilename;
			final boolean bLoaded = device.loadFont( strFilename );
			if ( bLoaded ) { 
				System.out.println( "Font loaded: " + strFilename );
			} else { // would this happen if the file is already loaded?
				System.out.println( "Failed to load font: " + strFilename );
			}
			final String strFontName = fontres.strFontName;
			final Font font = new Font( device, strFontName, iSize, iAttrs );
			
			FONT_CACHE.put( strKey, font );
			
			return font;
		}
		

		// if all else fails, return the system font
		final Font fontSystem = device.getSystemFont();
		return fontSystem;
	}

	
	public Font get(	final Device device,
							final FontResource fontres ) {
		return get( device, fontres, 12, SWT.NORMAL );
	}
	
	
}
