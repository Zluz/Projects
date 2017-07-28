package jmr.rpclient.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TabTreeDemo extends TabBase {


	public CTabItem tab = null;
	
	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	


	public TopSection getMenuItem() {
		return TopSection.TREE_DEMO;
	}
	
	@Override
	public Composite buildUI(Composite parent) {
	    final Composite comp = new Composite( parent, SWT.NONE );
	    comp.setLayout( new FillLayout() );
	    
//	    final Display display = parent.getDisplay();

	    final int iTreeOptions = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
		final Tree tree = new Tree( comp, iTreeOptions );
	    tree.setHeaderVisible(true);
	    TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
	    column1.setText("Column 1");
	    column1.setWidth(100);
	    TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
	    column2.setText("Column 2");
	    column2.setWidth(100);
	    TreeColumn column3 = new TreeColumn(tree, SWT.RIGHT);
	    column3.setText("Column 3");
	    column3.setWidth(100);
	    for (int i = 0; i < 4; i++) {
	      TreeItem item = new TreeItem(tree, SWT.NONE);
	      item.setText(new String[] { "item " + i, "abc", "defghi" });
	      for (int j = 0; j < 4; j++) {
	        TreeItem subItem = new TreeItem(item, SWT.NONE);
	        subItem
	            .setText(new String[] { "subitem " + j, "jklmnop",
	                "qrs" });
	        for (int k = 0; k < 4; k++) {
	          TreeItem subsubItem = new TreeItem(subItem, SWT.NONE);
	          subsubItem.setText(new String[] { "subsubitem " + k, "tuv",
	              "wxyz" });
	        }
	      }
	    }
	    		
		return comp;
	}


	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {
		
	    final CTabItem tabTreeDemo = new CTabItem( tabs, SWT.NONE );
//	    tabTreeDemo.setText( TAB_PAD_PREFIX + this.getName() + TAB_PAD_SUFFIX );
	    tabTreeDemo.setShowClose( false );
	    
	    final Composite compTreeDemo = this.buildUI( tabs );
	    tabTreeDemo.setControl( compTreeDemo );
	    this.tab = tabTreeDemo;
	    return tabTreeDemo;
	}
	
	
	
}
