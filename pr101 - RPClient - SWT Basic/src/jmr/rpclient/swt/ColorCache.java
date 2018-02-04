package jmr.rpclient.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

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
	
	public static Color getColor( 	final int iR,
									final int iG,
									final int iB ) {
		final RGB rgb = new RGB( iR, iG, iB );
		final Color color = getColor( rgb );
		return color;
	}
	
	public static Color getGray( final int iBright ) {
		final Color color = getColor( iBright, iBright, iBright );
		return color;
	}
	
}
