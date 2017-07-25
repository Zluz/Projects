package jmr.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class FileUtil {

	
	public static boolean saveToFile(	final File file,
										final String strContent ) {
		if ( null==file ) return false;
		if ( file.isDirectory() ) return false;

		file.getParentFile().mkdirs();
		
		try (	final FileWriter fw = new FileWriter( file );
				final BufferedWriter bw = new BufferedWriter( fw ) ) {
			
			bw.write( strContent );
			bw.flush();
			bw.close();
			
			return true;
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
}
