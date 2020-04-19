package jmr.pr136;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Date;
import java.util.EnumMap;
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
import jmr.pr136.OverheadServer.Listener;
import jmr.pr136.swt.CompositeRefresher;
import jmr.pr136.swt.UI;
import jmr.util.OSUtil;
import jmr.util.hardware.rpi.pimoroni.Port;
import jmr.util.report.Reporting;

public class UI_TeslaMain {

	private final static Logger 
				LOGGER = Logger.getLogger( UI_TeslaMain.class.getName() );

	
	public final static String DATA_STORE_PATH = "/media/pi/32GB";
	
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
	
//	private String strInput = null;
	
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
								new Rectangle( 1070, 460, 800, 160 ),
								400 );
		
		this.monitorAutoHAT = new MonitorAutoHAT();
		this.gaugeAutoHAT_12VBatt = new GaugeHistory( 
								monitorAutoHAT.getMonitor_1_12VBatt(), 
								new Rectangle( 1070, 460 + 180, 800, 160 ), 
								16 );
		this.gaugeAutoHAT_Accy = new GaugeHistory( 
								monitorAutoHAT.getMonitor_2_Accy(), 
								new Rectangle( 1070, 460 + 180 + 180, 800, 160 ),
								16 );
		
		this.server = new OverheadServer( createOverheadListener() );
		
		
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
			shell.setSize( HHD_X, HHD_Y + 20 ); // +20 for trim
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

		this.keylistener = createKeyListener();
		
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
		startAutoHatMonitor();
		
		mapStates.put( StateKey.SS_JAR, strJAR );
		
		log( "Started " + new Date().toString() );
	}
	

	private OverheadServer.Listener createOverheadListener() {

		final Listener listener = new OverheadServer.Listener() {
			@Override
			public void emitRequestImageHandled( 	final String strURL,
													final String strRemote,
													final int iResponse ) {
				final long lTimeNow = System.currentTimeMillis();
				
//				UI_TeslaMain.log( "Request from: " + strRemote );
//				UI_TeslaMain.log( "URL: " + strURL );
				
				if ( strURL.contains( "a=1" ) ) {
					handleKey( SWT.ARROW_DOWN, lTimeNow );
				} else if ( strURL.contains( "b=1" ) ) {
					handleKey( SWT.ARROW_RIGHT, lTimeNow );
				}
				
				mapStates.put( StateKey.SS_LAST_URL, strURL );
				mapStates.put( StateKey.SO_IP, strRemote );

				mapStates.put( StateKey.SO_IMG_LAST, lTimeNow );
			}

			@Override
			public void emitRequestKeyHandled( 	final String strURL, 
												final String strRemote,
												final int iResponse ) {
				final long lTimeNow = System.currentTimeMillis();
				
				mapStates.put( StateKey.SS_LAST_URL, strURL );
				mapStates.put( StateKey.SO_IP, strRemote );

				mapStates.put( StateKey.SO_KEY_LAST, lTimeNow );
			}
		};
		return listener;
	}
	
	
	private String handleKey( 	final int iKeyCode,
								final long lTimeNow ) {
		
		String strInput;
		switch ( iKeyCode ) {
			case SWT.ARROW_RIGHT : {
//				if ( null == aiPMenu ) { ..
				strInput = "Menu Right";
				Menu.getRoot().getSelectedChild().changeSelectedChild( 1 );
				break;
			}
			case SWT.ARROW_LEFT : {
//				if ( null == aiPMenu ) { ..
				strInput = "Menu Left";
				Menu.getRoot().getSelectedChild().changeSelectedChild( -1 );
				break;
			}
			case SWT.ARROW_UP : {
				if ( null == aiPMenu ) {
					strInput = "Menu Up";
					final int iNewValue = iMenuSelection - 1;
//					iMenuSelection--;
					aiPMenu = new AnimationIndex( 
//							300L, 0, 94,
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
//					final int iNewValue = iMenuSelection + 1;
					final int iNewValue = iMenuSelection;
					aiPMenu = new AnimationIndex( 
//							300, 94, 0,
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
		return strInput;
	}
	
	
	private KeyListener createKeyListener() {
		
		final KeyListener listener = new KeyAdapter() {
			@Override
			public void keyPressed( final KeyEvent event ) {

				final long lTimeNow = System.currentTimeMillis();
				final String strInput = handleKey( event.keyCode, lTimeNow );
				
				if ( null != strInput ) {
//					UI_TeslaMain.this.strInput = strInput;
					mapStates.put( StateKey.SS_INPUT, strInput );
					aiInput = new AnimationIndex( 
							lTimeNow, 2000, 
							255, 100, ()-> {
//								UI_TeslaMain.this.strInput = null;
								mapStates.put( StateKey.SS_INPUT, null );
								aiInput = null; 
							} );
				} else {
//					UI_TeslaMain.this.strInput = null;
					mapStates.put( StateKey.SS_INPUT, null );
				}
			}
		};

		return listener;
	}
	
	
	
	private void updateRefreshMonitor( final long lTimeNow ) {
		if ( this.lTimeLastPaint > 0 ) {
			final long lElapsed = lTimeNow - this.lTimeLastPaint;
			this.monitorRefreshRate.add( lTimeNow, lElapsed );
		}
		this.lTimeLastPaint = lTimeNow;
	}
	
	
	final private EnumMap< StateKey, Object > 
							mapStates = new EnumMap<>( StateKey.class );
	
	
	public String getOverheadDisplayKey( 	final int iMenuSelection,
											final int iYOffs,
											final int iSubSelection ) {
		final String strKey = String.format( 
				"%02d.%03d/%02d", iMenuSelection, iYOffs, iSubSelection );
		this.mapStates.put( StateKey.SO_KEY_LAST, strKey );
		return strKey;
	}
	
	
	public void startAutoHatMonitor() {
		final Thread thread = new Thread( "AutoHAT Monitor" ) {
			@Override
			public void run() {
				try {
					while ( ! shell.isDisposed() ) {
						TimeUnit.MILLISECONDS.sleep( 100 );
						
						for ( final StateKey key : StateKey.values() ) {
							final Port port = key.getPort();
							if ( null != port ) {
								final Object objValue;
								if ( port.isAnalog() ) {
									objValue = monitorAutoHAT
												.getAnalogValue( port );
								} else {
									objValue = monitorAutoHAT
												.getDigitalValue( port );
								}
								if ( null != objValue ) {
									mapStates.put( key, objValue ); 
								}
							}
						}

					}
				} catch ( final InterruptedException e ) {
					return;
				}
			}
		};
		thread.start();
	}
	
	
	
	
	
	
	Device display = null;
	Font font10 = null;
	Font font20 = null;
	Font font30 = null;
	Font font50 = null;
	Rectangle rectFull;
	Rectangle rectOverhead;
	
	
	
	
	private void paintStates( 	final Image image,
								final GC gc,
								final String strTypes,
								final Rectangle r ) {
		if ( null == image ) return;
		if ( null == gc ) return;
		if ( null == r ) return;
		
		final boolean bNarrowOnly = true;
		
		gc.setBackground( UI.getColor( SWT.COLOR_DARK_YELLOW ) );
		gc.setForeground( UI.getColor( SWT.COLOR_WHITE ) );
		gc.fillRectangle( r );
		gc.setFont( font20 );
		
		final int iXStart = r.x + 8;
		final int iYStep = gc.textExtent( "A" ).y;
		final int iXLabel = gc.textExtent( "SS_HOME_NET -" ).x;
		
		int iX = iXStart;
		int iY = r.y + 8;
		
		
		for ( StateKey key : StateKey.values() ) {
			if ( key.isVisible() 
						&& bNarrowOnly 
						&& ( strTypes.indexOf( key.getType() ) > -1 ) ) {
				final Object objValue = mapStates.get( key );
				final String strValue = key.asString( objValue );
				
				iX = iXStart;
				gc.drawText( key.name(), iX, iY, true );
				
				iX += iXLabel;
				gc.drawText( strValue, iX, iY, true );
				
				iY += iYStep;
			}
		}
	}
	
	
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
//			rectOverhead = new Rectangle( 1600, 40, 240, 135 );
			rectOverhead = new Rectangle( 50, 140, 240, 135 );
	
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
		gc.drawLine( 1920 - 100, 1080 - 100, 1920, 1080 );

		
		gc.setForeground( UI.getColor( SWT.COLOR_RED ) );
		gc.setBackground( UI.getColor( SWT.COLOR_WHITE ) );

		// draw time (or JAR filename)
		gc.setFont( font30 );
//		final String strTime = new Date().toString();
//		gc.drawText( strTime, 650, 10 );
//		gc.drawText( strJAR, 650, 10 );
		
//		gc.drawText( "Input: " + strInput, 190, 10 );

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

		
		final int iYMenuOffset;
		
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
			iYMenuOffset = 0;
		} else {
			gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
			iYMenuOffset = aiPMenu.getIndex( lTimeNow );
			iY += iYMenuOffset;
			gc.setAdvanced( false );
			gc.setAntialias( SWT.OFF );
		}

		int iYMenu = 0;
		final Rectangle rOH = rectOverhead;

		for ( final Item item : listItems ) {
			final String strText = item.getText();

			gc.setClipping( rectOverhead );
			gc.setFont( font10 );
			final int iY_OH = ( iY - 340 ) / 5 + 6 + rOH.y;
			if ( bFirst ) {
				gc.setForeground( UI.getColor( SWT.COLOR_YELLOW ) );
				gc.setBackground( UI.getColor( SWT.COLOR_YELLOW ) );
//				gc.fillRectangle( 1500, 80, 7, 20 );
				gc.fillRectangle( rOH.x, rOH.y + 20, 7, 24 );

				gc.setBackground( UI.getColor( SWT.COLOR_GREEN ) );
				gc.fillRectangle( rOH.x, rOH.y + 97, 7, 24 );

				// approx reserved region
//				gc.setBackground( UI.getColor( SWT.COLOR_DARK_GRAY ) );
//				gc.fillRectangle( r.x + 162, r.y + 108, 76, 26 );

			} else {
				gc.setForeground( UI.getColor( SWT.COLOR_GRAY ) );
			}
			gc.setBackground( UI.getColor( SWT.COLOR_BLACK ) );
			gc.drawText( item.getTextShort(), rOH.x + 22, iY_OH, true );
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

				
				int iX = 740;
				for ( final Item itemChild : listChildren ) {
					final String strCText = itemChild.getText();
					
					if ( itemChild == itemSelected ) {
						iYMenu = iX;

						gc.setForeground( UI.getColor( SWT.COLOR_GREEN ) );
						gc.setBackground( UI.getColor( SWT.COLOR_BLACK ) );

						gc.setFont( font10 );
						gc.drawText( strCText, rOH.x + 140, rOH.y + 7 );
						
						gc.setForeground( UI.getColor( SWT.COLOR_BLACK ) );
						gc.setBackground( UI.getColor( SWT.COLOR_DARK_CYAN ) );
					} else {
						gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
					}

					if ( listChildren.size() < 5 ) {
						gc.setFont( font50 );
					} else {
						gc.setFont( font30 );
					}

					gc.fillRectangle( iX, iY, iWidth - 40, 74 );
					gc.drawText( strCText, iX + 20, iY );
					iX += iWidth;
				}
				gc.setBackground( UI.getColor( SWT.COLOR_GRAY ) );
				gc.setFont( font50 );
			}
			iY += 94;
		}
		
		final String strKey = getOverheadDisplayKey( 
									iMenuSelection, iYMenuOffset, iYMenu );
		mapStates.put( StateKey.SO_IMG_KEY, strKey );
		server.prepareKey( strKey );
		
		
		gc.setFont( font30 );
//		gc.drawText( ""+ iMenuSelection, 580, 10 );
		
//		final long lElapsed = lTimeNow - lTimeStart;
//		gc.drawText( ""+ lElapsed, 1300, 10 );
		final Double dAvgRefresh = this.monitorRefreshRate.getAverageValue();
//		final String strFPS;
		if ( null != dAvgRefresh ) {
			final double dFPS = 1000.0 / dAvgRefresh;
//			strFPS = String.format( "FPS: %05.2f", dFPS );
			mapStates.put( StateKey.SS_FPS, dFPS );
		} else {
//			strFPS = "FPS: -";
		}
//		gc.drawText( strFPS, 1300, 10 );

		
//		paintStates( image, gc, new Rectangle( 940, 100, 540, 174 ) );
		paintStates( image, gc, "ADCS", new Rectangle( 730, 460, 300, 520 ) );
		paintStates( image, gc, "X", new Rectangle( 1260, 30, 580, 280 ) );


		
		gc.setFont( font20 );
		gc.setBackground( UI.getColor( SWT.COLOR_DARK_GREEN ) );
		gc.setForeground( UI.getColor( SWT.COLOR_WHITE ) );
		gc.fillRectangle( 330, 30, 900, 280 );
		iY = 280 + 30 - 38;
		for ( int i = listMessages.size() - 1; i > 0 && iY > 30; i-- ) {
			final String strLine = listMessages.get( i );
			gc.drawText( strLine, 336, iY );
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
