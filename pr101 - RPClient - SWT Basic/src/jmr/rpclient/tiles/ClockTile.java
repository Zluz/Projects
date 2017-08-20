package jmr.rpclient.tiles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ClockTile implements Tile {


	final public static String DATE_FORMAT_SHORT = "HH:mm:ss.SSS";
	final public static SimpleDateFormat FORMATTER_SHORT;
	
	final public static TimeZone 
				TIMEZONE = TimeZone.getTimeZone( "US/Eastern" );

//	final public static String DATE_FORMAT_LONG = "yyyy-MM-dd  E \nHH:mm:ss";
	final public static String DATE_FORMAT_LONG = 
						"yyyy-MM-dd   HH:mm:ss\n"
						+ "EEEE, MMMM d";
	final public static SimpleDateFormat FORMATTER_LONG;

	static {
		FORMATTER_SHORT = new SimpleDateFormat( DATE_FORMAT_SHORT );
		FORMATTER_LONG = new SimpleDateFormat( DATE_FORMAT_LONG );
		FORMATTER_SHORT.setTimeZone( TIMEZONE );
		FORMATTER_LONG.setTimeZone( TIMEZONE );
	}

	
	
	
	public void paint(	final GC gc,
						final Rectangle rect ) {
		final Date now = new Date();
		
		final int iXC = rect.x * 150 + rect.width * 150 / 2;
		final int iYC = rect.y * 150 + rect.height * 150 / 2;

		final String strTime = FORMATTER_SHORT.format( now );

		int iSize = 200;
		Point ptTest;
		do {
			iSize = iSize - 10;
			gc.setFont( Theme.get().getFont( iSize ) );
			ptTest = gc.textExtent( strTime );
		} while ( rect.width * 150 < ptTest.x );
		
		gc.setFont( Theme.get().getFont( iSize ) );
		final Point ptExtent = gc.textExtent( strTime );
		
		final int iX = iXC - ( ptExtent.x / 2 );
		final int iY = iYC - ( ptExtent.y / 2 );
		gc.drawText( strTime, iX, iY );
	}
		
	
	@Override
	public MouseListener getMouseListener() {
		return null;
	}

	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		final Date now = new Date();
		
		final Rectangle rect = image.getBounds();

//		final GC gc = new GC( imageBuffer );
//		gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
//		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
//		gc.fillRectangle( imageBuffer.getBounds() );

		final int iXC = rect.x + rect.width / 2;
		final int iYC = rect.y + rect.height / 2;

		final String strTime = FORMATTER_SHORT.format( now );

		int iSize = 200;
		Point ptTest;
		do {
			iSize = iSize - 10;
			gc.setFont( Theme.get().getFont( iSize ) );
			ptTest = gc.textExtent( strTime );
		} while ( rect.width < ptTest.x );
		
		gc.setFont( Theme.get().getFont( iSize ) );
		final Point ptExtent = gc.textExtent( strTime );
		
		final int iX = iXC - ( ptExtent.x / 2 );
		final int iY = iYC - ( ptExtent.y / 2 );
		gc.drawText( strTime, iX, iY );
	}

}
