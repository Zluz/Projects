package jmr.pr138.math;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Point {
	
	final static private Logger LOGGER = 
			Logger.getLogger( Point.class.getName() );
	
	public final static Point INVALID = new Point( Double.NaN, Double.NaN );
	
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

	public String toString( final int iPercision ) {
		return String.format( "Point( %." + iPercision + "f, "
				+ "%." + iPercision + "f )", this.dX, this.dY );
	}

	@Override
	public String toString() {
//		return String.format( "Point( %.2f, %.2f )", this.dX, this.dY );
		return this.toString( 3 );
	}

	public boolean isValid() {
//		if ( ! Double.isFinite( this.dX ) ) {
//			LOGGER.info( "Invalid X: " + this.dX );
//		}
//		if ( ! Double.isFinite( this.dY ) ) {
//			LOGGER.info( "Invalid Y: " + this.dY );
//		}
		
		return Double.isFinite( this.dX ) && Double.isFinite( this.dY );
	}
	
	
	@Override
	public boolean equals( final Object obj ) {
		if ( obj instanceof Point ) return false;
		final Point point = (Point)obj;
		
		if ( this.dX != point.dX ) return false; 
		if ( this.dY != point.dY ) return false; 

		return true;
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
	
	/**
	 * Returns the average of the valid points in the given point array.
	 * @param arrPoints
	 * @return
	 */
	public static Point getAveragePoint( final Point[] arrPoints ) {
		double dSumX = 0, dSumY = 0;
		int iCount = 0;
		for ( int i = 0; i < arrPoints.length; i++ ) {
			final Point point = arrPoints[ i ];
			if ( null != point && point.isValid() ) {
				iCount++;
				dSumX += arrPoints[ i ].getX();
				dSumY += arrPoints[ i ].getY();
			}
		}
		final Point point;
		if ( iCount > 0 ) {
			final double dX = dSumX / iCount;
			final double dY = dSumY / iCount;
			point = new Point( dX, dY );
		} else {
			point = Point.INVALID;
		}
		return point;
	}
	
	public static Point[] dropInvalid( final Point[] arrPoints ) {
		final List<Point> list = new ArrayList<>( arrPoints.length );
		for ( final Point point : arrPoints ) {
			if ( null != point && point.isValid() ) {
				list.add( point );
			}
		}
		final Point[] arrResult = list.toArray( new Point[ list.size() ] );
		return arrResult;
	}
	
	public static Point[] dedup( final Point[] arrPoints ) {
		final Set<Point> set = new HashSet<>();
		for ( final Point point : arrPoints ) {
			set.add( point );
		}
		final Point[] arrResult = set.toArray( new Point[ set.size() ] );
		return arrResult;
	}
	
}
