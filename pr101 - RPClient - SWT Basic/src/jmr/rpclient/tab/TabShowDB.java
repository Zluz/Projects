package jmr.rpclient.tab;

import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import jmr.sharedb.Node;
import jmr.sharedb.Peer;
import jmr.sharedb.Server;
import jmr.sharedb.Server.Listener;

public class TabShowDB extends TabBase {

	private Display display;
	private Tree treeNodes;
	private Tree treePeers;
	final private Server server;
	
	private TreeColumn colPath;
//	private TreeColumn colName;
//	private TreeColumn colValue;
	
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
//	    comp.setLayout( new FillLayout() );
	    comp.setLayout( new GridLayout( 5, true ) );
	    
	    final Composite compNodes = new Composite( comp, SWT.NONE );
	    compNodes.setLayout( new FillLayout() );
	    final GridData gdNode = new GridData( GridData.FILL_BOTH );
	    gdNode.horizontalSpan = 3;
		compNodes.setLayoutData( gdNode );

	    final Composite compPeers = new Composite( comp, SWT.NONE );
	    compPeers.setLayout( new FillLayout() );
	    final GridData gdPeer = new GridData( GridData.FILL_BOTH );
	    gdPeer.horizontalSpan = 2;
	    compPeers.setLayoutData( gdPeer );

		treeNodes = new Tree( compNodes, SWT.V_SCROLL );
		
		colPath = new TreeColumn( treeNodes, SWT.LEFT );
		colPath.setText( "Path" );
		colPath.setWidth( 460 );
		
//		colName = new TreeColumn( treeNodes, SWT.LEFT );
//		colName.setText( "Name" );
//		colName.setWidth( 100 );
		
//		colValue = new TreeColumn( treeNodes, SWT.LEFT );
//		colValue.setText( "Value" );
//		colValue.setWidth( 300 );
		
		treeNodes.setHeaderVisible( true );


		treePeers = new Tree( compPeers, SWT.V_SCROLL );
		
		colPath = new TreeColumn( treePeers, SWT.LEFT );
		colPath.setText( "Session Name" );
		colPath.setWidth( 300 );
		
//		colName = new TreeColumn( treePeers, SWT.LEFT );
//		colName.setText( "Last Activity" );
//		colName.setWidth( 100 );
		
		treePeers.setHeaderVisible( true );
		
		
		return comp;
	}
	
	
	private void drawTree() {
		if ( null==treeNodes ) return;
		if ( null==display ) return;
		if ( display.isDisposed() ) return;
		
//		System.out.println( "Redrawing tree.." );
		
		display.asyncExec( new Runnable() {
			@Override
			public void run() {
				if ( treeNodes.isDisposed() ) return;
				
				treeNodes.removeAll();
				
				for ( final Node node : server.getNodes() ) {
					
					final TreeItem itemNode = new TreeItem( treeNodes, SWT.NONE );
					itemNode.setText( 0, node.getPath() );
					
					for ( final Entry<String, String> entry : node.entrySet() ) {

						final TreeItem itemEntry = 
										new TreeItem( itemNode, SWT.NONE );
//						itemEntry.setText( 0, entry.getKey() );
//						itemEntry.setText( 1, entry.getValue() );
						final String strLine = 
								entry.getKey() + " = "
								+ "\"" + entry.getValue() + "\"";
						itemEntry.setText( 0, strLine );
					}
				}
				
				treePeers.removeAll();
				for ( final Peer peer : server.getSessions() ) {
					
					final TreeItem itemPeer = new TreeItem( treePeers, SWT.NONE );
					itemPeer.setText( 0, peer.getSessionName() );
					
					final TreeItem itemPeerLastActivity = 
							new TreeItem( itemPeer, SWT.NONE );

					itemPeerLastActivity.setText( 0, "Last: " + peer.getLastActivity() );
				}
			}
		});
	}
	
	

	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {

	    final CTabItem tabShowDB = new CTabItem( tabs, SWT.NONE );
	    tabShowDB.setText( TAB_PAD_PREFIX + this.getName() + TAB_PAD_SUFFIX );
	    tabShowDB.setShowClose( false );

	    tabs.addSelectionListener( new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected( final SelectionEvent event ) {
	    		if ( tabShowDB == event.item ) { 
	    			drawTree();
	    		}
	    	}
		});
	    
	    final Composite compDailyInfo = this.buildUI( tabs );
	    tabShowDB.setControl( compDailyInfo );
	    return tabShowDB;
	}

}
