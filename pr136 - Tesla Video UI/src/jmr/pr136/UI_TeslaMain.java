package jmr.pr136;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import jmr.pr136.Menu.Item;
import jmr.pr136.swt.CompositeRefresher;
import jmr.pr136.swt.UI;
import jmr.util.OSUtil;
import jmr.util.hardware.rpi.pimoroni.Port;
import jmr.util.report.Reporting;

public class UI_TeslaMain {

	private final static Logger 
				LOGGER = Logger.getLogger( UI_TeslaMain.class.getName() );

	
	public final static int FHD_X = 1920;
	public final static int FHD_Y = 1080;
	public final static int HHD_X = FHD_X / 2;
	public final static int HHD_Y = FHD_Y / 2;

	public final static boolean ABORT_INACTIVE_UI = true;
	
	
	
	public final static boolean HALF_SIZE = OSUtil.isWin();
	
	private static long lTimeStart = System.currentTimeMillis();
	private static String strJAR = OSUtil.getProgramName();
	
	private final Shell shell;
	
	private final Button btnClose;
	
	private int iMenuSelection = 0;
	
	private AnimationIndex aiPMenu = null;
	private AnimationIndex aiInput = null;
	
	private String strInput = null;
	
	private final KeyListener keylistener;
	
	private long lTimeLastPaint = 0;
	
	private final Monitor monitorRefreshRate;
	private final MonitorAutoHAT monitorAutoHAT;

	private final GaugeHistory gaugeRefreshRate;
	private final GaugeHistory gaugeAutoHAT_12VBatt;
	private final GaugeHistory gaugeAutoHAT_Accy;
	
	private long lLastUIUpdate = System.currentTimeMillis();
	
	private Image imageOverhead;
	
	private final OverheadServer server;
	
