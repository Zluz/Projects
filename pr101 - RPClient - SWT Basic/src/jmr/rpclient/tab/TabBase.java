package jmr.rpclient.tab;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

public abstract class TabBase {

    
	/* protected */ Composite comp;
	
	public abstract Composite buildUI( final Composite parent );
	
	public abstract CTabItem addToTabFolder( final CTabFolder tabs );
	
	public abstract TopSection getMenuItem();
	
	public abstract CTabItem getTab();
	
}
