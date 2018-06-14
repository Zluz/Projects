package jmr.s2fs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jmr.util.FileUtil;

public class FileSession {

	public enum SessionFile {
		DEVICE_INFO( true, "conky-device_info.txt" ),
		SYSTEM_INFO( true, "uname.out" ),
		IFCONFIG( true, "ifconfig.out" ),
		SCREENSHOT_FULL( false, "screenshot.png" ),
		SCREENSHOT_THUMB( false, "screenshot-thumb.png" ),
		CAPTURE_STILL( false, "capture_still_now.jpg" ),
		CAPTURE_LIST( true, "capture_list.txt" ),
		;
		
		public final String strFilename;
		public final boolean bText;
		
		SessionFile( final boolean bText, final String strFilename ) {
			this.bText = bText;
			this.strFilename = strFilename;
		}
	}

	
	/**
	 * Image search criteria. Currently only applies to still captures.
	 */
	public enum ImageLookupOptions {
		ALL,
		SINCE_PAST_DAY,
		SINCE_PAST_HOUR,
		ONLY_FULL,
		ONLY_THUMB,
		;
	}
	

	
	private final File dir; 
	
	private final EnumMap<SessionFile,String> 
			mapFileContents = new EnumMap<>( SessionFile.class );
	
	private File fileImage;
	private File fileThumb;
	private File fileCaptureStill;
	
	
	public FileSession( final File dir ) {
		this.dir = dir;
	}

	public File getFile( final SessionFile key ) {
		if ( null==key ) return null;
		final File file = new File( this.dir, key.strFilename );
		if ( file.isFile() ) {
			return file;
		} else {
			return null;
		}
	}

	
	//TODO invalidate old data?
	public String getFileContents( final SessionFile key ) {
		if ( !mapFileContents.containsKey( key ) ) {
			final String strContents;
			if ( key.bText ) {
				final File file = getFile( key );
				if ( null!=file && file.exists() ) {
					strContents = FileUtil.readFromFile( file );
				} else {
					strContents = "";
				}
			} else {
				strContents = "";
			}
			mapFileContents.put( key, strContents );
		}
		
		return mapFileContents.get( key );
	}
	
	/**
	 * Two screenshot images:
	 *   first the full size screenshot,
	 *   second the thumbnail of the same image.
	 * @return
	 */
	public File[] getScreenshotImageFiles() {
		final File[] list = new File[] { null, null };
		
		if ( null==this.fileImage || !this.fileImage.exists() ) {
			this.fileImage = getFile( SessionFile.SCREENSHOT_FULL );
		}
		list[0] = this.fileImage;
		
		if ( null==this.fileThumb || !this.fileThumb.exists() ) {
			this.fileThumb = getFile( SessionFile.SCREENSHOT_THUMB );
		}
		list[1] = this.fileThumb;

		return list;
	}

