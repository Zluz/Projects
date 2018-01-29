package jmr.rpclient.tiles;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


/*
 * Tiles are graphics mapped to 150x150 pixel regions.
 *  
 */

public interface Tile {

//	public void paint(	final GC gc,
//						final Rectangle rect );

	public void paint(	final GC gc,
						final Image imageBuffer );

//	public MouseListener getMouseListener();
	
	public boolean clickButtons( final Point point );
	public boolean clickCanvas( final Point point );

}
