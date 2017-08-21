package jmr.rpclient.tiles;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.UI;

public class BlankTile extends TileBase {


	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		final Rectangle r = image.getBounds();
		
		gc.setBackground( UI.COLOR_BLACK );
		gc.setForeground( UI.COLOR_DARK_GRAY );
		gc.fillRectangle( r.x, r.y, r.width, r.height );
		
		gc.drawLine( 0,0, r.width, r.height );
		gc.drawLine( 0,r.height, r.width,0 );

		drawTextCentered( "      (blank)      ", 65 );
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
