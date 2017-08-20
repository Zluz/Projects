package jmr.rpclient.tiles;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.UI;

public class CalibrationTile implements Tile {

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
		
//		final int iXC = rect.x * 150 + rect.width * 150 / 2;
//		final int iYC = rect.y * 150 + rect.height * 150 / 2;
//		final int iXC = r.x + r.width / 2;
//		final int iYC = r.y + r.height / 2;

		
//		String strText = "";
////		strText += "IP Address:\n" + NetUtil.getIPAddress();
////		strText += "\n\nMAC Address:\n" + NetUtil.getMAC();
//		strText += "Process:\n    " + NetUtil.getProcessName();
//		strText += "\nIP:\n    " + NetUtil.getIPAddress();
//		strText += "\nMAC:\n    " + NetUtil.getMAC();
//		strText += "\n";
////		final String strText = NetUtil.getIPAddress();
//
//		int iSize = 200;
//		Point ptTest;
//		do {
//			iSize = iSize - 10;
//			gc.setFont( Theme.get().getFont( iSize ) );
//			ptTest = gc.textExtent( strText );
//		} while ( r.width < ptTest.x );
//		
//		gc.setFont( Theme.get().getFont( iSize ) );
//		final Point ptExtent = gc.textExtent( strText );
//		
//		final int iX = iXC - ( ptExtent.x / 2 );
//		final int iY = iYC - ( ptExtent.y / 2 );
//		gc.drawText( strText, iX, iY );
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
