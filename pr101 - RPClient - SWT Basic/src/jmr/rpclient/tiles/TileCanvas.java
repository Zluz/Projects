package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import jmr.rpclient.ConsoleClient;
import jmr.rpclient.RPiTouchscreen;
import jmr.rpclient.screen.TextCanvas;
import jmr.rpclient.screen.TextScreen;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;
import jmr.util.NetUtil;
import jmr.util.OSUtil;

public class TileCanvas {

	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( TileCanvas.class.getName() );

	public static final int REFRESH_SLEEP = 100;
	
	public final static int TRIM_X = 0;
	public final static int TRIM_Y = 10;
	
	
	private Canvas canvas;
	
	private long lPaintCount = 0;
	
	private String strDeviceDescription;
	
	final Perspective perspective;
	
	final boolean bConsole;
	
	final private Map<String,String> mapOptions = new HashMap<>();
	
//	
//	// fixed tiles
//	final PerformanceMonitorTile tilePerf = new PerformanceMonitorTile();
//	
	
	
	public TileCanvas(	final String strDeviceDescription, 
						final String strPerspective,
						final boolean bConsole,
						final Map<String,String> mapOptions ) {
		this.perspective = Perspective.getPerspectiveFor( strPerspective );
		this.strDeviceDescription = strDeviceDescription;
		this.bConsole = bConsole;
		this.mapOptions.clear();
		this.mapOptions.putAll( mapOptions );
	}
	
	
//	public void rotate( final GC gc ) {}
	
	public Perspective getPerspective() {
		return this.perspective;
	}
	
	
	public void buildConsole() {
//		this.mapOptions.clear();
//		this.mapOptions.putAll( mapOptions );
		
		final Thread threadPaintConsole = new Thread( "Paint Console" ) {
			@Override
			public void run() {
				try {
					while ( !ConsoleClient.get().isShuttingDown() ) {
						Thread.sleep( 100 );
						
						final TextScreen screen = TextCanvas.getInstance().getScreen();
					
						for ( final TileGeometry geo : 
											TileCanvas.this.getTiles() ) {
							geo.tile.paint( screen );
						}
						
					}
				} catch ( final InterruptedException e ) {
//					e.printStackTrace();
					// just quit..
				}
			}
		};
		threadPaintConsole.start();
	}
	
	
	public Composite buildUI( final Composite parent ) {
		parent.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
//		this.mapOptions.clear();
//		this.mapOptions.putAll( mapOptions );

		final String strPad = "              ";
		final String strInfo = strPad 
				+ "IP: " + NetUtil.getIPAddress() + strPad
				+ "MAC: " + NetUtil.getMAC() + strPad
				+ "Device: " + strDeviceDescription + strPad
				+ "Executable: " + OSUtil.getProgramName() + strPad
//				+ "Process Name: " + NetUtil.getProcessName() + strPad
				;
		
	    this.canvas = new Canvas( parent, SWT.NO_BACKGROUND );
	    if ( RPiTouchscreen.getInstance().isEnabled() ) {
	    	this.canvas.setCursor( UI.CURSOR_HIDE );
	    }
    	
	    final Display display = parent.getDisplay();
	    canvas.addPaintListener( getPaintListener( display, strInfo, mapOptions ) );
	    
	    canvas.addMouseListener( getMouseListener() );
	    

//		final Thread threadRefresh = new Thread() {
//			@Override
//			public void run() {
//				try {
//					do {
//						UI.notifyUIIdle();
//						Thread.sleep( REFRESH_SLEEP );
//						if ( !canvas.isDisposed() ) {
//							canvas.getDisplay().asyncExec( new Runnable() {
//								@Override
//								public void run() {
//									canvas.redraw();
//								}
//							});
//						}
//					} while ( !parent.isDisposed() );
//				} catch ( final InterruptedException e ) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		};
//		threadRefresh.start();
	    
	    UI.listRefreshCanvases.add( canvas );
	    
	    return canvas;
	}

	private List<TileGeometry> tiles = null;
	
	private List<TileGeometry> getTiles() {
		if ( null==tiles ) {
			this.tiles = perspective.getTiles( mapOptions );
		}
		return this.tiles;
	}
	

