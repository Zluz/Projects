package jmr.rpclient.tiles;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import jmr.util.NetUtil;

public class SystemInfoTile implements Tile {


	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		final Rectangle rect = image.getBounds();
		
//		final int iXC = rect.x * 150 + rect.width * 150 / 2;
//		final int iYC = rect.y * 150 + rect.height * 150 / 2;
		final int iXC = rect.x + rect.width / 2;
		final int iYC = rect.y + rect.height / 2;

		
		String strText = "";
//		strText += "IP Address:\n" + NetUtil.getIPAddress();
//		strText += "\n\nMAC Address:\n" + NetUtil.getMAC();
		strText += "Process:\n    " + NetUtil.getProcessName();
		strText += "\nIP:\n    " + NetUtil.getIPAddress();
		strText += "\nMAC:\n    " + NetUtil.getMAC();
		strText += "\n";
//		final String strText = NetUtil.getIPAddress();

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
		final int iY = iYC - ( ptExtent.y / 2 );
		gc.drawText( strText, iX, iY );
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
