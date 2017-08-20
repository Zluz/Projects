package jmr.rpclient.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

import jmr.rpclient.tiles.TileCanvas;

public class TabTiles extends TabBase {

	public CTabItem tab = null;
	
	private TileCanvas canvastile;
	
	
	@Override
	public Composite buildUI( final Composite parent ) {

		canvastile = new TileCanvas();
		final Composite comp = canvastile.buildUI( parent );
		return comp;
		
//	    final Canvas canvas = new Canvas( parent, SWT.NONE );
//	    canvas.addPaintListener( new PaintListener() {
//			@Override
//			public void paintControl( final PaintEvent e ) {
//				final Rectangle r = canvas.getClientArea();
//				final int iXC = r.width / 2;
//				final int iYC = r.height / 2;
//				e.gc.drawOval( 100, 100, r.width-100, r.height-100 );
//				e.gc.drawLine( 0, iYC, iXC, r.height-1 );
//				e.gc.drawLine( 0, iYC, iXC, 0 );
//				e.gc.drawLine( iXC, 0, r.width-1, iYC );
//				e.gc.drawLine( iXC, r.height-1, r.width-1, iYC );
//			}
//		});
//	    
//	    return canvas;
	}

	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {

	    this.tab = new CTabItem( tabs, SWT.NONE );

	    final Composite comp = this.buildUI( tabs );
	    tab.setControl( comp );
	    return tab;
	}

	@Override
	public TopSection getMenuItem() {
		return TopSection.TILES;
	}

	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	
}
