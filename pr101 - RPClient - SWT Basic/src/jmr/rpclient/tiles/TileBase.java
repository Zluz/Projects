package jmr.rpclient.tiles;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;

public abstract class TileBase implements Tile {

	protected GC gc = null;
	protected Rectangle rect = null;
	protected int iXC;
	protected int iYC;
	
	public void paint( final Image imageBuffer ) {
		rect = imageBuffer.getBounds();
		gc = new GC( imageBuffer );
		gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		gc.fillRectangle( rect );

		this.iXC = rect.x + rect.width / 2;
		this.iYC = rect.y + rect.height / 2;

		
		paint( gc, imageBuffer );
		gc.dispose();
		gc = null;
		rect = null;
	}
	
	@Override
	public abstract void paint( final GC gc, Image imageBuffer );
	
	@Override
	public abstract MouseListener getMouseListener();
	
	

	//TODO optimize
	protected void drawTextCentered(	final String strText,
										final int iY ) {
		int iSize = 200;
		Point ptTest;
		do {
			iSize = iSize - 10;
			gc.setFont( Theme.get().getFont( iSize ) );
			ptTest = gc.textExtent( strText );
		} while ( rect.width < ptTest.x );
		
		gc.setFont( Theme.get().getFont( iSize ) );
		final Point ptExtent = gc.textExtent( strText );
		
		final int iX = iXC - ( ptExtent.x / 2 );
		gc.drawText( strText, iX, iY );
	}
	
	

}
