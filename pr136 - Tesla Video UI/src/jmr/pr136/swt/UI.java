package jmr.pr136.swt;

import java.util.logging.Logger;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


public abstract class UI {


	private final static Logger LOGGER = Logger.getLogger(UI.class.getName());


	final public static Display display;
	static {
		Display displayCandidate = null;
		try {
			displayCandidate = new Display();
		} catch ( final SWTError e ) {
			System.err.println( "Failed to initialize SWT Display. "
					+ "Encountered " + e.toString() );
			final String strUser = System.getenv( "USER" );
			if ( "root".equalsIgnoreCase( strUser ) ) {
				System.err.println( 
						"User is root. SWT may not work for root user." );
			} else {
				e.printStackTrace();
			}
			System.out.println( "SWT not available. Shutting down." );
			Runtime.getRuntime().exit( 100 );
		}
		display = displayCandidate;
	}
	
	public static Color getColor( final int iColor ) {
		return display.getSystemColor( iColor );
	}

}
