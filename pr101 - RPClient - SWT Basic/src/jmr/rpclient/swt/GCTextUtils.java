package jmr.rpclient.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class GCTextUtils {

	private final GC gc;
	private boolean bRightAligned = false;
	private Rectangle rect = null;
	private int iY = 2;
	private int iX = 2;
	
	public GCTextUtils( final GC gc ) {
		this.gc = gc;
	}
	
	public void setRightAligned( final boolean bRightAligned ) {
		this.bRightAligned = bRightAligned;
	}
	
	public void setRect( final Rectangle rect ) {
		this.rect = rect;
	}
	
	public void println( final String strText ) {
		if ( null==strText ) return;
		if ( null==rect ) return;
		if ( gc.isDisposed() ) return;
		
		final Point ptSize = gc.stringExtent( strText );
		
		if ( bRightAligned ) {
			gc.drawText( strText, rect.width - ptSize.x, rect.y + iY );
		} else {
			gc.drawText( strText, rect.x + iX, rect.y + iY );
		}
		iY = iY + ptSize.y;
	}
	
	public void println(	final boolean bValue,
							final String strText ) {
		final String strValue = bValue ? "[X]" : "[_]";
		println( strValue + " " + strText );
	}

	
	public void drawTextJustified(	final String strText,
									final Rectangle rect ) {
		if ( null==strText ) return;
		if ( null==rect ) return;
		if ( gc.isDisposed() ) return;
		
		if ( bRightAligned ) {
			final Point ptSize = gc.stringExtent( strText );
			gc.drawText( strText, rect.width - ptSize.x, rect.y );
		} else {
			gc.drawText( strText, rect.x + iX, rect.y );
		}
	}
	
	
}
