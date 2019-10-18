package jmr.s2fs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jmr.S2FSUtil;
import jmr.SessionPath;

public class FileSessionManager {


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
		final List<File> files = SessionPath.getPaths();
//		System.out.println( "Scanning for sessions "
//				+ "(under " + BASE_SESSION_PATH + ")" );
		
		int iCount = 0;
		for ( final File file : files ) {
			String[] arrDirs = file.list(new FilenameFilter() {
				@Override
				public boolean accept( final File current, final String name ) {
					final File dir = new File(current, name);
					final boolean bIsDir = dir.isDirectory();
					if ( !bIsDir ) return false;
					
					final String strNorm = S2FSUtil.normalizeMAC( name );
					final boolean bIsMAC = name.equals( strNorm );
					
					if ( bIsMAC ) {

						final FileSession sessionNew = new FileSession( dir );

						if ( MAP.containsKey( strNorm ) ) {
							final FileSession sessionExist = MAP.get( strNorm );
							final Long lTimeExist = sessionExist.getUpdateTime();
							
							final Long lTimeNew = sessionNew.getUpdateTime();
							
							final long lNormExist = null!=lTimeExist 
											? lTimeExist.longValue() : 0;
							final long lNormNew = null!=lTimeNew 
											? lTimeNew.longValue() : 0;
											
							if ( lNormNew > lNormExist ) {
								MAP.put( strNorm, sessionNew );
							} else {
								// keep the existing 
							}
						} else {
							MAP.put( strNorm, sessionNew );
	//						System.out.println( "\t" + strNorm );
						}
					}
					
					return bIsMAC;
				}
			});
			if ( null!=arrDirs ) {
				iCount = iCount + arrDirs.length;
			}
		}
//		System.out.println(Arrays.toString(arrDirs));
		return ( iCount > 0 );
	}

	public FileSession getFileSession( final String strMAC ) {
		final String strNorm = S2FSUtil.normalizeMAC( strMAC );
		if ( MAP.containsKey( strNorm ) ) {
			return MAP.get( strNorm );
		} else if ( null==strNorm ) {
			return null;
		} else {
			this.scan();
			if ( MAP.containsKey( strNorm ) ) {
				return MAP.get( strNorm );
			} else {
				return null;
			}
		}
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
