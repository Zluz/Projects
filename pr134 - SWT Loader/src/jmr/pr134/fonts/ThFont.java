package jmr.pr134.fonts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import jmr.pr134.fonts.FontProvider.FontResource;

/**
 * "Theme Font"; intended to be packed into a theme.
 * In the future this will include color handling and maybe some basic icons.
 * This code was migrated from pr101:jmr.rpclient.swt.Theme.ThFont.
 */
public enum ThFont {

	// general purpose large fonts (fixed digits)
	_66_RC( FontResource.ROBOTO_CONDENSED_REGULAR ),
	_50_RC( FontResource.ROBOTO_CONDENSED_REGULAR ),
	_15_RC( FontResource.ROBOTO_CONDENSED_REGULAR ),
//	_12_RCB( FontResource.ROBOTO_CONDENSED_BOLD, SWT.BOLD ),
	_12_RCB( FontResource.ROBOTO_CONDENSED_BOLD ),

	// good general purpose font (fixed digits)
	_18_PR( FontResource.PLAY_REGULAR ),
	_20_PR( FontResource.PLAY_REGULAR ),
	_25_PR( FontResource.PLAY_REGULAR ),

	// good general purpose font (fixed digits)
	_12_M_B( FontResource.MILFORD, SWT.BOLD ),
	_18_M_B( FontResource.MILFORD, SWT.BOLD ),

	_18_AN_B( FontResource.ARCHIVO_NARROW, SWT.BOLD ),
	_20_AN_B( FontResource.ARCHIVO_NARROW, SWT.BOLD ),

	// meh font, but good when smell; used for tesla overhead
	_15_CC_V_B( FontResource.CABIN_CONDENSED, SWT.BOLD ),

	// narrow font, but variable digits
	// 			not great below 9
	_9_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	_10_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	_11_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	_16_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	_18_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	_20_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	_25_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	_36_SSCM_V( FontResource.SAIRA_SEMI_CONDENSED_MEDIUM ),
	
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
