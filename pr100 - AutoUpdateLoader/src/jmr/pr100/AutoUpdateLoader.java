package jmr.pr100;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AutoUpdateLoader {

	private static final String 
//			MAIN_CLASS = AutoUpdateLoader.class.getSimpleName();
//			MAIN_CLASS = System.getProperty( "sun.java.command" ).split(" ")[0];
			PROGRAM_NAME;
//	public static final File fileProgram;
	static {
		final String strRunJar = System.getProperty( "sun.java.command" );
		final File fileProgram = new File( strRunJar );
		String strPathName = fileProgram.getName();
		strPathName = strPathName.replace( '\\', '/' );
		final String[] array = strPathName.split( "/" );
		final String strProgramName = array[ array.length-1 ];
		System.out.println( "Program name (to exclude): " + strProgramName );
		PROGRAM_NAME = strProgramName;
	}

	public final static File 
			fileDir = new File( System.getProperty( "user.dir" ) );
	
	public final static List<File> listFilesExcluded = new LinkedList<File>();
	
//	static {
//		listFilesExcluded.add( fileProgram );
//	}
	
	public static File fileRunning = null;

	private static Process proc = null;
	
	
	public static File getLatestFile() {
		long lLatest = 0;
		File fileLatest = null;
		for ( final File file : fileDir.listFiles() ) {
			
			final String strFilename = file.getName();

			if ( listFilesExcluded.contains( file ) ) {
				// found in the exclude list..
				
			} else if ( strFilename.contains( PROGRAM_NAME ) ) {
//				 likely this program, ignore..
				
			} else if ( isValidJar( file ) ) {
				if ( file.lastModified() > lLatest ) {
					fileLatest = file;
					lLatest = file.lastModified();
				}
			}
		}
		return fileLatest;
	};
	
	
	public static boolean isValidJar( final File file ) {
		if ( null==file ) return false;
		if ( !file.getName().toLowerCase().endsWith( ".jar" ) ) return false;

		for ( int i=0; i<4; i++ ) {
			
			try ( final JarFile jar = new JarFile( file ); ) {
				final Manifest manifest = jar.getManifest();
				final Attributes attrs = manifest.getMainAttributes();
				return ( null!=attrs );
				
			} catch ( final IOException e ) {
				// file may be incomplete, wait then try again
			}
			
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	
	
	
	public static void main( String[] args ) 
					throws IOException, InterruptedException {

		System.out.println( "Monitoring directory: " + fileDir );
		
		do {
			Thread.sleep( 1000 );
			
			final File fileNewLatest = getLatestFile();
			
			final boolean bNullRunning = ( null==fileRunning );
			final boolean bNewerFound = 
					null!=fileNewLatest
					&& null!=fileRunning
					&& ( fileNewLatest.lastModified() 
										> fileRunning.lastModified() );
			
			if ( bNullRunning || bNewerFound ) {

				if ( bNullRunning ) {
					System.out.println( "No progrem running, launching next.." );
				} else if ( bNewerFound ) {
					System.out.println( 
							"Newer JAR found: " + fileNewLatest.toString() );
				}

				if ( null!=proc ) {
					System.out.println( "Killing process: " + proc );
					proc.destroy();
					fileRunning = null;
				}

				Thread.sleep( 1000 ); // wait (finish copying)
				
				listFilesExcluded.add( fileNewLatest );
				fileRunning = fileNewLatest;

				final String strCommand = "java -jar " + fileNewLatest.getName();
				System.out.println( "Running command:\n" + strCommand );
				proc = Runtime.getRuntime().exec( strCommand );
				System.out.println( "Process created: " + proc );
				
				Thread.sleep( 1000 ); // wait (finish launching)

				final Thread threadStdErr = new Thread() {
					@Override
					public void run() {
						final InputStream is = proc.getErrorStream();
						final InputStreamReader isr = new InputStreamReader( is );
						final BufferedReader br = new BufferedReader( isr );
						try {
							String s;
							while ( null != ( s = br.readLine() ) ) {
								System.out.println( "err> " + s );
							}
						} catch ( final IOException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println( "Error stream monitor stopping. "
								+ "Process terminated?" );
					}
				};
				threadStdErr.start();

//				final Thread threadStdOut = new Thread() {
//					@Override
//					public void run() {
//						final InputStream is = proc.getInputStream();
//						final InputStreamReader isr = new InputStreamReader( is );
//						final BufferedReader br = new BufferedReader( isr );
//						try {
//							String s;
//							while ( null != ( s = br.readLine() ) ) {
//								System.out.println( "out> " + s );
//							}
//						} catch ( final IOException e ) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						System.out.println( "Process terminated?" );
//						fileRunning = null;
//					}
//				};
//				threadStdOut.start();

//				final Thread threadExitCode = new Thread() {
//					@Override
//					public void run() {
//						if ( null == proc ) return;
//
//						Integer iExit = null;
//						do {
//							try {
//								Thread.sleep( 100 );
//								iExit = proc.exitValue();
//							} catch ( final IllegalThreadStateException e ) {
//								// process still running, all ok.
//							} catch ( final InterruptedException e ) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						} while ( null==iExit );
//						if ( null!=iExit ) {
//							System.out.println( 
//									"Process returned. Exit code = " + iExit );
//							proc = null;
//						}
//					}
//				};
//				threadExitCode.start();

				final Thread threadWait = new Thread() {
					@Override
					public void run() {
						Integer iExit = null;
						try {
							iExit = ( null!=proc ) ? proc.waitFor() : null;
						} catch ( final InterruptedException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if ( null!=iExit ) {
							System.out.println( 
									"Process returned. Exit code = " + iExit );
						} else {
							System.out.println( "Process was null." );
						}
						proc = null;
					}
				};
				threadWait.start();
//				threadWait.run();
				
			}
			
		} while ( true );
		
	}
	
}
