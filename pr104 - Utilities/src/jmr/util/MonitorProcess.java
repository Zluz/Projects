package jmr.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MonitorProcess {

	
	
	private final String strName;
	private final String[] arrCommand;
	
	private final String[] arrLastLine = { "" };
	
	private final Thread thread;
	private boolean bRunning = false;
	
	private Process process = null;

	
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
				    
//				    System.out.println( "Process.isAlive()-1: " + process.isAlive() );
				    final InputStream is = process.getInputStream();
				    final InputStreamReader isr = new InputStreamReader(is);
				    final BufferedReader br = new BufferedReader(isr);
				    
//				    System.out.println( "Process.isAlive()-2: " + process.isAlive() );

				    String line;
				    while ((line = br.readLine()) != null) {
				      synchronized ( arrLastLine ) {
				    	  arrLastLine[0] = line;
//					      System.out.println(line);
				      }
				    }
//				    System.out.println("Program terminated!");
				    System.out.println( "External process "
				    					+ "stopped: \"" + strName + "\"" );
				    
				} catch ( final Exception e ) {
					// ignore, just quit
				    System.out.println( "Exception encountered while "
				    		+ "starting/monitoring process \"" + strName + "\"" );
				    e.printStackTrace();
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
		if ( null!=this.process && this.process.isAlive() ) {
			System.out.println( "Stopping "
						+ "external process \"" + this.strName + "\".." );
			this.process.destroy();
		}
	}
	
}
