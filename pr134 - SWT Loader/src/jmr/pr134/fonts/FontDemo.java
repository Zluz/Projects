package jmr.pr134.fonts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import jmr.pr134.fonts.FontProvider.FontResource;

public class FontDemo {

	final static String DEMO_NUMBERS = "1234567890";
	final static String DEMO_ABC = "AaBbCc-LTMWQjg";
	
	public static void main( final String[] args ) {
		
		final Display display = new Display();
		final Shell shell = new Shell( display, SWT.DIALOG_TRIM | SWT.RESIZE );
		shell.setLayout( new FillLayout() );
		
		final FontProvider fonts = new FontProvider( display );
		
		final Canvas canvas = new Canvas( shell, SWT.NO_BACKGROUND );
//		final Image image = new Image( display, 800, 600 );
		canvas.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {
				int iY = 0;
				int iX = 0;
				int iMaxX = 0;
				int iMaxY = 0;
				final GC gc = e.gc;
				gc.setBackground( display.getSystemColor( SWT.COLOR_DARK_BLUE ) );
				gc.setForeground( display.getSystemColor( SWT.COLOR_GRAY ) );
				gc.fillRectangle( gc.getClipping() );
				
				for ( final FontResource fr : FontResource.values() ) {
					final Font font = fonts.get( fr, 30, 0 );
					gc.setFont( font );
					final Point pointExtent = gc.textExtent( DEMO_ABC );
					iMaxY = Math.max( iMaxY, pointExtent.y ); 
				};

				iY = 0;
				for ( final FontResource fr : FontResource.values() ) {
					final Font font = fonts.get( fr, 30, 0 );
					gc.setFont( font );
					gc.drawText( DEMO_NUMBERS, iX, iY );
					final Point pointExtent = gc.textExtent( DEMO_NUMBERS );
					iMaxX = Math.max( iMaxX, pointExtent.x );
					
					int iMinNumX = Integer.MAX_VALUE;
					int iMaxNumX = 0;
					for ( int i = 0; i < 10; i++ ) {
						final String strDigit = Integer.toString( i );
						final int iWidth = gc.textExtent( strDigit ).x;
//						System.out.println( 
//									"testing: '" + strDigit + "' "
//									+ "width: " + iWidth );
						iMinNumX = Math.min( iMinNumX, iWidth );
						iMaxNumX = Math.max( iMaxNumX, iWidth );
					}
					final Font fontSub = fonts.get( fr, 14, 0 );
					gc.setFont( fontSub );
					int iXSub = iX + pointExtent.x;
					gc.drawText( "" + iMaxNumX, iXSub, iY );
					gc.drawText( "" + iMinNumX, iXSub, iY + ( iMaxY / 2 ) );
					iY += iMaxY;
				};

				iY = 0;
				iX += iMaxX + 40;
				iMaxX = 0;
				for ( final FontResource fr : FontResource.values() ) {
					final Font font = fonts.get( fr, 30, 0 );
					gc.setFont( font );
					gc.drawText( DEMO_ABC, iX, iY );
					final Point pointExtent = gc.textExtent( DEMO_ABC );
					iY += iMaxY;
					iMaxX = Math.max( iMaxX, pointExtent.x ); 
				};

				iY = 0;
				iX += iMaxX + 20;
				iMaxX = 0;
				for ( final FontResource fr : FontResource.values() ) {
					final Font font = fonts.get( fr, 30, 0 );
					gc.setFont( font );
					gc.drawText( fr.getName(), iX, iY );
					iY += iMaxY;
				};


			}
		});

		shell.setSize( 1100, 500 );
		shell.open();
		
		while ( ! shell.isDisposed() ) {
			if ( ! display.readAndDispatch() ) {
				display.sleep();
			}
		}
	}
	
}
