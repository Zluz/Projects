package jmr.s2fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jmr.S2FSUtil;

public class FileSessionManager {

//	public final static String BASE_SHARE_PATH = 
//						( '/'==File.separatorChar
//								? "/Share"
//								: "S:\\" );
//	
//	public final static String BASE_SESSION_PATH = 
//			BASE_SHARE_PATH + File.separatorChar + "Sessions";
	
	private final static String[] BASE_SESSION_PATHS =
					{	"/Share/Sessions",
						"S:\\Sessions\\",
						"H:\\Share\\Sessions\\"
					};
	private static File fileBaseSessionPath = null;
	
	private final Map<String,FileSession> MAP = new HashMap<>();
	
	private static FileSessionManager instance;
	
	private FileSessionManager() {
		scan();
	}
	
	public static synchronized FileSessionManager getInstance() {
		if ( null==instance ) {
			instance = new FileSessionManager();
		}
		return instance;
	}

	
	// NOTE: much of this is duplicated in pr104:SystemUtil .. 
	// move both of these elsewhere
	private static Properties properties = null;

	public static Properties getProperties() {
		if ( null==properties ) {
			properties = new Properties();
			try {
				final File fileSessionPath = getInstance().getBaseSessionPath();
				if ( null==fileSessionPath ) {
					return null;
				}
				final File fileBasePath = fileSessionPath.getParentFile();
				final File file = new File( fileBasePath, "settings.ini" ); 
				properties.load( new FileInputStream( file ) );
			} catch ( final IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return properties;
	}

	
	
	
	public synchronized File getBaseSessionPath() {
		if ( null!=fileBaseSessionPath ) {
			return fileBaseSessionPath;
		} else {
			for ( final String strPath : BASE_SESSION_PATHS ) {
				final File file = new File( strPath );
				if ( file.isDirectory() ) {
					fileBaseSessionPath = file;
					return file;
				}
			}
		}
		System.err.println( "Could not locate base share path. "
							+ "Please ensure Share is mapped." );
		return null;
	}
	
//	public File getBaseSessionPath() {
//		final File fileBaseShare = getBaseSharePath();
//		final File fileBaseSession = new File( fileBaseShare, "Sessions" );
//		return fileBaseSession;
//	}
	
	private boolean scan() {
		final File file = getBaseSessionPath();
//		System.out.println( "Scanning for sessions "
//				+ "(under " + BASE_SESSION_PATH + ")" );
		String[] arrDirs = file.list(new FilenameFilter() {
			@Override
			public boolean accept( final File current, final String name ) {
				final File dir = new File(current, name);
				final boolean bIsDir = dir.isDirectory();
				if ( !bIsDir ) return false;
				
				final String strNorm = S2FSUtil.normalizeMAC( name );
				final boolean bIsMAC = name.equals( strNorm );
				
				if ( bIsMAC ) {
					final FileSession session = new FileSession( dir );
					MAP.put( strNorm, session );
//					System.out.println( "\t" + strNorm );
				}
				
				return bIsMAC;
			}
		});
//		System.out.println(Arrays.toString(arrDirs));
		return ( null!=arrDirs && arrDirs.length > 0 );
	}

	public FileSession getFileSession( final String strMAC ) {
		final String strNorm = S2FSUtil.normalizeMAC( strMAC );
		if ( MAP.containsKey( strNorm ) ) {
			return MAP.get( strNorm );
		}
		return null;
	}

	public Set<String> getSessionKeys() {
		return MAP.keySet();
	}
	
	public Map<String,FileSession> getSessionMap() {
		return Collections.unmodifiableMap( this.MAP );
	}
	

	public static void main( final String[] args ) {
		FileSessionManager.getInstance().scan();
	}

}
