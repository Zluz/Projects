package jmr.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Simple popup window to show short text.
 */
public class QuickWindow {

	public final Display display;
	public final Shell shell;
	
	public final StringBuilder sbContent;
	
	public QuickWindow( final Display display,
						final StringBuilder sbContent ) {
//		final int iStyle = SWT.DIALOG_TRIM | SWT.RESIZE;
		final int iStyle = SWT.TOOL | SWT.SHELL_TRIM;
		this.shell = new Shell( display, iStyle );
		this.display = display;
		this.sbContent = sbContent;
		
		shell.setLayout( new FillLayout() );
		final Text text = new Text( shell, SWT.NONE );
		final String strContent = sbContent.toString();
		text.setText( strContent );
		
		text.addPaintListener( new PaintListener() {
			boolean bResized = false;
			@Override
			public void paintControl( final PaintEvent e ) {
				if ( bResized ) return;
				final String strText = text.getText();
				final Point ptContent = e.gc.textExtent( strText );
				final Point ptShell = shell.getSize();
				final Point ptText = text.getSize();
				final int iX = ptContent.x + ptShell.x - ptText.x + 8;
				final int iY = ptContent.y + ptShell.y - ptText.y + 8;
				shell.setSize( iX, iY );
				bResized = true;
			}
		} );
	}
	
	public Shell getShell() {
		return this.shell;
	}
	
	public void open() {
		if ( ! display.isDisposed() && ! shell.isDisposed() ) {
			this.display.asyncExec( ()-> shell.setVisible( true ) );
		}
	}
	
	public void close() {
		if ( ! display.isDisposed() && ! shell.isDisposed() ) {
			this.display.asyncExec( ()-> shell.setVisible( false ) );
		}
		shell.dispose();
	}
	

	public static void main( final String[] arr ) {
		final Display display = Display.getDefault();
		final StringBuilder sb = new StringBuilder();
		sb.append( "Hello, world!" );
		final QuickWindow window = new QuickWindow( display, sb );
		window.open();
		
		while ( ! window.getShell().isDisposed() ) {
			while ( display.readAndDispatch() ) {
				display.sleep();
			}
		}
	}
	
	
	
}
