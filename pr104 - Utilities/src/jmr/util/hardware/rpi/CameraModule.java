package jmr.util.hardware.rpi;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import jmr.util.OSUtil;

public class CameraModule {

	
	private static final Logger 
			LOGGER = Logger.getLogger( CameraModule.class.getName() );

	private static CameraModule instance;
	
	private Boolean bCameraPresent = null;
	
	
	
	private CameraModule() {};
	
	public synchronized static CameraModule get() {
		if ( null==instance ) {
			instance = new CameraModule();
		}
		return instance;
	}
	


	@SuppressWarnings("unused")
	private File _capture() {
//		if ( OSUtil.isWin() ) return null;
//		if ( !isCameraPresent() ) return null;

		try {
			final File file = 
					File.createTempFile( "raspistill_", ".jpg" );
			final String strCommand = 
					"/usr/bin/raspistill "
					+ "-o \"" + file.getAbsolutePath() + "\" "
					+ "-n -t 10 -q 10 -th none -h 300 -w 300";
			LOGGER.info( "Exec: " + strCommand );
			final Process process = 
					Runtime.getRuntime().exec( strCommand );
			final int iResult = process.waitFor();
			
			Thread.sleep( 200 );
			
			if ( 0==iResult && file.length() > 0 ) {
				return file;
			} else {
				file.delete();
				return null;
			}
			
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( final InterruptedException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private File captureThumbnail() {
//		if ( OSUtil.isWin() ) return null;
//		if ( !isCameraPresent() ) return null;

		try {
//			final File file = new File( "/tmp/capture_still_now.jpg" );
//			final File file = new File( "/tmp/capture_cam-thumb.jpg" );
			final File file = new File( "/tmp/session/capture_cam-thumb.jpg" );
			if ( file.exists() ) {
				return file;
			} else {
				return null;
			}
			
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public File getStillThumbnailFile() {
		if ( !isCameraPresent() ) return null;
		
		final File file = captureThumbnail();
		return file;
	}
	
	
	public boolean isCameraPresent() {
		if ( null==bCameraPresent ) {
			if ( OSUtil.isWin() ) {
				LOGGER.info( "RPi Camera Module is not present (Windows OS)" );
				bCameraPresent = false;
			} else {
				final File file = captureThumbnail();
				if ( null!=file ) {
					LOGGER.info( "RPi Camera Module available" );
					bCameraPresent = true;
					file.delete();
				} else {
					LOGGER.info( "RPi Camera Module is not present" );
					bCameraPresent = false;
//					return false;
				}
			}
		}
		return bCameraPresent.booleanValue();
	}
	
	
}
