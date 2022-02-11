package jmr.rpclient;
/*
 * Create a Tree with columns
 * 
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.gson.Gson;

import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.UI;
import jmr.rpclient.tab.TabBase;
import jmr.rpclient.tab.TabControls;
import jmr.rpclient.tab.TabDailyInfo;
import jmr.rpclient.tab.TabLog;
import jmr.rpclient.tab.TabS2DB;
import jmr.rpclient.tab.TabTiles;
import jmr.rpclient.tab.TabTreeDemo;
import jmr.rpclient.tab.TopSection;
import jmr.rpclient.tiles.PerformanceMonitorTile;
import jmr.rpclient.tiles.Perspective;
import jmr.rpclient.tiles.TileBase;
import jmr.rpclient.tiles.TileCanvas;
import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.s2db.event.EventType;
import jmr.s2db.event.SystemEvent;
import jmr.s2db.job.JobManager;
import jmr.s2db.tables.Event;
import jmr.util.Logging;
import jmr.util.NetUtil;
import jmr.util.OSUtil;
import jmr.util.SystemUtil;


/*
 * Taken from:
 * http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTreeWithMulticolumns.htm
 */
public class SWTBasic {


	public static boolean DEBUG = true;
	
	private static final Logger 
			LOGGER = Logger.getLogger( SWTBasic.class.getName() );

	
	private Text txtLog;
  
//  private static Cursor cursorHide;

  

	
	final static String NODE_PATH_DEVICES = "/active_devices/";
	
	static String NODE_PATH_THIS_SESSION;

	
	private Client s2db = null;
	
	
	
	

	// fixed tiles
	final PerformanceMonitorTile tilePerf = new PerformanceMonitorTile();
	
	
	
	
	

	private static SWTBasic instance;
	
	public static SWTBasic get() {
		return instance;
	}
	
  
	public void log(final String text) {
		if (null == txtLog) return;
		if (txtLog.isDisposed()) return;

		Logging.log(text);

		final String strText = txtLog.getText() + Text.DELIMITER
						+ text.replace("\n", Text.DELIMITER);
		txtLog.setText(strText);
	}

