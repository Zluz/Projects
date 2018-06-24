package jmr.rpclient.tiles;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
import jmr.rpclient.swt.UI;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;
import jmr.util.hardware.rpi.CameraModule;
import jmr.util.transform.DateFormatting;

public class CameraTile extends TileBase {

	public static enum CameraLocation {
		
		LOCAL( null ),
		ALL_CAPTURES( null );
		;
		
		final String strAddress;
		
		private CameraLocation( final String strAddress ) {
			this.strAddress = strAddress;
		}
	}
	
	public static enum DisplayMode {
		SINGLE_FULL,
		SHOW_CONTROLS,
		SHOW_ALL,
		;
	}
	

	public static final int BUTTON_SHOW_ALL = 1;
	public static final int BUTTON_SHOW_LOCAL = 2;
	

	public static final boolean GRAPHICS_ADVANCED = false;
	private Image imageStill = null;
	private File fileLastImage;
	private long lLastFileModified = 0;
	private String strLastMessage = null;
	
	private Point ptDesiredImageSize = null;
	
	private Point ptTouch = null;
	
	private final CameraLocation location;
	
	String strMessage = "Uninitialized";
	String strLocationMatch = null;
	
	Boolean bHasCameraModule = null;
	
	private final Map<Rectangle,String> mapClickRegions = new HashMap<>();
	
	Long lImageAge = null;
	
	private DisplayMode mode = DisplayMode.SINGLE_FULL;
	
