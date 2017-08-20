package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import jmr.rpclient.RPiTouchscreen;
import jmr.rpclient.UI;
import jmr.rpclient.tiles.Theme.Colors;
import jmr.util.NetUtil;

public class TileCanvas {

	public final static int TRIM_X = 0;
	public final static int TRIM_Y = 10;
	
	
	private Canvas canvas;
	
	private long lPaintCount = 0;
	
	private final List<TileGeometry> listTiles = new LinkedList<TileGeometry>();
	
	
	public TileCanvas() {
		listTiles.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 1, 0, 3, 3 ) ) );
		
		listTiles.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 0, 1, 1, 1 ) ) );
		
		listTiles.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 0, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 1, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 2, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 0, 0, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 0, 2, 1, 1 ) ) ); 
//		listTiles.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 0, 0, 1, 1 ) ) ); 
	}
	
	
	public Composite buildUI( final Composite parent ) {
		parent.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );

		final String strPad = "         ";
		final String strInfo = strPad 
				+ "IP: " + NetUtil.getIPAddress() + strPad
				+ "MAC: " + NetUtil.getMAC() + strPad
				+ "Process Name: " + NetUtil.getProcessName() + strPad
				;
		
	    this.canvas = new Canvas( parent, SWT.NO_BACKGROUND );
    	this.canvas.setCursor( UI.CURSOR_HIDE );

	    canvas.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {

				lPaintCount++;
				
				e.gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
				e.gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				e.gc.fillRectangle( 0,0, 800, TRIM_Y );
				e.gc.fillRectangle( 0,0, TRIM_X, 480 );
				
				
				if ( RPiTouchscreen.getInstance().isEnabled() ) {
					e.gc.setFont( Theme.get().getFont( 6 ) );
					e.gc.drawText( strInfo + "Frame " + lPaintCount, 10, 0 );
				} else {
					e.gc.setFont( Theme.get().getFont( 7 ) );
					e.gc.drawText( strInfo + "Frame " + lPaintCount, 10, -2 );
				}

//				final Rectangle r = canvas.getClientArea();
//				final int iXC = r.width / 2;
//				final int iYC = r.height / 2;
//				e.gc.drawOval( 100, 100, r.width-200, r.height-200 );
//				e.gc.drawLine( 0, iYC, iXC, r.height-1 );
//				e.gc.drawLine( 0, iYC, iXC, 0 );
//				e.gc.drawLine( iXC, 0, r.width-1, iYC );
//				e.gc.drawLine( iXC, r.height-1, r.width-1, iYC );
				
				for ( final TileGeometry geo : listTiles ) {
					final Tile tile = geo.tile;
					final Rectangle rect = geo.rect;
					
					final int iX = rect.x * 150;
					final int iY = rect.y * 150;
					final int iW = rect.width * 150;
					final int iH = rect.height * 150;
					
//					e.gc.setClipping( iX, iY, iW, iH );
//					e.gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
//					e.gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
					
//					e.gc.fillRectangle( iX, iY, iW, iH );
					
					final Image imageBuffer = new Image( e.display, iW, iH );
					final GC gc = new GC( imageBuffer );
					gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
					gc.fillRectangle( imageBuffer.getBounds() );

					tile.paint( gc, imageBuffer );
					
					e.gc.drawImage( imageBuffer, iX + TRIM_X, iY + TRIM_Y );
					imageBuffer.dispose();
				}
				
				
			}
		});
	    
		final Thread threadRefresh = new Thread() {
			@Override
			public void run() {
				try {
					do {
						Thread.sleep( 100 );
						if ( !canvas.isDisposed() ) {
							canvas.getDisplay().asyncExec( new Runnable() {
								@Override
								public void run() {
									canvas.redraw();
								}
							});
						}
					} while ( !parent.isDisposed() );
				} catch ( final InterruptedException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		threadRefresh.start();
	    
	    return canvas;
	}

	
	
}
