package jmr.pr138.math;

public class Point {

	public final double dX;
	public final double dY;
	
	public Point( 	final double dX,
					final double dY ) {
		this.dX = dX;
		this.dY = dY;
	}

	public double getX() {
		return this.dX;
	}
	
	public double getY() {
		return this.dY;
	}
	
	@Override
	public String toString() {
		return String.format( "Point( %.2f, %.2f )", this.dX, this.dY );
	}
	
	public boolean isValid() {
		return Double.isFinite( this.dX ) && Double.isFinite( this.dY );
	}
	
	
	public static Point[] createPointArray( final double... arrScalars ) {
		final int iSize = arrScalars.length / 2;
		final Point[] arrResult = new Point[ iSize ];
		for ( int i = 0; i < iSize; i++ ) {
			final double dX = arrScalars[ i * 2 ];
			final double dY = arrScalars[ i * 2 + 1 ];
			final Point point = new Point( dX, dY );
			arrResult[ i ] = point;
		}
		return arrResult;
	}
	
}
