package jmr.rpclient.tiles;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jmr.util.NetUtil;

public class SystemInfoTile extends TileBase {


	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		String strText = "";
		strText += "Process:\n    " + NetUtil.getProcessName();
		strText += "\nIP:\n    " + NetUtil.getIPAddress();
		strText += "\nMAC:\n    " + NetUtil.getMAC();
		strText += "\n";

		drawTextCentered( strText, 10 );
	}

	@Override
	public void click( final Point point ) {
		// TODO Auto-generated method stub
	}

}