	public static void close() {
		Logging.log("Application closing. " + new Date().toString());
		
//		Runtime.getRuntime().halt( 98 ); // <<<======== HALT

		new Thread( "Application Abort" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( 5000 );
					System.err.println(
							"Timeout (5s) elapsed. "
							+ "Application aborted." );

					Runtime.getRuntime().halt( 97 ); // <<<======== HALT

					System.exit( 100 );
					Runtime.getRuntime().exit( 100 );
				} catch ( final InterruptedException e ) {
					System.err.println( 
							"Application Abort thread interrupted. "
							+ "Application aborted." );
					e.printStackTrace();
					System.exit( 100 );
					Runtime.getRuntime().exit( 100 );
				}
			}
		}.start();
		
		LOGGER.log( Level.INFO, "Session ending." );
		new Thread( "Shut down S2 session" ) {
			@Override
			public void run() {
				get().s2db.close();
//				System.out.println( "Calling System.exit(0).." );
				SystemUtil.shutdown( 0, 
						"UI close requested, after S2 client close" );
			}
		}.start();
		
		new Thread( "UI Close followup" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( 2000 );
					SystemUtil.shutdown( 0, 
							"UI close requested, in SWTBasic.close()" );
				} catch ( final InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public final static SelectionAdapter selClose = new SelectionAdapter() {
		@Override
		public void widgetSelected(final SelectionEvent arg0) {
			SWTBasic.close();
		}
	};


	public static String report( final Rectangle r ) {
		if ( null==r ) return "(null)";
		return "(size:" + r.width + "," + r.height 
			  + " loc:" + r.x + "," + r.y + ")";
	}

	public static String report( final Point p ) {
		if ( null==p ) return "(null)";
		return "(" + p.x + "," + p.y + ")";
	}
  

    final static public int TRIM = 8;
    final static public int RPIT_CLIENT_WIDTH = 800 + 20;
    final static public int RPIT_CLIENT_HEIGHT = 480 + 16;
	
    
    public static void removeMargins( final GridLayout grid ) {
    	grid.horizontalSpacing = 0;
    	grid.marginTop = 0;
    	grid.marginBottom = 0;
    	grid.marginLeft = 0;
    	grid.marginRight = 0;
    	grid.verticalSpacing = 0;
    	grid.marginHeight = 0;
    	grid.marginWidth = 0;
    }

    
//    final boolean bConsole;
    

    
    public SWTBasic( final boolean bConsole ) {
//    	this.bConsole = bConsole;
    	this.shell = this.buildUI();
    	instance = this;
	}

	final Shell shell;

	final List<TabBase> listTabs = new LinkedList<TabBase>();

	private CTabFolder tabs;
	
	
	public void activate( final TopSection item ) {
		if ( null==item ) return;
		
		for ( final TabBase tab : listTabs ) {
			if ( item.equals( tab.getMenuItem() ) ) {
				final CTabItem cti = tab.getTab();
				if ( null!=cti ) {
					tabs.setSelection( cti );
				}
			}
		}
	}
    
    
	public Shell buildUI() {
		
		if ( DEBUG ) {
			System.out.println( "SWTBasic running in DEBUG mode." );
		}
		
	    /* S2DB stuff */
	    final long lNow = System.currentTimeMillis();
	    final Date now = new Date( lNow );
	    s2db = Client.get();
	    final String strIP = NetUtil.getIPAddress();
	    final String strClass = SWTBasic.class.getName();
	    final String strSessionID = NetUtil.getSessionID();
//		s2db.register( 	NetUtil.getMAC(), strIP, 
//	    				strSessionID, 
//	    				strClass, now );
		final Long seqSession = s2db.register( 
							ClientType.TILE_GUI, strSessionID, strClass );
		if ( null!=seqSession ) {
			LOGGER.info( "Client session " + seqSession );
		} else {
			LOGGER.severe( "Failed to initialize client session." );
		}
		
		final String strDeviceName = s2db.getThisDevice().getName();
		final Map<String,String> 
						mapOptionsRaw = s2db.getThisDevice().getOptions();

		final Map<String,String> mapOptionsNorm = new HashMap<>();
		
	    LOGGER.info( "Device: \"" + strDeviceName + "\"" );
	    for ( final Entry<String, String> entry : mapOptionsRaw.entrySet() ) {
//	    	final String strKey = entry.getKey().trim().toUpperCase();
	    	final String strKey = entry.getKey().trim();
	    	final String strValue = entry.getValue();
	    	if ( ! strValue.isEmpty() ) {
			    LOGGER.info( "Options entry: \"" + strKey + "\""
			    		+ " = \"" + strValue + "\"" );
			    mapOptionsNorm.put( strKey, strValue );
	    	}
	    }
	    
	    final String strRemoteName = mapOptionsNorm.get( "remote" );
	    if ( StringUtils.isNotBlank( strRemoteName ) ) {
	    	LOGGER.info( ()-> "Registering as remote "
	    								+ "\"" + strRemoteName + "\"" );
		    s2db.registerAsRemote( ClientType.TILE_GUI, strRemoteName, strIP );
	    }
	    
	    JobManager.getInstance().setOptions( mapOptionsNorm );
	    
	    final String strData = new Gson().toJson( mapOptionsNorm );
	    
		final Event event = Event.add(
				EventType.SYSTEM, SystemEvent.DEVICE_INFO.name(), 
				strDeviceName, null, 
				strData, lNow, null, null, null );

		if ( null==event ) {
			LOGGER.severe( "Failed to create system event." );
		} else {
			LOGGER.log( Level.INFO, "Session started. "
					+ "IP:" + strIP + ", Session:" + strSessionID + ", "
					+ "DEVICE_INFO Event:" + event.getEventSeq() );
		}
		
	    final TabTiles tTiles = 
	    			new TabTiles( strDeviceName, mapOptionsNorm, false );

		final Perspective perspective = tTiles.getPerspective();
		
		
		
		
		
		
		
		final Display display = UI.display;
		
	    final boolean bTouchscreen = RPiTouchscreen.getInstance().isEnabled();

	    final boolean bMediaServer = 
	    			Perspective.SURVEILLANCE_STAMP.equals( perspective );
	    
//	    final PaletteData palette = new PaletteData(
//	    		new RGB[] { UI.colorWhite.getRGB(), UI.colorBlack.getRGB() } );
//	    final ImageData idHide = new ImageData( 16, 16, 1, palette );
//	    cursorHide = new Cursor( UI.display, idHide, 0, 0 );
//	    idHide.transparentPixel = 0;
	    
	    final int iOptions;
	    if ( bMediaServer ) { // special case
	    	iOptions = SWT.TOOL | SWT.NO_TRIM | SWT.ON_TOP;
	    	
//	    if ( OSUtil.isWin() || !perspective.isFullscreen() ) {
	    } else if ( OSUtil.isWin() || !bTouchscreen ) {
//	    	iOptions = SWT.TOOL | SWT.SHELL_TRIM;
	    	iOptions = SWT.SHELL_TRIM;
	    } else {
	    	iOptions = SWT.TOOL | SWT.ON_TOP | SWT.NO_TRIM;
	    }
	    final Shell shell = new Shell( UI.display, iOptions );

	    if ( bTouchscreen ) {
	    	LOGGER.info( "Display is RPi touchscreen" );
		    shell.setSize( 800, 495 );
	    	shell.setLocation( 0, 0 );
	    
	    } else {
	    	
	    	final Rectangle rectArea = shell.getDisplay().getClientArea();
	    	LOGGER.info( "Display size: " 
	    				+ rectArea.width + " x " + rectArea.height );
	    	
	    	final int iXOffs;
	    	final int iYOffs;
	    	if ( bMediaServer ) {
	    		// iXOffs = 54; // fitted
	    		// iXOffs = 70; // push off
	    		iXOffs = 10; // push off
	    		iYOffs = 30;
	    		new Thread( ()-> {
	    			try { 
	    				Thread.sleep( 2000 );
	    				display.asyncExec( ()-> shell.setLocation( 1257, 48 ) );
	    				Thread.sleep( 2000 );
	    				display.asyncExec( ()-> shell.setLocation( 1257, 48 ) );
    				} catch ( final InterruptedException e ) {};
	    		} ).start();
	    		
	    	} else {
	    		iXOffs = 60;
	    		iYOffs = 66;
	    	}
	    	
	    	final int iX = perspective.getColCount() * 150 + iXOffs;
	    	final int iY = perspective.getRowCount() * 150 + iYOffs;
	    	
	    	if ( OSUtil.isWin() ) {
	    		shell.setSize( iX + 20, iY + 20 ); // window trim
	    	} else {
	    		shell.setSize( iX, iY );
	    	}
	    	
	    	if ( 1920 == rectArea.width && 1080 == rectArea.height ) { 
		    	LOGGER.info( "FHD screen, probably headless display. "
		    					+ "Adjusting for Conky." );
	    		shell.setLocation( 510, 12 );
	    	} else if ( shell.getLocation().x < 20 ) {
		    	LOGGER.info( "Display is probably normal full-size screen" );
	    		shell.setLocation( 50, 50 );
	    	}
	    }
		
		
		
	    final GridLayout glTop = new GridLayout( 3, false );
	    removeMargins( glTop );
		shell.setLayout( glTop );

//		final GridData gdLeft = new GridData( SWT.DEFAULT, SWT.FILL, false, true );
//		gdLeft.widthHint = 30;
		final GridData gdRight = new GridData( SWT.DEFAULT, SWT.FILL, false, true );
		gdRight.widthHint = 45;
		gdRight.heightHint = 450;
		final GridData gdMain = new GridData( SWT.FILL, SWT.FILL, true, true );
		gdMain.widthHint = 755;
		gdMain.heightHint = 450;

//	    final Composite compLeft = new Composite( shell, SWT.NONE );
//	    compLeft.setLayoutData( gdLeft );
//	    compLeft.setBackground( UI.COLOR_BLACK );
	    final Composite compMain = new Composite( shell, SWT.NONE );
	    // margin between the main and right control. all the way up and down. 
	    compMain.setBackground( UI.COLOR_BLACK ); 
	    compMain.setLayoutData( gdMain );
	    
//	    final Composite compRight = new Composite( shell, SWT.NONE );
	    final Canvas compRight = new Canvas( shell, SWT.NONE );
	    compRight.setLayoutData( gdRight );
//	    compRight.setBackground( UI.COLOR_DARK_BLUE );
	    compRight.setBackground( UI.COLOR_BLACK );
	    
	    
	    final Canvas canvasMonitor = new Canvas( compRight, SWT.NONE );
	    final GridData gdCanvas = new GridData();
	    gdCanvas.heightHint = 180;
	    gdCanvas.widthHint = 50;
		canvasMonitor.setLayoutData( gdCanvas );
		canvasMonitor.setLocation( 0, 0 );
		canvasMonitor.setSize( 50, 180 );
		canvasMonitor.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent event ) {
				paintMonitorCanvas( event.gc );
			}
		});
		UI.listRefreshCanvases.add( canvasMonitor );
	    
		
	    final int iH = 60;
	    compRight.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent event ) {
				final GC gc = event.gc;
				if ( null==gc ) return;
				
				gc.setBackground( UI.COLOR_DARK_BLUE );
//				gc.fillRectangle( 0, 180, 50, 300 );
				
				
				gc.setBackground( UI.COLOR_GRAY );
				gc.setForeground( UI.COLOR_BLUE );
				
//				gc.drawLine( 000, 000, 050, 480 );
//				gc.drawLine(  25,   0,  50, 100 );
//				gc.drawLine(  50, 100,  25, 200 );
//				gc.drawLine(  25,   0,   0, 100 );
//				gc.drawLine(  25, 200,   0, 100 );
//				gc.drawLine(  25, 480,  50, 380 );
//				gc.drawLine(  25, 480,   0, 380 );
				gc.drawLine(  30, 480,  50, 480 );
				gc.drawLine(  44, 480,  44, 470 );
				
				int iY = 0; iY = 3 * iH;
				final int iRH = iH - 20;
				for ( int i=0; i<5; i++ ) {
					gc.fillRoundRectangle( 8, iY + 10, 33, iRH, 10, 10 ); iY += iH;
				}

				gc.setForeground( UI.COLOR_BLACK );
				
				iY = 3 * iH - 8;
				int iXO = -3;
				gc.setFont( Theme.get().getFont( 11 ) );
				gc.drawText( "Top", 16+iXO, iY + 30 ); iY += iH;
				gc.drawText( "Tiles", 14+iXO, iY + 30 ); iY += iH;
				iY += iH; iY += iH;
				gc.drawText( "EXIT",  14+iXO, iY + 30 ); iY += iH;

				iY = 3 * iH - 8;
				gc.setFont( Theme.get().getFont( 10 ) );
				iY += iH; iY += iH;
				gc.drawText( "S2DB",  13+iXO, iY + 30 ); iY += iH;
//				gc.drawText( "Calib", 005+iXO, iY + 30 ); iY += iH;
				gc.drawText( "Device",11+iXO, iY + 30 ); iY += iH;
			}
		});
	    
	    compRight.addMouseListener( new MouseAdapter() {
	    	@Override
	    	public void mouseUp( final MouseEvent event ) {
	    		final int y = event.y / iH - 3;
	    		TopSection ts = null;
	    		switch ( y ) {
//	    			case 0	: SWTBasic.get().activate( TopSection. ); 
//	    			case 0	: ts = TopSection.DAILY_INFO; break;
		    		case 0 : {
		    			ts = TopSection.TILES;
		    			TileCanvas.getInstance().setPerspective( 
		    										Perspective.TOP_PAGE );
		    			break;
		    		}
    				case 1	: ts = TopSection.TILES; break;
    				case 2	: ts = TopSection.S2DB; break; 
    				case 3	: ts = TopSection.DEVICE_CONTROLS; break; 
    				case 4	: SWTBasic.close(); 
	    		}
				if ( null!=ts ) {
					SWTBasic.get().activate( ts ); 
				}
	    	}
		});
	    
	    final GridLayout gl = new GridLayout( 10, true );
	    removeMargins( gl );
	    compMain.setLayout( gl );
	    
	    
	    // should be unnecessary
	    shell.addShellListener( new ShellAdapter() {
	    	@Override
	    	public void shellClosed( final ShellEvent event ) {
	    		SWTBasic.close();
	    	}
		});
	
	    
	    
	    tabs = new CTabFolder( compMain, 
	    		SWT.TOP | SWT.NO_TRIM | SWT.SINGLE | SWT.FLAT );
	    final int iCols = gl.numColumns - 0;
		tabs.setLayoutData( 
	    		new GridData( SWT.FILL, SWT.FILL, true, true, iCols, 1 ) );
	    
	    tabs.setSimple( true );
	    tabs.setMaximizeVisible( false );
	    tabs.marginHeight = 0;
	    tabs.marginWidth = 0;
	    tabs.setBorderVisible( false );
	    tabs.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent event ) {
				// this is the blue space immediate over the tab region
				event.gc.setBackground( UI.COLOR_BLACK );
				event.gc.fillRectangle( tabs.getBounds() );
			}
		});
	
	    
	    final TabDailyInfo tDailyInfo = new TabDailyInfo();
	    tDailyInfo.addToTabFolder( tabs );
	    listTabs.add( tDailyInfo );
	    
	    
