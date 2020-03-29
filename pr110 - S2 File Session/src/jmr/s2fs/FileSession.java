package jmr.s2fs;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

import jmr.util.FileUtil;
import jmr.util.transform.JsonUtils;

public class FileSession {

	public enum SessionFile {
		DEVICE_INFO( 	  true,  true,  "conky-device_info.txt" ),
		DEVICE_CONFIG( 	  true,  false, "device_config.json" ),
		DEVICE_REPORT( 	  true,  false, "device_report.json" ),
		SYSTEM_INFO( 	  true,  true,  "uname.out" ),
		IFCONFIG( 		  true,  false, "ifconfig.out" ),
		SCREENSHOT_FULL(  false, false, "screenshot.png" ),
		SCREENSHOT_THUMB( false, false, "screenshot-thumb.png" ),
		CAPTURE_STILL( 	  false, false, "capture_still_now.jpg" ),
		CAPTURE_LIST( 	  true,  false, "capture_list.txt" ),
		;
		
		public final String strFilename;
		public final boolean bText;
		public final boolean bCache;
		
		SessionFile( final boolean bText, 
					 final boolean bCache, 
					 final String strFilename ) {
			this.bText = bText;
			this.bCache = bCache;
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
		/**
		 * Files that existed previously in this instance.
		 * This may happen if the image is in the process of being updated.
		 * In this case a new File will be created based on the same name.
		 */
		INCLUDE_MISSING, 
		;
	}
	

	
	private final File dir; 
	
	private final EnumMap<SessionFile,String> 
			mapFileContents = new EnumMap<>( SessionFile.class );

	private final Set<String> setPastFiles = new HashSet<>();
	
	
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
			
			if ( key.bCache ) {
				mapFileContents.put( key, strContents );
			} else {
				return strContents;
			}
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

	
	public static boolean isThumbnail( final String strFilename ) {
		if ( null==strFilename ) return false;
		return ( strFilename.contains( "thumb." ) );
	}
	
	
	public File getLatestCaptureStillImageFile( final String strRegex ) {
		final File[] files = this.dir.listFiles( new FileFilter() {
			@Override
			public boolean accept( final File file ) {
				final String strFilename = file.getName();
				if ( file.isFile() ) {
					if ( strFilename.matches( strRegex ) ) {
						return true;
					}
				}
				return false;
			}
		});
		long lLatest = Long.MIN_VALUE;
		File fileLatest = null;
		if ( null!=files ) {
			for ( final File file : files ) {
				final long lLatestThisFile = file.lastModified();
				if ( lLatestThisFile > lLatest ) {
					fileLatest = file;
					lLatest = lLatestThisFile;
				}
			}
		}
		return fileLatest;
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
			
//			// ex:  capture_vid0-t1565756724066.jpg
//			final File fileVid0 = getLatestCaptureStillImageFile( 
//										"capture_vid[0-9]\\-t[0-9]+\\.jpg" );
//			if ( null != fileVid0 ) {
//				list.add( fileVid0 );
//			}
			
			// ex:  capture_cam-t1565756231237.jpg
			final File fileCam = getLatestCaptureStillImageFile( 
										"capture_cam\\-t[0-9]+\\.jpg" );
			if ( null != fileCam ) {
				list.add( fileCam );
			}
		}
		
		if ( bThumb ) {
			
			final File fileCamThumb = new File( this.dir, "capture_cam-thumb.jpg" );
			if ( fileCamThumb.exists() && fileCamThumb.lastModified() > lRecent ) {
				list.add( fileCamThumb );
			}

//			// just give the full version for USB cameras for now
//			// ex:  capture_vid0-t1565756724066.jpg
//			final File fileVid0 = getLatestCaptureStillImageFile( 
//										"capture_vid[0-9]\\-t[0-9]+\\.jpg" );
//			if ( null != fileVid0 ) {
//				list.add( fileVid0 );
//			}
			
			// ex:  capture_cam-t1565756319807-thumb.jpg
			final File fileCam = getLatestCaptureStillImageFile( 
										"capture_cam\\-t[0-9]+\\-thumb\\.jpg" );
			if ( null != fileCam ) {
				list.add( fileCam );
			}			
		}
		
//		if ( this.dir.getAbsolutePath().contains( "94-C6-91-18-C8-33" ) ) {
//			System.out.println( "Checking 94-C6-91-18-C8-33.." );
//		}
		