	private MouseListener getMouseListener() {
		final MouseListener listenerCanvas = new MouseAdapter() {
			@Override
			public void mouseDown( final MouseEvent event ) {
				if ( null==event ) return;
				
				for ( final TileGeometry geo : TileCanvas.this.getTiles() ) {
					
//					final TileBase tile = geo.tile;
					final Rectangle rect = geo.rect;
					
					final int iX = rect.x * 150;
					final int iY = rect.y * 150;
					final int iW = rect.width * 150;
					final int iH = rect.height * 150;

//					if ( perspective.isRotated() ) { //TODO .. ?
					
					if ( ( event.x > iX ) && ( event.x < iX + iW )
							&& ( event.y > iY ) && ( event.y < iY + iH ) ) {
						
//						final MouseListener listenerTile = 
//										geo.tile.getMouseListener();
//						if ( null!=listenerTile ) {
//							listenerTile.mouseDown( event );
//						}
						
						final Point point = 
//								new Point( event.x, event.y );
								new Point( event.x - iX, event.y - iY - TRIM_Y );
						
						final boolean bButton = geo.tile.clickButtons( point );
						if ( bButton ) return;

						final boolean bCanvas = geo.tile.clickCanvas( point );
						if ( bCanvas ) return;
						
						return;
					}
				}
				
			}
		};
		return listenerCanvas;
	}


	private PaintListener getPaintListener(	
									final Display display,
									final String strInfo,
									final Map<String,String> mapOptions ) {

    	final int iYLimit = 150 * perspective.getRowCount();

		return new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {

				lPaintCount++;
				final long lNowPaint = System.currentTimeMillis();

		    	final Rectangle rectCanvas = canvas.getClientArea();

//				e.gc.setClipping( rectCanvas );
				e.gc.setBackground( UI.COLOR_BLACK );
				e.gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				e.gc.fillRectangle( 0,0, TRIM_X, iYLimit + 30 );
//				e.gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
				e.gc.setBackground( UI.COLOR_DARK_GRAY );
				e.gc.fillRectangle( 0,iYLimit+TRIM_Y, rectCanvas.width, rectCanvas.height-iYLimit );
				e.gc.fillRectangle( 750+TRIM_X,0, rectCanvas.width, iYLimit+TRIM_Y );

				e.gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );

				e.gc.fillRectangle( 0,0, 800, TRIM_Y );
				if ( RPiTouchscreen.getInstance().isEnabled() ) {
					e.gc.setFont( Theme.get().getFont( 6 ) );
					e.gc.drawText( strInfo + "Frame " + lPaintCount, 10, 0 );
				} else {
					e.gc.setFont( Theme.get().getFont( 7 ) );
					e.gc.drawText( strInfo + "Frame " + lPaintCount, 10, -2 );
				}

				for ( final TileGeometry geo : TileCanvas.this.getTiles() ) {
					
					final TileBase tile = geo.tile;
					final Rectangle rect = geo.rect;
					
					final int iX = rect.x * 150;
					final int iY = rect.y * 150;
					final int iW = rect.width * 150;
					final int iH = rect.height * 150;
					
					final Image imageBuffer = new Image( e.display, iW, iH );
					
					tile.paint( imageBuffer, lNowPaint );
					
					if ( perspective.isRotated() ) {
						e.gc.setAdvanced( true );
						
						final Transform tr = new Transform( display );
						
				        tr.rotate( (float) 90 );
				        tr.translate( +10l, -750l -10 );
				        
				        e.gc.setTransform( tr );
					}
					
					e.gc.drawImage( imageBuffer, iX + TRIM_X, iY + TRIM_Y );
					e.gc.setTransform( null );
					
					imageBuffer.dispose();
				}
				
			}
		};
	}

	
	public boolean processKey( final char c ) {
		for ( final TileGeometry geo : TileCanvas.this.getTiles() ) {
			if ( geo.tile.pressKey( c ) ) {
				ConsoleClient.get().showStatus( "Key: " + c + " " );
				return true;
			}
		}
		return false;
	}
	
	
}
