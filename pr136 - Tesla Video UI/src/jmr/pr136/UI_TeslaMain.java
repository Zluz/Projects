package jmr.pr136;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import jmr.pr136.Menu.Item;
import jmr.pr136.swt.CompositeRefresher;
import jmr.pr136.swt.UI;
import jmr.util.OSUtil;

public class UI_TeslaMain {
	
	public final static int FHD_X = 1920;
	public final static int FHD_Y = 1080;
	public final static int HHD_X = FHD_X / 2;
	public final static int HHD_Y = FHD_Y / 2;
	
	
	
	public final static boolean HALF_SIZE = OSUtil.isWin();
	
	private static long lTimeStart = System.currentTimeMillis();
	
	private final Shell shell;
	
	private final Button btnClose;
	
	private int iMenuSelection = 0;
	
	private AnimationIndex aiPMenu = null;
	private AnimationIndex aiInput = null;
	
	private String strInput = null;
	
	private final KeyListener keylistener;
	
	private long lTimeLastPaint = 0;
	
	private final Monitor monitorRefreshRate = new Monitor();

	
	public UI_TeslaMain() {
		//NOTE: refresh flickering can be fixed by SWT.DOUBLE_BUFFERED 
		final int iOptions;
		final boolean bFullscreen = ! OSUtil.isWin();
		if ( bFullscreen ) {
			iOptions = SWT.NO_TRIM | SWT.ON_TOP | SWT.DOUBLE_BUFFERED;
		} else {
			iOptions = SWT.SHELL_TRIM | SWT.DOUBLE_BUFFERED;
		}
		this.shell = new Shell( UI.display, iOptions );
		if ( bFullscreen ) {
			shell.setSize( FHD_X, FHD_Y );
			shell.setLocation( 0, 0 );
//			shell.setMaximized( true );
		} else {
			shell.setSize( HHD_X, HHD_Y );
		}
		shell.setText( "Tesla Main/Video UI" );
		
//		if ( ! HALF_SIZE ) {
//			shell.setSize( FHD_X, FHD_Y );
//		} else {
//			shell.setSize( HHD_X, HHD_Y );
//		}
		final FillLayout layout = new FillLayout();
		shell.setLayout( layout );
		
		final Composite comp = new Composite( shell, SWT.DOUBLE_BUFFERED );
//		comp.setBackground( UI.getColor( SWT.COLOR_DARK_CYAN ) );
		comp.setRedraw( true );

		if ( bFullscreen ) {
			this.btnClose = new Button( comp, SWT.PUSH );
			btnClose.setText( " +  Close" );
			btnClose.setSize( 130, 42 );
			btnClose.setLocation( 28, 43 );
		} else {
			btnClose = null;
		}

		comp.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent pe ) {
				final Image imageBuffer = 
								new Image( UI.display, FHD_X, FHD_Y );
				
				pe.gc.setAdvanced( true );
				pe.gc.setAntialias( SWT.ON );
				pe.gc.setTextAntialias( SWT.ON );
				
				paint( imageBuffer );

				if ( ! HALF_SIZE ) {
					pe.gc.drawImage( imageBuffer, 0,0 );
				} else {
					pe.gc.drawImage( imageBuffer, 	// buffer image 
									 0,0, 			// source coordinates 
									 FHD_X,FHD_Y, 	// source dimensions
									 0,0,			// dest coordinates
									 HHD_X, HHD_Y 	// dest dimensions
									 );
				}

				imageBuffer.dispose();
			}
		});

		this.keylistener = new KeyAdapter() {
			@Override
			public void keyPressed( final KeyEvent event ) {
				final long lTimeNow = System.currentTimeMillis();
				String strInput;
				switch ( event.keyCode ) {
					case SWT.ARROW_UP 	: {
						if ( null == aiPMenu ) {
							strInput = "Menu Up";
							final int iNewValue = iMenuSelection - 1;
//							iMenuSelection--;
							aiPMenu = new AnimationIndex( 
//									300L, 0, 94,
									0, 94, 35,
									()-> {
											iMenuSelection = iNewValue;
											aiPMenu = null;
										} );
						} else {
							strInput = "(Menu Up)";
						}
						break;
					}
					case SWT.ARROW_DOWN : {
						if ( null == aiPMenu ) {
							strInput = "Menu Down";
							iMenuSelection++;
//							final int iNewValue = iMenuSelection + 1;
							final int iNewValue = iMenuSelection;
							aiPMenu = new AnimationIndex( 
//									300, 94, 0,
									94, 0, -35,
									()-> {
											iMenuSelection = iNewValue;
											aiPMenu = null;
										} );
						} else {
							strInput = "(Menu Down)";
						}
						break;
					}
					default: {
						strInput = null;
					}
				}
				if ( null != strInput ) {
					UI_TeslaMain.this.strInput = strInput;
					aiInput = new AnimationIndex( 
							lTimeNow, 2000, 
							255, 100, ()-> {
								UI_TeslaMain.this.strInput = null;
								aiInput = null; 
							} );
				} else {
					UI_TeslaMain.this.strInput = null;
				}
			}
		};
		
		shell.addKeyListener( this.keylistener );

		if ( null != btnClose ) {
			btnClose.addKeyListener( this.keylistener );
			btnClose.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected( final SelectionEvent se ) {
					shell.close();
				}
			} );
		}


		shell.layout( true );
		
		new CompositeRefresher( comp, 10 );
		
		shell.open();
		
		if ( null != btnClose ) {
			btnClose.forceFocus();
			btnClose.setFocus();
		}
	}
	
	
	private void updateRefreshMonitor( final long lTimeNow ) {
		if ( this.lTimeLastPaint > 0 ) {
			final long lElapsed = lTimeNow - this.lTimeLastPaint;
			this.monitorRefreshRate.add( lTimeNow, lElapsed );
		}
		this.lTimeLastPaint = lTimeNow;
	}
	
	Device display = null;
	Font font30 = null;
	Font font50 = null;
	
	private void paint( final Image image ) {

		final long lTimeNow = System.currentTimeMillis();
		updateRefreshMonitor( lTimeNow );

		// note: micro-display is 240x135

		final GC gc = new GC( image );

		if ( null == display ) {
		
			display = image.getDevice();
	
			final Font fontSystem = display.getSystemFont();
		    
			final FontData fd30 = fontSystem.getFontData()[0];
		    fd30.setHeight( 26 );
			font30 = new Font( display, fd30 );
	
			final FontData fd50 = fontSystem.getFontData()[0];
		    fd50.setHeight( 36 );
			font50 = new Font( display, fd50 );
		}
		
		
		// draw overhead pic box
		gc.setBackground( UI.getColor( SWT.COLOR_DARK_GREEN ) );
		gc.fillRectangle( 1500, 40, 340, 235 );
		
		
		gc.setForeground( UI.getColor( SWT.COLOR_RED ) );
		gc.drawLine( 0, 0, 1920, 1080 );

		
		gc.setBackground( UI.getColor( SWT.COLOR_WHITE ) );

		// draw time
		final String strTime = new Date().toString();
		gc.setFont( font30 );
		gc.drawText( strTime, 650, 10 );
		
		gc.drawText( "Input: " + strInput, 190, 10 );

		// draw menu
		gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
		gc.setForeground( UI.getColor( SWT.COLOR_BLACK ) );
		int iY = 340;
		final int iTotalWidth = 1920 - 800;
//		final List<String> listItemParents = Menu.getItems().stream()
//									.map( i -> i.getText() )
//									.collect( Collectors.toList() );
		final List<Item> listItems = Menu.getItems();
		final int iSize = listItems.size();
		if ( iMenuSelection < 0 ) {
			iMenuSelection += iSize;
		} else if ( iMenuSelection >= iSize ) {
			iMenuSelection -= iSize;
		}
		for ( int i = iMenuSelection; i > 0; i-- ) {
			final Item item = listItems.remove( 0 );
			listItems.add( item );
		}
		gc.setFont( font50 );
		final boolean bLockedPMenu = null == this.aiPMenu;
		boolean bFirst = bLockedPMenu;
		if ( bLockedPMenu ) {
			gc.setBackground( UI.getColor( SWT.COLOR_DARK_CYAN ) );
			gc.setAdvanced( true );
			gc.setAntialias( SWT.ON );
		} else {
			gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
			iY += aiPMenu.getIndex( lTimeNow );
			gc.setAdvanced( false );
			gc.setAntialias( SWT.OFF );
		}
		
		for ( final Item item : listItems ) {
			final String strText = item.getText();
			gc.fillRectangle( 50, iY, 640, 74 );
			gc.drawText( strText, 70, iY );
			gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
			
			if ( bFirst ) {
				bFirst = false;
				final List<Item> listChildren = item.getChildren();
				final int iWidth = iTotalWidth / listChildren.size();

				if ( listChildren.size() < 5 ) {
					gc.setFont( font50 );
				} else {
					gc.setFont( font30 );
				}

				int iX = 740;
				for ( final Item itemChild : listChildren ) {
					final String strCText = itemChild.getText();
					gc.fillRectangle( iX, iY, iWidth - 40, 74 );
					gc.drawText( strCText, iX + 20, iY );
					iX += iWidth;
				}
				gc.setFont( font50 );
			}
			iY += 94;
		}
		
		gc.setFont( font30 );
		gc.drawText( ""+ iMenuSelection, 580, 10 );
		
//		final long lElapsed = lTimeNow - lTimeStart;
//		gc.drawText( ""+ lElapsed, 1300, 10 );
		final Double dAvgRefresh = this.monitorRefreshRate.getAverageValue();
		final String strFPS;
		if ( null != dAvgRefresh ) {
			final double dFPS = 1000.0 / dAvgRefresh;
			strFPS = String.format( "FPS: %05.2f", dFPS );
		} else {
			strFPS = "FPS: -";
		}
		gc.drawText( strFPS, 1300, 10 );
		
		// draw 'close' box
		gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
		gc.fillRectangle( 28, 43, 130, 42 );
	}
	
	
	public Shell getShell() {
		return this.shell;
	}

	
	public static void main( final String[] arrArguments ) {
		
		final UI_TeslaMain ui = new UI_TeslaMain();
		
		while ( ! ui.getShell().isDisposed() ) {
			if ( ! UI.display.readAndDispatch() ) {
//				UI.notifyUIIdle(); // see pr101:UI
				UI.display.sleep();
			}
		}
	    UI.display.dispose();
//		Logging.log( "Application closing. " + new Date().toString() );
	}

}
