package jmr.rpclient.tab;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import jmr.s2db.Client;
import jmr.s2db.Watcher;
import jmr.s2db.Watcher.Listener;
import jmr.s2db.tables.Page;
import jmr.s2db.tree.TreeModel;
import jmr.s2db.tree.TreeModel.Node;
import jmr.util.Logging;

public class TabS2DB extends TabBase {

	private Display display;
	private Tree treeNodes;
//	private Tree treePeers;
	private Table tableDetails;
//	final private Server server;
	
	private TreeColumn colPath;
//	private TreeColumn colName;
//	private TreeColumn colValue;
	
	private Menu menu;
	
	private Page tablePage;
	

	boolean bIsVisible = false;

	
	public TabS2DB( final Client s2db ) {
		tablePage = new Page();
		Watcher.get().addListener( new Listener() {
			@Override
			public void addedPage() {
				Logging.log( "Page added, invaliding S2DB page." );
				invalidate();
			}
			@Override
			public void addedSession() {
				Logging.log( "Session added, invaliding S2DB page." );
				invalidate();
			}
			@Override
			public void updatedPage() {
				Logging.log( "Page updated, invaliding S2DB page." );
				invalidate();
			}
		});
	}
	
	
	private boolean valid = false;
	
	private void invalidate() {
		valid = false;
		//		if ( treeNodes.isVisible() ) {
		if ( bIsVisible ) {
//			treeNodes.getDisplay().asyncExec( new Runnable() { });
			drawTree();
		}
	}
	

	public TopSection getMenuItem() {
		return TopSection.S2DB;
	}

	public CTabItem tab = null;
	private Composite compDetails;
	
	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	
	
	@Override
	public Composite buildUI( final Composite parent ) {
		display = parent.getDisplay();
		
	    final Composite comp = new Composite( parent, SWT.NONE );
//	    comp.setLayout( new FillLayout() );
	    comp.setLayout( new GridLayout( 6, true ) );
	    
	    final Composite compNodes = new Composite( comp, SWT.NONE );
	    compNodes.setLayout( new FillLayout() );
	    final GridData gdNode = new GridData( GridData.FILL_BOTH );
	    gdNode.horizontalSpan = 2;
		compNodes.setLayoutData( gdNode );

	    compDetails = new Composite( comp, SWT.NONE );
	    compDetails.setLayout( new FillLayout() );
	    final GridData gdPeer = new GridData( GridData.FILL_BOTH );
	    gdPeer.horizontalSpan = 4;
	    compDetails.setLayoutData( gdPeer );

		menu = new Menu( parent.getShell(), SWT.POP_UP );

		treeNodes = new Tree( compNodes, SWT.V_SCROLL );
		treeNodes.setMenu( menu );

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


//		treePeers = new Tree( compPeers, SWT.V_SCROLL );
		tableDetails = new Table( compDetails, SWT.V_SCROLL );
		
		TableColumn tcolName = new TableColumn( tableDetails, SWT.LEFT );
		tcolName.setText( "Name" );
		tcolName.setWidth( 180 );
		TableColumn tcolValue = new TableColumn( tableDetails, SWT.LEFT );
		tcolValue.setText( "Value" );
		tcolValue.setWidth( 500 );
		
		tableDetails.showColumn( tcolName );
		tableDetails.showColumn( tcolValue );
		
		tableDetails.setHeaderVisible( true );
		
//		colPath = new TreeColumn( treePeers, SWT.LEFT );
//		colPath.setText( "Session Name" );
//		colPath.setWidth( 300 );
		
//		colName = new TreeColumn( treePeers, SWT.LEFT );
//		colName.setText( "Last Activity" );
//		colName.setWidth( 100 );
		
//		treePeers.setHeaderVisible( true );
		
		
	    treeNodes.addFocusListener( new FocusAdapter() {
	    	@Override
	    	public void focusGained( final FocusEvent event ) {
    			drawTree();
	    	}
		});
		
	    treeNodes.addSelectionListener( new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected( final SelectionEvent event ) {
//	    		if ( event.item instanceof TreeItem ) {
	    			final Object obj = event.item.getData();
	    			if ( obj instanceof Node ) {
	    				showDetails( (Node) obj );
	    			}
//	    		}
	    	}

		});
	    

	    final MenuItem miDeactivate = new MenuItem( menu, SWT.PUSH );
	    miDeactivate.setText( "Deactivate" );
	    

