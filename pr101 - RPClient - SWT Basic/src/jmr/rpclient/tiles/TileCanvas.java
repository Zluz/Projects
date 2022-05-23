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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import jmr.rpclient.ConsoleClient;
import jmr.rpclient.ModalMessage;
import jmr.rpclient.RPiTouchscreen;
import jmr.rpclient.screen.TextCanvas;
import jmr.rpclient.screen.TextScreen;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.Theme.ThFont;
import jmr.rpclient.swt.UI;
import jmr.s2db.Client;
import jmr.util.NetUtil;
import jmr.util.OSUtil;
import jmr.util.SystemUtil;
import jmr.util.hardware.rpi.DeviceExamine;
import jmr.util.transform.DateFormatting;

public class TileCanvas {

	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( TileCanvas.class.getName() );

	private static TileCanvas instance;
	
	
	public static final int REFRESH_SLEEP = 100;
	
	public final static int TRIM_X = 0;
	public final static int TRIM_Y = 10;
	
	
	private Canvas canvas = null;
	
//	private long lPaintCount = 0;
	
	private String strDeviceDescription;
	
	Perspective perspective;
	
	final boolean bConsole;
	
	final private Map<String,String> mapOptions = new HashMap<>();

	private PaintListener listenerCanvasPainter;

	private MouseListener listenerCanvasMouse;
	
//	private boolean bModalDisplayed = false;
	private ModalMessage message = null;
	
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
		TileCanvas.instance = this;
	}
	
	public static TileCanvas getInstance() {
		return instance;
	}
	
	public void setPerspective( final Perspective perspective ) {
		this.perspective = perspective;
		this.buildUI( this.canvas.getParent() );
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
						
						final TextScreen screen = 
										TextCanvas.getInstance().getScreen();
					
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
		
		final String strPerspectiveName = this.getPerspective().name();
		final Long lDeviceSeq = Client.get().getDeviceSeq();
		final String strDeviceId;
		if ( null==lDeviceSeq ) {
			strDeviceId = "<no-seq>";
		} else {
			strDeviceId = String.format( "D %03d", lDeviceSeq );
		}

		final String strPad = "         ";
		final String strInfo = "  " 
				+ "IP: " + NetUtil.getIPAddress() + strPad
				+ "MAC: " + NetUtil.getMAC() + strPad
//				+ "D " + lDeviceSeq + strPad
				+ strDeviceId + strPad
				+ strDeviceDescription + strPad
				+ OSUtil.getProgramName() + strPad
//				+ "Process Name: " + NetUtil.getProcessName() + strPad
				+ "Perspective: " + strPerspectiveName + strPad
				+ strPad + DeviceExamine.get().getThrottleStatus();
				;
		
		if ( null==this.canvas ) {
		    this.canvas = new Canvas( parent, SWT.NO_BACKGROUND );
		    if ( RPiTouchscreen.getInstance().isEnabled() ) {
		    	this.canvas.setCursor( UI.CURSOR_HIDE );
		    }

		    UI.listRefreshCanvases.add( canvas );
		} else {
			canvas.removePaintListener( this.listenerCanvasPainter );
			canvas.removeMouseListener( this.listenerCanvasMouse );
			this.listenerCanvasPainter = null;
			this.listenerCanvasMouse = null;
			
			this.tiles = null;
		}
    	
	    final Display display = parent.getDisplay();
	    
	    this.listenerCanvasPainter = 
	    				getPaintListener( display, strInfo, mapOptions );
		canvas.addPaintListener( this.listenerCanvasPainter );
	    
	    this.listenerCanvasMouse = getMouseListener();
		canvas.addMouseListener( this.listenerCanvasMouse );
	    
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
				
				if ( null != TileCanvas.this.message ) {
					message.close();
					return;
				}
				
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


	

	private void drawModalMessage( 	final int iXLimit, 
									final int iYLimit,
									final GC gcFull, 
									final long lTimeNow ) {

//		final ModalMessage message = ModalMessage.getNext( lTimeNow );

		final GC gcMsg = gcFull;
		
		final String strTitle = message.getTitle();
		final String strBody = message.getBody();
		final String strContent = strTitle + "\n" + strBody;
		final long lRemain = message.getRemainingMS( lTimeNow );
//			final String strRemain = "" + lRemain + " ms";
		final String strRemain = 
					DateFormatting.getSmallTime( lRemain, true );

		gcMsg.setFont( Theme.ThFont._12_M_B.getFont() );
		final Point ptExtentTime = gcMsg.stringExtent( strRemain );
		
		ThFont themefont = Theme.ThFont._18_SSCM_V;
		Font fontBody = themefont.getFont();
		final String strFontName = fontBody.getFontData()[0].getName();
		Point ptExtent;
		boolean bAdjustSmaller = false;
		do {
			fontBody = themefont.getFont();
			gcMsg.setFont( fontBody );
			ptExtent = gcMsg.textExtent( strContent );
			if ( ptExtent.x + 30 > iXLimit ) {
				final int iOrd = themefont.ordinal() - 1;
				ThFont tfSmaller = Theme.ThFont.values()[ iOrd ];
				final Font fontSmaller = tfSmaller.getFont();
				if ( strFontName.equals( 
						fontSmaller.getFontData()[0].getName() ) ) {
					themefont = tfSmaller;
					bAdjustSmaller = true;
				} else {
					bAdjustSmaller = false;
				}
			} else {
				bAdjustSmaller = false;
			}
		} while ( bAdjustSmaller );
		
		final Point ptExtentTitle = gcMsg.textExtent( strTitle );
		
		
		final int iB = 8;
		
		final int iX1 = ( iXLimit - ptExtent.x ) / 2; 
		final int iY1 = ( iYLimit - ptExtent.y ) / 2; 

		gcMsg.setBackground( Theme.get().getColor( 
						Colors.BACKGROUND_INFO ) );
		gcMsg.setForeground( Theme.get().getColor( 
						Colors.TEXT_LIGHT ) );
		gcMsg.fillRectangle( iX1 - iB, iY1 - iB, 
					ptExtent.x + 2 * iB, ptExtent.y + 2 * iB );
		gcMsg.drawRectangle( iX1 - iB, iY1 - iB, 
				ptExtent.x + 2 * iB, ptExtent.y + 2 * iB );

		gcMsg.setForeground( Theme.get().getColor( 
				Colors.BACKGROUND ) );
		gcMsg.drawRectangle( iX1 - iB - 1, iY1 - iB - 1, 
				ptExtent.x + 2 * iB + 2, ptExtent.y + 2 * iB + 2 );

		gcMsg.setForeground( Theme.get().getColor( 
						Colors.TEXT_LIGHT ) );

		gcMsg.drawText( strContent, iX1, iY1 );
		

		gcMsg.setBackground( Theme.get().getColor( 
						Colors.TEXT_LIGHT ) );
		
		gcMsg.fillRectangle( iX1 - iB, iY1 - iB, 
				ptExtent.x + 2 * iB, ptExtentTitle.y + iB );

		gcMsg.setForeground( Theme.get().getColor( 
				Colors.BACKGROUND ) );
		
		gcMsg.drawText( strTitle, iX1 + 2, iY1 + 1, true );
		gcMsg.drawText( strTitle, iX1 - 1, iY1 + 1, true );

		gcMsg.setFont( Theme.ThFont._12_M_B.getFont() );

		gcMsg.drawText( strRemain, 
						iX1 + ptExtent.x - ptExtentTime.x,
						iY1, 
						true );

		gcMsg.setFont( fontBody );

		gcMsg.setForeground( Theme.get().getColor( 
				Colors.TEXT_BOLD ) );

		gcMsg.drawText( strTitle, iX1, iY1, true );
		gcMsg.drawText( strTitle, iX1 + 1, iY1, true );
	}
	
	
	private PaintListener getPaintListener(	
									final Display display,
									final String strInfo,
									final Map<String,String> mapOptions ) {

    	final int iXLimit = 150 * perspective.getColCount();
    	final int iYLimit = 150 * perspective.getRowCount();

    	System.out.println( "New TileCanvas PaintListener created for " 
    										+ perspective.name() );
    	
		return new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {
				
				final Image imageFullBuffer = 
								new Image( e.display, iXLimit, iYLimit );
				final GC gcFull = new GC( imageFullBuffer );

				e.gc.setFont( Theme.get().getFont( 10 ) );

//				lPaintCount++;
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
//				final String strHeaderInfo = strInfo + "Frame " + lPaintCount;
				final String strHeaderInfo = strInfo;
//				if ( RPiTouchscreen.getInstance().isEnabled() ) {
//					e.gc.setFont( Theme.get().getFont( 6 ) );
//					e.gc.drawText( strHeaderInfo, 10, 0 );
//				} else {
//					e.gc.setFont( Theme.get().getFont( 7 ) );
					e.gc.setFont( Theme.ThFont._7_O_V.getFont() );
					e.gc.drawText( strHeaderInfo, 10, -2 );
//				}

				for ( final TileGeometry geo : TileCanvas.this.getTiles() ) {
					
					final TileBase tile = geo.tile;
					final Rectangle rect = geo.rect;
					
					final int iX = rect.x * 150;
					final int iY = rect.y * 150;
					final int iW = rect.width * 150;
					final int iH = rect.height * 150;
					
					tile.clearInfoRegions();
					
					final Image imageBuffer = new Image( e.display, iW, iH );

					try {
						tile.paint( imageBuffer, lNowPaint );
					} catch ( final Throwable t ) {
						
						System.err.println( "ERROR while rendering tile " 
										+ tile.getClass().getSimpleName() 
										+ " on " + perspective.name() );

						t.printStackTrace();
						SystemUtil.shutdown( 1200, "Error rendering tile" );
//						display.close();
					}
					
					if ( perspective.isRotated() ) {
//						e.gc.setAdvanced( true );
						gcFull.setAdvanced( true );
						
						final Transform tr = new Transform( display );
						
				        tr.rotate( (float) 90 );
				        tr.translate( +10l, -750l -10 );
				        
//				        e.gc.setTransform( tr );
				        gcFull.setTransform( tr );
					}
					
//					e.gc.drawImage( imageBuffer, iX + TRIM_X, iY + TRIM_Y );
//					e.gc.setTransform( null );
					gcFull.drawImage( imageBuffer, iX, iY );
					gcFull.setTransform( null );
					
					imageBuffer.dispose();
				}
				
				
				final long lTimeNow = System.currentTimeMillis();
//				final ModalMessage message = ModalMessage.getNext( lTimeNow );
				TileCanvas.this.message = ModalMessage.getNext( lTimeNow );
				if ( null != message ) {
					drawModalMessage( iXLimit, iYLimit, gcFull, lTimeNow);
				}
				
				e.gc.drawImage( imageFullBuffer, TRIM_X, TRIM_Y );
				imageFullBuffer.dispose();
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
