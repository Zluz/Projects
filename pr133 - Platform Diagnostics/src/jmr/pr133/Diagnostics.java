package jmr.pr133;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Diagnostics {

	public final static Display DISPLAY = new Display();
	
	private final Shell shell;

	
	public Diagnostics( final Display display ) {
		
	    this.shell = new Shell( display );
	    
	    shell.setLayout(new FillLayout());
	    shell.setText( "Test SWT - pr133" );
	    
	    final Canvas canvas = new Canvas( shell, SWT.NONE );
	    canvas.addPaintListener( generateCanvasPaintListener() );

//	    final Rectangle rect = display.getClientArea();
//	    shell.pack();
//	    shell.setMaximized( true );
	    shell.setSize( 800, 600 );
	    shell.open();
	}
	
	
	private PaintListener generateCanvasPaintListener() {
		final PaintListener painter = new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {
				final GC gc = e.gc;
				gc.setAdvanced( true );
				gc.setAntialias( SWT.ON );
				gc.setTextAntialias( SWT.ON );
				
				final Canvas canvas = (Canvas)e.widget;
				final Display display = canvas.getDisplay();
				
				gc.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
				gc.setBackground( display.getSystemColor( SWT.COLOR_BLACK ) );
				final Rectangle rectBounds = canvas.getBounds();
				gc.fillRectangle( rectBounds );
				final int iMaxY = rectBounds.height;
				final int iMaxX = rectBounds.width;

				gc.setForeground( display.getSystemColor( SWT.COLOR_GRAY ) );

				int iY = 0;
				while ( iY < iMaxY ) {
					final int iSpacing = 6 * iY / iMaxY + 1;
					gc.setLineWidth( iSpacing );
					gc.drawLine( 0, iY, 30, iY );
					iY = iY + iSpacing + iSpacing;
				}
				int iX = 30;
				while ( iX < iMaxX ) {
					final int iSpacing = 6 * iX / iMaxX + 1;
					gc.setLineWidth( iSpacing );
					gc.drawLine( iX, 0, iX, 30 );
					iX = iX + iSpacing + iSpacing;
				}
				
				iY = 0;
				int iSize = 6;
				String strText = "AaBbCc Font height ";
				while ( iY < iMaxY ) {
					
//					gc.setAdvanced( true );
//					gc.setAntialias( SWT.ON );
//					gc.setTextAntialias( SWT.ON );

				    final FontData fd = display.getSystemFont().getFontData()[0];
				    fd.setHeight( iSize );
					final Font font = new Font( display, fd );
					gc.setFont( font );
					
					final String strTextFull = strText + iSize;
					final Point ptExtent = gc.textExtent( strTextFull );
					final int iHeight = (int) ( 0.7d * ptExtent.y );
					gc.drawText( strTextFull, 34, iY + 30, true );
					font.dispose();

					if ( ptExtent.x > 180 ) {
						if ( strText.length() > 12 ) {
							strText = "AaBbCc Font ";
						} else {
							strText = "AaBbCc ";
						}
					}
					iSize = iSize + 1;
					iY = iY + iHeight + 1;
				}
				

			    final FontData fd = display.getSystemFont().getFontData()[0];
			    fd.setHeight( 30 );
				final Font font = new Font( display, fd );
				gc.setFont( font );
				
				final String strTime = new Date().toString();
				
				gc.drawText( strTime, 34 + 180, 30, true );

				
			}
		};
		return painter;
	}
	
	
	public static void launch() {
		main( new String[]{} );
	}
	
	public static void main( String[] args ) {
		
		final Diagnostics diag = new Diagnostics( DISPLAY );
		final Shell shell = diag.shell;
		
	    while ( ! shell.isDisposed() ) {
	      if ( ! shell.getDisplay().readAndDispatch() ) {
	        DISPLAY.sleep();
	      }
	    }
	    DISPLAY.dispose();
	}
	
}
