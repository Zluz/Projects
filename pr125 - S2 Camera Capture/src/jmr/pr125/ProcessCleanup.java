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
		
		long lCount = 0;
		
		if ( ! fileTEMP.exists() ) return lCount;
		
		final File[] arrFiles = fileTEMP.listFiles();
		for ( final File file : arrFiles ) {
			
			final long lModTime = file.lastModified();

			// continue if newer than 2 minutes
			if ( lNow - TimeUnit.MINUTES.toMillis( 2 ) < lModTime ) continue;

			try {
				final String strName = file.getName();
				
				if ( strName.contains( "webcam-lock-" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				}

				// continue if newer than 2 hours
				if ( lNow - TimeUnit.HOURS.toMillis( 2 ) < lModTime ) continue;

				if ( strName.startsWith( "pr125_" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				} else if ( strName.startsWith( "pr124_" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				}
				
				// continue if newer than a day
				if ( lNow - TimeUnit.DAYS.toMillis( 1 ) < lModTime ) continue;

				if ( strName.startsWith( "BridJExtractedLibraries" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				} else if ( strName.contains( "jar_cache" ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				} else if ( strName.startsWith( "WER" ) 
						&& strName.contains( ".tmp." ) ) {
					FileUtils.deleteQuietly( file );
					lCount++;
				} 
				
				// continue if newer than 7 days
				if ( lNow - TimeUnit.DAYS.toMillis( 7 ) < lModTime ) continue;

				if ( strName.startsWith( "~" ) 
						&& strName.endsWith( ".TMP" ) ) {
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
