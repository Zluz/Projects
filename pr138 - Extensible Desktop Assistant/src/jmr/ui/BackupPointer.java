package jmr.ui;

import java.awt.MouseInfo;
import java.awt.PointerInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BackupPointer {
	
	final static Display display = Display.getDefault();
	
	private static PaintListener paintlistener = new PaintListener() {
		@Override
		public void paintControl( final PaintEvent event ) {
			final GC gc = event.gc;
			gc.setForeground( display.getSystemColor( SWT.COLOR_DARK_GREEN ) );
			gc.drawLine( 0, 1, 10, 11 );
			gc.drawLine( 0, 0, 10, 10 );
			gc.drawLine( 0, 0, 10, 10 );
			gc.drawLine( 1, 0, 1, 20 );
			gc.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
			gc.drawLine( 1, 0, 11, 10 );
			gc.drawLine( 0, 0, 0, 20 );
		}
	};
	
	private static Region getPointerRegion() {
		final Region region = new Region();
		final int[] arr = new int[] {
				0,1,
				2,1,
				11,10,
				0,14
		};
		region.add( arr );
		return region;
	}
	
	private final Shell shell;
	private final Thread thread;
	
	public BackupPointer() {
		this.shell = new Shell( display, SWT.ON_TOP | SWT.NO_TRIM );
		shell.setText( "Backup Pointer" );
		shell.setRegion( getPointerRegion() );
		shell.setSize( 10,  14 );
		shell.open();
		shell.setAlpha( 140 );
		shell.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );
		shell.addPaintListener( paintlistener );
		
		this.thread = new Thread( "Backup Pointer" ) {
			public void run() {
				try {
					PointerInfo piLast = null;
					while ( ! shell.isDisposed() ) {
						Thread.sleep( 0 );
						final boolean bUpdate;
						final PointerInfo piNow = MouseInfo.getPointerInfo();
						if ( null == piNow ) {
							bUpdate = false;
						} else if ( null == piLast ) {
							bUpdate = true;
							piLast = piNow;
						} else {
							if ( piLast.getLocation().equals( 
									piNow.getLocation() ) ) {
								bUpdate = false;
							} else {
								bUpdate = true;
							}
							piLast = piNow;
						}
						if ( bUpdate ) {
							final int x = piNow.getLocation().x;
							final int y = piNow.getLocation().y;
							display.asyncExec( ()-> {
								if ( ! shell.isDisposed() ) {
									shell.setLocation( x,  y + 1 );
									shell.setActive();
								}
							});
						}
					}
				} catch ( final InterruptedException e ) {
					e.printStackTrace();
				}
			};
		};
		thread.start();
//		thread.setPriority( Thread.MIN_PRIORITY );
	}
	
	public Shell getShell() {
		return this.shell;
	}
	
	public void close() {
		if ( ! display.isDisposed() ) {
			display.asyncExec( ()-> this.shell.dispose() );
		}
	}
	
	public static void main( final String[] arr ) {
		final BackupPointer pointer = new BackupPointer();
		
		while ( ! pointer.getShell().isDisposed() ) {
			while ( display.readAndDispatch() ) {
				display.sleep();
			}
		}
	}

}
