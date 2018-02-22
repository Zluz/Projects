package jmr.rpclient.tab;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

import jmr.rpclient.tiles.Perspective;
import jmr.rpclient.tiles.TileCanvas;

public class TabTiles extends TabBase {

	public CTabItem tab = null;
	
	private final TileCanvas canvastile;
	
//	private final String strDeviceDescription;

	private final String strPerspective; 
	
	private final Map<String, String> mapOptions;
	
//	private final boolean bConsole;
	
	public TabTiles(	final String strDeviceDescription,
						final Map<String,String> mapOptions,
						final boolean bConsole ) {
//		this.strDeviceDescription = strDeviceDescription;
//		this.bConsole = bConsole;
		this.mapOptions = mapOptions;
		this.strPerspective = mapOptions.get( "tiles.perspective" );
		
		this.canvastile = new TileCanvas( strDeviceDescription, 
								strPerspective, bConsole, mapOptions );
		
		if ( bConsole ) {
			this.canvastile.buildConsole();
		}
	}
	
	
	@Override
	public Composite buildUI( final Composite parent ) {
		final Composite comp = canvastile.buildUI( parent );
		return comp;
	}
	
	public Perspective getPerspective() {
		return this.canvastile.getPerspective();
	}

	public TileCanvas getTiles() {
		return this.canvastile;
	}
	
	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {

	    this.tab = new CTabItem( tabs, SWT.NONE );

	    final Composite comp = this.buildUI( tabs );
	    tab.setControl( comp );
	    return tab;
	}

	@Override
	public TopSection getMenuItem() {
		return TopSection.TILES;
	}

	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	
}
