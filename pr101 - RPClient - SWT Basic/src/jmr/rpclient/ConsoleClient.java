package jmr.rpclient;
/*
 * Create a Tree with columns
 * 
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;

import jmr.rpclient.screen.TextCanvas;
import jmr.rpclient.screen.TextScreen;
import jmr.rpclient.tab.TabTiles;
import jmr.rpclient.tiles.PerformanceMonitorTile;
import jmr.rpclient.tiles.Perspective;
import jmr.rpclient.tiles.TileBase;
import jmr.rpclient.tiles.TileCanvas;
import jmr.s2db.Client;
import jmr.s2db.job.JobManager;
import jmr.util.Logging;
import jmr.util.NetUtil;
import jmr.util.OSUtil;



public class ConsoleClient {

	
	private static final Logger 
			LOGGER = Logger.getLogger( ConsoleClient.class.getName() );

  

	
	final static String NODE_PATH_DEVICES = "/active_devices/";
	
	static String NODE_PATH_THIS_SESSION;

	
	private Client s2db = null;
	
	
	
	

	// fixed tiles
	final PerformanceMonitorTile tilePerf = new PerformanceMonitorTile();
	
	
	
	
	

	private static ConsoleClient instance;
	
	boolean bShutdown = false;
	
	
	public static ConsoleClient get() {
		return instance;
	}
	
  
	public void log( final String text ) {
		System.out.println( text );
//		if (null == txtLog) return;
//		if (txtLog.isDisposed()) return;
//
//		Logging.log(text);
//
//		final String strText = txtLog.getText() + Text.DELIMITER
//						+ text.replace("\n", Text.DELIMITER);
//		txtLog.setText(strText);
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
			ConsoleClient.close();
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


    
    public ConsoleClient() {
    	instance = this;
    	this.buildUI();
	}


    
    private TileCanvas tiles = null;

	public Perspective getPerspective() {
		return this.tiles.getPerspective(); 
	}
    
	public void buildUI() {
		
	    /* S2DB stuff */
	    final Date now = new Date();
	    s2db = Client.get();
	    final String strIP = NetUtil.getIPAddress();
	    final String strClass = ConsoleClient.class.getName();
	    final String strSessionID = NetUtil.getSessionID();
//		s2db.register( 	NetUtil.getMAC(), strIP, 
//	    				strSessionID, 
//	    				strClass, now );
		s2db.register( 	strSessionID, strClass );
		
		final String strDeviceName = s2db.getThisDevice().getName();
		final Map<String,String> mapOptions = s2db.getThisDevice().getOptions();
		
	    LOGGER.info( "Device: \"" + strDeviceName + "\"" );
	    for ( final Entry<String, String> entry : mapOptions.entrySet() ) {
		    LOGGER.info( "Options entry: \"" + entry.getKey() + "\""
		    		+ " = \"" + entry.getValue() + "\"" );
	    }
	
	    JobManager.getInstance().setOptions( mapOptions );
	    
		LOGGER.log( Level.INFO, "Session started. "
				+ "IP:" + strIP + ", Session:" + strSessionID );
	    
		
	    TabTiles tabtiles = new TabTiles( strDeviceName, mapOptions, true );
	    this.tiles = tabtiles.getTiles();

		
		
		
		
	    final Map<String,String> mapSessionPage = new HashMap<>();
	    mapSessionPage.put( "page.source.class", ConsoleClient.class.getName() );
	    mapSessionPage.put( "session.start", "" + now.getTime() );
	    mapSessionPage.put( "session.id", "" + NetUtil.getSessionID() );
	    mapSessionPage.put( "device.mac", NetUtil.getMAC() );
	    mapSessionPage.put( "process.name", NetUtil.getProcessName() );
	    mapSessionPage.put( "device.ip", NetUtil.getIPAddress() );
	    mapSessionPage.put( "device.host.port", "none" );
	    mapSessionPage.put( "executable", OSUtil.getProgramName() );
	    mapSessionPage.put( "device.name", strDeviceName );
	    final String strSessionPath = "/Sessions/" + NetUtil.getSessionID();
		final long seqSessionPage = s2db.savePage( strSessionPath, mapSessionPage );
	    s2db.setSessionPage( seqSessionPage );

	    
	    
	    log( new Date().toString() + "\nStarted." );

//	    Logging.setDir( session.getSessionDir() );

	    log( "Session ID: " + NetUtil.getSessionID() );
	    
	
//	    log( "ShareDB initializing.." );
//
//	    final ClientSession session = ClientSession.get();
//	    final Server server = new Server( session );
	    
	    log( "ShareDB ready." );

	    NODE_PATH_THIS_SESSION = 
	    			NODE_PATH_DEVICES + NetUtil.getSessionID() + "/";
	    

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
	
	

	public boolean isShuttingDown() {
		return this.bShutdown;
	}
	
	
	private String strStatus = "";
	
	public void showStatus( final String strStatus ) {
		this.strStatus = strStatus;
	}
	
	

	public static void main( final String[] args ) {
//		boolean bTrue = true;

		final ConsoleClient ui = new ConsoleClient();

//		 Toolkit.init();
//	        while (true) {
//	        	int y = 0;
//	            CharColor color = new CharColor(CharColor.BLACK, CharColor.WHITE);
//	            InputChar c = Toolkit.readCharacter();
//	            if ('q' == c.getCharacter())
//	                break;
//	            Toolkit.printString(
//	            		String.format("c : %c", c.getCharacter()), 0, y++, color);
//	        }
//	        Toolkit.shutdown();
		
		
		
		
		
		final TextCanvas text = TextCanvas.getInstance();

//		final ConsoleClient ui = new ConsoleClient();
		ui.showStatus( "(starting)" );

		
		long lFrame = 0;
	    while ( !ui.isShuttingDown() ) {
	    	try {
	    		lFrame++;
	    		
	    		final TextScreen screen = text.getScreen();
				screen.print( 60, 0, "frame " + lFrame );
				screen.print( 10, 0, "tiles:" + ui.getPerspective().name() );
				screen.print( 10, screen.getRows()-1, ui.strStatus );
	    		
	    		
				text.paint();
	    		Thread.sleep( 200 );
	    		

//	    		if ( bis.available()>0 ) {
//	    			ui.tiles.processKey( (char)bis.read() );
//	    		}
//	    		if ( 0!=input[0] ) {
//	    			ui.tiles.processKey( input[0] );
//	    			input[0] = 0;
//	    		}
	    		
	    		
	    	} catch ( final InterruptedException e ) {
	    		Logging.log( e.toString() + " encountered." );
	    		ui.bShutdown = true;
	    	} catch ( final Exception e ) {
				e.printStackTrace();
	    		ui.bShutdown = true;
			}
	    }
		
		Logging.log( "Application closing. " + new Date().toString() );
	}

	
}