		final File[] files = this.dir.listFiles( new FilenameFilter() {
			@Override
			public boolean accept(	final File dir, 
									final String strName ) {
				final String strTest = strName.toLowerCase();

				
				// for now, accept any as thumbnails
				if ( strTest.matches( "capture_vid[0-9]+\\.jpg" ) ) return true;

				
				
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
		
		// ex:  capture_vid0-t1565756724066.jpg
		final File fileVid0 = getLatestCaptureStillImageFile( 
									"capture_vid[0-9]\\-t[0-9]+\\.jpg" );
		if ( null != fileVid0 ) {
			list.add( fileVid0 );
		}
		
//		if ( !setPastFiles.isEmpty() ) {
//			System.out.println( "File list:" );
//		}
		
		// add everything to setPastFiles
		for ( final File file : list ) {
			final String strFile = file.getName();
//			System.out.println( "\t" + strFile );
			setPastFiles.add( strFile );
		}

//		System.out.println( "Added (missing):" );

		// if INCLUDE_MISSING then create the missing files (from setPastFiles)
		if ( setOptions.contains( ImageLookupOptions.INCLUDE_MISSING ) ) {
			for ( final String strSetFile : setPastFiles ) {
				boolean bFound = false;
				for ( final File file : list ) {
					final String strListFile = file.getName();
					if ( strSetFile.equals( strListFile ) ) {
						bFound = true;
					}
				}
				if ( !bFound ) {
					if ( testAddFile( strSetFile, setOptions ) ) {
//						System.out.println( "\tadding: " + strSetFile );
						final File fileMissing = new File( strSetFile );
						list.add( fileMissing );
					}
				}
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

	
	private boolean testAddFile(	
						final String strFilename,
						final Set<ImageLookupOptions> setOptions  ) {
		if ( null==strFilename ) return false;
		if ( strFilename.isEmpty() ) return false;

		final boolean bThumb = isThumbnail( strFilename );
		
		if ( setOptions.contains( ImageLookupOptions.ONLY_THUMB ) ) {
			if ( bThumb ) {
				return true;
			}
		} else if ( setOptions.contains( ImageLookupOptions.ONLY_FULL ) ) {
			if ( !bThumb ) {
				return true;
			}
		} else {
			return true;
		}

		return false;
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
	
	
	public Map<String,String> getIPs() {
		String strInterface = null;
		final Map<String,String> map = new HashMap<>();
		for ( final String line : getNetworkInterfaceInfo().split( "\n" ) ) {
			if ( 0==line.length() ) {
				strInterface = null;
			} else if ( Character.isAlphabetic( line.charAt( 0 ) ) ) {
				final int iPosColon = line.indexOf( ":" );
				final int iPosSpace = line.indexOf( " " );
				final int iPos = Math.min( iPosColon, iPosSpace );
				if ( iPos > 0 ) {
					strInterface = line.substring( 0, iPos ).trim();
				}
			}
			if ( line.contains( "192.168." ) ) {
				String strIP = line.substring( 
						line.indexOf( "192.168." ), line.length() );
				strIP = strIP.substring( 0,13 ).trim();
				map.put( strInterface, strIP );
			}
		}
		return map;
	}
	
	
	public Long getUpdateTime() {
		final File file = this.getFile( SessionFile.IFCONFIG );
		if ( null==file ) {
			return null;
		} else if ( file.isFile() ) {
			final long lModified = file.lastModified();
			return lModified;
		} else {
			return null;
		}
	}
	
	
	public JsonObject getDeviceConfig() {
		final String strJsonConfig = getFileContents( SessionFile.DEVICE_CONFIG );
		if ( StringUtils.isNotEmpty( strJsonConfig ) ) {
			final JsonObject json = JsonUtils.getJsonObjectFor( strJsonConfig );
			return json;
		} else {
			return new JsonObject();
		}
	}

	public JsonObject getDeviceReport() {
		final String strJsonConfig = getFileContents( SessionFile.DEVICE_REPORT );
		if ( StringUtils.isNotEmpty( strJsonConfig ) ) {
			final JsonObject json = JsonUtils.getJsonObjectFor( strJsonConfig );
			return json;
		} else {
			return new JsonObject();
		}
	}
	
}