//	    final ClientSession session = ClientSession.get();
//	    final Server server = new Server( session );
//	    
//	    /* S2DB stuff */
//	    final Date now = new Date();
//	    s2db = Client.get();
//	    final String strIP = NetUtil.getIPAddress();
//	    final String strClass = SWTBasic.class.getName();
//	    final String strSessionID = NetUtil.getSessionID();
////		s2db.register( 	NetUtil.getMAC(), strIP, 
////	    				strSessionID, 
////	    				strClass, now );
//		s2db.register( 	strSessionID, strClass );
//		
//		final String strDeviceName = s2db.getThisDevice().getName();
//		final Map<String,String> mapOptions = s2db.getThisDevice().getOptions();
//		
//	    LOGGER.info( "Device: \"" + strDeviceName + "\"" );
//	    for ( final Entry<String, String> entry : mapOptions.entrySet() ) {
//		    LOGGER.info( "Options entry: \"" + entry.getKey() + "\""
//		    		+ " = \"" + entry.getValue() + "\"" );
//	    }
//	
//		LOGGER.log( Level.INFO, "Session started. "
//				+ "IP:" + strIP + ", Session:" + strSessionID );
//	    
	    final Map<String,String> mapSessionPage = new HashMap<>();
	    mapSessionPage.put( "page.source.class", SWTBasic.class.getName() );
	    mapSessionPage.put( "session.start", "" + now.getTime() );
	    mapSessionPage.put( "session.id", "" + NetUtil.getSessionID() );
	    mapSessionPage.put( "device.mac", NetUtil.getMAC() );
	    mapSessionPage.put( "process.name", NetUtil.getProcessName() );
	    mapSessionPage.put( "device.ip", NetUtil.getIPAddress() );
	    mapSessionPage.put( "device.host.port", "none" );
	    mapSessionPage.put( "executable", OSUtil.getProgramName() );
	    mapSessionPage.put( "device.name", strDeviceName );
	    final String strSessionPath = "/Sessions/" + NetUtil.getSessionID();
	    try {
			final long seqSessionPage = s2db.savePage( 
										strSessionPath, mapSessionPage );
		    s2db.setSessionPage( seqSessionPage );
	    } catch ( final IllegalStateException e ) { 
	    	if ( DEBUG ) { // no session in debug; ignore
	    		LOGGER.info( "Ignoring IllegalStateException while in debug." );
	    		LOGGER.info( "(Port bind error is expected while "
	    				+ "another S2 program is running locally.);" ); 
	    	} else {
	    		e.printStackTrace();
	    		Runtime.getRuntime().exit( 100 );
	    	}
	    }

