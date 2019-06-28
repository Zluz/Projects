package jmr.pr125;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import jmr.util.MonitorProcess;
import jmr.util.SystemUtil;

public class PostStillsContinuous {
	

	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( PostStillsContinuous.class.getName() );

	
	
	boolean bActive;
	boolean bRunning;
	boolean bForceStop;
	
	final File fileTempDir = SystemUtil.getTempDir();
	
	final Set<String> setFilesPosted = new TreeSet<>();
	
	private Process process;
	

	public static interface PostStillsListener {
		
		public void reportNewFile( final File file );
		
		public void reportProcessEnded();
	}
	
	
	private PostStillsListener listener;
	

	public PostStillsContinuous( final PostStillsListener listener ) {
		this.listener = listener;
	}
	
	public void setListener( final PostStillsListener listener ) {
		this.listener = listener;
	}
	
	
	final IOFileFilter filter = new IOFileFilter() {
		
		@Override
		public boolean accept( final File dir, 
							   final String name ) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean accept( final File file ) {
			final boolean bAccept = file.getName().endsWith( ".jpg" );
			return bAccept;
		}
	};
	
	
	public void start() {
		final String strCommand = ImageCaptureS2.getCommand() + " continuous";
		
		final Thread thread = new Thread( "pr124 process monitor" ) {
			@Override
			public void run() {

				try {
					final long lTimeStart = System.currentTimeMillis();
					process = Runtime.getRuntime().exec( strCommand );
					System.out.println( "Launched: " + strCommand );
					
					bRunning = true;
					
					boolean bInitializing = true;

					final Long[] lLastOutput = { lTimeStart, lTimeStart };
					boolean bUnresponsive = false;

					// required end the process when it stops working
					MonitorProcess.addDummyStream( process );
					
					// monitor STDERR
					MonitorProcess.addConsoleEcho( process, false, lLastOutput );
					
					while ( process.isAlive() 
									&& ! bForceStop 
									&& ! bUnresponsive ) {

//						System.out.println( "Scanning temp dir.." );
						
						final Collection<File> listFiles = 
								FileUtils.listFiles( fileTempDir, filter, null );
						
						for ( final File file : listFiles ) {
							final String strName = file.getName();
							if ( ! setFilesPosted.contains( strName ) ) {
								if ( ! bInitializing ) {
									
//									System.out.println( "File to post: " 
//												+ file.getAbsolutePath() );
									if ( null!=listener ) {
										listener.reportNewFile( file );
									}
								}
								setFilesPosted.add( strName );
							}
						}
						
						bInitializing = false;
						
						//TODO see if WatchService can be used here 
						try {
							Thread.sleep( 100 );
						} catch ( final InterruptedException e ) {
							System.out.println( "Interrupted" );
							process.destroyForcibly();
							// maybe wait?
							
							bActive = false;
						}
						
						final long lNow = System.currentTimeMillis();
						final long lElapsed = lNow - lLastOutput[0];
						if ( lElapsed > TimeUnit.SECONDS.toMillis( 120 ) ) {
							bUnresponsive = true;
							System.out.println( "Process is unresponsive." );
						}
					}
					
					if ( ! process.isAlive() ) {
						System.out.println( "Process ended." );
						System.out.println( "Exit code: " + process.exitValue() );
					} else {
						System.out.println( "Process monitoring abandoned, "
											+ "process still alive." );
					}
					bRunning = false;
					
				} catch ( final IOException e ) {
					e.printStackTrace();
					bRunning = false;
				}
				
				if ( null!=listener ) {
					listener.reportProcessEnded();
				}
			}
		};
		thread.start();
		
		try {
			Thread.sleep( 2000 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void stop() {
		bForceStop = true;
	}
	
	public boolean isActive() {
		return this.bActive;
	}
	
	public boolean isRunning() {
		return this.bRunning;
	}
	
	
	public static void main_( final String[] args ) 
								throws IOException, InterruptedException {
		
		final String strCommand = ImageCaptureS2.getCommand();
		final String strCommandContinuous = strCommand + " continuous";
		System.out.println( "Command: " + strCommandContinuous );
		
		final Process process = Runtime.getRuntime().exec( strCommand );
		
		process.waitFor( 10, TimeUnit.SECONDS );
		final int iExitCode = process.exitValue();
		
		System.out.println( "Exit code: " + iExitCode );
	}
	
	
	public static void main( final String[] args ) {
		final PostStillsListener listener = new PostStillsListener() {
			@Override
			public void reportProcessEnded() {}
			@Override
			public void reportNewFile(File file) {}
		};
		final PostStillsContinuous post = new PostStillsContinuous( listener );
		post.start();
	}
	
	
}
