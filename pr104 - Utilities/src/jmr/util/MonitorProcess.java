package jmr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class MonitorProcess {

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


	private final List<Runnable> listRunnables = new LinkedList<>();

	
	public MonitorProcess(	final String strName,
							final String[] arrCommand ) {
		this.strName = strName;
		this.arrCommand = arrCommand;
		this.thread = buildThread();
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
			public void run() {
				close();
			}
		});
	}
	
	public void addRunnable( final Runnable runnable ) {
		this.listRunnables.add( runnable );
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
				    
				    process = Runtime.getRuntime().exec( arrCommand );
				    lStartTime = System.currentTimeMillis();
				    
//				    System.out.println( "Process.isAlive()-1: " + process.isAlive() );
				    final InputStream is = process.getInputStream();
				    final InputStreamReader isr = new InputStreamReader(is);
				    final BufferedReader br = new BufferedReader(isr);
				    
				    os = process.getOutputStream();
				    
//				    System.out.println( "Process.isAlive()-2: " + process.isAlive() );

				    String line;
				    while ((line = br.readLine()) != null) {
				    	
				    	final long lNow = System.currentTimeMillis();
				    	
				    	if ( lNow - lStartTime < ECHO_OUTPUT_DURATION ) {
				    		System.out.println( "> " + line );
				    	}
				    	
				    	if ( bRunning ) {
							synchronized (arrLastLine) {
								arrLastLine[0] = line;

								for ( final Runnable runnable : listRunnables ) {
									runnable.run();
								}
							}
				    	}
				    }
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
