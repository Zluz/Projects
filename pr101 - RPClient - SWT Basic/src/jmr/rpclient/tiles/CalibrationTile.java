package jmr.rpclient.tiles;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.UI;

public class CalibrationTile extends TileBase {

	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		final Rectangle r = image.getBounds();
		
		gc.setBackground( UI.COLOR_BLUE );
		gc.setForeground( UI.COLOR_RED );
		gc.fillGradientRectangle( r.x, r.y, r.width, r.height, true );
		
		gc.setForeground( UI.COLOR_WHITE );
//		gc.drawLine( r.x, r.y, r.x+r.width, r.y+r.height );
		gc.drawLine( 0,0, r.width, r.height );
		gc.drawLine( 0,r.height, r.width,0 );
		gc.drawRectangle( 2, 2, r.width - 4 - 1, r.height - 4 - 1 );
		gc.drawRoundRectangle( 0, 0, r.width - 1, r.height - 1, 30, 30 );
	}
	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
