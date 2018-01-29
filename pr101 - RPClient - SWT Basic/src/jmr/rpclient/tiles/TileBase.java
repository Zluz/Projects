package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;

public abstract class TileBase implements Tile {

	private final List<Button> buttons = new LinkedList<>();
	
	
	protected GC gc = null;
	protected Rectangle rect = null;
	protected int iXC;
	protected int iYC;


	
	private static class Button {
		Rectangle rect;
		int iIndex;
	}

	
	public void paint( final Image imageBuffer ) {
		rect = imageBuffer.getBounds();
		gc = new GC( imageBuffer );
		gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		gc.fillRectangle( rect );

		this.iXC = rect.x + rect.width / 2;
		this.iYC = rect.y + rect.height / 2;

		buttons.clear();
		
		paint( gc, imageBuffer );
		gc.dispose();
		gc = null;
		rect = null;
	}
	
	@Override
	public abstract void paint( final GC gc, Image imageBuffer );
	
	
	@Override
	public boolean clickCanvas( final Point point ) {
		return false;
	}
	
	
	@Override
	public boolean clickButtons( final Point point ) {

		boolean bButton = false;
		for ( final Button button : buttons ) {
			final Rectangle r = button.rect;
			if ( ( point.x > r.x ) && ( point.x < r.x + r.width )
					&& ( point.y > r.y ) && ( point.y < r.y + r.height ) ) {
				
				activateButton( button.iIndex );
				
				bButton = true;
			}
		}
		
		return bButton;	
	}
	
	
	protected abstract void activateButton( final int iIndex );


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


	protected void addButton(	final GC gc,
								final int iIndex,
								final int iX, final int iY,
								final int iW, final int iH,
								final String strText ) {
		if ( null==gc || gc.isDisposed() ) return;
		if ( null==strText ) return;
		
		gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		gc.drawRoundRectangle( iX, iY, iW, iH, 20, 20 );
		
		gc.setFont( Theme.get().getFont( 11 ) );

		final Point ptSize = gc.textExtent( strText );
		
		final int iTextX = iX + (int)((float)iW/2 - (float)ptSize.x/2);
		final int iTextY = iY + (int)((float)iH/2 - (float)ptSize.y/2);
		
		gc.drawText( strText, iTextX, iTextY );
		
		final Button button = new Button();
		button.rect = new Rectangle( iX, iY, iW, iH );
		button.iIndex = iIndex;
		this.buttons.add( button );
	}

}
