package jmr.rpclient.swt;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import jmr.s2db.Client;
import jmr.util.SystemUtil;

public abstract class UI {

	final public static Display display = new Display();

	public static final int REFRESH_SLEEP = 100;
	
	public static final long UI_LOOP_DELAY_WARN = 500;
	public static final long UI_LOOP_DELAY_ABORT = TimeUnit.SECONDS.toMillis( 20 );
	
	
	final public static Color COLOR_WHITE = 
					display.getSystemColor( SWT.COLOR_WHITE );
	
	final public static Color COLOR_BLACK = 
					display.getSystemColor( SWT.COLOR_BLACK );
	
	final public static Color COLOR_GREEN = 
					display.getSystemColor( SWT.COLOR_GREEN );

	final public static Color COLOR_BLUE = 
					display.getSystemColor( SWT.COLOR_BLUE );

	final public static Color COLOR_DARK_BLUE = 
					display.getSystemColor( SWT.COLOR_DARK_BLUE );

	final public static Color COLOR_RED = 
					display.getSystemColor( SWT.COLOR_RED );

	final public static Color COLOR_GRAY = 
					display.getSystemColor( SWT.COLOR_GRAY );
		
	final public static Color COLOR_DARK_GRAY = 
//					display.getSystemColor( SWT.COLOR_DARK_GRAY );
					new Color( display, 30, 30, 30 );

  

    
	public static final Font FONT_HUGE;
	public static final Font FONT_LARGE;
	public static final Font FONT_MEDIUM;
	public static final Font FONT_SMALL;

	
    static {
    	final FontData fdHuge = display.getSystemFont().getFontData()[0];
        fdHuge.setHeight( 140 );
    	FONT_HUGE = new Font( display, fdHuge );

    	final FontData fdLarge = display.getSystemFont().getFontData()[0];
        fdLarge.setHeight( 40 );
    	FONT_LARGE = new Font( display, fdLarge );

    	final FontData fdMedium = display.getSystemFont().getFontData()[0];
        fdMedium.setHeight( 25 );
    	FONT_MEDIUM = new Font( display, fdMedium );

    	final FontData fdSmall = display.getSystemFont().getFontData()[0];
        fdSmall.setHeight( 15 );
    	FONT_SMALL = new Font( display, fdSmall );
    }
	

	final private static Long[] lLastUpdate = { System.currentTimeMillis() };

	
    public static void runUIWatchdog() {
		lLastUpdate[0] = System.currentTimeMillis();
		final Thread threadUIWatchdog = new Thread( "UI Watchdog" ) {
			@Override
			public void run() {
				try {
					while ( !UI.display.isDisposed() ) {
						final long lNow = System.currentTimeMillis();
						final long lElapsed = lNow - lLastUpdate[0];
						if ( lElapsed > UI_LOOP_DELAY_ABORT ) {
							emergencyShutdown( "UI thread unresponsive" );
						} else if ( lElapsed > UI_LOOP_DELAY_WARN ) {
							printUIStack( "UI thread is less responsive"
									+ " (" + lElapsed + "ms elapsed)." );
						}
						Thread.sleep( 2000 );
					}
				} catch ( final InterruptedException e ) {
					emergencyShutdown( "UI watchdog thread interrupted" );
				}
			}
		};
		threadUIWatchdog.start();
    }
    
    
    public static void printUIStack( final String strMessage ) {
    	System.out.println( strMessage );
		final StackTraceElement[] stack = 
				UI.display.getThread().getStackTrace();
		for ( final StackTraceElement frame : stack ) {
			System.out.println( "\t" + frame.toString() );
		}
    }
    
    public static void emergencyShutdown( final String strReason ) {
    	System.out.println();
		System.err.println( "Emergency shutdown" );
		System.err.println( strReason );
		
		new Thread( "Closing S2 client" ) {
			@Override
			public void run() {
				Client.get().close();
			}
		}.start();

		new Thread( "Attempting to close UI" ) {
			@Override
			public void run() {
				if ( !display.isDisposed() ) {
					display.asyncExec( new Runnable() {
						@Override
						public void run() {
							if ( !display.isDisposed() ) {
								display.close();
							}
						}
					} );
				}
			}
		}.start();
		
		new Thread( "Printing UI stack" ) {
			@Override
			public void run() {
				printUIStack( "UI Stack:" );
			}
		}.start();

		try {
			Thread.sleep( 2000 );
		} catch ( final InterruptedException e ) {
			e.printStackTrace();
		}
		
//		System.out.println( "Calling System.exit(1000).." );
//		System.exit( 1000 );
		SystemUtil.shutdown( 1000, strReason );
    }
    
    
    public static final List<Canvas> listRefreshCanvases = new LinkedList<>();
    
    
    public static void runUIRefresh() {
    	final Thread threadUIRefresh = new Thread( "UI Refresh" ) {
    		@Override
    		public void run() {
				try {
					while ( !display.isDisposed() ) {
						display.asyncExec( new Runnable() {
							@Override
							public void run() {
								try {
									// invalidate canvases (queue to repaint)
									for ( final Canvas canvas : listRefreshCanvases ) {
										if ( !canvas.isDisposed() ) {
											canvas.redraw();
										}
									}
								} catch ( final Throwable t ) {
									System.err.println( 
											"ERROR encountered on the "
											+ "UI thread, aborting." );
									t.printStackTrace();
									SystemUtil.shutdown( 1100, 
											"Exception on the UI thread" );
//									display.close();
								}
								UI.notifyUIIdle();
							}
						});
						Thread.sleep( REFRESH_SLEEP );
					};
				} catch ( final InterruptedException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	};
    	threadUIRefresh.start();
    }
    
    
    public static void notifyUIIdle() {
  	  lLastUpdate[0] = System.currentTimeMillis();
    }
	
	
	public final static Cursor CURSOR_HIDE;

  
	static {
	    final PaletteData palette = new PaletteData(
	    		new RGB[] { UI.COLOR_WHITE.getRGB(), UI.COLOR_BLACK.getRGB() } );
	    final ImageData idHide = new ImageData( 16, 16, 1, palette );
	    idHide.transparentPixel = 0;
	    CURSOR_HIDE = new Cursor( UI.display, idHide, 0, 0 );
	}
    
  
}
