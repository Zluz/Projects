package jmr.pr138.actions;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import jmr.pr140.Domain;

public class MouseJitter implements ActionEntry {

	final static private Logger LOGGER = 
			Logger.getLogger( MouseJitter.class.getName() );
	
	private long lJitterDuration;
	
	private Thread thread;
	private boolean bActive = false;
	private Shell shell;
	

	public static JitterFrequency getDefault() {
//		if ( Domain.getDomain() )
//		return JitterFrequency.OFF;
		return JitterFrequency._60;
	}
	
	
	
	@Override
	public MenuItem getMenuItem( final Menu menuParent ) {
		final MenuItem mi = new MenuItem( menuParent, SWT.CASCADE );
		mi.setText( "Mouse Jitter" );
		this.shell = menuParent.getShell();
		
		final MenuItem[] miSelect = { null };
		final SelectionAdapter[] saSelect = { null };
		
		final Menu menuSub = new Menu( this.shell, SWT.DROP_DOWN );
		mi.setMenu( menuSub );
		
		for ( final JitterFrequency jf : JitterFrequency.values() ) {
			final MenuItem miJF = new MenuItem( menuSub, SWT.RADIO );
			final long lDuration = jf.getDuration();
			final String strDuration = jf.getText();
			miJF.setText( strDuration );
			final SelectionAdapter listener = new SelectionAdapter() {
				public void widgetSelected( SelectionEvent e ) {
					if ( miJF.getSelection() ) {
						lJitterDuration = lDuration;
						mi.setText( "Mouse Jitter (" + strDuration + ")" );
						LOGGER.info( "Mouse jitter set to " + strDuration );
					}
				};
			};
			miJF.addSelectionListener( listener );
			
			if ( jf.equals( MouseJitter.getDefault() ) ) {
				miSelect[0] = miJF;
				saSelect[0] = listener;
			}
		}
		
		this.start();
		
		if ( null != miSelect[0] ) {
			miSelect[0].setSelection( true );
			saSelect[0].widgetSelected( null );
		}
		
		return mi;
	}

	
	public static void doMouseJitter( final Robot robot ) 
							throws InterruptedException {
		
		final long lTimeStart = System.currentTimeMillis();
		final PointerInfo piNow = MouseInfo.getPointerInfo();
		if ( null==piNow ) return;
		
		final java.awt.Point ptMouseNow = piNow.getLocation();
		if ( null==ptMouseNow ) return;
		
		final int iX = ptMouseNow.x;
		final int iY = ptMouseNow.y;
		if ( iX > 0 ) {
			robot.mouseMove( iX - 1, iY ); 
		} else {
			robot.mouseMove( iX + 1, iY ); 
		}
		Thread.sleep( 10 );
		final long lTimeAfter = System.currentTimeMillis();
		final long lElapsed = lTimeAfter - lTimeStart;
		if ( lElapsed < 50 ) {
			robot.mouseMove( iX,  iY );
		}
	}
	
	
	public void start() {
		if ( null != this.thread ) {
			this.stop();
		}
		
		this.bActive = true;

		this.thread = new Thread() {
			public void run() {
				try {
					final Robot robot = new Robot();
					while ( bActive && ! shell.isDisposed() ) {
						TimeUnit.SECONDS.sleep( lJitterDuration );
						
						if ( lJitterDuration > 0 ) {
							doMouseJitter( robot );
						} else {
							TimeUnit.SECONDS.sleep( 30 );
						}
					}
				} catch ( final InterruptedException e ) {
					// just drop out
				} catch ( final AWTException e ) {
					e.printStackTrace();
				}
			};
		};
		this.thread.start();
	}
	
	public void stop() {
		this.bActive = false;
		this.thread = null;
	}
	
}
