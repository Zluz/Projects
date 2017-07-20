package jmr.rpclient.tab;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

public abstract class TabBase {

    final String TAB_PAD_SUFFIX = "      ";
    final String TAB_PAD_PREFIX = "   ";
    
	/* protected */ Composite comp;
	
	public abstract String getName();
	
	public abstract Composite buildUI( final Composite parent );
	
	public abstract CTabItem addToTabFolder( final CTabFolder tabs );
	
}
