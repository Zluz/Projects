package jmr.pr115.ui;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.SWTX;
import jmr.pr115.model.TextModelExample;

public class KTableTest {

	
	public static void main( final String[] args ) {
		final Display display = Display.getDefault();
		final Shell shell = new Shell( display );
		
		shell.setLayout( new FillLayout() );
//		new Label( shell, SWT.NONE ).setText( "test" );
		
		final KTable table = new KTable( shell, 
//				SWT.NULL );
				SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
				| SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY );
		table.setModel( new TextModelExample() );
		
		shell.pack();
		shell.open();
		
		while ( !shell.isDisposed() ) {
			if ( display.readAndDispatch() ) {
				display.sleep();
			}
		}
	}
	
}
