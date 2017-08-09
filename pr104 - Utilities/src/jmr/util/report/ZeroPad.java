package jmr.util.report;

public class ZeroPad {

	final private int iDigits;
	
	public ZeroPad( final long iMaxValue ) {
		iDigits = (int)( Math.log10( iMaxValue ) + 1 );
	}
	
	public String str( final long iValue ) {
		return String.format( "%0" + iDigits + "d", iValue );
	}
	
}
