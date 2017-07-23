package jmr.rpclient.tab;

import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import jmr.sharedb.Node;
import jmr.sharedb.Server;
import jmr.sharedb.Server.Listener;

public class TabShowDB extends TabBase {

	private Display display;
	private Tree tree;
	final private Server server;
	
	private TreeColumn colPath;
	private TreeColumn colName;
	private TreeColumn colValue;
	
	public TabShowDB( final Server server ) {
		this.server = server;
		this.server.addListener( new Listener() {
			@Override
			public void changed() {
				drawTree();
			}
		});
	}
	
	
	@Override
	public String getName() {
		return "Data Listing";
	}

	@Override
	public Composite buildUI( final Composite parent ) {
		display = parent.getDisplay();
	    final Composite comp = new Composite( parent, SWT.NONE );
	    comp.setLayout( new FillLayout() );

		tree = new Tree( comp, SWT.V_SCROLL );
		
		colPath = new TreeColumn( tree, SWT.LEFT );
		colPath.setText( "Path" );
		colPath.setWidth( 300 );
		
		colName = new TreeColumn( tree, SWT.LEFT );
		colPath.setText( "Name" );
		colPath.setWidth( 100 );
		
		colValue = new TreeColumn( tree, SWT.LEFT );
		colPath.setText( "Value" );
		colPath.setWidth( 300 );
		
		tree.setHeaderVisible( true );
		
		return comp;
	}
	
	
	private void drawTree() {
		if ( null==tree ) return;
		if ( null==display ) return;
		if ( display.isDisposed() ) return;
		
		System.out.println( "Redrawing tree.." );
		
		display.asyncExec( new Runnable() {
			@Override
			public void run() {
				if ( tree.isDisposed() ) return;
				
				tree.clearAll( true );
				
				for ( final Node node : server.getNodes() ) {
					
					final TreeItem itemNode = new TreeItem( tree, SWT.NONE );
					itemNode.setText( 0, node.getPath() );
					
					for ( final Entry<String, String> entry : node.entrySet() ) {

						final TreeItem itemEntry = 
										new TreeItem( itemNode, SWT.NONE );
						itemEntry.setText( 1, entry.getKey() );
						itemEntry.setText( 2, entry.getValue() );
					}
				}
			}
		});
	}
	
	

	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {

	    final CTabItem tabShowDB = new CTabItem( tabs, SWT.NONE );
	    tabShowDB.setText( TAB_PAD_PREFIX + this.getName() + TAB_PAD_SUFFIX );
	    tabShowDB.setShowClose( false );
	    
	    final Composite compDailyInfo = this.buildUI( tabs );
	    tabShowDB.setControl( compDailyInfo );
	    return tabShowDB;
	}

}
