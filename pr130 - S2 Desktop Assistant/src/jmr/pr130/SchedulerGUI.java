package jmr.pr130;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SchedulerGUI {

//	final static Display display = Display.getCurrent();
	final static Display display = S2TrayIcon.display;

	final private Shell shell;
	
	private static SchedulerGUI instance = null;
	
	
	private SchedulerGUI() {
		shell = new Shell( display, SWT.TITLE | SWT.MIN | SWT.RESIZE );
		this.shell.setText( SchedulerGUI.class.getName() );
		
		shell.setLayout( new FillLayout() );
		
		final Text text = new Text( shell, SWT.MULTI );
		text.setText( "<Configuration text here>" );
		
		shell.setSize( 400, 200 );
		shell.layout();
		
		final Image image = S2TrayIcon.getS2Icon();
		shell.setImage( image );
		
		shell.addListener( SWT.Close, new Listener() {
			@Override
			public void handleEvent( final Event event ) {
				shell.setVisible( false );
				event.doit = false;
			}
		});
	}
	
	
	public static synchronized SchedulerGUI get() {
		if ( null==instance ) {
			instance = new SchedulerGUI();
		}
		return instance;
	}
	

	public void open() {
		shell.setVisible( true );
		shell.setActive();
	}
	
	
	public Shell getShell() {
		return this.shell;
	}

	
	public void close() {
		this.shell.close();
		this.shell.dispose();
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
