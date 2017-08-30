package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import jmr.rpclient.RPiTouchscreen;
import jmr.rpclient.UI;
import jmr.rpclient.tiles.Theme.Colors;
import jmr.util.NetUtil;
import jmr.util.OSUtil;

public class TileCanvas {

	public static final int REFRESH_SLEEP = 100;
	
	public final static int TRIM_X = 0;
	public final static int TRIM_Y = 10;
	
	
	private Canvas canvas;
	
	private long lPaintCount = 0;
	
	private String strDeviceDescription;
	
	private final List<TileGeometry> 
							listTiles = new LinkedList<TileGeometry>();
	
	
	@SuppressWarnings("unused")
	private void build_Calibration() {
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
		//listTiles.add( new TileGeometry( new SystemInfoTile(), 
		//				new Rectangle( 0, 0, 1, 1 ) ) ); 
	}
	
	
	private void build_Camera() {
		listTiles.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		listTiles.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 1, 1, 1, 1 ) ) );

		listTiles.add( new TileGeometry( new CameraTile(), 
						new Rectangle( 3, 0, 2, 2 ) ) ); 

//		listTiles.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 4, 0, 1, 1 ) ) ); 
//		listTiles.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 4, 1, 1, 1 ) ) ); 
//		listTiles.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 4, 2, 1, 1 ) ) );

		listTiles.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 2, 1, 1, 1 ) ) );
		
		listTiles.add( new TileGeometry( new BlankTile(), 
						new Rectangle( 0, 1, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new BlankTile(), 
						new Rectangle( 1, 1, 1, 1 ) ) ); 
//		listTiles.add( new TileGeometry( new BlankTile(), 
//						new Rectangle( 3, 0, 1, 1 ) ) ); 

		listTiles.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
		//listTiles.add( new TileGeometry( new SystemInfoTile(), 
		//				new Rectangle( 0, 0, 1, 1 ) ) ); 
	}
	

	private void build_Daily() {
		listTiles.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		listTiles.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 2, 1, 1, 1 ) ) );

//		listTiles.add( new TileGeometry( new CameraTile(), 
//						new Rectangle( 3, 0, 2, 2 ) ) ); 

		listTiles.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 0, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 1, 1, 1 ) ) ); 
//		listTiles.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 4, 2, 1, 1 ) ) );

		listTiles.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 3, 1, 1, 1 ) ) );
		
		listTiles.add( new TileGeometry( new BlankTile(), 
						new Rectangle( 0, 1, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new BlankTile(), 
						new Rectangle( 1, 1, 1, 1 ) ) ); 
		listTiles.add( new TileGeometry( new BlankTile(), 
						new Rectangle( 3, 0, 1, 1 ) ) ); 

		listTiles.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
		//listTiles.add( new TileGeometry( new SystemInfoTile(), 
		//				new Rectangle( 0, 0, 1, 1 ) ) ); 
	}
	
	
	
	public TileCanvas(	final String strDeviceDescription, 
						final String strPerspective ) {
		if ( "camera".equals( strPerspective ) ) {
			build_Camera();
		} else {
			build_Daily();
		}
		build_Daily();
		this.strDeviceDescription = strDeviceDescription;
	}
	
	
	public Composite buildUI( final Composite parent ) {
		parent.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );

		final String strPad = "              ";
		final String strInfo = strPad 
				+ "IP: " + NetUtil.getIPAddress() + strPad
				+ "MAC: " + NetUtil.getMAC() + strPad
				+ "Device: " + strDeviceDescription + strPad
				+ "Executable: " + OSUtil.getProgramName() + strPad
//				+ "Process Name: " + NetUtil.getProcessName() + strPad
				;
		
	    this.canvas = new Canvas( parent, SWT.NO_BACKGROUND );
    	this.canvas.setCursor( UI.CURSOR_HIDE );

	    canvas.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {

				lPaintCount++;

		    	final Rectangle rectCanvas = canvas.getClientArea();

//				e.gc.setClipping( rectCanvas );
				e.gc.setBackground( UI.COLOR_BLACK );
				e.gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				e.gc.fillRectangle( 0,0, TRIM_X, 480 );
//				e.gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
				e.gc.setBackground( UI.COLOR_DARK_GRAY );
				e.gc.fillRectangle( 0,450+TRIM_Y, rectCanvas.width, rectCanvas.height-450 );
				e.gc.fillRectangle( 750+TRIM_X,0, rectCanvas.width, 450+TRIM_Y );

				e.gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );

				e.gc.fillRectangle( 0,0, 800, TRIM_Y );
				if ( RPiTouchscreen.getInstance().isEnabled() ) {
					e.gc.setFont( Theme.get().getFont( 6 ) );
					e.gc.drawText( strInfo + "Frame " + lPaintCount, 10, 0 );
				} else {
					e.gc.setFont( Theme.get().getFont( 7 ) );
					e.gc.drawText( strInfo + "Frame " + lPaintCount, 10, -2 );
				}

				for ( final TileGeometry geo : listTiles ) {
					
					final TileBase tile = geo.tile;
					final Rectangle rect = geo.rect;
					
					final int iX = rect.x * 150;
					final int iY = rect.y * 150;
					final int iW = rect.width * 150;
					final int iH = rect.height * 150;
					
					final Image imageBuffer = new Image( e.display, iW, iH );
					
					tile.paint( imageBuffer );
					
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
						Thread.sleep( REFRESH_SLEEP );
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
