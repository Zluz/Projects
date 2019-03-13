package jmr.pr125;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class ProcessCleanup {


	private static final Logger 
					LOGGER = Logger.getLogger( ProcessCleanup.class.getName() );

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
		final long lCutoffFar = lNow - TimeUnit.DAYS.toMillis( 1 );
		final long lCutoffClose = lNow - TimeUnit.MINUTES.toMillis( 2 );
		
		long lCount = 0;
		
		if ( ! fileTEMP.exists() ) return lCount;
		
		final File[] arrFiles = fileTEMP.listFiles();
		for ( final File file : arrFiles ) {

			if ( file.lastModified() > lCutoffClose ) continue;

			try {
				
				final String strName = file.getName();
				
				if ( strName.contains( "webcam-lock-" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				}

				if ( strName.startsWith( "pr125_" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				}

				if ( file.lastModified() > lCutoffFar ) continue;
				
				if ( strName.contains( "jar_cache" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				}
				
				if ( strName.startsWith( "BridJExtractedLibraries" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				}
				
			} catch ( final Exception e ) {
				LOGGER.warning( 
						()-> "Exception during cleanup: " + e.toString() );
			}
		}
		return lCount;
	}
	
}
