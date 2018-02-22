package jmr.rpclient.screen;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import jmr.util.OSUtil;

public class TextCanvas {

	public final static int ROWS_WIN = 15;
	public final static int COLS_WIN = 80;
	
	
	private static TextCanvas instance;
	
	public TextScreen screen;
	
	
	private TextCanvas() {
		this.buildTextScreen();
	}
	
	public static synchronized TextCanvas getInstance() {
		if ( null==instance ) {
			instance = new TextCanvas();
		}
		return instance;
	}
	
	
	private static int getIntEnvValue(	final String strName,
										final int iDefault ) {
		final String strValue = System.getenv( strName );
		try {
			final int iValue = Integer.parseInt( strValue );
			return iValue;
		} catch ( final NumberFormatException e ) {
			return iDefault;
		}
	}
	
	
	public TextScreen buildTextScreen() {
		final int iCols;
		final int iRows;
		if( OSUtil.isWin() ) {
			iCols = COLS_WIN;
			iRows = ROWS_WIN;
		} else {
			iCols = getIntEnvValue( "COLUMNS", COLS_WIN );
			iRows = getIntEnvValue( "LINES", ROWS_WIN );
		}
		
		final TextScreen screen = new TextScreen( iCols, iRows );
		
		for ( int x=1; x<iCols-1; x++ ) {
			screen.print( x, 0, "-" );
			screen.print( x, iRows-1, "-" );
		}
		for ( int y=1; y<iRows-1; y++ ) {
			screen.print( 0, y, "|" );
			screen.print( iCols-1, y, "|" );
		}
		screen.print( 0, 0, "." );
		screen.print( iCols-1, 0, "." );
		screen.print( 0, iRows-1, "'" );
		screen.print( iCols-1, iRows-1, "'" );
		
		
		this.screen = screen;
		return this.screen;
	}
	
	private static void run( final String command ) {
		try {
			Runtime.getRuntime().exec( command );
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void clear() {
		if ( OSUtil.isWin() ) {
//			run( "cls" );
//			ConsoleReader
			try {
				final ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
//				pb.inheritIO().start().waitFor();
				pb.redirectOutput( Redirect.INHERIT ).start().waitFor();
//				pb.redirectInput( Redirect.INHERIT ).start().waitFor(); // does not work in Eclipse
//				pb.start().waitFor();
			} catch ( final Exception e ) {
				// just ignore
			}
		} else {
			System.out.print("\033[H\033[2J");  
			System.out.flush();
		}
	}
	
	public TextScreen getScreen() {
		return this.screen;
	}
	
	public void paint() {
		this.clear();
		synchronized ( this.screen.screen ) {
			final StringBuilder sb = new StringBuilder("");
			for ( int row=0; row < this.screen.screen.length; row++ ) {
				for ( int col=0; col < this.screen.screen[0].length; col++ ) {
					sb.append( this.screen.screen[ row ][ col ] );
				}
				System.out.println( sb.toString() );
				sb.setLength( 0 );
			}
		}
	}
	
	
	
}
