package jmr.pr130;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import java.io.InputStream;

public class SchedulerGUI {


	final static Display display = new Display();

	final private Shell shell;
	final static Tray tray = display.getSystemTray();
	
	
//	final static String ICON_S2 = "/resources/icons/1872821.png"; // 3-line
//	final static String ICON_S2 = "/resources/icons/121155.png"; // cyan
	final static String ICON_S2 = "/resources/icons/116048.png"; // on-off
	
	
	public SchedulerGUI() {
		shell = new Shell( display );
		this.shell.setText( SchedulerGUI.class.getName() );
		
		shell.setLayout( new FillLayout() );
		
		final Text text = new Text( shell, SWT.MULTI );
		text.setText( "<Configuration text here>" );
		
		shell.setSize( 400, 200 );
		shell.layout();
		
		final TrayItem ti = new TrayItem( tray, SWT.NONE );
		ti.setToolTipText( "Desktop Assistant" );
		
		final Class<SchedulerGUI> classThis = SchedulerGUI.class;
		final InputStream is = classThis.getResourceAsStream( ICON_S2 );
		
		final Image image = new Image( display, is );
		ti.setImage( image );
		shell.setImage( image );
		
		ti.addListener( SWT.Selection, new Listener() {
			@Override
			public void handleEvent( final Event e ) {
				shell.setVisible( true );
				shell.setActive();
			}
		});
		
		final Menu menu = new Menu( shell, SWT.POP_UP );
		final MenuItem miShowSchedule = new MenuItem( menu, SWT.PUSH );
		final MenuItem miClipboard = new MenuItem( menu, SWT.PUSH );
		final MenuItem miPostEvent = new MenuItem( menu, SWT.PUSH );
		
		miShowSchedule.setText( "Show Scheduler GUI" );
		miClipboard.setText( "Clipboard to plain text" );
		miPostEvent.setText( "Post S2 status event" );
		
		ti.addListener( SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent( final Event event ) {
				menu.setVisible( true );
			}
		});
		
		miShowSchedule.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected( final SelectionEvent e ) {
				shell.setVisible( true );
				shell.setActive();
			}
		});
	}
	

	public void open() {
		shell.setVisible( true );
		shell.setActive();
	}
	
	
	public Shell getShell() {
		return this.shell;
	}

	

	public static void main( final String[] args ) {

		final SchedulerGUI gui = new SchedulerGUI();
		gui.open();

	    while ( ! gui.getShell().isDisposed()) {
		      if ( display.readAndDispatch()) {
		    	  display.sleep();
		      }
		}
		display.dispose(); 
	}
	
}
