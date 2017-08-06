package jmr.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {

	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( Logging.class.getName() );

	
//	private static Logging instance;
	
	public final static String FILE_LOG = "Session.log";
	
	private static File file;
	private static FileWriter fw = null; 
	private static BufferedWriter bw = null;
	
	public static void setDir( final File fileDir ) {
		if ( null!=Logging.file ) return;
		if ( null==fileDir ) return;
		if ( !fileDir.isDirectory() ) return;
		
		file = new File( fileDir, FILE_LOG );

		try {
			fw = new FileWriter( file ); 
			bw = new BufferedWriter( fw );
					
			bw.write( "Log started " + new Date().toString() + "\n" );
			bw.flush();
//			bw.close();
			
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void log( final String strMessage ) {
		
		LOGGER.log( Level.FINE, strMessage );
		
		if ( null==strMessage ) return;
		if ( null==bw ) return;
		
		try {
			bw.write( strMessage + "\n" );
			bw.flush();
			
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
