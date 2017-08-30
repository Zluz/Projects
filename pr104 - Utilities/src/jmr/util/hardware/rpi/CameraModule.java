package jmr.util.hardware.rpi;

import java.io.File;
import java.io.IOException;

import jmr.util.OSUtil;

public class CameraModule {

	private static CameraModule instance;
	
	private Boolean bCameraPresent = null;
	
	
	private CameraModule() {};
	
	public synchronized static CameraModule get() {
		if ( null==instance ) {
			instance = new CameraModule();
		}
		return instance;
	}
	

	
	private File capture() {
//		if ( OSUtil.isWin() ) return null;
//		if ( !isCameraPresent() ) return null;

		try {
			final File file = 
					File.createTempFile( "raspistill_", ".jpg" );
			final String strCommand = 
					"/usr/bin/raspistill "
					+ "-o " + file.getAbsolutePath() 
					+ " -n -t 10 -q 10 -th none";
			final Process process = 
					Runtime.getRuntime().exec( strCommand );
			final int iResult = process.waitFor();
			
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
	
	
	public File getStillPictureFile() {
		if ( !isCameraPresent() ) return null;
		
		final File file = capture();
//		final Image image = new Image();
		return file;
	}
	
	
	public boolean isCameraPresent() {
		if ( null==bCameraPresent ) {
			if ( OSUtil.isWin() ) {
				bCameraPresent = false;
			} else {
				final File file = capture();
				if ( null!=file ) {
					bCameraPresent = true;
					file.delete();
				} else {
					return false;
				}
			}
		}
		return bCameraPresent.booleanValue();
	}
	
	
}
