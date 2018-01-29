package jmr.rpclient.tiles;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;
import jmr.util.hardware.rpi.CameraModule;

public class CameraTile extends TileBase {


	private Image imageStill = null;
//	private Object semaphore = "semaphore";
	private File fileLastImage;
	
	private Point ptDesiredImageSize = null;
	
	
	public CameraTile() {
		final CameraModule camera = CameraModule.get();
		if ( camera.isCameraPresent() ) {
			final Thread thread = new Thread( "Camera Tile capture" ) {
				@Override
				public void run() {
					try {
						for (;;) {
							Thread.sleep( 2000 );
							if ( null!=ptDesiredImageSize ) {
								if ( null!=fileLastImage ) {
									fileLastImage.delete();
									fileLastImage = null;
								}
								final File file = camera.getStillPictureFile();
								if ( null!=file ) {
									final Image imgRaw = new Image( 
											UI.display, file.getAbsolutePath() );
									final Image imgScaled = new Image( 
														UI.display, 
														ptDesiredImageSize.x, 
														ptDesiredImageSize.y );
									final GC gc = new GC( imgScaled );
									gc.drawImage( imgRaw, 0, 0,
											imgRaw.getBounds().width,
											imgRaw.getBounds().height,
											0, 0,
											ptDesiredImageSize.x,
											ptDesiredImageSize.y );
									gc.dispose();
									imgRaw.dispose();

									if ( null!=imageStill ) {
										imageStill.dispose();
										imageStill = null;
									}
									imageStill = imgScaled;
		//							file.delete();
									fileLastImage = file;
								}
							}
						}
					} catch ( final InterruptedException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}			
			};
			thread.start();
		}
	}
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		final Rectangle r = image.getBounds();
		if ( null==ptDesiredImageSize ) {
			ptDesiredImageSize = new Point( r.width, r.height );
		}
		
//		synchronized ( semaphore ) {
//			gc.setBackground( UI.COLOR_BLACK );
//			gc.setForeground( UI.COLOR_DARK_GRAY );
			
			if ( null!=imageStill ) {
				gc.setFont( Theme.get().getFont( 12 ) );
				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				try {
					gc.setAntialias( SWT.OFF );
					gc.setInterpolation( SWT.LOW );
					gc.setInterpolation( SWT.OFF );
					synchronized ( imageStill ) {
//						final ImageData idStill = imageStill.getImageData();
//						gc.drawImage( 
//								imageStill, 0, 0, idStill.width, idStill.height,
//								0, 0, rect.width, rect.height );
						gc.drawImage( imageStill, 0, 0 );
					}
				} catch ( final Exception e ) {
					e.printStackTrace();
					gc.drawText( e.toString(), 4, 20 );
				}
			} else {
				gc.setForeground( Theme.get().getColor( Colors.LINE_FAINT ) );

				gc.fillRectangle( r.x, r.y, r.width, r.height );
				
				gc.drawLine( 0,0, r.width, r.height );
				gc.drawLine( 0,r.height, r.width,0 );

				drawTextCentered( "     (no camera data)     ", 70 );
			}
//		}
		
	}
	
	@Override
	protected void activateButton( final int iIndex ) {}
	

}
