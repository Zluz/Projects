package jmr.rpclient.tiles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.tiles.Theme.Colors;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;
import jmr.s2fs.S2FSUtil;

public class SessionListTile extends TileBase {


	public final static int SCREENSHOT_WIDTH = 100;

//	final static Map<String,Long> map = new HashMap<>();

	final static Map<String,Map<String,String>> map2 = new HashMap<>();
	final static Map<String,Image> mapScreenshots = new HashMap<>();
	boolean bScreenshotsUpdating = false;

	private Image imgNoImage;

	private Thread threadUpdater;

	public SessionListTile() {
		threadUpdater = new Thread( "NetworkList Updater" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
		
					for (;;) {
						synchronized ( map2 ) {
							try {
								updateMap();
//							} catch ( final SQLException e ) {
//								// ignore.. 
//								// JDBC connection may have been dropped..
							} catch ( final Exception e ) {
								e.printStackTrace();
							}
						}
		
						Thread.sleep( TimeUnit.SECONDS.toMillis( 10 ) );
					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
//		threadUpdater.start();
		
		final Thread threadScreenshotRefresh = 
						new Thread( "Screenshot Refresh" ) {
			@Override
			public void run() {
				bScreenshotsUpdating = true;
				int iCount = 0;
				boolean bLongWait = false;
				try {
					for (;;) {
						iCount++;
						if ( null!=mapScreenshots ) {
							bLongWait = invalidateScreenshot( iCount );
						}
						Thread.sleep( 5000 );
						if ( bLongWait ) {
							Thread.sleep( 10000 );
						}
					}
				} catch ( final InterruptedException e ) {
					// interrupted? probably quitting..
				}
				bScreenshotsUpdating = false;
			}
		};
		threadScreenshotRefresh.start();
	}
	
	private boolean invalidateScreenshot( final int iCount ) {
		final Image imgRemove;
		final int iIndex;
		synchronized ( mapScreenshots ) {
			if ( mapScreenshots.size() > 0 ) {
				iIndex = iCount % mapScreenshots.size();
				final Object[] array = mapScreenshots.keySet().toArray();
				final String strKey = array[ iIndex ].toString();
				imgRemove = mapScreenshots.get( strKey );
				mapScreenshots.put( strKey, null );
			} else {
				imgRemove = null;
				iIndex = 0;
			}
		}
		if ( null!=imgRemove ) {
			imgRemove.dispose();
		}
		final boolean bLongWait = 0==iIndex;
		return bLongWait;
	}
	
	
	private Image getImageNotFound( final Device device ) {
		if ( null==this.imgNoImage ) {
			this.imgNoImage = new Image( device, 1,1 );
		}
		return this.imgNoImage;
	}


	private void updateMap() {

		final String strPath = "/Sessions/";

		final Map<String, Long> mapSessions = 
						new Path().getChildPages( strPath, true );
		
//		map.clear();
//		map.putAll( mapSessions );

		final Page page = new Page();
		
		map2.clear();
		
		for ( final Entry<String, Long> entry : mapSessions.entrySet() ) {

			final String strSession = entry.getKey();
			final long lPageSeq = entry.getValue();
			
			final Map<String, String> map = 
//						Client.get().loadPage( strPath );
						page.getMap( lPageSeq );
			
			final String strMAC = map.get( "device.mac" );
			final String strNorm = S2FSUtil.normalizeMAC( strMAC );
			
			map2.put( strNorm, map );
		}
		
		final FileSessionManager fsm = FileSessionManager.getInstance();
		
		for ( final String strKey : fsm.getSessionKeys() ) {
			final FileSession session = fsm.getFileSession( strKey );
			
			if ( null!=session ) {

				final Map<String,String> map;
				
				if ( map2.containsKey( strKey ) ) {
					map = map2.get( strKey );
				} else {
					map = new HashMap<>();
					map2.put( strKey, map );
				}
				map.put( "uname", session.getAllSystemInfo() );
				map.put( "conky", session.getDeviceInfo() );
				map.put( "ifconfig", session.getNetworkInterfaceInfo() );
			}
		}
	}
	
	
	public Image getScreenshot( final String strMAC,
								final Device display,
								final int iY,
								final List<String> listScaled ) {
		final Image image;
		synchronized ( mapScreenshots ) {
			
			// check if never added or if null'ed out (to refresh)
			final Image imgValue = mapScreenshots.get( strMAC );
			
			if ( null==imgValue ) {
				final FileSessionManager fsm = FileSessionManager.getInstance();
				final FileSession session = fsm.getFileSession( strMAC );
				if ( null!=session ) {
					final File file = session.getScreenshotImageFile();
					if ( null!=file && file.isFile() ) {
						
						Image imgCapture = null;
						try {
							imgCapture = new Image( 
									display, file.getAbsolutePath() );
						} catch ( final SWTException e ) {
							// potential: Unsupported or unrecognized format
						}
						if ( null!=imgCapture ) {
							final Image imgScaled = 
									new Image( display, SCREENSHOT_WIDTH, iY );
							
							final GC gc = new GC( imgScaled );
							if ( listScaled.isEmpty() ) {
								gc.setAntialias(SWT.ON);
								gc.setInterpolation(SWT.HIGH);
								listScaled.add( strMAC );
							}
							
							gc.drawImage(imgCapture, 0, 0,
									imgCapture.getBounds().width, 
									imgCapture.getBounds().height,
									0, 0, SCREENSHOT_WIDTH, iY );
							
							gc.dispose();
							imgCapture.dispose();
							
							image = imgScaled;
						} else {
							image = getImageNotFound( display );
						}
					} else {
						image = getImageNotFound( display );
					}
				} else {
					image = getImageNotFound( display );
				}
				mapScreenshots.put( strMAC, image );
			} else {
				image = mapScreenshots.get( strMAC );
			}
			return image;
		}
	}
	

	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
		final GCTextUtils util = new GCTextUtils( gc );
		
//		gc.setAntialias( SWT.ON );
//		gc.setInterpolation( SWT.HIGH );
		
//		final int iX = SCREENSHOT_WIDTH;
		
		final int iY_screenshot_bump = 10;
		
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		
		final List<String> listScaled = new ArrayList<String>( 1 );
		
		synchronized ( map2 ) {

			final int iTotal = map2.size();
			final Rectangle bounds = image.getBounds();
			final double dRowHeight = (double)bounds.height / iTotal;
			final int iRowHeight = (int)dRowHeight;
			
			int iCount = 0;
			boolean bLeft = true;
			
			for ( final Entry<String, Map<String, String>> 
											entry : map2.entrySet() ) {
				final String strKey = entry.getKey();
				final Map<String,String> map = entry.getValue();
				
				util.setRightAligned( !bLeft );
				
				final int iY = (int)( dRowHeight * iCount );

				final int iX_screenshot = 
						bLeft ? 0 : bounds.width - SCREENSHOT_WIDTH;
				final int iY_screenshot = 
						bLeft ? iY : iY - iY_screenshot_bump;

				final Rectangle rect;
				if ( bLeft ) {
					rect = new Rectangle( 
							SCREENSHOT_WIDTH + 4, iY + 4, 
							bounds.width - SCREENSHOT_WIDTH - 6, 
							bounds.height );
				} else {
					rect = new Rectangle( 
							4, iY + iY_screenshot_bump, 
							bounds.width - SCREENSHOT_WIDTH - 6, 
							bounds.height );
				}

				boolean bImageDrawn = false;
				synchronized ( mapScreenshots ) {
					final Image imgScreenshot = 
							getScreenshot( strKey, gc.getDevice(), 
												iRowHeight, listScaled );
					if ( null!=imgScreenshot && !imgScreenshot.isDisposed() ) {
						final ImageData data = imgScreenshot.getImageData();
						if ( data.height > 1 ) {
							gc.drawImage( imgScreenshot, 
								0,0,  data.width, data.height,
								iX_screenshot, 
										iY_screenshot,
								SCREENSHOT_WIDTH, 
										iRowHeight + iY_screenshot_bump );
							bImageDrawn = true;
						}
					}
				}
				if ( !bImageDrawn ) {
					gc.setForeground( Theme.get().getColor( Colors.LINE_FAINT ) );
					gc.drawRectangle( new Rectangle( 
							iX_screenshot, iY_screenshot, 
							SCREENSHOT_WIDTH, iRowHeight + iY_screenshot_bump ) );
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				}
				
				gc.setFont( Theme.get().getFont( 12 ) );
				final String strIP = getIP( map );
				util.drawTextJustified( strIP, rect );
				rect.y = rect.y + 16;
				
				gc.setFont( Theme.get().getFont( 9 ) );
				final String strName = getDescription( map );
				util.drawTextJustified( strName, rect );
				rect.y = rect.y + 16;


				iCount++;
				bLeft = !bLeft;
			}
		}

//		drawTextCentered( strText, 10 );
	}
	

	public static String getDescription( final Map<String,String> map ) {
		if ( null==map ) return "<null>";
		
		final String strDeviceName = map.get( "device.name" );
		if ( null!=strDeviceName ) return strDeviceName;
		
		final String str_ifconfig = map.get( "conky" );
		if ( null!=str_ifconfig && !str_ifconfig.isEmpty() ) {
			return str_ifconfig;
		}
		return "<unknown>";
	}

	public static String getIP( final Map<String,String> map ) {
		if ( null==map ) return "<null>";
		
		final String strDeviceIP = map.get( "device.ip" );
		if ( null!=strDeviceIP ) return strDeviceIP;
		
		final String str_ifconfig = map.get( "ifconfig" );
		if ( null!=str_ifconfig ) {
			final String[] strs = str_ifconfig.split( "\n" );
			for ( final String str : strs ) {
				if ( str.contains( "inet addr:192.168" ) ) {
					int iStart = str.indexOf( "addr:192.168" ) + 5;
					int iEnd = str.indexOf( " ", iStart );
					final String strsub = str.substring( iStart, iEnd );
					return strsub;
				}
				if ( str.contains( "inet 192.168" ) ) {
					int iStart = str.indexOf( "inet 192.168" ) + 5;
					int iEnd = str.indexOf( " ", iStart );
					final String strsub = str.substring( iStart, iEnd );
					return strsub;
				}
			}
		}
		return "<unknown>";
	}
	
	
	public static String checkNull( final String text ) {
		if ( null==text ) {
			return "<null>";
		} else {
			return text;
		}
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
