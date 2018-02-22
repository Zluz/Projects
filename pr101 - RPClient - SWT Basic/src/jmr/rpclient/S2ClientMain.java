package jmr.rpclient;

import java.util.Date;

import jmr.util.Logging;

public class S2ClientMain {

	public static void main( final String[] args ) {

		Logging.log( "Application starting. " + new Date().toString() );

		boolean bConsole = false;
		for ( final String arg : args ) {
			if ( arg.toLowerCase().endsWith( "console" ) ) {
				bConsole = true;
			}
		}
		
		if ( !bConsole ) {
			SWTBasic.main( args );
		} else {
			ConsoleClient.main( args );
		}

		Logging.log( "Application closing. " + new Date().toString() );
	}

}
