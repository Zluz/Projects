package jmr.pr136;

import java.io.File;

import jmr.util.transform.DateFormatting;

public class DataArchive {

	public final static int REV = 1;

	public final File filePath;
	
//	public static DataArchive get() {
//		return instance;
//	}
	
	
	public DataArchive( final String strArchivePath ) {
		this.filePath = new File( strArchivePath );
	}
	
	
	public void log(	final long lTime,
						final float fAccVolts,
						final float f12VDC,
						final float fAnalog3,
						final boolean bOverhead1,
						final boolean bVehLAN,
						final boolean bRelay3 ) {
		final String strTime = DateFormatting.getDateTime( lTime );
	}
	
}