//	    final TabControls tControls = new TabControls( server );
	    final TabControls tControls = new TabControls();
	    tControls.addToTabFolder( tabs );
	    listTabs.add( tControls );
	    
	    final TabS2DB tS2DB = new TabS2DB( s2db );
	    tS2DB.addToTabFolder( tabs );
	    listTabs.add( tS2DB );

//	    final TabShowDB tShowDB = new TabShowDB( server );
//	    tShowDB.addToTabFolder( tabs );
//	    listTabs.add( tShowDB );

	    final TabTreeDemo tTreeDemo = new TabTreeDemo();
	    tTreeDemo.addToTabFolder( tabs );
	    listTabs.add( tTreeDemo );

//	    final TabCanvas tCanvas = new TabCanvas();
//	    tCanvas.addToTabFolder( tabs );
//	    listTabs.add( tCanvas );

//	    final TabTiles tTiles = new TabTiles( strDeviceName, mapOptions );
	    tTiles.addToTabFolder( tabs );
	    listTabs.add( tTiles );

	    final TabLog tLog = new TabLog();
	    tLog.addToTabFolder( tabs );
	    listTabs.add( tLog );
	    
	    this.txtLog = tLog.getTextWidget();

//	    tabs.setSelection( tDailyInfo.getTab() );
	    tabs.setSelection( tTiles.getTab() );
	    
	    tabs.addSelectionListener( new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected( final SelectionEvent event ) {
	    		if ( event.widget instanceof CTabFolder ) {
	    			final CTabFolder tabs = (CTabFolder)(event.widget);
					final CTabItem item = tabs.getSelection();
					final String strName = item.getText().trim();
					
					LOGGER.fine( "Tab selected: " + strName );
//	    			server.postData( NODE_PATH_THIS_SESSION + "Selection/", 
//	    					"tab.selected", strName,
//	    					"time", ""+System.currentTimeMillis() );
	    		}
	    	}
		});
	
	    
	    
	    log( new Date().toString() + "\nStarted." );

