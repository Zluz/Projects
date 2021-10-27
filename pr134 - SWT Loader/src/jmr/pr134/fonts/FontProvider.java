package jmr.pr134.fonts;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class FontProvider {


	public final static String FONT_BASE_PATH; 

	static {
		if ( '/' == File.separatorChar ) {
			FONT_BASE_PATH = "/Share/Resources/fonts/truetype/";
		} else {
			FONT_BASE_PATH = "T:\\Resources\\fonts\\truetype\\";
		}
	}


	public final static String[] FONT_BASE_PATHS = {
			"/Local/Resources/fonts/truetype/",
			"/Share/Resources/fonts/truetype/",
			"T:\\Resources\\fonts\\truetype\\",
			"\\\\h04\\Share\\Resources\\fonts\\truetype",
			"\\\\192.168.6.223\\Share\\Resources\\fonts\\truetype",
	}; 


	
	public static enum FontResource {
		
		ROBOTO_CONDENSED_REGULAR( 		"Roboto Condensed", 
				"-",	"RobotoCondensed-Regular.ttf" ),
		ROBOTO_CONDENSED_LIGHT( 		"Roboto Condensed Light", 
				"-",	"RobotoCondensed-Light.ttf" ),
		ROBOTO_CONDENSED_BOLD( 			"Roboto Condensed Bold", 
				"-",	"RobotoCondensed-Bold.ttf" ),

		FJALLAONE_REGULAR( 				"FjallaOne Regular", 
				"K",	"FjallaOne-Regular.ttf" ),

		ARCHIVO_NARROW( 				"Archivo Narrow", 
				"-",	"ArchivoNarrow-Regular.ttf" ),
		
		MILFORD( 						"Milford", 
				"-",	"MILF____.ttf" ),
		
		PLAY_REGULAR( 					"Play",
				"-",	"Play-Regular.ttf" ),
		
		// good at very small sizes (ie, micro display)
		// WARNING: variable width numbers
		CABIN_CONDENSED( 				"Cabin Condensed", 
				"NK",	"CabinCondensed-Regular.ttf" ),

		BARLOW_CONDENSED_MEDIUM(		"Barlow Condensed Medium", 
				"NK",	"BarlowCondensed-Medium.ttf" ),
		
		MERRIWEATHER_SANS( 				"Merriweather Sans", 
				"N",	"MerriweatherSans-Regular.ttf" ),

		// NOTE: there are 9 other variation font files
		SAIRA_SEMI_CONDENSED_MEDIUM(	"Saira SemiCondensed Medium", 
				"N",	"SairaSemiCondensed-Medium.ttf" ),
		SAIRA_SEMI_CONDENSED_SEMIBOLD(	"Saira SemiCondensed SemiBold", 
				"N",	"SairaSemiCondensed-SemiBold.ttf" ),

		
		// organize..

		OVERPASS_REGULAR( 				"Overpass",
				"N",	"Overpass_Regular.ttf" ),

		DYNO_REGULAR( 					"Dyno Regular",
				"N",	"Dyno Regular.ttf" ),

		;
		
		private final String strFontName;
		private final String strFilename;
		private final boolean bVariablePitchNumbers;
		private final boolean bKerning;

		/**
		 * For strOptions:
		 * 		N - Numeric not fixed pitch (Digits have different widths)
		 *		K - Kerning (characters apply kerning generously) 
		 *
		 * @param strFontName
		 * @param strOptions
		 * @param strFilename
		 */
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
		ThFont.initialize( this );
	}
	
	
	public static String getFontKey(	final FontResource fontres,
										final int iSize,
										final int iAttrs ) {
		final String strKey = 
					"" + fontres.ordinal() + "/" + iSize + "/" + iAttrs;
		return strKey;
	}
	
	
	public String findFontFile( final FontResource fontres ) {
		for ( final String strPath : FONT_BASE_PATHS ) {
			final String strFile = strPath + fontres.strFilename;
			final File file = new File( strFile );
			if ( file.canRead() ) {
				return strFile;
			}
		}
		
		System.err.println( "Failed to find font file for \"" 
					+ fontres.getName() + "\", expected filename: " 
					+ fontres.strFilename );
		System.err.println( "Searched in these paths:" );
		for ( final String strPath : FONT_BASE_PATHS ) {
			System.err.println( "\t" + strPath );
		}
		
		return null;
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
				
				final String strFilename = findFontFile( fontres );
				boolean bLoaded = false;
				
				if ( null == strFilename ) return null;
				if ( strFilename.isEmpty() ) return null;
				
				try {
					bLoaded = device.loadFont( strFilename );
				} catch ( final Throwable t ) {
					// have seen a weird error.. (gone after reboot)
					/*
The program 'SWT' received an X Window System error.
This probably reflects a bug in the program.
The error was 'BadAccess (attempt to access private resource denied)'.
  (Details: serial 459 error_code 10 request_code 130 minor_code 1)
  (Note to programmers: normally, X errors are reported asynchronously;
   that is, you will receive the error a while after causing it.
   To debug your program, run it with the --sync command line
   option to change this behavior. You can then get a meaningful
   backtrace from your debugger if you break on the gdk_x_error() function.)
					 */
					t.printStackTrace();
					bLoaded = false;
				}

				if ( bLoaded ) { 
					System.out.println( "Font loaded: " + strFilename );
				} else { // is this fatal? .. will fall-back
					System.err.println( "Failed to load font: " + strFilename );
				}
				setLoadedFiles.add( fontres );
			}
			
			final String strFontName = fontres.strFontName;
			final Font font = new Font( device, strFontName, iSize, iAttrs );
			
//			final String strNameCheck = font.getFontData()[0].getName();
			
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
	
	
	public List<String> getFontList() {
//		final List<String> list = new LinkedList<>();
		final Set<String> set = new HashSet<>();
		final FontData[] arrFonts = device.getFontList( null, true );
		for ( final FontData fd : arrFonts ) {
//			list.add( "\"" + fd.getName() + "\"  - "
//								+ "style: " + fd.getStyle() + ", "
//								+ "height: " + fd.getHeight() + ", "
//								+ "locale: " + fd.getLocale() 
//								);
			set.add( fd.getName() );
		}
		final List<String> list = new LinkedList<>( set );
		Collections.sort( list );
		return list;
	}
	
}
