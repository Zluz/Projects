package jmr.rpclient.tiles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.screen.TextScreen;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;

public class ClockTile extends TileBase {


	final public static String[] TIME_FORMATS = { 
									"h:mm:ss",
									"aa",
									"yyyy-MM-dd",
									"EEEE, MMMM d",
									"SSS", 
									"h:mm",
									};
	
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

	

	
	private String[] getTimeStrings() {
		final Date now = new Date();
		
//		final String strTime = FORMATTER_SHORT.format( now );
//		drawTextCentered( strTime, 10 );
		
		

		final String[] strTimes = new String[ TIME_FORMATS.length ];
//		for ( final SimpleDateFormat formatter : FORMATTERS ) {
		for ( int i=0; i<TIME_FORMATS.length; i++ ) {
			strTimes[i] = FORMATTERS[i].format( now );
		}
		
		return strTimes;
	}
	
	
	@Override
	public void paint( final TextScreen screen ) {
		final String[] strTimes = getTimeStrings();
		screen.print( 1, 1, strTimes[3] );
	}

	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {

		final String[] strTimes = getTimeStrings();
		final int iWidth = gc.getClipping().width;


		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

		

		
		String strTimeFull = strTimes[0]; // normal time w/seconds
		if ( strTimeFull.charAt( 1 ) == ':' ) {
			strTimeFull = " " + strTimeFull;
		}
		final String strTimeMin = strTimeFull.substring( 0, 5 );
		final String strTimeSec = strTimeFull.substring( 5 );


		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		
//		gc.setFont( Theme.get().getNFont( 66 ) );
		gc.setFont( Theme.ThFont._66_RC.getFont() );
		
		gc.drawText( strTimeMin, 22, 1, true );
		
		gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
		gc.drawText( strTimeMin, 21, 0, true );
		final int iMinutesWidth = gc.textExtent( strTimeMin ).x;

		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

//		gc.setFont( Theme.get().getNFont( 50 ) );
		gc.setFont( Theme.ThFont._50_RC.getFont() );
		
		gc.drawText( strTimeSec, 26 + iMinutesWidth, 14, true );

		
		if ( iWidth > 300 ) { // 3 tiles wide
	
//			gc.setFont( Theme.get().getFont( 25 ) ); 
			gc.setFont( Theme.ThFont._25_SSCM_V.getFont() );
			gc.drawText( strTimes[1], iMinutesWidth + 126, 20 ); // AM/PM

//			gc.setFont( Theme.get().getNFont( 15 ) );
			gc.setFont( Theme.ThFont._15_RC.getFont() );
			gc.drawText( "." + strTimes[4], iMinutesWidth + 114, 60 ); // ms
		}

		gc.setFont( Theme.ThFont._25_SSCM_V.getFont() );
		if ( gc.textExtent( strTimes[3] ).x > iWidth ) {
			gc.setFont( Theme.ThFont._25_BCM_V.getFont() );
		}
		drawTextCentered( strTimes[3], 100, 24, 30 ); // long date
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
