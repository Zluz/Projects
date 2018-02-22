package jmr.rpclient.screen;

public class TextScreen {

	final char[][] screen;
	
	int iRow = 0;
	int iCol = 0;
	
	public TextScreen(	final int iCols,
						final int iRows ) {
		screen = new char[iRows][iCols];
		for ( int c=0; c<iCols; c++ ) {
			for ( int r=0; r<iRows; r++ ) {
				screen[r][c] = ' ';
			}
		}
	}
	
	private void set(	final int iCol,
						final int iRow,
						final char c ) {
		if ( iCol<0 || iCol>=getCols() ) return;
		if ( iRow<0 || iRow>=getRows() ) return;
		screen[iRow][iCol] = c;
	}
	
	public void print(	final int iCol,
						final int iRow,
						final String strText ) {
		if ( null==strText ) return;
		synchronized ( this.screen ) {
			for ( int i=0; i<strText.length(); i++ ) {
	//			screen[ iY ][ iX + i ] = strText.charAt( i );
				set( iCol + i, iRow, strText.charAt( i ) );
			}
		}
	}
	
	public void go( final int iCol,
					final int iRow ) {
		this.iRow = iRow;
		this.iCol = iCol;
	}
	
	public void println( final String strText ) {
		this.print( this.iCol, this.iRow, strText );
		this.go( iCol, iRow + 1 );
	}
	
	public int getRows() {
		return screen.length;
	}
	
	public int getCols() {
		return screen[0].length;
	}
	
	
}
