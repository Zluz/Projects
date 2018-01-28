package jmr.rpclient.tiles;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.swt.UI;

public class TextTile extends TileBase {


	final private boolean bBlank;
	final private String strText;
	
	
	public TextTile( final String strText ) {
		if ( null==strText || strText.isEmpty() ) {
			this.bBlank = true;
			this.strText = "      (blank)      ";
		} else {
			this.bBlank = false;
			this.strText = strText;
		}
	}
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		final Rectangle r = image.getBounds();
		
		gc.setBackground( UI.COLOR_BLACK );
		if ( this.bBlank ) {
			gc.setForeground( UI.COLOR_DARK_GRAY );
		} else {
			gc.setForeground( UI.COLOR_RED );
		}
		gc.fillRectangle( r.x, r.y, r.width, r.height );
		
		gc.drawLine( 0,0, r.width, r.height );
		gc.drawLine( 0,r.height, r.width,0 );

		if ( !this.bBlank ) {
			gc.setForeground( UI.COLOR_WHITE );
		}
		drawTextCentered( strText, 65 );
	}

	@Override
	public void click( final Point point ) {
		// TODO Auto-generated method stub
	}

}
