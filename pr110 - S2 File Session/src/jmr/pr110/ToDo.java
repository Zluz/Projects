package jmr.pr110;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import jmr.util.FileUtil;
import jmr.util.OSUtil;
import jmr.util.transform.DateFormatting;


public class ToDo {

	private final static Logger 
				LOGGER = Logger.getLogger( ToDo.class.getName() );
	
	public final static String LF;
	static {
		LF = OSUtil.isWin() ? "\r\n" : "\n";
	}
	


	public static File getFile() {
		final File fileBase;
		if ( OSUtil.isWin() ) {
			fileBase = new File( "S:/Sessions/00-00-CA-FE-BA-BE/" );
		} else {
//			fileBase = new File( "/tmp/session/" );
//			fileBase = new File( "~/" );
			fileBase = new File( "/tmp/" );
		}
		
		if ( ! fileBase.isDirectory() ) {
			LOGGER.warning( ()-> "Failed to access directory: " 
									+ fileBase.getAbsolutePath() );
			return null;
		}
		
		final File file = new File( fileBase, "ToDo.txt" );
		
		return file;
	}
	
	
	public static void add( final String strNewToDo ) {
		if ( null == strNewToDo ) return;
		
		final String strNormToDo = strNewToDo.trim();
		if ( strNormToDo.isEmpty() ) return;
		
		final File file = getFile();
		if ( null == file ) return;
		
//		final List<String> listContent;
		final String strContent;
		
		if ( file.exists() ) {
			strContent = FileUtil.readFromFile( file );
			final String[] arrContent = strContent.split( "\\n" );
			for ( final String strLine : arrContent ) {
				if ( strNormToDo.equals( strLine.trim() ) ) {
					return;
				}
			}
		} else {
			try {
				file.createNewFile();
			} catch ( final IOException e ) {
				e.printStackTrace();
				LOGGER.warning( ()-> "Failed to create ToDo file, "
						+ "encountered " + e.toString() );
				return;
			}
//			listContent = new LinkedList<String>();
//			strContent = "(ToDo file)";
			strContent = "ToDo file created " + DateFormatting.getTimestamp();
			LOGGER.info( ()-> strContent );
		}
		
		final String strNewContent = 
						strContent.trim() + LF 
						+ strNormToDo + LF;
		FileUtil.saveToFile( file, strNewContent ); 
	}
	
	
	public static void main( final String[] args ) {
		ToDo.add( "test!" );
	}
	
}
