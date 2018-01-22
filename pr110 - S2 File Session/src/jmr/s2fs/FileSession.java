package jmr.s2fs;

import java.io.File;
import java.util.EnumMap;

import jmr.util.FileUtil;

public class FileSession {

	public enum SessionFile {
		DEVICE_INFO( true, "conky-device_info.txt" ),
		SYSTEM_INFO( true, "uname.out" ),
		IFCONFIG( true, "ifconfig.out" ),
		SCREENSHOT( false, "screenshot.png" ),
		;
		
		public final String strFilename;
		public final boolean bText;
		
		SessionFile( final boolean bText, final String strFilename ) {
			this.bText = bText;
			this.strFilename = strFilename;
		}
	}
	
	private final File dir; 
	
	private final EnumMap<SessionFile,String> 
			mapFileContents = new EnumMap<>( SessionFile.class );
	
	private File fileImage;
	
	
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
	
	public File getScreenshotImageFile() {
		if ( null==this.fileImage ) {
			this.fileImage = getFile( SessionFile.SCREENSHOT );
		}
		return this.fileImage;
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
	
	
}
