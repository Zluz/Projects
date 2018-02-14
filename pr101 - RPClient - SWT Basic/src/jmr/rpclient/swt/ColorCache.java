package jmr.rpclient.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorCache {

	public final static Map<RGB,Color> CACHE = new HashMap<>();
	
	public static Color getColor( final RGB rgb ) {
		if ( CACHE.containsKey( rgb ) ) {
			final Color color = CACHE.get( rgb );
			return color;
		} else {
			final Color color = new Color( UI.display, rgb );
			CACHE.put( rgb, color );
			System.out.println( "ColorCache.CACHE.size() = " + CACHE.size() );
			return color;
		}
	}
	
	private static int safe( final int iValue ) {
		return Math.max( Math.min( iValue, 255 ), 0 );
	}
	
	public static Color getColor( 	final int iR,
									final int iG,
									final int iB ) {
		try {
//			final RGB rgb = new RGB( iR, iG, iB );
			final RGB rgb = new RGB( safe( iR ), safe( iG ), safe( iB ) );
			final Color color = getColor( rgb );
			return color;
		} catch ( final IllegalArgumentException e ) {
			System.err.println( e.toString() + " encountered." );
			System.err.println( "( R=" + iR + ", G=" + iG + ", B=" + iB + " )" );
			e.printStackTrace();
			return Display.getDefault().getSystemColor( SWT.COLOR_MAGENTA );
		}
	}
	
	public static Color getGray( final int iBright ) {
		final Color color = getColor( iBright, iBright, iBright );
		return color;
	}
	
}