	public File getCaptureStillImageFile() {
		if ( null==this.fileCaptureStill || !this.fileCaptureStill.exists() ) {
			this.fileCaptureStill = getFile( SessionFile.CAPTURE_STILL );
		}
		return this.fileCaptureStill;
	}

	
	public List<File> getCaptureStillImageFiles( 
								final ImageLookupOptions... options ) {
		final Set<ImageLookupOptions> setOptions = new HashSet<>();
		setOptions.addAll( Arrays.asList( options ) );
		
		final List<File> list = new LinkedList<>();
		
		final long lRecent;
		if ( setOptions.contains( ImageLookupOptions.SINCE_PAST_HOUR ) ) {
			lRecent = System.currentTimeMillis() - TimeUnit.HOURS.toMillis( 1 );
		} else if ( setOptions.contains( ImageLookupOptions.SINCE_PAST_DAY ) ) {
			lRecent = System.currentTimeMillis() - TimeUnit.DAYS.toMillis( 1 );
		} else {
			lRecent = 0;
		}
		
		final boolean bFull;
		final boolean bThumb;
		if ( setOptions.contains( ImageLookupOptions.ONLY_FULL ) ) {
			bFull = true;
			bThumb = false;
		} else if ( setOptions.contains( ImageLookupOptions.ONLY_THUMB ) ) {
			bFull = false;
			bThumb = true;
		} else {
			bFull = true;
			bThumb = true;
		}
		
		if ( bFull ) {
			final File fileCamFull = new File( this.dir, "capture_cam.jpg" );
			if ( fileCamFull.exists() && fileCamFull.lastModified() > lRecent ) {
				list.add( fileCamFull );
			}
		}
		
		if ( bThumb ) {
			final File fileCamThumb = new File( this.dir, "capture_cam-thumb.jpg" );
			if ( fileCamThumb.exists() && fileCamThumb.lastModified() > lRecent ) {
				list.add( fileCamThumb );
			}
		}
		
		final File[] files = this.dir.listFiles( new FilenameFilter() {
			@Override
			public boolean accept(	final File dir, 
									final String strName ) {
				final String strTest = strName.toLowerCase();
				if ( bThumb 
						&& strTest.matches( "capture_vid[0-9]+\\-thumb\\.jpg" ) ) {
					return true;
				} else if ( bFull 
						&& strTest.matches( "capture_vid[0-9]+\\.jpg" ) ) {
					return true;
				} else {
					return false;
				}
			}
		});

		for ( final File file : files ) {
			if ( file.lastModified() > lRecent ) {
				list.add( file );
			}
		}
		
//		final Comparator<File> comparator = new Comparator<File>() {
//			@Override
//			public int compare(	final File lhs, 
//								final File rhs ) {
//				return lhs.getName().compareTo( rhs.getName() );
//			}
//		};
		final Comparator<File> comparator = 
					(lhs, rhs) -> lhs.getName().compareTo( rhs.getName() );
		Collections.sort( list, comparator );
		
		return list;
	}

	
	/**
	 * Given a capture image file, collect and return information about
	 * the source of the image. For instance, a description of the camera
	 * device used.
	 * 
	 * @param file
	 * @return
	 */
	public String getDescriptionForImageSource( final File file ) {
		if ( null==file ) return null;
		
		final String strFilename = file.getName().toLowerCase();
		if ( strFilename.equals( "capture_cam.jpg" ) ) {
			return "RPi camera module";
		}

		if ( strFilename.startsWith( "capture_vid" ) 
				&& strFilename.endsWith( ".jpg" ) ) {
			
			final String strInfo = getFileContents( SessionFile.CAPTURE_LIST );

			final String strNumber = strFilename
					.substring( 0, strFilename.length() - 4 )
					.substring( "capture_vid".length() );
			final String strDevice = "/video" + strNumber;
			
			for ( final String line : strInfo.split( "\n" ) ) {
				if ( line.endsWith( strDevice ) ) {
/*					looks like:
lrwxrwxrwx 1 root root 12 Jun  1 20:55 usb-Generic_USB2.0_PC_CAMERA-video-index0 -> ../../video0
*/
					String edit = line.substring( line.indexOf( " usb-" ) + 5 );
					edit = edit.substring( 0, edit.indexOf( "-video-" ) );
					return edit;
				}
			}
		}
		
		return null;
	}
	
	
	public String getAllSystemInfo() {
		return getFileContents( SessionFile.SYSTEM_INFO );
	}

	public String getDeviceInfo() {
		return getFileContents( SessionFile.DEVICE_INFO );
	}
	
	public String getNetworkInterfaceInfo() {
		return getFileContents( SessionFile.IFCONFIG );
	}
	
	public String getIP() {
		for ( final String line : getNetworkInterfaceInfo().split( "\n" ) ) {
			if ( line.contains( "192.168." ) ) {
				String strIP = line.substring( 
						line.indexOf( "192.168." ), line.length() );
				strIP = strIP.substring( 0,13 ).trim();
				return strIP;
			}
		}
		return "(unknown)";
	}
	
	
}
