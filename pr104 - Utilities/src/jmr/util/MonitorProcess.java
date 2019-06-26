package jmr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	private long lStartTime = 0L;
	
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
					while ( ( strLine = br.readLine() ) != null ) {
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

//				    final OutputStream osLocal = new LogOutputStream() {
//						
//						@Override
//						protected void processLine(String line, int logLevel) {
//							// TODO Auto-generated method stub
//							
//						}
//					};();
//					
//					final ByteArrayInputStream bais = new ByteArrayInputStream(buf)
//				    
//				    final InputStreamPumper isb = new InputStreamPumper(is, streamStdErr)
//				    final InputStream streamStdIn = new InputStream() {
//				    	
//				    };
				    
//				    final InputStreamPumper isp = new InputStreamPumper();
				    
//				    new ExecInputStream();
//				    final PipedOutputStream pos = new PipedOutputStream();
				    final PipedOutputStream pos = new PipedOutputStream();
				    os = pos;
				    final PipedInputStream pis = new PipedInputStream( pos );
				    
				    
					final PumpStreamHandler psh = new PumpStreamHandler( 
			    					streamStdOut, streamStdErr, pis );
				    executor.setStreamHandler( psh );

				    
				    lStartTime = System.currentTimeMillis();

				    final int iExitValue = executor.execute( cmd );

				    
//				    System.out.println( "Process.isAlive()-1: " + process.isAlive() );
//				    final InputStream is = process.getInputStream();

////				    final InputStreamReader isr = new InputStreamReader(is);
//				    final InputStreamReader isr = new InputStreamReader( is ) {
//				    	
//				    	@Override
//						public int read(char[] cbuf, int offset, int length)
//								throws IOException {
////				    		if ( bDebug ) {
////				    			System.out.print( "2" );
////				    		}
//							final int iResult = super.read(cbuf, offset, length);
//							if ( bDebug ) {
//								System.out.println( "[" + iResult + "]" );
//							}
//							return iResult;
//						}
//				    	
//				    	@Override
//				    	public boolean ready() throws IOException {
//				    		final boolean bReady = super.ready();
//				    		if ( bDebug ) {
//				    			System.out.print( bReady ? "+" : "|" );
//				    		}
//				    		return bReady;
//				    	}
//				    };
//				    
////				    final BufferedReader br = new BufferedReader(isr);
////				    final BufferedReader br = new BufferedReader( isr, 10 ) {
//				    final BufferedReader br = new BufferedReader( isr ) {
//				    	
//				    	@Override
//				    	public int read(char[] cbuf, int off, int len)
//				    			throws IOException {
//				    		if ( bDebug ) {
//				    			System.out.print( "3" );
//				    		}
//				    		return super.read(cbuf, off, len);
//				    	}
//				    };
				    
//				    os = process.getOutputStream();
				    
//				    addOutputMonitor( br );
//				    addOutputMonitor_03( is );
				    
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
	

	private void addOutputMonitor( final BufferedReader br ) 
													throws IOException {
//		addOutputMonitor_01( br );
//		addOutputMonitor_02( br );
//		addOutputMonitor_03( br );
	}

	
	private void addOutputMonitor_02( final BufferedReader br ) 
													throws IOException {

//	    final int iBufLen = 30;
//	    final CharBuffer cb = CharBuffer.allocate( iBufLen );
	    final StringBuilder sb = new StringBuilder();
//		cb.clear();
	    
//		char c;
		int i;
		
//    	while ( bRunning && ( -1 != br.read( cb ) ) ) {
//      while ( bRunning && br.ready() ) {
        while ( bRunning ) {
    		
       		i = br.read();
       		
//    		final String strRead = cb.toString();
    		
//    		sb.append( cb );
//    		sb.append( strRead );
//       		if ( (int) '\n' == i ) {
//       			
//       		} else 
       		if ( -1 != i ) {
       			sb.append( (char) i );
       		} else {
//       			System.out.println( "[END: -1 encountered]");
       		}
    		
//    		cb.reset();
//    		cb.clear();
    		
//    		System.out.println( "> " + strRead + ", sb: " + sb.toString() );
//    		System.out.println( "> sb: " + sb.toString() );
			

    		final String strBuffer = sb.toString();
    		if ( strBuffer.contains( "\n" ) ) {

//        		System.out.println( "> sb: " + sb.toString() );

    	    	final long lNow = System.currentTimeMillis();

    			final String[] strSplit = strBuffer.split( "\\n" );
				final List<String> listSplit = Arrays.asList( strSplit );
				final List<String> list = new ArrayList<>( listSplit );
    			
    			while ( list.size() > 1 ) {
    				final String strLine = list.remove( 0 ).trim();
    				
            		System.out.println( "line> " + strLine );
    				
					synchronized ( arrLastLine ) {
						arrLastLine[0] = strLine;
					}
    				for ( final Listener listener : listListeners ) {
    					listener.process( lNow, strLine );
    				}
    				
    			}
    			sb.setLength( 0 );
    			if ( list.size() > 0 ) {
    				sb.append( list.get( 0 ) );
    			}
    		}
    	}
	}

	private void addOutputMonitor_03( final InputStream is ) 
//	private void addOutputMonitor_03( final BufferedReader br ) 
													throws IOException {

	    final StringBuilder sb = new StringBuilder();
		int i;
		
//		final InputStream is = null;
		
        while ( bRunning ) {
    		
       		i = is.read();
       		
       		if ( (int) '\n' == i ) {

       			final String strLine = sb.toString().trim();
//        		System.out.println( "> sb-line: " + strLine );
        		sb.setLength( 0 );

        		
    	    	final long lNow = System.currentTimeMillis();
        		
//        		System.out.println( "line> " + strLine );
				
				synchronized ( arrLastLine ) {
					arrLastLine[0] = strLine;
				}
				for ( final Listener listener : listListeners ) {
					listener.process( lNow, strLine );
				}

        		
        		
       		} else if ( -1 != i ) {
       			sb.append( (char) i );
       		} else {
       			try {
					Thread.sleep( 10 );
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//       			System.out.println( "[END: -1 encountered]");
       		}
    	}
	}
	
	
	private void addOutputMonitor_01( final BufferedReader br ) 
													throws IOException {
	    String line;
	    while ((line = br.readLine()) != null) {
	    	
//	    	System.out.println( "> " + line );
	    	
	    	final long lNow = System.currentTimeMillis();
	    	
//	    	if ( lNow - lStartTime < ECHO_OUTPUT_DURATION ) {
//	    		System.out.println( "> " + line );
//	    	}
	    	
	    	if ( bRunning ) {
				synchronized (arrLastLine) {
					arrLastLine[0] = line;
				}
				
				for ( final Listener listener : listListeners ) {
					listener.process( lNow, line );
				}
	    	}
	    }
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
