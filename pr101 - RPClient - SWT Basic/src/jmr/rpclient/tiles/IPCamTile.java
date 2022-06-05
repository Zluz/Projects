package jmr.rpclient.tiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.fasterxml.jackson.databind.JsonNode;

import jmr.S2Path;
import jmr.S2Properties;
import jmr.pr151.S2ES;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.Tracker;
import jmr.rpclient.swt.UI;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;
import jmr.util.devices.IPCamera;
import jmr.util.transform.DateFormatting;

public class IPCamTile extends TileBase {


	public static enum DisplayMode {
		SINGLE_FULL,
		SINGLE_RECENT_MOTION,
//		SHOW_CONTROLS,
//		SHOW_ALL,
		;
	}
	
	private boolean bShowControls = false;
	
	private final static Logger 
				LOGGER = Logger.getLogger( IPCamTile.class.getName() );

	
	public static final int BUTTON_SHOW_ALL = 1;
	public static final int BUTTON_SHOW_LOCAL = 2;
	public static final int BUTTON_CAM_BASE = 10;
	public static final int BUTTON_CAM_MOTION = 100;
	

	public static final boolean GRAPHICS_ADVANCED = false;
	private Image imageStill = null;
	private File fileLastImage;
	private long lLastFileModified = 0;
	private String strLastMessage = null;
	
	private Point ptDesiredImageSize = null;
	
	private Point ptTouch = null;
	
//	private Camera camera;
	private IPCamera camera;
	
	String strMessage = "Uninitialized";
	String strLocationMatch = null;
	
	Boolean bHasCameraModule = null;
	
//	private final Map<Rectangle,String> mapClickRegions = new HashMap<>();
	private final Map<String,Rectangle> mapClickRegions = new HashMap<>();

	
	Long lImageAge = null;
	
	private DisplayMode mode = DisplayMode.SINGLE_FULL;

	FileSession filesession = null;
	

	private Image imageLastMotion = null;
	private String strLastMotionFilename = null;
	
	private boolean bRequestRefresh = false;
	
	
	
