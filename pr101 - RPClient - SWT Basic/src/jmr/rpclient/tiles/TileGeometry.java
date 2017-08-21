package jmr.rpclient.tiles;

import org.eclipse.swt.graphics.Rectangle;

public class TileGeometry {

	public final TileBase tile;
	
	public final Rectangle rect;
	
	public TileGeometry(	final TileBase tile,
							final Rectangle rect ) {
		this.tile = tile;
		this.rect = rect;
	}
	
}
