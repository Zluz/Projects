package jmr.pr134.fonts;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import jmr.pr134.fonts.FontProvider.FontResource;

public class FontDemo {

	
	final static String DEMO_NUMBERS = "1234567890";
	final static String DEMO_ABC = "AaBbCc-LTMWQjg";
	final static String DEMO_SENTANCE = 
							"The quick brown fox jumps over the lazy dog.";
	
	final static String IMAGE_SAVE_FILE = "FontDemo.png";
	
	
	public static void main( final String[] args ) {
		
		final Display display = new Display();
		final Shell shell = new Shell( display, SWT.DIALOG_TRIM | SWT.RESIZE );
		shell.setLayout( new FillLayout() );
		shell.setText( "Font Demo" );
		shell.setSize( 1300, 900 );

		final FontProvider fonts = new FontProvider( display );
		
		for ( final FontResource fr : FontResource.values() ) {
			fonts.get( fr ); // just make sure to load everything
		}
		final List<String> list = fonts.getFontList();
		System.out.println( "Available fonts:" );
		for ( final String strName : list ) {
			System.out.println( "\t" + strName );
		}

		final String[] strFilename = new String[]{ null };
		
		
		final Canvas canvas = new Canvas( shell, SWT.NO_BACKGROUND );
		
		canvas.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {
				int iY = 0;
				int iX = 0;
				int iMaxX = 0;
				int iMaxY = 0;
				final GC gcEvent = e.gc;
				int iIndex = 0;

				final Image image = new Image( display, gcEvent.getClipping() );
				final GC gc = new GC( image );

				gc.setBackground( display.getSystemColor( SWT.COLOR_DARK_BLUE ) );
				gc.setForeground( display.getSystemColor( SWT.COLOR_GRAY ) );
				gc.fillRectangle( gc.getClipping() );

				for ( final FontResource fr : FontResource.values() ) {
					final Font font = fonts.get( fr, 32, 0 );
					gc.setFont( font );
					final Point pointExtent = gc.textExtent( DEMO_ABC );
					iMaxY = Math.max( iMaxY, pointExtent.y ); 
				};

				
				final Font fontSystem = display.getSystemFont();
				gc.setFont( fontSystem );
				final String strSystemFontName = 
									fontSystem.getFontData()[0].getName();
				final String strText = 
						"  Time Now: " + new Date().toString() 
						+ ",  System Font: " + strSystemFontName;
				gc.drawText( strText, 0, 0 );
				
				
				final int iTop = gc.textExtent( "Aj" ).y;
				iY = iTop;
				iIndex = 0;
				for ( final FontResource fr : FontResource.values() ) {
					
					iY = iIndex * iMaxY + iTop;
					iIndex++;
					
					final Font font = fonts.get( fr, 32, 0 );
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

				iY = iTop;
				iX += iMaxX + 40;
				iMaxX = 0;
				iIndex = 0;
				for ( final FontResource fr : FontResource.values() ) {

					iY = iIndex * iMaxY + iTop;
					iIndex++;

					final Font font = fonts.get( fr, 32, 0 );
					gc.setFont( font );
					gc.drawText( DEMO_ABC, iX, iY );
					final Point pointExtent = gc.textExtent( DEMO_ABC );
					iY += iMaxY;
					iMaxX = Math.max( iMaxX, pointExtent.x ); 
				};

				iY = iTop;
				iX += iMaxX + 20;
				iMaxX = 0;
				iIndex = 0;
				for ( final FontResource fr : FontResource.values() ) {
					
					iY = iIndex * iMaxY + iTop;
					iIndex++;
					
					gc.setForeground( display.getSystemColor( SWT.COLOR_DARK_GREEN ) );
					gc.drawLine( 0, iY, gc.getClipping().width, iY );

//					final Font font = fonts.get( fr, 20, 0 );
//					gc.setFont( font );
					gc.setFont( fonts.get( fr, 20, SWT.BOLD ) );
					gc.setForeground( display.getSystemColor( SWT.COLOR_CYAN ) );
					
					gc.drawText( fr.getName(), iX, iY, true );
					iY += iMaxY / 2;
					
					gc.setFont( fonts.get( fr, 18, 0 ) );
					gc.setForeground( display.getSystemColor( SWT.COLOR_GRAY ) );

					gc.drawText( DEMO_SENTANCE, iX, iY );
					iY += iMaxY / 2;
				};


				if ( null == strFilename[0] ) {

					final File file = new File( 
							FontProvider.FONT_BASE_PATH, IMAGE_SAVE_FILE );
					strFilename[0] = file.getAbsolutePath();
					
					final ImageLoader ilSave = new ImageLoader();
					ilSave.data = new ImageData[] { image.getImageData() };
					ilSave.save( strFilename[0], SWT.IMAGE_PNG );
					
					System.out.println( "File saved: " + strFilename[0] );
				}
				
				gcEvent.drawImage( image, 0, 0 );
			}
		});

		shell.open();
		
		while ( ! shell.isDisposed() ) {
			if ( ! display.readAndDispatch() ) {
				display.sleep();
			}
		}
	}
	
}
