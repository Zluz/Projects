package jmr.pr125;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class ProcessCleanup {

	final File fileTEMP;
	
	public ProcessCleanup() {
		String strTempDir = System.getenv( "TEMP" );
		if ( StringUtils.isBlank( strTempDir ) ) {
			strTempDir = System.getenv( "TMP" );
		}
		fileTEMP = new File( strTempDir );
	}
	
	public long markForDeletion() {
		
		final long lNow = System.currentTimeMillis();
		final long lCutoff = lNow - TimeUnit.DAYS.toMillis( 2 );
		
		long lCount = 0;
		
		if ( ! fileTEMP.exists() ) return lCount;
		
		final File[] arrFiles = fileTEMP.listFiles();
		for ( final File file : arrFiles ) {
			
			boolean bDelete = file.isFile();
			bDelete = bDelete && file.lastModified() < lCutoff;
			
			if ( bDelete ) {
				final String strName = file.getName();
				
				if ( strName.contains( "webcam-lock-" ) ) {
					file.deleteOnExit();
					lCount++;
				} else if ( strName.contains( "jar_cache" ) ) {
					file.deleteOnExit();
					lCount++;
				}
			}
		}
		return lCount;
	}
	
}
