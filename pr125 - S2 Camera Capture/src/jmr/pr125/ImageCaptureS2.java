package jmr.pr125;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import jmr.SessionPath;
import jmr.util.MonitorProcess;
import jmr.util.OSUtil;

public class ImageCaptureS2 {


	private static final Logger 
					LOGGER = Logger.getLogger( ImageCaptureS2.class.getName() );


	private static final String COMMAND_KILL_ERROR_WIN = 
				"C:\\windows\\system32\\taskkill.exe /im \"werfault.exe\" /f";

	
	final File fileDir;
	final List<File> listFiles = new LinkedList<>();

	
	public ImageCaptureS2() {
		File file = null;
		try {
			final String strTemp = System.getenv( "TEMP" );
			if ( ! StringUtils.isBlank( strTemp ) ) {
				final String strDir = "pr125_" + System.currentTimeMillis();
				file = new File( strTemp, strDir );
				FileUtils.forceMkdir( file );
			} 
			if ( null==file ) {
				file = Files.createTempDirectory( "pr125_" ).toFile();
			}
			
			if ( null!=file && file.isDirectory() ) {
				LOGGER.info( "Using temporary directory: " 
												+ file.getAbsolutePath() );
			} else {
				LOGGER.severe( "Failed to allocate a temporary directory." );
			}
		} catch ( final IOException e ) {
			file = null;
			LOGGER.severe( ()-> "Failed to set up a temporary directory." );
			LOGGER.log( Level.WARNING, "Call stack", e );
		}
		this.fileDir = file;
	}
	
	
	public static File getLatest_pr124() {
		
		System.out.println( "Looking for the latest pr124 jar.." );
		
		final List<File> list = SessionPath.getPath_DevelopmentExport();
		if ( list.isEmpty() ) throw new IllegalStateException( 
							"Development Export path not found." );
//		final File filePath = list.get( 0 );
		
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept( final File dir, final String name ) {
				return name.startsWith( "pr124_" ) && name.endsWith( ".jar" );
			}
		};
		long lLatest = 0;
		File fileLatest = null;
		for ( final File dir : list ) {
			System.out.println( "\tChecking: " + dir.getAbsolutePath() );
			final File[] files = dir.listFiles(filter);
			if ( null!=files ) {
				for ( final File file : files ) {
					final long lTimestamp = file.lastModified();
					if ( lTimestamp > lLatest ) {
						System.out.println( "\t\tFound: " + file.getName() );
						lLatest = lTimestamp;
						fileLatest = file;
					}
				}
			}
		}
		
		if ( null==fileLatest ) {
			LOGGER.warning( "Failed to locate a pr124 program JAR." );
			return null;
		}
		
