package jmr.rpclient.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class GCTextUtils {

	private final GC gc;
	private boolean bRightAligned = false;
	
	public GCTextUtils( final GC gc ) {
		this.gc = gc;
	}
	
	public void setRightAligned( final boolean bRightAligned ) {
		this.bRightAligned = bRightAligned;
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
			gc.drawText( strText, rect.x, rect.y );
		}
	}
	
	
}