//	    Logging.setDir( session.getSessionDir() );

	    log( "Session ID: " + NetUtil.getSessionID() );
	    
	    final String strTitle;
	    if ( StringUtils.isBlank( strDeviceName ) ) {
	    	strTitle = NetUtil.getSessionID();
	    } else {
	    	strTitle = NetUtil.getSessionID() 
	    					+ "  -  \"" + strDeviceName + "\"";
	    }
	    
	    //TODO add "remote" name to the title
	    
	    shell.setText( strTitle );
	    
	    
		//    for ( final Display display : Display.)
	    log( "Display Information: " );
	    log( "\tDisplay (bounds): " + report( display.getBounds() ) );
	    log( "\tDisplay (client): " + report( display.getClientArea() ) );
		log( "\tDisplay color depth: " + display.getDepth() );
		log( "\tDisplay DPI: " + display.getDPI() );
		log( "\tDisplay touch enabled: " + display.getTouchEnabled() );
	    
	    final Monitor monitor = display.getPrimaryMonitor();
		log( "\tPrimary screen (bounds): " + report( monitor.getBounds() ) );
		log( "\tPrimary screen (client): " + report( monitor.getClientArea() ) );
	    
//	    log( "ShareDB initializing.." );
//
//	    final ClientSession session = ClientSession.get();
//	    final Server server = new Server( session );
	    
	    log( "ShareDB ready." );

	    NODE_PATH_THIS_SESSION = 
	    			NODE_PATH_DEVICES + NetUtil.getSessionID() + "/";
	    
