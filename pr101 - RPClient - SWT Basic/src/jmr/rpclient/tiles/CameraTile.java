package jmr.rpclient.tiles;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;
import jmr.rpclient.swt.UI;
import jmr.util.hardware.rpi.CameraModule;

public class CameraTile extends TileBase {

	public static enum CameraLocation {
		
		LOCAL( null ),
		DRIVEWAY( "B8-27-EB-4E-46-E2/capture_vid0.jpg" ),
		ALL_CAPTURES( null );
		;
		
		final String strAddress;
		
		private CameraLocation( final String strAddress ) {
			this.strAddress = strAddress;
		}
	}
	
	

	public static final boolean GRAPHICS_ADVANCED = false;
	private Image imageStill = null;
//	private Object semaphore = "semaphore";
	private File fileLastImage;
	
	private Point ptDesiredImageSize = null;
	
	private final CameraLocation location;
	
	String strMessage = "Uninitialized";
	String strLocationMatch = null;
	
	boolean bShowAll = false;
	
	private final Map<Rectangle,String> mapClickRegions = new HashMap<>();
	
	
	
	public CameraTile( final CameraLocation location ) {
		this.location = location;

		final boolean bOk;
		
		if ( CameraLocation.LOCAL.equals( location ) ) {
//			final CameraModule camera = CameraModule.get();
//			bOk = camera.isCameraPresent();
			bOk = true;
		} else {
			bOk = true;
		}
		
		if ( CameraLocation.ALL_CAPTURES.equals( location ) ) {
			this.bShowAll = true;
		}
		
		if ( bOk ) {
			final Thread thread = new Thread( "Camera Tile capture" ) {
				@Override
				public void run() {
					try {
						for (;;) {
							Thread.sleep( 1000 );
							if ( null!=ptDesiredImageSize ) {
								
								final Image image;
								
								if ( bShowAll ) {
									image = prerenderAllCaptures();
								} else {
									image = prerenderSingleImage();
								}
								
								if ( null!=image ) {
//								synchronized ( imageStill ) {
									if ( null!=imageStill ) {
										imageStill.dispose();
										imageStill = null;
									}
									imageStill = image;
	//							}
								}

							}
						}
					} catch ( final InterruptedException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch ( final Exception e ) {
						// can arrive here from NPE on new GC() above..
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}			
			};
			thread.start();
		}
	}
	
	
	private Image prerenderAllCaptures() {
		
		mapClickRegions.clear();
		
		final FileSessionManager fsm = FileSessionManager.getInstance();
		final Map<String, FileSession> map = fsm.getSessionMap();

		final Image imgScaled = new Image( 
				UI.display, 
				ptDesiredImageSize.x, 
				ptDesiredImageSize.y );
		
		final GC gc = new GC( imgScaled ); // NPE?
//		if ( !GRAPHICS_ADVANCED ) {
			gc.setAdvanced( false );
			gc.setAntialias( SWT.OFF );
//		}

		final long lCutoff = 
				System.currentTimeMillis() - TimeUnit.DAYS.toMillis( 1 );
		

		int iX = 50;
		int iY = 10;
		int iCount = 0;
		
		for ( final Entry<String, FileSession> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final FileSession session = entry.getValue();
			
			final List<File> files = session.getCaptureStillImageFiles();
			for ( final File file : files ) {
				final String strFilename = file.getName();
				if ( file.isFile() && 
						strFilename.contains( "-thumb." ) ) {
					if ( file.lastModified() > lCutoff ) {
						
						final Image imgRaw = new Image( 
								UI.display, file.getAbsolutePath() );

						int iRX = imgRaw.getBounds().width;
						int iRY = imgRaw.getBounds().height;
						
						final Rectangle r = new Rectangle( 
									iX, iY, iRX * 200/300, iRY * 200/300 );
						
						gc.drawImage( imgRaw, 0, 0, iRX, iRY, 
								r.x, r.y, r.width, r.height );
						iCount++;
						
						final int iPos = strFilename.indexOf( "-thumb." );
						final String strFull = strFilename.substring( 0, iPos );
						mapClickRegions.put( r, strKey + "/" + strFull );

						iY = iY + 200;
						if ( iY+200 >= ptDesiredImageSize.y ) {
							iY = 10;
//							iX = iX + 150;
							iX = iX + 250;
						}
						
					}
				}
			}

		}
		
		strMessage = "" + iCount + " captures";
		
		return imgScaled;
	}
	
	
	private Image prerenderSingleImage() {

		mapClickRegions.clear();
		
		if ( null!=fileLastImage ) {
//			fileLastImage.delete();
			fileLastImage = null;
		}
		final File file = getImageFile();
		if ( null!=file ) {
			final Image imgRaw = new Image( 
					UI.display, file.getAbsolutePath() );
			final Image imgScaled = new Image( 
								UI.display, 
								ptDesiredImageSize.x, 
								ptDesiredImageSize.y );
			final GC gc = new GC( imgScaled ); // NPE?
			if ( !GRAPHICS_ADVANCED ) {
				gc.setAdvanced( false );
				gc.setAntialias( SWT.OFF );
			}
			gc.drawImage( imgRaw, 0, 0,
					imgRaw.getBounds().width,
					imgRaw.getBounds().height,
					0, 0,
					ptDesiredImageSize.x,
					ptDesiredImageSize.y );
			
			gc.dispose();
			imgRaw.dispose();
			
			fileLastImage = file;
			
			return imgScaled;

		} else {
			return null;
		}
	}
	
	
	public File getImageFile() {
		if ( CameraLocation.LOCAL.equals( location ) ) {
			final CameraModule module = CameraModule.get();
			if ( module.isCameraPresent() ) {
				final File file = module.getStillThumbnailFile();
				if ( null!=file ) {
					strMessage = "Local";
					return file;
				} else {
					strMessage = "Image from camera module is null";
				}
			} else {
				strMessage = "Camera module not found";
			}
		} else {
			
			final String strLocation;
			if ( null!=this.strLocationMatch ) {
				strLocation = this.strLocationMatch;
			} else {
				strLocation = this.location.strAddress;
			}
			
			
			if ( null==strLocation ) {
				strMessage = "Camera location is null";
				return null;
			}
			
			final boolean bRequireThumb;
			if ( null!=this.ptDesiredImageSize 
					&& this.ptDesiredImageSize.x <= 300 ) {
				bRequireThumb = true;
			} else {
				bRequireThumb = false;
			}
			
			// find matching image (matching thumb or full)
			final File fileFirst = searchForFile( strLocation, bRequireThumb );
			if ( null!=fileFirst ) {
				return fileFirst;
			}

			// if no perfect match, find alt thumb/full
			final File fileSecond = searchForFile( strLocation, null );
			if ( null!=fileSecond ) {
				return fileSecond;
			}
			

			strMessage = "Remote camera not found: " + strLocation;
		}
		return null;
	}
	
	
	public File searchForFile(	final String strLocation,
								final Boolean bRequireThumb ) {
		
		final int iPosSlash = strLocation.indexOf( "/" );
		final String strFileMatch = strLocation.substring( iPosSlash + 1 );

		final FileSessionManager fsm = FileSessionManager.getInstance();
		final Map<String, FileSession> map = fsm.getSessionMap();
		for ( final Entry<String, FileSession> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			
			if ( strLocation.startsWith( strKey ) ) {
				
				final FileSession session = entry.getValue();
				
				final List<File> files = session.getCaptureStillImageFiles();
				
				for ( final File file : files ) {

					final String strFilename = file.getName();

					boolean bGood = true;
					
					if ( null!=bRequireThumb ) {
						final boolean bIsThumb = strFilename.contains( "-thumb." );
						bGood = ( bRequireThumb.equals( bIsThumb ) );
					}

					if ( bGood && strFilename.contains( strFileMatch ) ) {
						strMessage = strFilename;
						return file;
					}
				}
			}
		}
		return null;
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
					if ( !GRAPHICS_ADVANCED ) {
						gc.setAdvanced( false );
						gc.setAntialias( SWT.OFF );
						gc.setInterpolation( SWT.OFF );
					}
					synchronized ( imageStill ) {
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
			

			if ( null!=strMessage ) {
				gc.setFont( UI.FONT_SMALL );
				
				gc.setForeground( UI.COLOR_BLACK );
				gc.drawText( strMessage, 18, 18, true );
				gc.drawText( strMessage, 18, 20, true );
				gc.drawText( strMessage, 20, 18, true );
				gc.drawText( strMessage, 20, 20, true );
				gc.setForeground( UI.COLOR_WHITE );
				gc.drawText( strMessage, 19, 19, true );
			}
			
//		}
		
	}
	
	@Override
	protected void activateButton( final S2Button button ) {}
	
	@Override
	public boolean clickCanvas( final Point point ) {
		
		if ( bShowAll ) {
			
			for ( final Entry<Rectangle, String> entry : 
									mapClickRegions.entrySet() ) {
				final Rectangle r = entry.getKey();
				if ( r.contains( point ) ) {
					this.strLocationMatch = entry.getValue();
					bShowAll = false;
					return true;
				}
			}
			return false;
			
		} else {
			this.bShowAll = true;
		}
		return true;
	}
	

}
