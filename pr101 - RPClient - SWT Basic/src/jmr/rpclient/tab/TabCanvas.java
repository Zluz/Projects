package jmr.rpclient.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TabCanvas extends TabBase {

	public CTabItem tab = null;
	
	@Override
	public Composite buildUI( final Composite parent ) {

	    final Canvas canvas = new Canvas( parent, SWT.NONE );
	    canvas.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {
				final Rectangle r = canvas.getClientArea();
				final int iXC = r.width / 2;
				final int iYC = r.height / 2;
				e.gc.drawOval( 0, 0, r.width-1, r.height-1 );
				e.gc.drawLine( 0, iYC, iXC, r.height-1 );
				e.gc.drawLine( 0, iYC, iXC, 0 );
				e.gc.drawLine( iXC, 0, r.width-1, iYC );
				e.gc.drawLine( iXC, r.height-1, r.width-1, iYC );
			}
		});
	    
	    return canvas;
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
		return TopSection.CANVAS;
	}

	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	
}
