package jmr.rpclient.tab;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

import jmr.rpclient.tiles.TileCanvas;

public class TabTiles extends TabBase {

	public CTabItem tab = null;
	
	private TileCanvas canvastile;
	
	private final String strDeviceDescription;

	private final Map<String, String> mapOptions;
	
	public TabTiles(	final String strDeviceDescription,
						final Map<String,String> mapOptions ) {
		this.strDeviceDescription = strDeviceDescription;
		this.mapOptions = mapOptions;
	}
	
	
	@Override
	public Composite buildUI( final Composite parent ) {

		final String strPerspective = mapOptions.get( "tiles.perspective" );
		canvastile = new TileCanvas( strDeviceDescription, strPerspective );
		final Composite comp = canvastile.buildUI( parent );
		return comp;
		
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