//	    server.postData(	NODE_PATH_THIS_SESSION, 
//							"session.start", 
//							Long.toString( System.currentTimeMillis() ) );
	    
	    
	    shell.open();
	    
	    OSUtil.register( strDeviceName );
	    
//	    final ShellTopMenu menu = new ShellTopMenu( shell );

//	    compLeft.addMouseMoveListener( new MouseMoveListener() {
//			@Override
//			public void mouseMove( final MouseEvent event ) {
//				menu.show( true );
//			}
//		});
//	    compMain.addMouseMoveListener( new MouseMoveListener() {
//			@Override
//			public void mouseMove( final MouseEvent event ) {
//				if ( event.x < 80 ) {
//					menu.show( true );
//				}
//			}
//		});
	    
	    if ( DEBUG ) {
//	    	JobMonitor.get().setDebug( true );
	    	Client.get().setDebug( true );
	    }
	    
	    return shell;
	}

	
	public void paintMonitorCanvas( final GC gc ) {
		
		// draw the performance tile at the edge of the nav pane
		final TileBase tile = tilePerf;
		
		final Device device = gc.getDevice();
		
		final int iX = 0; // 750; // 5 * 150;
		final int iY = 0; // 0 * 150;
		final int iW = 50;
		final int iH = 180;

		final Image imageBuffer = new Image( device, iW, iH );
		tile.paint( imageBuffer, System.currentTimeMillis() );
		gc.drawImage( imageBuffer, iX, iY );
//		gc.fillOval( 750, 100, 20, 20 );
		imageBuffer.dispose();
	}
	


	public static void main( final String[] args ) {
		/*
		 * pass launch parameter "DEBUG" to enable debug mode.
		 */
		if ( args.length > 0 && args[0].equalsIgnoreCase( "DEBUG" ) ) {
			DEBUG = true;
		}
		
		boolean bConsole = false;
		for ( final String arg : args ) {
			if ( arg.toLowerCase().endsWith( "console" ) ) {
				bConsole = true;
			}
		}
		
		final SWTBasic ui = new SWTBasic( bConsole );
	    
		UI.runUIWatchdog();
		UI.runUIRefresh();
	    while ( ! ui.shell.isDisposed() ) {
	      if ( ! UI.display.readAndDispatch() ) {
	    	  UI.notifyUIIdle();
	    	  UI.display.sleep();
	      }
	    }
	    UI.display.dispose();
		Logging.log( "Application closing. " + new Date().toString() );
	}

	
}
