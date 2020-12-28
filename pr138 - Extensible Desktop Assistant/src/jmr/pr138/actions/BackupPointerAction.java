package jmr.pr138.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TrayItem;

import jmr.ui.BackupPointer;

public class BackupPointerAction implements ActionEntry {

	private static BackupPointer pointer = null;
	
	public BackupPointerAction( final TrayItem trayitem,
								final Menu menu ) {

		trayitem.addListener( SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent( final Event event ) {
				if ( null == BackupPointerAction.pointer ) {
					menu.setVisible( true );
				} else {
					BackupPointerAction.pointer.close();
					menu.setVisible( true );
					BackupPointerAction.pointer = new BackupPointer();
				}
			}
		});
	}
	
	@Override
	public MenuItem getMenuItem( final Menu menu ) {
		final MenuItem mi = new MenuItem( menu, SWT.CHECK );
		mi.setText( "Backup Pointer" );
//		this.shell = menu.getShell();
		
		final SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected( final SelectionEvent e ) {
				final boolean bRenderPointer = mi.getSelection();
				if ( null != pointer ) {
					pointer.close();
					pointer = null;
				}
				if ( bRenderPointer ) {
					pointer = new BackupPointer();
				}
			};
		};
		mi.addSelectionListener( listener );

		return mi;
	}

}
