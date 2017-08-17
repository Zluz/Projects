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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import jmr.rpclient.tab.TabBase;
import jmr.rpclient.tab.TabCanvas;
import jmr.rpclient.tab.TabControls;
import jmr.rpclient.tab.TabDailyInfo;
import jmr.rpclient.tab.TabLog;
import jmr.rpclient.tab.TabS2DB;
import jmr.rpclient.tab.TabTreeDemo;
import jmr.rpclient.tab.TopSection;
import jmr.s2db.Client;
import jmr.util.Logging;
import jmr.util.NetUtil;
import jmr.util.OSUtil;

/*
 * Taken from:
 * http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTreeWithMulticolumns.htm
 */
public class SWTBasic {

	
	private static final Logger 
			LOGGER = Logger.getLogger( SWTBasic.class.getName() );

	
	private Text txtLog;
  
//  private static Cursor cursorHide;

  

	
	final static String NODE_PATH_DEVICES = "/active_devices/";
	
	static String NODE_PATH_THIS_SESSION;

	
	private Client s2db = null;
	

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
		LOGGER.log( Level.INFO, "Session ending." );
		get().s2db.close();
		System.exit(0);
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

    
    
    public SWTBasic() {
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
		
		final Display display = UI.display;
	    
//	    final PaletteData palette = new PaletteData(
//	    		new RGB[] { UI.colorWhite.getRGB(), UI.colorBlack.getRGB() } );
//	    final ImageData idHide = new ImageData( 16, 16, 1, palette );
//	    cursorHide = new Cursor( UI.display, idHide, 0, 0 );
//	    idHide.transparentPixel = 0;
	    
	    final int iOptions;
	    if ( OSUtil.isWin() ) {
//	    	iOptions = SWT.TOOL | SWT.SHELL_TRIM;
	    	iOptions = SWT.SHELL_TRIM;
	    } else {
	    	iOptions = SWT.TOOL | SWT.ON_TOP | SWT.NO_TRIM;
	    }
	    final Shell shell = new Shell( UI.display, iOptions );
	    shell.setSize( RPIT_CLIENT_WIDTH, RPIT_CLIENT_HEIGHT );
	    
	    
	    final GridLayout glTop = new GridLayout( 3, false );
	    removeMargins( glTop );
		shell.setLayout( glTop );

		final GridData gdLeft = new GridData( SWT.DEFAULT, SWT.FILL, false, true );
		gdLeft.widthHint = 30;
		final GridData gdRight = new GridData( SWT.DEFAULT, SWT.FILL, false, true );
		gdRight.widthHint = 10;
		final GridData gdMain = new GridData( SWT.FILL, SWT.FILL, true, true );

	    final Composite compLeft = new Composite( shell, SWT.NONE );
	    compLeft.setLayoutData( gdLeft );
	    compLeft.setBackground( UI.COLOR_BLACK );
	    final Composite compMain = new Composite( shell, SWT.NONE );
	    compMain.setLayoutData( gdMain );
	    compMain.setBackground( UI.COLOR_BLACK );
	    final Composite compRight = new Composite( shell, SWT.NONE );
	    compRight.setLayoutData( gdRight );
	    compRight.setBackground( UI.COLOR_BLACK );
	    
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
	
	    
	    
	    tabs = new CTabFolder( compMain, SWT.TOP | SWT.NO_TRIM );
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
				event.gc.setBackground( UI.COLOR_BLACK );
				event.gc.fillRectangle( tabs.getBounds() );
			}
		});
	
	    
	    final TabDailyInfo tDailyInfo = new TabDailyInfo();
	    tDailyInfo.addToTabFolder( tabs );
	    listTabs.add( tDailyInfo );
	    
//	    final ClientSession session = ClientSession.get();
//	    final Server server = new Server( session );
	    
	    /* S2DB stuff */
	    final Date now = new Date();
	    s2db = Client.get();
	    final String strIP = NetUtil.getIPAddress();
	    final String strClass = SWTBasic.class.getName();
	    final String strSessionID = NetUtil.getSessionID();
		s2db.register( 	NetUtil.getMAC(), strIP, 
	    				strSessionID, 
	    				strClass, now );
		
//		S2DBLogHandler.registerLoggers();
		
		LOGGER.log( Level.INFO, "Session started. "
				+ "IP:" + strIP + ", Session:" + strSessionID );
	    
	    final Map<String,String> mapSessionPage = new HashMap<>();
	    mapSessionPage.put( "page.source.class", SWTBasic.class.getName() );
	    mapSessionPage.put( "session.start", "" + now.getTime() );
	    mapSessionPage.put( "session.id", "" + NetUtil.getSessionID() );
	    mapSessionPage.put( "device.mac", NetUtil.getMAC() );
	    mapSessionPage.put( "process.name", NetUtil.getProcessName() );
	    mapSessionPage.put( "device.ip", NetUtil.getIPAddress() );
	    mapSessionPage.put( "device.host.port", "none" );
	    final String strSessionPath = "/Sessions/" + NetUtil.getSessionID();
		final long seqSessionPage = s2db.savePage( strSessionPath, mapSessionPage );
	    s2db.setSessionPage( seqSessionPage );

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

	    final TabCanvas tCanvas = new TabCanvas();
	    tCanvas.addToTabFolder( tabs );
	    listTabs.add( tCanvas );

	    final TabLog tLog = new TabLog();
	    tLog.addToTabFolder( tabs );
	    listTabs.add( tLog );
	    
	    this.txtLog = tLog.getTextWidget();

	    
	
	    tabs.setSelection( tDailyInfo.getTab() );
	    
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
	    
	    shell.setText( NetUtil.getSessionID() );
	    
	    
	    
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
	    
	    final ShellTopMenu menu = new ShellTopMenu( shell );

	    compLeft.addMouseMoveListener( new MouseMoveListener() {
			@Override
			public void mouseMove( final MouseEvent event ) {
				menu.show( true );
			}
		});
	    compMain.addMouseMoveListener( new MouseMoveListener() {
			@Override
			public void mouseMove( final MouseEvent event ) {
				if ( event.x < 80 ) {
					menu.show( true );
				}
			}
		});
	    
	    if ( 800 == display.getBounds().width ) {
	    	log( "Display is RPi touchscreen" );
	    	shell.setLocation( -TRIM, -TRIM );
	    
	//    } else if ( shell.getSize().x > display.getClientArea().x ) {
	//    } else if ( display.getBounds().x > 1000 ) {
	    } else {
	    	log( "Display is probably normal full-size screen" );
	    	if ( shell.getLocation().x < 20 ) {
	    		shell.setLocation( 50, 50 );
	    	}
	//    } else {
	//    	shell.setLocation( 0, 0 );
	    }
	    return shell;
	}

	public static void main( final String[] args ) {

		final SWTBasic ui = new SWTBasic();
	    
	    while (!ui.shell.isDisposed()) {
	      if (!UI.display.readAndDispatch()) {
	    	  UI.display.sleep();
	      }
	    }
	    UI.display.dispose();
		Logging.log( "Application closing. " + new Date().toString() );
	}

	
}
