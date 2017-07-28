package jmr.rpclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import jmr.rpclient.tab.TopSection;

public class ShellTopMenu {
	
	
	public final static int WIDTH = 150;

	private final Shell shell, shellMain;
	
	private boolean bSliding = false;
	
	private static ShellTopMenu instance;
	
	public static ShellTopMenu get() {
		return instance;
	}
	
	
	public ShellTopMenu( final Shell shellMain ) {
		shell = new Shell( shellMain.getDisplay(), SWT.ON_TOP );
		this.shellMain = shellMain;
		
		shell.setSize( WIDTH, 440 );
		this.align();
		
		shell.setLayout( new FillLayout( SWT.VERTICAL ) );
		
		shellMain.addControlListener( new ControlAdapter() {
			@Override
			public void controlMoved( final ControlEvent event ) {
				align();
			}
		});
		
	    for ( final TopSection menu : TopSection.values() ) {
		    final Button btn = new Button( shell, SWT.PUSH );
		    btn.setText( menu.getCaption() );
		    btn.setFont( UI.FONT_SMALL );
		    
		    btn.addSelectionListener( new SelectionAdapter() {
		    	@Override
		    	public void widgetSelected( final SelectionEvent event ) {
		    		SWTBasic.get().activate( menu );
		    	}
			});
		    btn.addMouseListener( new MouseAdapter() {
		    	@Override
		    	public void mouseDown( final MouseEvent event ) {
		    		SWTBasic.get().activate( menu );
		    	}
			});
	    }

		shell.open();
		
		shell.addFocusListener( new FocusAdapter() {
			@Override
			public void focusLost( final FocusEvent event ) {
				show( false );
			}
		});
		instance = this;
	}
	
	
	public void show( final boolean bShow ) {
		if ( bSliding ) return;
		
		bSliding = true;
		final int iXOStart;
		final int iXOFinal;
		if ( bShow ) {
			iXOStart = -1 * WIDTH - 10;
			iXOFinal = 0;
		} else {
			iXOStart = 0;
			iXOFinal = -1 * WIDTH - 10;
		}
		final Thread threadSlide = new Thread() {
			@Override
			public void run() {
				try {
					for ( int i=0; i<19; i++ ) {
						final int iX = (int)( ((float)(18-i)/18) * iXOStart 
										+ ((float)i/18) * iXOFinal );
						UI.display.asyncExec( new Runnable() {
							@Override
							public void run() {
								final Point loc = shellMain.getLocation();
								shell.setLocation( loc.x + iX, loc.y + 40 );
							}
						} );
					}
					Thread.sleep( 100 );
				} catch ( final InterruptedException e ) {
					// ignore
				}
				bSliding = false;
				if ( bShow ) {
					UI.display.asyncExec( new Runnable() {
						@Override
						public void run() {
							shell.forceFocus();
						}
					});
				}
			}
		};
		threadSlide.start();
	}
	
	
	private void align() {
		final Point loc = shellMain.getLocation();
		shell.setLocation( loc.x - WIDTH, loc.y + 40 );
	}
	
	
}
