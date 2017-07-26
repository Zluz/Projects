package jmr.rpclient;
/*
 * Create a Tree with columns
 * 
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import jmr.rpclient.tab.TabControls;
import jmr.rpclient.tab.TabDailyInfo;
import jmr.rpclient.tab.TabShowDB;
import jmr.rpclient.tab.TabTreeDemo;
import jmr.sharedb.ClientSession;
import jmr.sharedb.Server;
import jmr.util.Logging;
import jmr.util.NetUtil;
import jmr.util.OSUtil;

/*
 * Taken from:
 * http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTreeWithMulticolumns.htm
 */
public class SWTBasic {
	
	
	private static Text txtLog;
  
//  private static Cursor cursorHide;

  

	
	final static String NODE_PATH_DEVICES = "/active_devices/";
	
	static String NODE_PATH_THIS_SESSION;
	
  
  
	public static void log(final String text) {
		if (null == txtLog) return;
		if (txtLog.isDisposed()) return;

		Logging.log(text);

		final String strText = txtLog.getText() + Text.DELIMITER
						+ text.replace("\n", Text.DELIMITER);
		txtLog.setText(strText);
	}

	public static void close() {
		Logging.log("Application closing. " + new Date().toString());
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
	
    
    
    
	public static void main( final String[] args ) {
		
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
	    
	//    new org.eclipse.swt.widgets.Shell();
	    
	//    shell.setLayout(new FillLayout());
	    final GridLayout gl = new GridLayout( 10, true );
		shell.setLayout( gl );
	    shell.setText( "Test SWT" );
	    
	    // should be unnecessary
	    shell.addShellListener( new ShellAdapter() {
	    	@Override
	    	public void shellClosed( final ShellEvent event ) {
	    		SWTBasic.close();
	    	}
		});
	
	    
	    
	    final CTabFolder tabs = new CTabFolder( shell, SWT.TOP | SWT.NO_TRIM );
	    final int iCols = gl.numColumns - 0;
		tabs.setLayoutData( 
	    		new GridData( SWT.FILL, SWT.FILL, true, true, iCols, 1 ) );
	    
	    tabs.setSimple( false );
	    tabs.setMaximizeVisible( false );
	
	    final String TAB_PAD_SUFFIX = "      ";
	    final String TAB_PAD_PREFIX = "   ";
	    
	    final TabDailyInfo tDailyInfo = new TabDailyInfo();
	    final CTabItem tabDailyInfo = tDailyInfo.addToTabFolder( tabs );

	    
	    final ClientSession session = ClientSession.get();
	    final Server server = new Server( session );

	    final TabControls tControls = new TabControls( server );
	    /*final CTabItem tabShowDB = */ tControls.addToTabFolder( tabs );

	    final TabShowDB tShowDB = new TabShowDB( server );
	    /*final CTabItem tabShowDB = */ tShowDB.addToTabFolder( tabs );

	    final TabTreeDemo tTreeDemo = new TabTreeDemo();
	    /*final CTabItem tabTreeDemo = */ tTreeDemo.addToTabFolder( tabs );

	    
	    final CTabItem tabCanvas = new CTabItem( tabs, SWT.NONE );
	    tabCanvas.setText( TAB_PAD_PREFIX + "Canvas" + TAB_PAD_SUFFIX );
	    tabCanvas.setShowClose( true );
	    final Composite compCanvas = new Composite( tabs, SWT.NONE );
	    compCanvas.setLayout( new FillLayout() );
	    tabCanvas.setControl( compCanvas );
	
	    final CTabItem tabLog = new CTabItem( tabs, SWT.NONE );
	    tabLog.setText( TAB_PAD_PREFIX + "Log" + TAB_PAD_SUFFIX );
	    tabLog.setShowClose( false );
	    txtLog = new Text( tabs, SWT.MULTI | SWT.V_SCROLL );
	    tabLog.setControl( txtLog );
	
	    tabs.setSelection( tabDailyInfo );
	    
	    tabs.addSelectionListener( new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected( final SelectionEvent event ) {
	    		if ( event.widget instanceof CTabFolder ) {
	    			final CTabFolder tabs = (CTabFolder)(event.widget);
					final CTabItem item = tabs.getSelection();
					final String strName = item.getText().trim();
					
	    			server.postData( NODE_PATH_THIS_SESSION + "Selection/", 
	    					"tab.selected", strName,
	    					"time", ""+System.currentTimeMillis() );
	    		}
	    	}
		});
	
	    
	    
	    
	    final Canvas canvas = new Canvas( compCanvas, SWT.NONE );
	    canvas.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {
				final Rectangle r = canvas.getClientArea();
				final int iXC = r.width / 2;
				final int iYC = r.height / 2;
				e.gc.drawOval( 0, 0, r.width, r.height );
				e.gc.drawLine( 0, iYC, iXC, r.height );
				e.gc.drawLine( 0, iYC, iXC, 0 );
				e.gc.drawLine( iXC, 0, r.width, iYC );
				e.gc.drawLine( iXC, r.height, r.width, iYC );
			}
		});
	    
	    log( new Date().toString() + "\nStarted." );

	    Logging.setDir( session.getSessionDir() );

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
	    
	    server.postData(	NODE_PATH_THIS_SESSION, 
							"session.start", 
							Long.toString( System.currentTimeMillis() ) );
	    
	    
	    shell.open();
	    
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
	    
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch()) {
	        display.sleep();
	      }
	    }
	    display.dispose();
		Logging.log( "Application closing. " + new Date().toString() );
	}

}
