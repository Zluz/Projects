package jmr.rpclient.swt;

import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import jmr.data.Conversion;
import jmr.rpclient.swt.Theme.Colors;
import jmr.util.transform.DateFormatting;

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
	
	public void addSpace( final int iAdvanceY ) {
		this.iY = this.iY + iAdvanceY;
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
	
	
	public void drawDataAge(	final Map<String,String> mapData,
								final long lNow,
								boolean bTop ) {
		String strAge = "<?>";
		if ( null!=mapData ) {
			final String strTime = mapData.get( ".last_modified_uxt" );
			final Long lTime = Conversion.getMaxLongFromStrings( strTime );
			if ( null!=lTime ) {
				strAge = DateFormatting.getSmallTime( lNow - lTime );
			}
		}
		gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		gc.setFont( Theme.get().getFont( 6 ) );
		final Rectangle rect = gc.getClipping();
		final int iX = rect.x + rect.width - gc.textExtent( strAge ).x - 1;
		if ( bTop ) {
			gc.drawText( strAge, iX, 0 );
		} else {
			gc.drawText( strAge, iX, rect.y + rect.height - 10 );
		}
	}
	
	
}
