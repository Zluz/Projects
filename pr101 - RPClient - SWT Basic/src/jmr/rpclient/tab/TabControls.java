package jmr.rpclient.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;

import jmr.rpclient.RPiTouchscreen;
import jmr.rpclient.SWTBasic;
import jmr.sharedb.Server;

@SuppressWarnings("unused")
public class TabControls extends TabBase {

	private Display display;
	final private Server server;
	private CTabFolder tabs;
	
	public enum Section {
		DEVICE( "Local Device" ),
		AUTOMATION( "Home Automation" ),
		TESLA( "Tesla Vehicle Interface" ),
		SPOTIFY( "Spotify Control" ),
		DEBUG( "Debugging Controls" ),
		;
		
		final public String strCaption;
		
		Section( final String strCaption ) {
			this.strCaption = strCaption;
		}
	}
	
	public CTabItem tab = null;
	
	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	
	public TabControls( final Server server ) {
		this.server = server;
	}
	
	
	public TopSection getMenuItem() {
		return TopSection.DEVICE_CONTROLS;
	}
	

	@Override
	public Composite buildUI( final Composite parent ) {
		display = parent.getDisplay();
		
	    final Composite comp = new Composite( parent, SWT.NONE );
	    comp.setLayout( new GridLayout( 5, true ) );
	    
	    final Composite compMaster = new Composite( comp, SWT.NONE );
	    compMaster.setLayout( new FillLayout( SWT.VERTICAL ) );
	    final GridData gdNode = new GridData( GridData.FILL_BOTH );
	    gdNode.horizontalSpan = 1;
		compMaster.setLayoutData( gdNode );

	    final Composite compDetail = new Composite( comp, SWT.NONE );
	    compDetail.setLayout( new FillLayout() );
	    final GridData gdPeer = new GridData( GridData.FILL_BOTH );
	    gdPeer.horizontalSpan = 4;
	    compDetail.setLayoutData( gdPeer );
	    

	    tabs = new CTabFolder( compDetail, SWT.NONE );
	    tabs.setSimple( true );
	    tabs.setMaximizeVisible( false );
	    
	    
	    
	    for ( final Section section : Section.values() ) {
		    final Button btn = new Button( compMaster, SWT.PUSH );
		    btn.setText( section.strCaption );
		    
		    final CTabItem tab = new CTabItem( tabs, SWT.NONE );
		    tab.setText( section.strCaption );
		    tab.setShowClose( false );
		    final Composite compTab = new Composite( tabs, SWT.NONE );
		    compTab.setLayout( new FillLayout() );
		    tab.setControl( compTab );
		    
		    buildUI( section, compTab );
		    
		    btn.addSelectionListener( new SelectionAdapter() {
		    	@Override
		    	public void widgetSelected( final SelectionEvent event ) {
		    		tabs.setSelection( tab );
		    	}
			});
	    }

		return comp;
	}
	
	private void buildUI(	final Section section,
							final Composite comp ) {
		if ( null==section ) return;
		switch ( section ) {
		
			case DEVICE:
				comp.setBackground( display.getSystemColor( SWT.COLOR_BLUE ) );

				final Composite compV = new Composite( comp, SWT.NONE );
				compV.setLayout( new FillLayout( SWT.VERTICAL ) );
				final Composite compA = new Composite( compV, SWT.NONE );
				final Composite compB = new Composite( compV, SWT.NONE );
				compB.setBackground( display.getSystemColor( SWT.COLOR_GREEN ) );

				final Composite compC = new Composite( compV, SWT.NONE );
				final Composite compBrightness = new Composite( compV, SWT.NONE );
				compBrightness.setLayout( new FillLayout() );
				final Scale scale = new Scale( compBrightness, SWT.HORIZONTAL );
				scale.setMinimum( 10 );
				scale.setMaximum( 255 );
				scale.addSelectionListener( new SelectionAdapter() {
					@Override
					public void widgetSelected( final SelectionEvent event ) {
						final int iPos = scale.getSelection();
						SWTBasic.get().log( "Brightness to " + iPos );
						RPiTouchscreen.getInstance().setBrightness( iPos );
					}
				});
				break;
		
			case DEBUG:
				final Button btnClose = new Button( comp, SWT.PUSH );
				btnClose.setText( "Close Application" );
				btnClose.addSelectionListener( new SelectionAdapter() {
					@Override
					public void widgetSelected( final SelectionEvent event ) {
						SWTBasic.close();
					}
				});
				break;

			default:
				break;
		}
	}
	

	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {

	    final CTabItem tab = new CTabItem( tabs, SWT.NONE );
//	    tab.setText( TAB_PAD_PREFIX + this.getName() + TAB_PAD_SUFFIX );
	    tab.setShowClose( false );

//	    tabs.addSelectionListener( new SelectionAdapter() {
//	    	@Override
//	    	public void widgetSelected( final SelectionEvent event ) {
//	    		if ( tabShowDB == event.item ) { 
//	    			drawTree();
//	    		}
//	    	}
//		});
	    
	    final Composite comp = this.buildUI( tabs );
	    tab.setControl( comp );
	    this.tab = tab;
	    return tab;	
    }

}