	public IPCamTile( final IPCamera camera ) {
		this.camera = camera;
		
		IPCamera.setProperties( S2Properties.get() );

		final boolean bOk = true;
		
//		if ( CameraLocation.ALL_CAPTURES.equals( location ) ) {
//			this.setMode( DisplayMode.SHOW_ALL );
//		}
		
		if ( bOk ) {
			final Thread thread = new Thread( "Camera Tile capture" ) {
				@Override
				public void run() {
					try {
						for (;;) {
							Thread.sleep( 4000 );
							if ( bRequestRefresh ) {
								refreshImageData( false );
								bRequestRefresh = false;
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
	
	
	private synchronized void refreshImageData( final boolean bFast ) {
		PerformanceMonitorTile.getInstance().addEvent( "refreshImageData()" );

		if ( UI.display.isDisposed() ) return;
		
		if ( null != ptDesiredImageSize ) {
			
			final Image image;
	
			image = prerenderSingleImage( bFast ); 
			
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
		if ( UI.display.isDisposed() ) return null;
		
//		this.mapClickRegions.clear();
		clearMapRegions();

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
		
		final Set<String> setKeys = new HashSet<>( map.keySet() );
		for ( final String strRegionKey : mapClickRegions.keySet() ) {
			final int iPos = strRegionKey.indexOf( '/' );
			final String strSession = strRegionKey.substring( 0, iPos );
			setKeys.add( strSession );
		}
		final List<String> listKeys = new LinkedList<>( setKeys );
//		listKeys.addAll( mapClickRegions.keySet() );
		Collections.sort( listKeys );
		
		for ( final String strKey : listKeys ) {
			final FileSession session = map.get( strKey );
			
			final List<File> files;
			if ( null!=session ) {
				files = session.getCaptureStillImageFiles( 
						FileSession.ImageLookupOptions.INCLUDE_MISSING, 
						FileSession.ImageLookupOptions.ONLY_THUMB, 
						FileSession.ImageLookupOptions.SINCE_PAST_HOUR );
			} else {
				files = Collections.emptyList();
			}
			
			final Map<String,File> mapFiles = new HashMap<>();
			for ( final File file : files ) {
				mapFiles.put( file.getName(), file );
			}
			
//			for ( final String strRegionKey : mapClickRegions.keySet() ) {
//				final int iPos = strRegionKey.indexOf( '/' );
//				final String strRegionSession = strRegionKey.substring( 0, iPos );
//				if ( strKey.equals( strRegionKey ) ) {
//					final String strFile = strRegionKey.substring( iPos + 1 );
//				}
//			}
			

			
			for ( final File file : files ) {
				
//				String strFull = "<missing file>";
				final String strFull;
				
				final String strFilename = file.getName();
				
				final int iPos = strFilename.indexOf( "-thumb." );
				final String strBase;
				if ( iPos>0 ) {
					strBase = strFilename.substring( 0, iPos );
				} else {
					final int iPosDot = strFilename.lastIndexOf( '.' );
					strBase = strFilename.substring( 0, iPosDot); 
				}
				
				if ( file.isFile() && 
						strFilename.contains( "-thumb." ) ) {
					if ( file.lastModified() > lCutoff ) {

						strFull = strBase;

						try {
							final Image imgRaw = new Image( 
									UI.display, file.getAbsolutePath() );
//							Tracker.get().add( imgRaw );

							int iRX = imgRaw.getBounds().width;
							int iRY = imgRaw.getBounds().height;
							
							final Rectangle r = new Rectangle( 
										iX, iY, iRX * 165/300, iRY * 165/300 );
							
							gc.drawImage( imgRaw, 0, 0, iRX, iRY, 
									r.x, r.y, r.width, r.height );
							
							imgRaw.dispose();
							
//							mapClickRegions.put( r, strKey + "/" + strFull );
							mapClickRegions.put( strKey + "/" + strFull, r );
							
						} catch ( final SWTException e ) {
							e.printStackTrace();
							final Throwable throwCause = e.getCause();
							if ( null!=throwCause 
									&& FileNotFoundException.class.equals( 
												throwCause.getClass() ) ) {
								gc.drawText( "FileNotFound..", iX, iY );
							} else {
								gc.drawText( "SWT/" + throwCause, iX, iY );
//								e.printStackTrace();
							}
						} catch ( final Exception e ) {
							e.printStackTrace();
							gc.drawText( e.toString(), iX, iY );
						}
					} else {
						strFull = "(outdated)";
					}
				} else {
					strFull = "(" + strBase + ")";
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
				
//				iY = iY + 220;
//				if ( iY+100 > ptDesiredImageSize.y ) {
//					iY = 60;
//					iX = iX + 240;
//				}

				
			}
		}
		gc.dispose();
		
		strMessage = "\n\n\n" + iCount + " captures";
		
		return imgScaled;
	}
	
	
	private void clearMapRegions() {
//		this.mapClickRegions.clear();
		for ( final Entry<String, Rectangle> entry : mapClickRegions.entrySet() ) {
			mapClickRegions.put( entry.getKey(), new Rectangle( 0, 0, 0, 0 ) );
		}
	}
	
	
	private String strMotionFilenameBase = null;
	

	private Image getRawImage() {
		
		if ( DisplayMode.SINGLE_FULL == this.mode ) { 
			if ( null == this.camera ) return null;

			Image imageRemote = null;

			strMotionFilenameBase = null; // this.camera.getTitle();
			
			try {
				final ImageLoader loader = new ImageLoader();
				final URL url = this.camera.getURL();
	//			final URL url = new URL( strURL );
				if ( null != url ) {
					final URLConnection c = url.openConnection();
					c.setReadTimeout( 5000 );
					c.setConnectTimeout( 5000 );
		//			try ( final InputStream is = url.openStream() ) {
					try ( final InputStream is = c.getInputStream() ) {
						final ImageData[] arrData = loader.load( is );
						this.lImageAge = System.currentTimeMillis();
						imageRemote = new Image( UI.display, arrData[0] );
						Tracker.get().add( imageRemote );
					}
				}
			} catch ( final Exception e ) {
	//			e.printStackTrace();
				LOGGER.warning( ()-> String.format( 
						"While reading image from %s, encountered %s", 
						camera,
						e.toString() ) );
				return null;
			}
			return imageRemote;
			
		} else if ( DisplayMode.SINGLE_RECENT_MOTION == this.mode ) {
			
			final JsonNode jn = S2ES.get().retrieveLatestCamMotion();
			
			if ( null == jn ) return this.imageLastMotion;
			
			final JsonNode jnCore = jn.at( "/core" );
			final String strFilenameRaw = jnCore.at( "/file-image" ).asText();
			final String strScanLabel = jnCore.at( "/time-scan-label" ).asText();
			
			
			if ( null == strFilenameRaw ) {
				return this.imageLastMotion;
			}
			
			final boolean bChanged = 
						! strFilenameRaw.equals( strLastMotionFilename );

			PerformanceMonitorTile.getInstance().addEvent( 
					StringUtils.substringAfter( strScanLabel, "_" ) 
					+ ( bChanged ? " <<< New" : "" ) );
			
//			if ( ! bChanged ) {
//				return this.imageLastMotion;
//			}
			
			
			File fileVerified = null;

			fileVerified = new File( strFilenameRaw );
			if ( ! fileVerified.canRead() ) {
				final Set<String> set = S2Path.getLocalAlts( strFilenameRaw );
				boolean bFound = false;
				for ( final String strFile : set ) {
					if ( ! bFound ) {
						fileVerified = new File( strFile );
						if ( fileVerified.canRead() ) {
							bFound = true;
						}
					}
				}
				if ( ! bFound ) {
					return this.imageLastMotion;
				}
			}
			
			strMotionFilenameBase = StringUtils.substringBefore( 
										fileVerified.getName(), "." );
			
//			if ( null != strLastMotionFilename 
//					&& strLastMotionFilename.equals( strFilenameRaw ) 
//					&& null != this.imageLastMotion 
//					&& ! this.imageLastMotion.isDisposed() ) {
//				return this.imageLastMotion;
//			}
			
			this.lImageAge = fileVerified.lastModified();
			if ( null != imageLastMotion && ! imageLastMotion.isDisposed()) {
				imageLastMotion.dispose();
				imageLastMotion = null;
			}
			imageLastMotion = new Image( 
								UI.display, fileVerified.getAbsolutePath() );
			Tracker.get().addAutoDispose( imageLastMotion );
			
			PerformanceMonitorTile.getInstance().addEvent( 
							StringUtils.abbreviateMiddle( 
									strMotionFilenameBase, "~", 12 ) );
//							"Loaded" );
			
			this.strLastMotionFilename = strFilenameRaw;
			return imageLastMotion;
			
		} else {
			return null;
		}
	}
	
	
	private Image prerenderSingleImage( final boolean bFast ) {
		if ( DisplayMode.SINGLE_FULL == this.mode 
				&& null == this.camera ) return null;
		
		PerformanceMonitorTile.getInstance().addEvent( "prerenderSingleImage()" );

//		mapClickRegions.clear();
		clearMapRegions();

		Image imgRaw = null;

//		if ( null!=file ) {
		try {

			imgRaw = getRawImage();

			if ( null != imgRaw && ! imgRaw.isDisposed() ) {

				if ( null != this.camera ) {
					this.strMessage = this.camera.name() 
								+ " - " + this.camera.getTitle();
				} else if ( null != this.strMotionFilenameBase ) {
					this.strMessage = strMotionFilenameBase;
				} else {
					this.strMessage = "(unknown camera)";
				}

				if ( bShowControls ) {
//				if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
//					this.strMessage += "\n"
//							+ "" + file.getParent() + "\n"
//							+ "File size: " + file.length() + "\n";
				}
				
				
				
//				final Image imgRaw = new Image( 
//						UI.display, file.getAbsolutePath() );
//				final Image imgRaw = imageRemote;
				
				final Image imgScaled = new Image( 
									UI.display, 
									ptDesiredImageSize.x, 
									ptDesiredImageSize.y );
				
				//TODO fix the Image management, go back to add()
				Tracker.get().addAutoDispose( imgScaled );
				
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
				
				if ( bShowControls ) {
//				if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
					this.strMessage += "\n"
							+ "Image size: " + iRX + " x " + iRY + "\n";
//					this.strMessage += ""
//							+ "bytesPerLine: " 
//							+ imgRaw.getImageData().bytesPerLine + "\n";
					
//					if ( null!=filesession ) {
//						final String strDescription = 
//								filesession.getDescriptionForImageSource( file );
//						this.strMessage += "\n\n\n\n\n\n\n" 
//								+ strDescription + "\n";
//					}
					
				}
				
				
				gc.drawImage( imgRaw, 0, 0, iRX, iRY, 
						0, 0,
						ptDesiredImageSize.x,
						ptDesiredImageSize.y );


				gc.dispose();
				imgRaw.dispose();
				imgRaw = null;
				
				
//				fileLastImage = file;
//				lLastFileModified = file.lastModified();
				
//				this.lImageAge = file.lastModified();
				this.strLastMessage = this.strMessage;
				
				return imgScaled;
			}
				
		} catch ( final Exception e ) {

			// may run into FileNotFoundException here
			// because image file may update during this method.
			// just skip.
			this.strMessage += "\n"
					+ e.toString();
			e.printStackTrace();
		} finally {
			if ( null != gc && ! gc.isDisposed() ) {
				gc.dispose();
				gc = null;
			}
			if ( null != imgRaw && ! imgRaw.isDisposed() ) {
				imgRaw.dispose();
				imgRaw = null;
			}
		}

		
		this.lImageAge = null;
		return null;
	}
	
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		this.bRequestRefresh = true;

		final Rectangle r = image.getBounds();
		final long lNow = System.currentTimeMillis();

		if ( null==ptDesiredImageSize ) {
			ptDesiredImageSize = new Point( r.width, r.height );
		}
		
		if ( null!=imageStill && ! imageStill.isDisposed() ) {
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
		

		gc.setFont( UI.FONT_SMALL );
		final String strText = ( null != strMessage ) ? strMessage : "(null)";
		{
			int iY = 20;
			for ( final String strLine : strText.split( "\n" ) ) {
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
		
		int iY;
		if ( null != this.lImageAge ) {
			final long lElapsed = lNow - lImageAge.longValue();
			
			final boolean bBig = 
					DisplayMode.SINGLE_RECENT_MOTION.equals( this.mode );
			
			if ( bBig ) {
				gc.setFont( Theme.get().getFont( 22 ) );
			} else {
				gc.setFont( Theme.get().getFont( 14 ) );
			}
			
			final String strLine = DateFormatting.getSmallTime( lElapsed );
			final Point pt = gc.textExtent( strLine );
			final int iX = r.width - 20 - pt.x;
			iY = r.height - 14 - pt.y;
			gc.setForeground( UI.COLOR_BLACK );
			gc.drawText( strLine, iX - 1, iY + 0, true );
			gc.drawText( strLine, iX + 1, iY + 0, true );
			gc.drawText( strLine, iX - 1, iY + 2, true );
			gc.drawText( strLine, iX + 1, iY + 2, true );
			gc.setForeground( UI.COLOR_WHITE );
			gc.drawText( strLine, iX + 0, iY + 1, true );
		} else {
			iY = r.height - 41;
		}
		
//		gc.setFont( UI.FONT_SMALL );
//		gc.drawText( this.mode.name(), 15, iY, true );

		if ( bShowControls ) {
//		if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
			
//			super.addButton( gc, BUTTON_SHOW_ALL, 
//							20, r.height - 180, 120, 40, "Show All" );
//			final S2Button btnShowLocal = 
//					super.addButton( gc, BUTTON_SHOW_LOCAL, 
//							20, r.height - 120, 120, 40, "Show Local" );
//			if ( Boolean.FALSE.equals( this.bHasCameraModule ) ) {
//				btnShowLocal.setState( ButtonState.DISABLED );
//			}

			iY = r.height - 300;

			final S2Button btnLatest = super.addButton( 
					gc, BUTTON_CAM_MOTION, 
					20, iY, 250, 36, "(Latest motion)" );
			iY += 4;
			
			
			for ( final IPCamera camera : IPCamera.values() ) {
				iY += 44;
				
				final String strTitle = 
							camera.getHost() + " - " + camera.getTitle();
				final int iIndex = camera.ordinal();
				
				final S2Button btn = super.addButton( 
						gc, BUTTON_CAM_BASE + iIndex, 
						20, iY, 250, 36, strTitle );
				if ( null != this.camera 
						&& this.camera.equals( camera ) ) {
					btn.setState( ButtonState.DISABLED );
				}
				
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
		
		if ( ! bShowControls ) {
			this.strMessage = null;
		}
		refreshImageData( true );
		
//		switch ( mode ) {
////			case SHOW_ALL: {
////				refreshImageData( true );
////				break;
////			}
//			case SHOW_CONTROLS: {
////				if ( DisplayMode.SHOW_ALL.equals( modeLast ) ) {
//					refreshImageData( true );
////				}
//				break;
//			}
//			case SINGLE_FULL: {
////				if ( DisplayMode.SHOW_ALL.equals( modeLast ) ) {
//					this.strMessage = null;
//					refreshImageData( true );
////					new Thread() {
////						public void run() {
////							try {
////								Thread.sleep( 200 );
////								UI.display.asyncExec( new Runnable() {
////									@Override
////									public void run() {
////										refreshImageData( false );
////									}
////								});
////							} catch ( final InterruptedException e ) {
////								// ignore
////							}
////						};
////					}.start();
////				}
//				break;
//			}
//		}
	}
	
	
	@Override
	protected void activateButton( final S2Button button ) {
		if ( null==button ) return;
		
		button.setState( ButtonState.WORKING );
		this.strLastMessage = null;
		
		final int iButtonIndex = button.getIndex();
		if ( iButtonIndex >= BUTTON_CAM_BASE ) {
			if ( iButtonIndex == BUTTON_CAM_MOTION ) {
				this.camera = null;
				this.setMode( DisplayMode.SINGLE_RECENT_MOTION );
			} else {
				final int iCamSelected = iButtonIndex - BUTTON_CAM_BASE;
				final IPCamera cameraNew = IPCamera.values()[ iCamSelected ];
				this.camera = cameraNew;
				this.setMode( DisplayMode.SINGLE_FULL );
			}
			
			this.removeAllButtons();

		} else {
			switch ( button.getIndex() ) {
	//			case BUTTON_SHOW_ALL: {
	//				setMode( DisplayMode.SHOW_ALL );
	//				this.removeAllButtons();
	//				break;
	//			}
			}
		}
		
		button.setState( ButtonState.READY );
	}
	
	
	@Override
	public boolean clickCanvas( final Point point ) {
		
//		if ( DisplayMode.SHOW_ALL.equals( this.mode ) ) {
			
//			for ( final Entry<Rectangle, String> entry : 
//			for ( final Entry<String, Rectangle> entry : 
//									mapClickRegions.entrySet() ) {
//				final Rectangle r = entry.getValue();
//				if ( r.contains( point ) ) {
//					this.ptTouch = null;
//					this.strLocationMatch = entry.getKey();
//					this.setMode( DisplayMode.SINGLE_FULL );
//					return true;
//				}
//			}
//			
//			this.ptTouch = point;
//			return false;
			
//		} else
		if ( bShowControls ) {
//		if ( DisplayMode.SHOW_CONTROLS.equals( this.mode ) ) {
			this.ptTouch = null;
			this.removeAllButtons();
			bShowControls = false;
//			this.setMode( DisplayMode.SINGLE_FULL );
		} else {
			this.ptTouch = null;
//			this.setMode( DisplayMode.SHOW_CONTROLS );
			bShowControls = true;
		}
		return true;
	}
	

}
