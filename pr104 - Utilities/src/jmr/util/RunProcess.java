package jmr.util;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

public class RunProcess {
	
	private final DefaultExecutor executor; 
    private final CommandLine cmd;
    
    private final StringBuilder sbStdOut = new StringBuilder();
    private final StringBuilder sbStdErr = new StringBuilder();
	
	public RunProcess( final String[] strCommand ) {
		
	    executor = new DefaultExecutor();
	    CommandLine cl = null;
	    for ( int i = 0; i < strCommand.length; i++ ) {
	    	final String strEntry = strCommand[ i ];
	    	if ( 0 == i ) {
			    cl = new CommandLine( strEntry );
	    	} else {
	    		cl.addArgument( strEntry );
	    	}
	    }
	    this.cmd = cl;
	    
	    
	    final LogOutputStream streamStdOut = new LogOutputStream() {
			@Override
			protected void processLine( final String strLine,
										final int logLevel ) {
				sbStdOut.append( strLine + "\n" );
			}
	    };

	    final LogOutputStream streamStdErr = new LogOutputStream() {
			@Override
			protected void processLine( final String strLine,
										final int logLevel ) {
				sbStdErr.append( strLine + "\n" );
			}
	    };
	    
		final PumpStreamHandler psh = new PumpStreamHandler( 
				streamStdOut, streamStdErr );
		executor.setStreamHandler( psh );
	}

	
	public Integer run() {
		try {
			final int iExitValue = executor.execute( cmd );
		    return iExitValue;
		} catch ( final ExecuteException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public String getStdOut() {
		return this.sbStdOut.toString();
	}

	
	public String getStdErr() {
		return this.sbStdErr.toString();
	}

}
