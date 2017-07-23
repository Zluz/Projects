package jmr.rpclient;

import java.io.IOException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

public class RPiTouchscreen {
	
/*
Tue Jul 18 03:54:19 UTC 2017
Started.
Display (bounds): (size:800,480 loc:0,0)
Display (client): (size:800,480 loc:0,0)
Display color depth: 24
Display DPI: Point {96, 96}
Primary screen (bounds): (size:800,480 loc:0,0)
Primary screen (client): (size:800,444 loc:0,0)
Display is RPi touchscreen
*/

	private static RPiTouchscreen instance = null;
	
	private final static Display display = Display.getDefault();
	
	private final boolean isEnabled;
	
	
	private RPiTouchscreen() {
		this.isEnabled = isRPiTouchscreen( display );
	}
	
	
	private static boolean isRPiTouchscreen( final Display display ) {
		if ( null==display ) return false;
		
		if ( 24 != display.getDepth() ) return false;
		
//		if ( !display.getTouchEnabled() ) return false;
		
		final Point dpi = display.getDPI();
		if ( 96 != dpi.x ) return false;
		if ( 96 != dpi.y ) return false;
		
		final Monitor monitor = display.getPrimaryMonitor();
		if ( null==monitor ) return false;
		
		final Rectangle bounds = monitor.getBounds();
		if ( 800 != bounds.width ) return false;
		if ( 480 != bounds.height ) return false;
		
		return true;
	}
	
	
	public static RPiTouchscreen getInstance() {
		if ( null==instance ) {
			instance = new RPiTouchscreen();
		}
		return instance;
	}

	
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	
	public void setBrightness( final int iBrightness ) {
		if ( !this.isEnabled() ) return;
		
		SWTBasic.log( "Setting brightness to " + iBrightness );
		
		final String strCommand = 
				"echo " + iBrightness + " > "
						+ "/sys/class/backlight/rpi_backlight/brightness";
		try {
			SWTBasic.log( "Running command:\n" + strCommand );
			final Process proc = Runtime.getRuntime().exec( strCommand );
			Thread.sleep( 500 );
//			proc.waitFor( 1, TimeUnit.SECONDS );
			final int iExitValue = proc.exitValue();
			SWTBasic.log( "Exit value: " + iExitValue );
		} catch ( final InterruptedException | IOException e ) {
			SWTBasic.log( e.toString() );
//			e.printStackTrace();
		}
	}
	
	
}
