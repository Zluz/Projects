package jmr.rpclient.swt;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import jmr.pr134.fonts.FontProvider;
import jmr.pr134.fonts.FontProvider.FontResource;

public class Theme {
	
	public enum Colors {
		BACKGROUND( 0,0,0 ),
		LINE_FAINT( 0,0,70 ),
		LINE_BOLD( 255, 253, 250 ),
		TEXT( 190, 190, 190 ),
		TEXT_LIGHT( 140,140,140 ),
		TEXT_BOLD( 255, 253, 250 ),
		
		BACKGROUND_FLASH_ALERT_ALT( 60, 0, 0 ),
		BACKGROUND_FLASH_ALERT( 120, 0, 0, BACKGROUND_FLASH_ALERT_ALT ),
		;
		
		final RGB rgb;
		final Colors colorAltCycle;
		
		private Colors(	final int r, 
						final int g, 
						final int b,
						final Colors colorAlt ) {
			this.rgb = new RGB( r,g,b );
			this.colorAltCycle = colorAlt;
		}
		
		private Colors(	final int r, 
						final int g, 
						final int b ) {
			this( r, g, b, null );
		}
		
		public Colors getAlternateCycleColor() {
			return this.colorAltCycle;
		}
		
		public boolean isCycled() {
			return null != this.colorAltCycle;
		}
	}
	
	
	public enum ThFont {
		
		// general purpose large fonts (fixed digits)
		_66_RC( FontResource.ROBOTO_CONDENSED_REGULAR ),
		_50_RC( FontResource.ROBOTO_CONDENSED_REGULAR ),
		_15_RC( FontResource.ROBOTO_CONDENSED_REGULAR ),
//		_12_RCB( FontResource.ROBOTO_CONDENSED_BOLD, SWT.BOLD ),
		_12_RCB( FontResource.ROBOTO_CONDENSED_BOLD ),

		// good general purpose font (fixed digits)
		_25_PR( FontResource.PLAY_REGULAR ),

		// good general purpose font (fixed digits)
		_12_M_B( FontResource.MILFORD, SWT.BOLD ),

		_15_CC( FontResource.CABIN_CONDENSED ),

		// narrow font, but variable digits
		// 			not great below 9
		_9_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
		_10_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
		_11_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
		_16_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
		_18_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
		_25_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
		
		_11_SSCM_V_B( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM, SWT.BOLD ),

		_9_SSCSB_V( FontResource.SAIRA_SEMI_CONDENSED_SEMIBOLD ),
		_18_SSCSB_V( FontResource.SAIRA_SEMI_CONDENSED_SEMIBOLD ),
		_12_SSCSB_V( FontResource.SAIRA_SEMI_CONDENSED_SEMIBOLD ),
		_14_SSCSB_V( FontResource.SAIRA_SEMI_CONDENSED_SEMIBOLD ),
		
		// very narrow font, but variable digits 
		_12_BCM_V( FontResource.BARLOW_CONDENSED_MEDIUM ),
		_25_BCM_V( FontResource.BARLOW_CONDENSED_MEDIUM ),
		
		// seems good with small sizes
		_7_O_V( FontResource.OVERPASS_REGULAR ),

		;
		
		
		final FontResource fr;
		final int iSize;
		final int iStyle;
		
		static FontProvider provider;
		
		
		ThFont( final FontResource fr,
				final int iStyle ) {
			this.fr = fr;
			this.iStyle = iStyle;
			final int iPos = this.name().indexOf( '_', 1 );
			final String strSize = this.name().substring( 1, iPos );
			try {
				this.iSize = Integer.valueOf( strSize );
			} catch ( final NumberFormatException e ) {
				throw new IllegalStateException( 
								"Bad ThFont name: " + this.name() );
			}
		}

		ThFont(	final FontResource fr ) {
			this( fr, SWT.NORMAL );
		}
		
		public FontResource getFontResource() {
			return this.fr;
		}
		
		public int getSize() {
			return this.iSize;
		}
		
		public static void initialize( final FontProvider provider ) {
			ThFont.provider = provider;
		}
		
		public Font getFont() {
			return provider.get( getFontResource(), getSize(), this.iStyle );
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
		ThFont.initialize( fontprovider );
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
			case BACKGROUND_FLASH_ALERT		
								: iColor = SWT.COLOR_DARK_RED; break;
			default				: iColor = SWT.COLOR_GRAY; break;
		}
		return display.getSystemColor( iColor );
	}
	

	public Color getColor( Colors color ) {

		final Colors colorSafe;
		
		if ( null != color ) {
			if ( color.isCycled() ) {
				final boolean bAlertCycle = 
						Math.floor(System.currentTimeMillis()/500) % 2 == 0;
				if ( bAlertCycle ) {
					colorSafe = color;
				} else {
					colorSafe = color.colorAltCycle;
				}
			} else {
				colorSafe = color;
			}
		} else {
			colorSafe = Colors.TEXT_BOLD;
		}
		return COLORMAP.get( colorSafe );
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
	public Font getFont(	final int iSize,
							final boolean bNumberCompat ) {
		if ( !mapFontNormal.containsKey( iSize ) ) {
			final FontResource fr;
			if ( iSize < 6 ) {
//				fr = FontResource.CABIN_CONDENSED;
				fr = FontResource.SAIRA_SEMI_CONDENSED_MEDIUM;
			} else if ( iSize > 14 ){
				if ( bNumberCompat ) {
					fr = FontResource.ROBOTO_CONDENSED_BOLD;
				} else {
					fr = FontResource.BARLOW_CONDENSED_MEDIUM;
				}
			} else {
				fr = FontResource.ARCHIVO_NARROW;
			}
			final Font font = fontprovider.get( fr, iSize, SWT.NORMAL );
			mapFontNormal.put( iSize, font );
		}
		return mapFontNormal.get( iSize );
	}

	public Font getFont( final int iSize ) {
		return getFont( iSize, false );
	}

	public Font getNFont( final int iSize ) {
		return getFont( iSize, true );
	}
	
	public Font getFont( final ThFont font ) {
		return fontprovider.get( 
						font.getFontResource(), 
						font.getSize(),
						SWT.NONE );
	}

	
	
	public Font getBoldFont( final int iSize ) {
		if ( !mapFontBold.containsKey( iSize ) ) {
			final Font font = fontprovider.get(
							FontResource.ROBOTO_CONDENSED_BOLD, iSize, SWT.BOLD );
			mapFontBold.put( iSize, font );
		}
		return mapFontBold.get( iSize );
	}
	
	
}
