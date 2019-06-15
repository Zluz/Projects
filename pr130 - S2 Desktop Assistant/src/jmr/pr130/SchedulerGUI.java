package jmr.pr130;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SchedulerGUI {


	final static Display display = new Display();

	final private Shell shell;
	
	
	public SchedulerGUI() {
		shell = new Shell( display );
		this.shell.setText( SchedulerGUI.class.getName() );
		shell.layout();
	}
	

	public void open() {
		shell.setVisible( true );
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
