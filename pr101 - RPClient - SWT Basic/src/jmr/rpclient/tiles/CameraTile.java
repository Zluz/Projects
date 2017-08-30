package jmr.rpclient.tiles;

import java.io.File;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.UI;
import jmr.util.hardware.rpi.CameraModule;

public class CameraTile extends TileBase {


	private Image imageStill = null;
	private Object semaphore = 1L;
	
	
	public CameraTile() {
		if ( CameraModule.get().isCameraPresent() ) {
			final Thread thread = new Thread( "Camera Tile capture" ) {
				@Override
				public void run() {
					try {
						Thread.sleep( 400 );
						final File file = 
								CameraModule.get().getStillPictureFile();
						if ( null!=file ) {
							synchronized ( semaphore ) {
								if ( null!=imageStill ) {
									imageStill.dispose();
									imageStill = null;
								}
								imageStill = new Image( UI.display, 
													file.getAbsolutePath() );
							}
							file.delete();
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
		
		synchronized ( semaphore ) {
			if ( null!=imageStill ) {
				final ImageData idStill = imageStill.getImageData();
				gc.drawImage( 
						imageStill, 0, 0, idStill.width, idStill.height,
						0, 0, rect.width, rect.height );
			} else {
				gc.setBackground( UI.COLOR_BLACK );
				gc.setForeground( UI.COLOR_DARK_GRAY );
				gc.fillRectangle( r.x, r.y, r.width, r.height );
				
				gc.drawLine( 0,0, r.width, r.height );
				gc.drawLine( 0,r.height, r.width,0 );

				drawTextCentered( "     (no camera data)     ", 70 );
			}
		}
		
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
