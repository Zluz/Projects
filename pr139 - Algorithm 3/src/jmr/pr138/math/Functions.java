package jmr.pr138.math;

public class Functions {

	
	/**
	 * Given 3 Points defining a vertical parabola, return the vertex, if any.
	 * Returns null if impossible.
	 * 
	 * @param arrPoints
	 * @return
	 */
	// see https://stackoverflow.com/questions/717762/how-to-calculate-the-vertex-of-a-parabola-given-three-points
	public static Point getParabolicVertex( final Point[] arrPoints ) {
		if ( null == arrPoints ) return null;
		if ( arrPoints.length < 3 ) return null;
		for ( final Point point : arrPoints ) {
			if ( ! point.isValid() ) return null;
		}
		
		final double x1 = arrPoints[ 0 ].getX();
		final double y1 = arrPoints[ 0 ].getY();
		final double x2 = arrPoints[ 1 ].getX();
		final double y2 = arrPoints[ 1 ].getY();
		final double x3 = arrPoints[ 2 ].getX();
		final double y3 = arrPoints[ 2 ].getY();
		
		final double dD = ( x1 - x2 ) * ( x1 - x3 ) * ( x2 - x3 );
		final double dA = ( x3 * ( y2 - y1 ) 
								+ x2 * ( y1 - y3 ) 
								+ x1 * ( y3 - y2 ) ) / dD;
		final double dB = ( x3*x3 * ( y1 - y2 ) 
								+ x2*x2 * ( y3 - y1 ) 
								+ x1*x1 * ( y2 - y3 ) ) / dD;
		final double dC = ( x2 * x3 * ( x2 - x3 ) * y1 
								+ x3 * x1 * ( x3 - x1 ) * y2 
								+ x1 * x2 * ( x1 - x2 ) * y3 ) / dD;
		
		final double dX = -dB / ( 2.0 * dA );
		final double dY = dC - dB * dB / ( 4.0 * dA );
		final Point point = new Point( dX, dY );
		return point;
	}
	
	
	public static void main( final String[] args ) {
		
		// simple: vertex at origin
		final Point[] arr0 = Point.createPointArray( -10,10, 0,0, 10,10 );
		final Point pt0 = getParabolicVertex( arr0 );
		System.out.println( pt0 );
		
		// simple, but offset
		final Point[] arr1 = Point.createPointArray( -10,0, 0,10, 10,100 );
		final Point pt1 = getParabolicVertex( arr1 );
		System.out.println( pt1 );

		// invalid function: parabola extends to the right
		final Point[] arr2 = Point.createPointArray( 10,-10, 0,0, 10,10 );
		final Point pt2 = getParabolicVertex( arr2 );
		System.out.println( pt2 );

		// invalid: points define a straight line
		final Point[] arr3 = Point.createPointArray( 0,0, 10,10, 100,100 );
		final Point pt3 = getParabolicVertex( arr3 );
		System.out.println( pt3 );

		// similar to #3 but last point pushed up a bit, making a curve
		final Point[] arr4 = Point.createPointArray( 0,0, 10,10, 100,200 );
		final Point pt4 = getParabolicVertex( arr4 );
		System.out.println( pt4 );

		// valid. extends to -Y.
		final Point[] arr5 = Point.createPointArray( 0,-10, 10,50, 20,-10 );
		final Point pt5 = getParabolicVertex( arr5 );
		System.out.println( pt5 );

		final Point[] arr6 = Point.createPointArray( 0,0, 10,100, 100,10 );
		final Point pt6 = getParabolicVertex( arr6 );
		System.out.println( pt6 );

		// testing EarlySelector results..
		final Point[] arr7 = Point.createPointArray( 
						3.0,570.091, 3.2,570.286, 3.4,571.455 );
		final Point pt7 = getParabolicVertex( arr7 );
		System.out.println( pt7 );
		
	}
	
}
