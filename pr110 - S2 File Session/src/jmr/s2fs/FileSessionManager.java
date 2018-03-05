package jmr.s2fs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jmr.S2FSUtil;

public class FileSessionManager {

	public final static String BASE_SHARE_PATH = 
						( '/'==File.separatorChar
								? "/Share"
								: "S:\\" );
	
	public final static String BASE_SESSION_PATH = 
			BASE_SHARE_PATH + File.separatorChar + "Sessions";
	
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

	private boolean scan() {
		final File file = new File(BASE_SESSION_PATH);
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
