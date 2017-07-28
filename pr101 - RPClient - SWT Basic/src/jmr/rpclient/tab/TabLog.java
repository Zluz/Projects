package jmr.rpclient.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TabLog extends TabBase {

	public CTabItem tab = null;
	
	private Text txtLog;
	
	
	@Override
	public Composite buildUI( final Composite parent ) {
	    final Composite comp = new Composite( parent, SWT.NONE );
	    comp.setLayout( new FillLayout() );
	    
	    txtLog = new Text( comp, SWT.MULTI | SWT.V_SCROLL );

	    return comp;
	}
	
	public Text getTextWidget() {
		return txtLog;
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
		return TopSection.LOG;
	}

	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	
}