	    // see http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/Enablemenuitemsdynamicallywhenmenushown.htm
	    menu.addListener( SWT.Show, new org.eclipse.swt.widgets.Listener() {
			@Override
			public void handleEvent( final Event event ) {
	    		final TreeItem[] selection = treeNodes.getSelection();
    			miDeactivate.setEnabled( 1==selection.length );
			}
	    });
	    
	    
	    miDeactivate.addSelectionListener( new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected( final SelectionEvent event ) {
//    			if ( null==event ) return;
//    			final Widget item = event.item;
//    			if ( null==item ) return;
//	    		final TreeItem[] selection = treeNodes.getSelection();
	    		final TreeItem item = treeNodes.getSelection()[0];
				final Object obj = item.getData();
    			if ( obj instanceof Node ) {
//    				showDetails( (Node) obj );
    				final Node node = (Node)obj;
    				final Long seqPage = node.getPageSeq();
    				if ( null!=seqPage ) {
    					tablePage.setState( seqPage, null, 'E' );
    					invalidate();
    				}
    			}
	    	}
		});
	    
		
		return comp;
	}
	
	

	private void showDetails( final Node node ) {
		if ( null==node ) return;
		if ( null==tableDetails || tableDetails.isDisposed() ) return;
		
		final String strTitle = "Node:  " + node.strFull;
		tableDetails.getShell().setText( strTitle );
		
		for ( final Control child : tableDetails.getChildren() ) {
			child.dispose();
		}
		tableDetails.clearAll();
		tableDetails.removeAll();
		final ScrollBar hbar = tableDetails.getHorizontalBar();
		if ( null!=hbar ) {
			hbar.setSelection( 0 );
		}

		final TableItem itemTitle = new TableItem( tableDetails, SWT.NONE );
		itemTitle.setText( 0, "Node Path" );
		itemTitle.setText( 1, node.strFull );
//		new TableItem( tableDetails, SWT.NONE ); // space

		final Map<String, String> map = node.getMap();
		if ( null!=map ) {
			final List<String> listKeys = new LinkedList<>( map.keySet() );
			Collections.sort( listKeys );
			
			char cLastPrefix = ' ';
			
			for ( final String strName : listKeys ) {
				if ( null!=strName && !strName.isEmpty() ) {
	
					final char cThisPrefix = strName.charAt( 0 );
					if ( cLastPrefix != cThisPrefix ) {
						
						if ( !Character.isAlphabetic( cLastPrefix ) ) {
							new TableItem( tableDetails, SWT.NONE );
						}
						cLastPrefix = cThisPrefix;
					}
					
					final String strValue = map.get( strName );
					
					final TableItem item = new TableItem( tableDetails, SWT.NONE );
					item.setText( 0, strName );
					item.setText( 1, strValue );
				}
			}
		}
	}
	
	
	private void addNode(	final TreeItem parent,
							final Node node ) {
		final TreeItem item = new TreeItem( parent, SWT.NONE );
		item.setText( node.strName );
		item.setData( node );
		
		for ( final Node nodeChild : node.list ) {
			addNode( item, nodeChild );
		}
	}
	
	
	private void drawTree() {
		if ( null==treeNodes ) return;
		if ( null==display ) return;
		if ( display.isDisposed() ) return;
		
		if ( valid ) return;
		
//		System.out.println( "Redrawing tree.." );
		
		display.asyncExec( new Runnable() {
			@Override
			public void run() {
				if ( treeNodes.isDisposed() ) return;
				
				treeNodes.removeAll();
				
				TreeModel model = new TreeModel();
				model.build();
				
				for ( final Node node : model.listRoots ) {

					final TreeItem itemNode = new TreeItem( treeNodes, SWT.NONE );
					itemNode.setText( node.strName );
					
					for ( final Node nodeChild : node.list ) {
						addNode( itemNode, nodeChild );
					}
					
				}
				valid = true;
			}
		});
	}
	
	

	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {

	    final CTabItem tab = new CTabItem( tabs, SWT.NONE );
//	    tab.setText( TAB_PAD_PREFIX + this.getName() + TAB_PAD_SUFFIX );
	    tab.setShowClose( false );

	    final Composite comp = this.buildUI( tabs );

	    comp.addFocusListener( new FocusAdapter() {
	    	@Override
	    	public void focusGained(FocusEvent arg0) {
    			drawTree();
	    	}
		});

	    tabs.addSelectionListener( new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected( final SelectionEvent event ) {
	    		if ( tab == event.item ) { 
	    			drawTree();
	    		}
	    	}
		});
	    
	    tab.setControl( comp );
	    this.tab = tab;
	    return tab;
	}

}
