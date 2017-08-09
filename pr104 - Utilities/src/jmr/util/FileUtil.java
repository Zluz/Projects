package jmr.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
	
	
	public static String readFromFile( final File file ) {
		final Path path = file.toPath();
		try {
			final String strContent = new String( Files.readAllBytes( path ) );
			return strContent;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

	public static boolean deleteDirRecurse( final File file ) {
		if ( null==file ) return false;
		if ( file.isDirectory() ) {
			for ( final File c : file.listFiles() ) {
				deleteDirRecurse(c);
			}
		}
		return file.delete();
	}
	
	
	
	
}
