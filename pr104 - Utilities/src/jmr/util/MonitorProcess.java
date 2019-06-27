package jmr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

public class MonitorProcess {

	private static final Logger 
					LOGGER = Logger.getLogger( MonitorProcess.class.getName() );

	
	/** echo output from the process to the console for the first second. */
	public static final long ECHO_OUTPUT_DURATION = 3000;
	
	
	private final String strName;
	private final String[] arrCommand;
	
	private final String[] arrLastLine = { "" };
	
	private final Thread thread;
	private boolean bRunning = false;
	
	private Process process = null;
	
	private OutputStream os;
	
//	private long lStartTime = 0L;
	
	private final boolean bDebug;


//	private final List<Runnable> listRunnables = new LinkedList<>();
	private final List<Listener> listListeners = new LinkedList<>();
	
	public static interface Listener {
		public void process( final long lTime,
							 final String strLine );
	}
	
	
	

	public static void addConsoleEcho( final Process process,
									   final boolean bStdOut ) {
		
		final Thread threadOutput = new Thread() {
			public void run() {
				final InputStream is = bStdOut 
										? process.getInputStream()
										: process.getErrorStream();
				final InputStreamReader isr = new InputStreamReader( is );
				try ( final BufferedReader br = new BufferedReader( isr ) ) {
					String strLine = null;
					while ( ( strLine = br.readLine() ) != null 
												&& process.isAlive() ) {
						System.out.println( 
								( bStdOut ? "\tout> " : "\terr> " ) + strLine );
					}
				} catch ( final IOException e ) {
					LOGGER.warning( e.toString() + " encountered "
							+ "while handling " 
							+ ( bStdOut ? "STDOUT." : "STDERR." ) );
				}
			};
		};
		threadOutput.start();
	}
	
	


	/**
	 * This was added to handle the pr124 process.
	 * This seems to be required to get the JRE error to end the process.
	 * Otherwise the process will stop functioning but continue to exist.
	 * @param process
	 */
	public static void addDummyStream( final Process process ) {
		
		final InputStream is = process.getInputStream();
		final InputStreamReader isr = new InputStreamReader( is );
		try ( final BufferedReader br = new BufferedReader( isr ) ) {
//			// do nothing
		} catch ( final IOException e ) {
			LOGGER.warning( e.toString() + " encountered "
					+ "while attaching to STDOUT." );
		}
	}
	
	
	


	public MonitorProcess(	final String strName,
							final String[] arrCommand,
							final boolean bDebug ) {
		this.strName = strName;
		this.arrCommand = arrCommand;
		this.bDebug = bDebug;
		this.thread = buildThread();
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
			public void run() {
				close();
			}
		});
	}

	
	public MonitorProcess(	final String strName,
							final List<String> listCommand, 
							final boolean bDebug ) {
		this( 	strName, 
				listCommand.toArray( new String[ listCommand.size() ] ), 
				bDebug );
	}


	public MonitorProcess(	final String strName,
							final String[] arrCommand ) {
		this( strName, arrCommand, false );
	}

	
//	public void addRunnable( final Runnable runnable ) {
//		this.listRunnables.add( runnable );
//	}

	public void addListener( final Listener listener ) {
		this.listListeners.add( listener );
	}

	private Thread buildThread() {
		final Thread thread = new Thread( this.strName ) {


			public void run() {
				
				try {
				    System.out.println( "Running external process "
				    		+ "(in thread named) \"" + strName + "\"" );
				    for ( final String str : arrCommand ) {
				    	System.out.println( "\t" + str );
				    }
				    
//				    process = Runtime.getRuntime().exec( arrCommand );
//				    final ProcessBuilder pb = new ProcessBuilder( arrCommand );
//				    process = pb.start();
				    
				    final DefaultExecutor executor = new DefaultExecutor();
				    CommandLine cmd = null;
				    for ( int i = 0; i < arrCommand.length; i++ ) {
				    	final String strEntry = arrCommand[ i ];
				    	if ( 0 == i ) {
						    cmd = new CommandLine( strEntry );
				    	} else {
				    		cmd.addArgument( strEntry );
				    	}
				    }
				    
//				    final OutputStream os = new ByteArrayOutputStream();
//				    
//				    executor.getStreamHandler().setProcessInputStream(os);
					
				    final LogOutputStream streamStdOut 
				    							= new LogOutputStream() {
						@Override
						protected void processLine( final String strLine,
													final int logLevel ) {
							
							final long lNow = System.currentTimeMillis();
		    				
							if ( bDebug ) {
								System.out.println( "line> " + strLine );
							}
		    				
							synchronized ( arrLastLine ) {
								arrLastLine[0] = strLine;
							}
		    				for ( final Listener listener : listListeners ) {
		    					listener.process( lNow, strLine );
		    				}
							
						}
				    };

				    final LogOutputStream streamStdErr 
				    							= new LogOutputStream() {
						@Override
						protected void processLine( final String strLine,
													final int logLevel ) {
							
							final long lNow = System.currentTimeMillis();
		    				
		            		System.out.println( "ERR> " + strLine );
		    				
							synchronized ( arrLastLine ) {
								arrLastLine[0] = strLine;
							}
		    				for ( final Listener listener : listListeners ) {
		    					listener.process( lNow, strLine );
		    				}
						}
				    };

//				    final InputStreamPumper isp = new InputStreamPumper();
				    
//				    new ExecInputStream();
//				    final PipedOutputStream pos = new PipedOutputStream();
				    final PipedOutputStream pos = new PipedOutputStream();
				    os = pos;
				    final PipedInputStream pis = new PipedInputStream( pos );
				    
				    
					final PumpStreamHandler psh = new PumpStreamHandler( 
			    					streamStdOut, streamStdErr, pis );
				    executor.setStreamHandler( psh );

				    
//				    lStartTime = System.currentTimeMillis();

//				    final int iExitValue = executor.execute( cmd );

				    
				    System.out.println( "External process "
				    					+ "stopped: \"" + strName + "\"" );
				    
				} catch ( final Exception e ) {
					// ignore, just quit
					if ( bRunning ) {
					    System.out.println( "Exception encountered while "
					    		+ "starting/monitoring process \"" + strName + "\"" );
					    e.printStackTrace();
					}
				    close();
				}
				
			};
		};
		return thread;
	}
	

	
	
	public void start() {
		this.thread.start();
		this.bRunning = true;
	}
	
	public boolean isRunning() {
		return this.bRunning;
	}
	
	public void write( final String strText ) {
		if ( null!=os ) {
			try {
				for ( final char c : strText.toCharArray() ) {
					os.write( c );
				}
			} catch ( final IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Guaranteed to be non-null.
	 * @return
	 */
	public String getLatestLine() {
		final String strLine;
		synchronized ( arrLastLine ) {
			strLine = arrLastLine[0];
		}
		return strLine;
	}

	public void close() {
		this.bRunning = false;
		if ( null!=this.process && this.process.isAlive() ) {
			System.out.println( "Stopping "
						+ "external process \"" + this.strName + "\".." );
			this.process.destroy();
		}
	}
	
}
