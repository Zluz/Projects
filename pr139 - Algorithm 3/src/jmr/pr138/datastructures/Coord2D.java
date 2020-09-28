package jmr.pr138.datastructures;

public class Coord2D implements Comparable {
	
	final public double dX;
	final public double dY;
	
	public Coord2D( final double dX,
					final double dY ) {
		this.dX = dX;
		this.dY = dY;
	}

	@Override
	public int compareTo( final Object obj ) {
		if ( obj instanceof Coord2D ) {
			final Coord2D cRHS = (Coord2D)obj;
			if ( this.dX < cRHS.dX ) return -1;
			if ( this.dX > cRHS.dX ) return 1;
			if ( this.dY < cRHS.dY ) return -1;
			if ( this.dY > cRHS.dY ) return 1;
			return 0;
		} else {
			return -1;
		}
	}
	
}
