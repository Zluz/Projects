package jmr.pr134.fonts;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

		ROBOTO_CONDENSED( 				"Roboto Condensed", 
				"-",	"RobotoCondensed-Regular.ttf" ),
		ROBOTO_CONDENSED_LIGHT( 		"Roboto Condensed Light", 
				"-",	"RobotoCondensed-Light.ttf" ),

		FJALLAONE_REGULAR( 				"FjallaOne Regular", 
				"K",	"FjallaOne-Regular.ttf" ),

		ARCHIVO_NARROW( 				"Archivo Narrow", 
				"-",	"ArchivoNarrow-Regular.ttf" ),
		
		MILFORD( 						"Milford", 
				"-",	"MILF____.ttf" ),
		
		// good at very small sizes (ie, micro display)
		// WARNING: variable width numbers
		CABIN_CONDENSED( 				"Cabin Condensed", 
				"NK",	"CabinCondensed-Regular.ttf" ),

		BARLOW_CONDENSED_MEDIUM(		"Barlow Condensed Medium", 
				"NK",	"BarlowCondensed-Medium.ttf" ),
		
		MERRIWEATHER_SANS( 				"Merriweather Sans", 
				"N",	"MerriweatherSans-Regular.ttf" ),

		;
		
		private final String strFontName;
		private final String strFilename;
		private final boolean bVariablePitchNumbers;
		private final boolean bKerning;
		
		private FontResource( 	final String strFontName,
								final String strOptions,
								final String strFilename ) {
			this.strFontName = strFontName;
			this.strFilename = strFilename;
			
			if ( strOptions.indexOf( 'N' ) > -1 ) {
				bVariablePitchNumbers = true;
			} else {
				bVariablePitchNumbers = false;
			}
			
			if ( strOptions.indexOf( 'K' ) > -1 ) {
				bKerning = true;
			} else {
				bKerning = false;
			}
		}
		
		public String getName() {
			return this.strFontName;
		}
	}
	
//	private static final Map<String,Font> FONT_CACHE = new HashMap<>();
	private final Map<String,Font> mapFontCache = new HashMap<>();
	
	private final Set<FontResource> setLoadedFiles = new HashSet<>();
	
	public final Device device;


	public FontProvider(	final Device device ) {
		this.device = device;
	}
	
	
	public static String getFontKey(	final FontResource fontres,
										final int iSize,
										final int iAttrs ) {
		final String strKey = 
					"" + fontres.ordinal() + "/" + iSize + "/" + iAttrs;
		return strKey;
	}

	public Font get(	final FontResource fontres,
						final int iSize,
						final int iAttrs ) {
		if ( null != fontres ) {
			
			final String strKey = getFontKey( fontres, iSize, iAttrs );
			if ( mapFontCache.containsKey( strKey ) ) {
				return mapFontCache.get( strKey );
			}
			
//			final FontData[] arrFonts = display.getFontList( null, true );
//			System.out.println( "Available fonts (" + arrFonts.length + "):" );
//			for ( final FontData fd : arrFonts ) {
//				System.out.println( "\t" + fd.getName() );
//			}

			if ( ! setLoadedFiles.contains( fontres ) ) {
				final String strFilename = FONT_BASE_PATH + fontres.strFilename;
				final boolean bLoaded = device.loadFont( strFilename );
				if ( bLoaded ) { 
					System.out.println( "Font loaded: " + strFilename );
				} else { // is this fatal? .. will fall-back
					System.err.println( "Failed to load font: " + strFilename );
				}
				setLoadedFiles.add( fontres );
			}
			
			final String strFontName = fontres.strFontName;
			final Font font = new Font( device, strFontName, iSize, iAttrs );
			
			mapFontCache.put( strKey, font );
			
			return font;
		}
		

		// if all else fails, return the system font
		final Font fontSystem = device.getSystemFont();
		return fontSystem;
	}

	
	public Font get( final FontResource fontres ) {
		return get( fontres, 12, SWT.NORMAL );
	}
	
	
}
