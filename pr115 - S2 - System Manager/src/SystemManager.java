import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellSelectionListener;
import de.kupzog.ktable.SWTX;

public class SystemManager {

	
	public static void main( final String[] args ) {
		
		final Display display = Display.getDefault();
		final Shell shell = new Shell( display, SWT.SHELL_TRIM );
		
		shell.setText( "System Manager" );
		shell.setLayout( new FillLayout() );
//		shell.setSize( 1300, 500 );

		final DeviceTableModel model = new DeviceTableModel();

		final KTable table = new KTable( shell, 
//				SWT.NULL );
				SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
				| SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY );
		table.setModel( model );
		
		final KTableCellSelectionListener listenerSelection = 
						new KTableCellSelectionListener() {
			@Override
			public void cellSelected(int col, int row,
					int statemask) {
				System.out.println();
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
		final Integer[] arrWidth = new Integer[]{ 0 };
		
		table.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent event ) {
				if ( 0==arrWidth[0] ) {
					final int iWidth = event.gc.textExtent( "0123456789" ).x;
					final int iX = Math.round( iWidth / 10 );
					arrWidth[0] = iX;
					model.setCharWidth( iX );
				};
			}
		});
		
//		shell.pack();
		shell.setSize( 1800, 600 );
		shell.open();
		table.redraw();

		while ( !shell.isDisposed() ) {
			if ( display.readAndDispatch() ) {
				display.sleep();
			}
		}
		System.exit( 1 );
	}
	
}
