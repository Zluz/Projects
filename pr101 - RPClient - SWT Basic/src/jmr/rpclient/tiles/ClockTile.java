package jmr.rpclient.tiles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;

public class ClockTile extends TileBase {


	final public static String[] TIME_FORMATS = { 
									"h:mm:ss",
									"aa",
									"yyyy-MM-dd",
									"EEEE, MMMM d",
									"SSS" };
	
	final public static String DATE_FORMAT_SHORT = "HH:mm:ss.SSS";
	final public static SimpleDateFormat FORMATTER_SHORT;

	final public static SimpleDateFormat[] FORMATTERS;
	
	final public static TimeZone 
				TIMEZONE = TimeZone.getTimeZone( "US/Eastern" );

	static {
		FORMATTER_SHORT = new SimpleDateFormat( DATE_FORMAT_SHORT );

		FORMATTERS = new SimpleDateFormat[ TIME_FORMATS.length ];
//		for ( final String strFormat : TIME_FORMATS ) {
		for ( int i=0; i<TIME_FORMATS.length; i++ ) {
			FORMATTERS[i] = new SimpleDateFormat( TIME_FORMATS[i] );
			FORMATTERS[i].setTimeZone( TIMEZONE );
		}
	}

	
	

	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		final Date now = new Date();
		
//		final String strTime = FORMATTER_SHORT.format( now );
//		drawTextCentered( strTime, 10 );
		
		

		final String[] strTimes = new String[ TIME_FORMATS.length ];
//		for ( final SimpleDateFormat formatter : FORMATTERS ) {
		for ( int i=0; i<TIME_FORMATS.length; i++ ) {
			strTimes[i] = FORMATTERS[i].format( now );
		}
		
//		gc.fillRectangle( rect );

		gc.setFont( Theme.get().getFont( 25 ) );
		gc.drawText( strTimes[1], 385, 25 );

		gc.setFont( Theme.get().getFont( 25 ) );
		drawTextCentered( strTimes[3], 100, 24, 30 );

		gc.setFont( Theme.get().getFont( 15 ) );
		gc.drawText( "." + strTimes[4], 380, 65 );

		
		String strTimeSec = strTimes[0];
		if ( strTimeSec.charAt( 1 ) == ':' ) {
			strTimeSec = " " + strTimeSec;
		}
		gc.setFont( Theme.get().getFont( 66 ) );
		gc.drawText( strTimeSec, 31, 0 );
		
		String strTimeMin = strTimeSec.substring( 0, 5 );
//		if ( strTime.charAt( 4 ) == ':' ) {
//			strTime = strTime.substring( 0, 4 );
//		}
//		if ( strTime.charAt(0) == '0' ) {
//			strTime = "0" + strTime.substring( 1 );
//		}
		gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
		gc.drawText( strTimeMin, 31, 0 );

	}
	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
