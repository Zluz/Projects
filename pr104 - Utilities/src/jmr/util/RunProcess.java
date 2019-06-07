package jmr.util;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;

public class RunProcess {

	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( RunProcess.class.getName() );


	private final DefaultExecutor executor; 
    private final CommandLine cmd;
    
    private final StringBuilder sbStdOut = new StringBuilder();
    private final StringBuilder sbStdErr = new StringBuilder();
    
//    private Float fOutputValue;
//    private Integer iOutputValue;
	
	public RunProcess( final String[] strCommand ) {
		
	    executor = new DefaultExecutor();
	    CommandLine cl = null;
	    for ( int i = 0; i < strCommand.length; i++ ) {
	    	final String strEntry = strCommand[ i ];
	    	if ( 0 == i ) {
			    cl = new CommandLine( strEntry );
	    	} else {
	    		if ( StringUtils.isNotBlank( strEntry ) ) {
	    			cl.addArgument( strEntry );
	    		}
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
			final int iExitValue = e.getExitValue();
			return iExitValue;
			
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
	
	
	public Float getOutputFloat() {
		if ( 0==this.sbStdOut.length() ) return null;

		final String strOut = this.sbStdOut.toString().trim();
		if ( strOut.isEmpty() ) return null;
		
		try {
			final String strValue = strOut.split( "\\n" )[0];
			final float fOutput = Float.parseFloat( strValue );
			return fOutput;
		} catch ( final NumberFormatException e ) {
//			LOGGER.warning( strLogPrefix + "Failed to parse float. "
//					+ "Full process output:\n" + strOut );
			return null;
		}
	}

}