	FileSession filesession = null;
	
	
	
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
			this.setMode( DisplayMode.SHOW_ALL );
		}
		
		if ( bOk ) {
			final Thread thread = new Thread( "Camera Tile capture" ) {
				@Override
				public void run() {
					try {
						for (;;) {
							Thread.sleep( 1000 );
							refreshImageData( false );
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
	
	
	private synchronized void refreshImageData( final boolean bFast ) {

		if ( UI.display.isDisposed() ) return;
		
		if ( null!=ptDesiredImageSize ) {
			
			final Image image;
	
			switch ( mode ) {
				case SHOW_ALL: {
					image = prerenderAllCaptures();
					break;
				}
				case SHOW_CONTROLS:
				case SINGLE_FULL:
				default: {
					image = prerenderSingleImage( bFast ); 
				}
			}
			
			if ( null!=image ) {
				if ( null!=imageStill ) {
					imageStill.dispose();
					imageStill = null;
				}
				imageStill = image;
			}
		}
	}
	
	
	private Image prerenderAllCaptures() {
		
		this.mapClickRegions.clear();
		this.lImageAge = null;

		
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
		

		gc.setBackground( UI.COLOR_DARK_GRAY );
		gc.fillRectangle( 0, 0, ptDesiredImageSize.x, ptDesiredImageSize.y );
		
		gc.setFont( UI.FONT_SMALL );
		gc.setForeground( UI.COLOR_WHITE );
		
		
		int iX = 20;
		int iY = 60;
		int iCount = 0;
		
		final List<String> listKeys = new LinkedList<>( map.keySet() );
		Collections.sort( listKeys );
		for ( final String strKey : listKeys ) {
			final FileSession session = map.get( strKey );
			
			final List<File> files = session.getCaptureStillImageFiles();
			for ( final File file : files ) {
				final String strFilename = file.getName();
				if ( file.isFile() && 
						strFilename.contains( "-thumb." ) ) {
					if ( file.lastModified() > lCutoff ) {

						final int iPos = strFilename.indexOf( "-thumb." );
						final String strFull = strFilename.substring( 0, iPos );

						try {
							final Image imgRaw = new Image( 
									UI.display, file.getAbsolutePath() );
	
							int iRX = imgRaw.getBounds().width;
							int iRY = imgRaw.getBounds().height;
							
							final Rectangle r = new Rectangle( 
										iX, iY, iRX * 165/300, iRY * 165/300 );
							
							gc.drawImage( imgRaw, 0, 0, iRX, iRY, 
									r.x, r.y, r.width, r.height );
							
							
							mapClickRegions.put( r, strKey + "/" + strFull );
							
						} catch ( final Exception e ) {
							gc.drawText( e.toString(), iX, iY );
							e.printStackTrace();
						}
						iCount++;

						final String strDisplayKey = "x-" + strKey.substring( 9 );
						
						gc.drawText( strDisplayKey, iX, iY - 52 );
						gc.drawText( strFull, iX, iY - 30 );

						iX = iX + 185;
						if ( iX + 100 > gc.getClipping().width ) {
							iX = 20;
							iY = iY + 200;
						}
						
//						iY = iY + 220;
//						if ( iY+100 > ptDesiredImageSize.y ) {
//							iY = 60;
//							iX = iX + 240;
//						}
					}
				}
			}
		}
		
		strMessage = "\n\n\n" + iCount + " captures";
		
		return imgScaled;
	}
	
	
	private Image prerenderSingleImage( final boolean bFast ) {

		mapClickRegions.clear();
		
		final File file = getImageFile( bFast );
		if ( null!=fileLastImage ) {
			if ( lLastFileModified == file.lastModified() 
						&& null!=strLastMessage ) {
				this.strMessage = strLastMessage;
				return null;
			}
			
			fileLastImage = null;
		}
		if ( null!=file ) {
			try {
				
				if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
					this.strMessage += "\n"
							+ "" + file.getParent() + "\n"
							+ "File size: " + file.length() + "\n";
				}
				
				
				
				final Image imgRaw = new Image( 
						UI.display, file.getAbsolutePath() );
				final Image imgScaled = new Image( 
									UI.display, 
									ptDesiredImageSize.x, 
									ptDesiredImageSize.y );
				final GC gc;
				try {
					gc = new GC( imgScaled ); // NPE?
				} catch ( final NullPointerException e ) {
					return null;
				}
				if ( !GRAPHICS_ADVANCED ) {
					gc.setAdvanced( false );
					gc.setAntialias( SWT.OFF );
				}
				final int iRX = imgRaw.getBounds().width;
				final int iRY = imgRaw.getBounds().height;
				
				if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
					this.strMessage += "\n"
							+ "Image size: " + iRX + " x " + iRY + "\n";
					this.strMessage += ""
							+ "bytesPerLine: " 
							+ imgRaw.getImageData().bytesPerLine + "\n";
					
					if ( null!=filesession ) {
						final String strDescription = 
								filesession.getDescriptionForImageSource( file );
						this.strMessage += "\n\n\n\n\n\n\n" 
								+ strDescription + "\n";
					}
					
				}
				
				
				gc.drawImage( imgRaw, 0, 0, iRX, iRY, 
						0, 0,
						ptDesiredImageSize.x,
						ptDesiredImageSize.y );


				gc.dispose();
				imgRaw.dispose();
				
				fileLastImage = file;
				lLastFileModified = file.lastModified();
				
				this.lImageAge = file.lastModified();
				this.strLastMessage = this.strMessage;
				
				return imgScaled;
				
			} catch ( final Exception e ) {
				// may run into FileNotFoundException here
				// because image file may update during this method.
				// just skip.
				this.strMessage += "\n"
						+ e.toString();
			}
		}
				
		this.lImageAge = null;
		return null;
	}
	
	
	public File getImageFile( final boolean bFast ) {
		if ( CameraLocation.LOCAL.equals( location ) ) {
			final CameraModule module = CameraModule.get();
			if ( module.isCameraPresent() ) {
				this.bHasCameraModule = true;
				final File file = module.getStillThumbnailFile();
				if ( null!=file ) {
					strMessage = "Local";
					return file;
				} else {
					strMessage = "Image from camera module is null";
				}
			} else {
				this.bHasCameraModule = false;
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
			if ( bFast ) {
				bRequireThumb = true;
			} else if ( null!=this.ptDesiredImageSize 
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
						filesession = session;
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
		final long lNow = System.currentTimeMillis();

		if ( null==ptDesiredImageSize ) {
			ptDesiredImageSize = new Point( r.width, r.height );
		}
		
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
		

		if ( null != strMessage ) {
			gc.setFont( UI.FONT_SMALL );

			int iY = 20;
			for ( final String strLine : strMessage.split( "\n" ) ) {
				if ( !strLine.isEmpty() ) {
					gc.setForeground( UI.COLOR_BLACK );
					gc.drawText( strLine, 18, iY + 0, true );
					gc.drawText( strLine, 20, iY + 0, true );
					gc.drawText( strLine, 18, iY + 2, true );
					gc.drawText( strLine, 20, iY + 2, true );
					gc.setForeground( UI.COLOR_WHITE );
					gc.drawText( strLine, 19, iY + 1, true );
				}
				iY += 28;
			}
		}
		
		if ( null != this.lImageAge ) {
			final long lElapsed = lNow - lImageAge.longValue();
			
			final String strLine = DateFormatting.getSmallTime( lElapsed );
			final Point pt = gc.textExtent( strLine );
			final int iX = r.width - 30 - pt.x;
			final int iY = r.height - 30 - pt.y;
			gc.setForeground( UI.COLOR_BLACK );
			gc.drawText( strLine, iX - 1, iY + 0, true );
			gc.drawText( strLine, iX + 1, iY + 0, true );
			gc.drawText( strLine, iX - 1, iY + 2, true );
			gc.drawText( strLine, iX + 1, iY + 2, true );
			gc.setForeground( UI.COLOR_WHITE );
			gc.drawText( strLine, iX + 0, iY + 1, true );
		}
		
		if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
			
			super.addButton( gc, BUTTON_SHOW_ALL, 
							20, r.height - 180, 120, 40, "Show All" );
			final S2Button btnShowLocal = 
					super.addButton( gc, BUTTON_SHOW_LOCAL, 
							20, r.height - 120, 120, 40, "Show Local" );
			if ( Boolean.FALSE.equals( this.bHasCameraModule ) ) {
				btnShowLocal.setState( ButtonState.DISABLED );
			}
		}
		
		final Point pt = this.ptTouch;
		if ( null!=pt ) {
			gc.setForeground( UI.COLOR_BLUE );
			gc.drawOval( pt.x - 10, pt.y - 10, 20, 20 );
		}
		
	}
	
	
	private void setMode( final DisplayMode mode ) {
		if ( null==mode ) return;
		
		if ( this.mode == mode ) return;
		
		this.strLastMessage = null;
		final DisplayMode modeLast = this.mode;
		this.mode = mode;
		this.filesession = null;
		
		switch ( mode ) {
			case SHOW_ALL: {
				refreshImageData( true );
				break;
			}
			case SHOW_CONTROLS: {
				if ( DisplayMode.SHOW_ALL.equals( modeLast ) ) {
					refreshImageData( true );
				}
				break;
			}
			case SINGLE_FULL: {
				if ( DisplayMode.SHOW_ALL.equals( modeLast ) ) {
					this.strMessage = null;
					refreshImageData( true );
//					new Thread() {
//						public void run() {
//							try {
//								Thread.sleep( 200 );
//								UI.display.asyncExec( new Runnable() {
//									@Override
//									public void run() {
//										refreshImageData( false );
//									}
//								});
//							} catch ( final InterruptedException e ) {
//								// ignore
//							}
//						};
//					}.start();
				}
				break;
			}
		}
	}
	
	
	@Override
	protected void activateButton( final S2Button button ) {
		if ( null==button ) return;
		
		button.setState( ButtonState.WORKING );
		this.strLastMessage = null;
		
		switch ( button.getIndex() ) {
			case BUTTON_SHOW_ALL: {
				setMode( DisplayMode.SHOW_ALL );
				this.removeAllButtons();
				break;
			}
		}
		
		button.setState( ButtonState.READY );
	}
	
	
	@Override
	public boolean clickCanvas( final Point point ) {
		
		if ( DisplayMode.SHOW_ALL.equals( this.mode ) ) {
			
			for ( final Entry<Rectangle, String> entry : 
									mapClickRegions.entrySet() ) {
				final Rectangle r = entry.getKey();
				if ( r.contains( point ) ) {
					this.ptTouch = null;
					this.strLocationMatch = entry.getValue();
					this.setMode( DisplayMode.SINGLE_FULL );
					return true;
				}
			}
			
			this.ptTouch = point;
			return false;
			
		} else if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
			this.ptTouch = null;
			this.removeAllButtons();
			this.setMode( DisplayMode.SINGLE_FULL );
		} else {
			this.ptTouch = null;
			this.setMode( DisplayMode.SHOW_CONTROLS );
		}
		return true;
	}
	

}
