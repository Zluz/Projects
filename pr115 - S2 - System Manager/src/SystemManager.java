import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellSelectionListener;
import de.kupzog.ktable.SWTX;

public class SystemManager {

	
	public static void main( final String[] args ) {
		
		final Display display = Display.getDefault();
		final Shell shell = new Shell( display );
		
		shell.setText( "System Manager" );
		shell.setLayout( new FillLayout() );
		shell.setSize( 500, 500 );
//		new Label( shell, SWT.NONE ).setText( "test" );
		
		final KTable table = new KTable( shell, 
//				SWT.NULL );
				SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
				| SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY );
		final DeviceTableModel model = new DeviceTableModel();
		table.setModel( model );
		
		final KTableCellSelectionListener listenerSelection = 
						new KTableCellSelectionListener() {
			@Override
			public void cellSelected(int col, int row,
					int statemask) {
		    	System.out.println( "DeviceTableKTableCellSelectionListenerModel.cellSelected() - "
		    			+ "col:" + col + ", row:" + row + ", statemask:" + statemask );
		    	final Object content = model.doGetContentAt( col, row );
		    	System.out.println( "\tContent: " + content );
			}

			@Override
			public void fixedCellSelected(int col, int row,
					int statemask) {}
		};
		table.addCellSelectionListener( listenerSelection );
		
//		shell.pack();
		shell.open();
		
		while ( !shell.isDisposed() ) {
			if ( display.readAndDispatch() ) {
				display.sleep();
			}
		}
	}
	
}