	private final static List<String> listMessages = new LinkedList<>();

	
	public UI_TeslaMain() {

		this.monitorRefreshRate = new Monitor( "UI Refresh Latency" );
		this.gaugeRefreshRate = new GaugeHistory( monitorRefreshRate, 
								new Rectangle( 1000, 460, 800, 160 ),
								400 );
		
		this.monitorAutoHAT = new MonitorAutoHAT();
		this.gaugeAutoHAT_12VBatt = new GaugeHistory( 
								monitorAutoHAT.getMonitor_1_12VBatt(), 
								new Rectangle( 1000, 460 + 180, 800, 160 ), 
								16 );
		this.gaugeAutoHAT_Accy = new GaugeHistory( 
								monitorAutoHAT.getMonitor_2_Accy(), 
								new Rectangle( 1000, 460 + 180 + 180, 800, 160 ),
								16 );
		
		this.server = new OverheadServer();
		
		
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
				
				// grab overhead image ( 240 x 135 )
				if ( null != rectOverhead ) {
					final Rectangle r = rectOverhead;
					if ( null != imageOverhead ) {
						imageOverhead.dispose();
					}
//					final GC gc = new GC( UI.display );
//					imageOverhead = new Image( UI.display, r.width, r.height );
					imageOverhead = new Image( UI.display, r.width, r.height );
					final GC gc = new GC( imageOverhead );
					gc.drawImage( imageBuffer, 			// buffer image 
									 r.x, r.y, 			// source coordinates 
									 r.width, r.height, // source dimensions
									 0,0,				// dest coordinates
									 r.width, r.height 	// dest dimensions
									 );
					server.prepareImage( imageOverhead );
				}

				imageBuffer.dispose();
			}
		} );

		this.keylistener = new KeyAdapter() {
			@Override
			public void keyPressed( final KeyEvent event ) {
				final long lTimeNow = System.currentTimeMillis();
				String strInput;
				switch ( event.keyCode ) {
					case SWT.ARROW_RIGHT : {
//						if ( null == aiPMenu ) { ..
						strInput = "Menu Right";
						Menu.getRoot().getSelectedChild()
										.changeSelectedChild( 1 );
						break;
					}
					case SWT.ARROW_LEFT : {
//						if ( null == aiPMenu ) { ..
						strInput = "Menu Left";
						Menu.getRoot().getSelectedChild()
										.changeSelectedChild( -1 );
						break;
					}
					case SWT.ARROW_UP : {
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
			shell.setEnabled( true );
			shell.setActive();
			shell.setFocus();
			btnClose.forceFocus();
			btnClose.setFocus();
			
			final Runtime runtime = Runtime.getRuntime();
			
			// difficulty focusing main window.. 
			// also mouses out of the way w/o having to hide the pointer
			new Thread( ()-> {
					try {
						Thread.sleep( 1000 );
					} catch ( final InterruptedException e ) {
						e.printStackTrace();
						runtime.exit( 300 );
					};
					try {
						final Robot robot = new Robot();
						robot.mouseMove( 1920, 1080 );
						
						robot.mousePress( InputEvent.BUTTON1_MASK  );
						Thread.sleep( 100 );
						robot.mouseRelease( InputEvent.BUTTON1_MASK  );
						Thread.sleep( 100 );

					} catch ( final Exception e ) {
						e.printStackTrace();
						runtime.exit( 301 );
					}
				}
			).run();
		}

		if ( bFullscreen && ABORT_INACTIVE_UI ) {
			final Thread threadUIWatchdog = new Thread( ()-> {
				try {
					while ( ! shell.isDisposed() ) {
						final long lNow = System.currentTimeMillis();
						if ( lNow - lLastUIUpdate > 5000 ) {
							System.err.println( "UI is not updating. Aborting." );
							System.out.println( Reporting.reportAllThreads() );
							Runtime.getRuntime().exit( 101 );
						}
						Thread.sleep( 2000 );
					}
				} catch ( final InterruptedException e ) {
					Runtime.getRuntime().exit( 102 );
				}
			} );
			threadUIWatchdog.start();
		}
		
		addMenuActions();
		
		log( "Started " + new Date().toString() );
	}
	
	
	private void updateRefreshMonitor( final long lTimeNow ) {
		if ( this.lTimeLastPaint > 0 ) {
			final long lElapsed = lTimeNow - this.lTimeLastPaint;
			this.monitorRefreshRate.add( lTimeNow, lElapsed );
		}
		this.lTimeLastPaint = lTimeNow;
	}
	
	
	Device display = null;
	Font font10 = null;
	Font font20 = null;
	Font font30 = null;
	Font font50 = null;
	Rectangle rectFull;
	Rectangle rectOverhead;
	
	
	private void paint( final Image image ) {

		final long lTimeNow = System.currentTimeMillis();
		updateRefreshMonitor( lTimeNow );
		
		lLastUIUpdate = lTimeNow;

		// note: micro-display is 240x135

		final GC gc = new GC( image );

		if ( null == display ) {
		
			display = image.getDevice();
			
			rectFull = new Rectangle( 0, 0, 1920, 1080 );
//			rectOverhead = new Rectangle( 1500, 40, 340, 235 );
			rectOverhead = new Rectangle( 1500, 40, 240, 135 );
	
			final Font fontSystem = display.getSystemFont();
		    
			final FontData fd10 = fontSystem.getFontData()[0];
		    fd10.setHeight( 15 );
		    fd10.setStyle( SWT.BOLD );
			font10 = new Font( display, fd10 );
			
			final FontData fd20 = fontSystem.getFontData()[0];
		    fd20.setHeight( 20 );
//		    fd20.setStyle( SWT.BOLD );
			font20 = new Font( display, fd20 );
			
			final FontData fd30 = fontSystem.getFontData()[0];
		    fd30.setHeight( 26 );
		    fd30.setStyle( SWT.BOLD );
			font30 = new Font( display, fd30 );
	
			final FontData fd50 = fontSystem.getFontData()[0];
		    fd50.setHeight( 36 );
		    fd50.setStyle( SWT.BOLD );
			font50 = new Font( display, fd50 );
		}
		
		
		// draw overhead pic box
		gc.setBackground( UI.getColor( SWT.COLOR_BLACK ) );
//		gc.fillRectangle( 1500, 40, 340, 235 );
		gc.fillRectangle( rectOverhead );
		
		
		gc.setForeground( UI.getColor( SWT.COLOR_CYAN ) );
		gc.drawLine( 0, 0, 1920, 1080 );

		
		gc.setForeground( UI.getColor( SWT.COLOR_RED ) );
		gc.setBackground( UI.getColor( SWT.COLOR_WHITE ) );

		// draw time (or JAR filename)
		gc.setFont( font30 );
//		final String strTime = new Date().toString();
//		gc.drawText( strTime, 650, 10 );
		gc.drawText( strJAR, 650, 10 );
		
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
		
		//TODO .. just testing..
		gc.setAdvanced( false );
		gc.setAntialias( SWT.OFF );

		
		for ( int i = iMenuSelection; i > 0; i-- ) {
			final Item item = listItems.remove( 0 );
			listItems.add( item );
		}
//		gc.setFont( font50 );
		final boolean bLockedPMenu = null == this.aiPMenu;
		boolean bFirst = bLockedPMenu;
		if ( bLockedPMenu ) {
			gc.setBackground( UI.getColor( SWT.COLOR_DARK_CYAN ) );
//			gc.setAdvanced( true );
//			gc.setAntialias( SWT.ON );
		} else {
			gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
			iY += aiPMenu.getIndex( lTimeNow );
			gc.setAdvanced( false );
			gc.setAntialias( SWT.OFF );
		}
		
		for ( final Item item : listItems ) {
			final String strText = item.getText();

			gc.setClipping( rectOverhead );
			gc.setFont( font10 );
			final int iY_OH = ( iY - 340 ) / 5 + 46;
			if ( bFirst ) {
				gc.setForeground( UI.getColor( SWT.COLOR_YELLOW ) );
				gc.setBackground( UI.getColor( SWT.COLOR_YELLOW ) );
				gc.fillRectangle( 1500, 80, 7, 20 );
			} else {
				gc.setForeground( UI.getColor( SWT.COLOR_GRAY ) );
			}
			gc.setBackground( UI.getColor( SWT.COLOR_BLACK ) );
			gc.drawText( item.getTextShort(), 1522, iY_OH, true );
			gc.setClipping( rectFull );

			gc.setForeground( UI.getColor( SWT.COLOR_BLACK ) );
			gc.setBackground( UI.getColor( SWT.COLOR_DARK_CYAN ) );
			gc.fillRectangle( 50, iY, 640, 74 );
			gc.setFont( font50 );
			gc.drawText( strText, 70, iY, true );
			gc.setBackground( UI.getColor( SWT.COLOR_GRAY )  );
			
			if ( bFirst ) {
				bFirst = false;
				final List<Item> listChildren = item.getChildren();
				final Item itemSelected = item.getSelectedChild();
				final int iWidth = iTotalWidth / listChildren.size();
				
				if ( bLockedPMenu ) {
					Menu.getRoot().setSelected( item );
				}

				if ( listChildren.size() < 5 ) {
					gc.setFont( font50 );
				} else {
					gc.setFont( font30 );
				}
				
				int iX = 740;
				for ( final Item itemChild : listChildren ) {
					
					if ( itemChild == itemSelected ) {
						gc.setBackground( UI.getColor( SWT.COLOR_DARK_CYAN ) );
					} else {
						gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
					}
					
					final String strCText = itemChild.getText();
					gc.fillRectangle( iX, iY, iWidth - 40, 74 );
					gc.drawText( strCText, iX + 20, iY );
					iX += iWidth;
				}
				gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
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

		
		gc.setBackground( UI.getColor( SWT.COLOR_DARK_YELLOW ) );
		gc.setForeground( UI.getColor( SWT.COLOR_WHITE ) );
		gc.fillRectangle( 940, 100, 540, 174 );
		final Boolean bOutR1 = monitorAutoHAT.getDigitalValue( Port.OUT_R_1 );
		final Boolean bOutR2 = monitorAutoHAT.getDigitalValue( Port.OUT_R_2 );
		final Boolean bOutR3 = monitorAutoHAT.getDigitalValue( Port.OUT_R_3 );
		final Boolean bInD1 = monitorAutoHAT.getDigitalValue( Port.IN_D_1 );
		final Boolean bInD2 = monitorAutoHAT.getDigitalValue( Port.IN_D_2 );
		final Boolean bInD3 = monitorAutoHAT.getDigitalValue( Port.IN_D_3 );
		final Float fInA1 = monitorAutoHAT.getAnalogValue( Port.IN_A_1 );
		final Float fInA2 = monitorAutoHAT.getAnalogValue( Port.IN_A_2 );
		final Float fInA3 = monitorAutoHAT.getAnalogValue( Port.IN_A_3 );
		int iX = 960;
		final int iXStep = 170;
		final int iYStep = 40; 
		iY = 100;
		gc.drawText( "Relays", iX, iY ); iY += iYStep + 6;
		gc.drawText( "R1: " + bOutR1, iX, iY ); iY += iYStep;
		gc.drawText( "R2: " + bOutR2, iX, iY ); iY += iYStep;
		gc.drawText( "R3: " + bOutR3, iX, iY ); iY += iYStep;
		iY = 100; 
		iX += iXStep;
		gc.drawText( "Digital-In", iX, iY ); iY += iYStep + 6;
		gc.drawText( "D1: " + bInD1, iX, iY ); iY += iYStep;
		gc.drawText( "D2: " + bInD2, iX, iY ); iY += iYStep;
		gc.drawText( "D3: " + bInD3, iX, iY ); iY += iYStep;
		iY = 100; 
		iX += iXStep;
		gc.drawText( "Analog-In", iX, iY ); iY += iYStep + 6;
		gc.drawText( String.format( "A1: %.4f", fInA1 ), iX, iY ); iY += iYStep;
		gc.drawText( String.format( "A2: %.4f", fInA2 ), iX, iY ); iY += iYStep;
		gc.drawText( String.format( "A3: %.4f", fInA3 ), iX, iY ); iY += iYStep;

		
		gc.setFont( font20 );
		gc.setBackground( UI.getColor( SWT.COLOR_DARK_GREEN ) );
		gc.setForeground( UI.getColor( SWT.COLOR_WHITE ) );
		gc.fillRectangle( 40, 100, 880, 190 );
		iY = 250;
		for ( int i = listMessages.size() - 1; i > 0 && iY > 100; i-- ) {
			final String strLine = listMessages.get( i );
			gc.drawText( strLine, 46, iY );
			iY -= 36;
		}
		
		
		

		this.gaugeRefreshRate.paint( lTimeNow, gc, image );
		this.gaugeAutoHAT_12VBatt.paint( lTimeNow, gc, image );
		this.gaugeAutoHAT_Accy.paint( lTimeNow, gc, image );
		
	
		// draw 'close' box
		gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
		gc.fillRectangle( 28, 43, 130, 42 );
		
//		// grab the overhead image (do this in the paint handler)
//		imageOverhead
	}
	
	
	public void addMenuActions() {
		
		// also initialize hardware
		monitorAutoHAT.setRelayState( Port.OUT_R_1, true );
		monitorAutoHAT.setRelayState( Port.OUT_R_2, true );

		// now setup menu actions
		
		Menu.addRunnable( "DASHCAM/ON", ()-> {
			monitorAutoHAT.setRelayState( Port.OUT_R_1, true );
			log( "Blackvue Dashcam ON" );
		} );

		Menu.addRunnable( "DASHCAM/OFF", ()-> {
			monitorAutoHAT.setRelayState( Port.OUT_R_1, false );
			log( "Blackvue Dashcam OFF" );
		} );

		Menu.addRunnable( "NETWORK/ON", ()-> {
			monitorAutoHAT.setRelayState( Port.OUT_R_2, true );
			log( "Vehicle Network ON" );
		} );

		Menu.addRunnable( "NETWORK/OFF", ()-> {
			monitorAutoHAT.setRelayState( Port.OUT_R_2, false );
			log( "Vehicle Network OFF" );
		} );
	}
	
	
	public Shell getShell() {
		return this.shell;
	}

	
	public static void log( final String str ) {
		listMessages.add( str );
	}
	
	
	
	public static void main( final String[] arrArguments ) {
		
		final UI_TeslaMain ui = new UI_TeslaMain();
		
		if ( null != arrArguments && arrArguments.length > 0 ) {
			if ( "TIMER_EXIT".equalsIgnoreCase( arrArguments[ 0 ] ) ) {
				LOGGER.info( "TIMER_EXIT enabled. Will exit after 2 minutes." );
				new Thread( ()-> {
					try { 
						TimeUnit.MINUTES.sleep( 2 );
					} catch ( final InterruptedException e ) {
						// will exit anyway
					}
					Runtime.getRuntime().exit( 100 );
				} ).start();
			}
		}
		
		while ( ! ui.getShell().isDisposed() ) {
			if ( ! UI.display.readAndDispatch() ) {
//				UI.notifyUIIdle(); // see pr101:UI
				UI.display.sleep();
			}
		}
	    UI.display.dispose();
	    
	    ui.monitorAutoHAT.shutdown();
//		Logging.log( "Application closing. " + new Date().toString() );
	    Runtime.getRuntime().exit( 0 );
	}

}
