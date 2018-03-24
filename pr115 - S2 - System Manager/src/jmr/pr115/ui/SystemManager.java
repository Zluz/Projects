package jmr.pr115.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SystemManager {

	
	public static void main( final String[] args ) {
		
		final Display display = Display.getDefault();
		final Shell shell = new Shell( display, SWT.SHELL_TRIM );
		
		shell.setText( "System Manager" );
		shell.setLayout( new FillLayout() );
//		shell.setSize( 1300, 500 );


		final CTabFolder folder = new CTabFolder( shell, SWT.NONE );
		folder.setSimple( false );
		
		final CTabItem tabDevices = new CTabItem( folder, SWT.NONE );
		tabDevices.setText( "Devices" );
		final Composite compDevices = new Composite( folder, SWT.NONE );
		tabDevices.setControl( compDevices );

		final CTabItem tabSchedules = new CTabItem( folder, SWT.NONE );
		tabSchedules.setText( "Schedules" );
		final Composite compSchedules = new Composite( folder, SWT.NONE );
		tabSchedules.setControl( compSchedules );

		final CTabItem tabEvents = new CTabItem( folder, SWT.NONE );
		tabEvents.setText( "Events" );
		final Composite compEvents = new Composite( folder, SWT.NONE );
		tabEvents.setControl( compEvents );
		

		compDevices.setLayout( new FillLayout() );
		final DeviceTable devices = new DeviceTable( compDevices );
		
		
		
		shell.setSize( 1700, 800 );
		shell.open();
		devices.redraw();

		while ( !shell.isDisposed() ) {
			if ( display.readAndDispatch() ) {
				display.sleep();
			}
		}
		System.exit( 1 );
	}
	
}