		try {
			final File fileTemp = File.createTempFile( "pr124_", ".jar" );
			fileTemp.deleteOnExit();
			FileUtils.copyFile( fileLatest, fileTemp );
			return fileTemp;
		} catch ( final IOException e )  {
			LOGGER.warning( "Failed to copy JAR to temporary file." );
			return null;
		}
	}
	
	
	public void killErrorProcess() {

		final String strCommand;
		if ( OSUtil.isWin() ) {
			strCommand = COMMAND_KILL_ERROR_WIN;
		} else {
			strCommand = null;
		}
		
		if ( null==strCommand ) return;
		
		try {
			System.out.print( "Command to clear old error messages:"
					+ " \"" + strCommand + "\". Running..." );
		    final Process process = 
		    				Runtime.getRuntime().exec( strCommand );
		    process.waitFor( 10, TimeUnit.SECONDS );
		    final int iExitCode = process.exitValue();
		    System.out.println( "Done, exit code " + iExitCode + "." );
		} catch ( final Exception e ) {
			LOGGER.warning( ()-> "Problem running the process to kill "
					+ "a past process in an error state (the "
					+ "error message): " + e.toString() );
		}
	}
	
	
	
	public synchronized boolean runCapture( final boolean bShowOutput ) {
		final String strCommand = getCommand();
		
		for ( final File file : this.listFiles ) {
			if ( file.exists() ) {
				file.delete();
			}
		}
		
		killErrorProcess();
		
		try {
			
			// see 
			// https://stackoverflow.com/questions/6223765/start-a-java-process-at-low-priority-using-runtime-exec-processbuilder-start
//			final String strLowPriorityCommand = 
//						"cmd.exe /C start /B /belownormal " + strCommand;
		
			LOGGER.info( ()-> "Launching process: \n" + strCommand + "\n" );  
//						+ "\nin dir: " + this.fileDir.getAbsolutePath() );
			
			final Process process = Runtime.getRuntime().exec( 
							strCommand, null, fileDir );
//							strLowPriorityCommand, null, fileDir );

			if ( bShowOutput ) {
				MonitorProcess.addConsoleEcho( process, true, null );
				MonitorProcess.addConsoleEcho( process, false, null );
			}
			
			process.waitFor( 60L, TimeUnit.SECONDS );

			if ( process.isAlive() ) {
				LOGGER.warning( "Capture execution (pr124) is taking too long." );
//				return false;
			}
			

		} catch ( final InterruptedException e ) {
			LOGGER.info( "Capture process interrupted." );
			return false;
		} catch ( final IOException e ) {
			LOGGER.severe( "Failed to run capture process, "
									+ "encountered " + e.toString() );
//			return false;
		}
		
//		for ( final File file : fileDir.listFiles() ) {
//			System.out.println( "\t" + file.getAbsolutePath() );
//			this.listFiles.add( file );
//		}
		
		this.listFiles.clear();
		this.listFiles.addAll( Arrays.asList( fileDir.listFiles() ) );
		final Comparator<File> comparator = new Comparator<File>() {
			@Override
			public int compare( final File fileLHS, final File fileRHS ) {
				final long lTimeLHS = fileLHS.lastModified();
				final long lTimeRHS = fileRHS.lastModified();
				return Long.compare( lTimeLHS, lTimeRHS );
			}
		};
		Collections.sort( this.listFiles, comparator );
		
		return true;
	}
	
	
	public synchronized List<File> getImageFiles() {
		return Collections.unmodifiableList( this.listFiles );
	}
	
	
	private static String strCommand = null;
	
	
	public static String getCommand() {
		
		if ( null==strCommand ) {
			
			final File file_pr124 = getLatest_pr124();
			strCommand = "java.exe -classpath "
	//				+ "\"S:\\Development\\Export\\pr124_20190224_007.jar;"
					+ "\"" + file_pr124.getAbsolutePath() + ";"
					+ "S:\\Resources\\lib\\webcam-capture\\target\\classes;"
					+ "S:\\Resources\\lib\\bridj-0.7.0.jar;"
					+ "S:\\Resources\\lib\\slf4j-api-1.7.2.jar;"
					+ "S:\\Resources\\lib\\logback-classic-1.0.9.jar;"
					+ "S:\\Resources\\lib\\logback-core-1.0.9.jar;"
					+ "S:\\Resources\\lib\\hamcrest-library-1.3.jar;"
					+ "S:\\Resources\\lib\\hamcrest-core-1.3.jar;"
					+ "S:\\Resources\\lib\\cglib-nodep-3.1.jar;"
					+ "S:\\Resources\\lib\\objenesis-2.1.jar;"
					+ "S:\\Resources\\lib\\commons-lang3-3.7.jar;"
					+ "S:\\Resources\\lib\\commons-io-2.6.jar\" "
					+ "jmr.pr124.ImageCapture";
		}
		return strCommand;
	}
	
	
	public boolean isValid() {
		if ( null==this.fileDir ) return false;
		if ( ! this.fileDir.isDirectory() ) return false;
		
		return true;
	}
	
	
//	public static void main(String[] args) 
//							throws IOException, InterruptedException {
//		
////		final Client client = Client.get();
//		
//		final FileSessionManager fsm = FileSessionManager.getInstance();
//		
//		System.out.println( "Session Keys" );
//		for ( final String strKeys : fsm.getSessionKeys() ) {
//			System.out.println( "\t" + strKeys );
//		}
//		
//		System.out.println( "Session path: " + SessionPath.getPath() );
//
//		final File file_pr124 = getLatest_pr124();
//		
//		System.out.println( "Latest pr124: " + file_pr124 );
//
//		System.out.println( "Command: " + getCommand() );
//
//		new ImageCaptureS2().runCapture( false );
//	}
	
	
}
