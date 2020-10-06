package jmr.pr138.datastructures;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class AreaMap {
	
	// Hits: 9944976, Misses: 55024
	// Elapsed time: 3601
	private final Map<Long,Double> map = new TreeMap<>();
	
	// Hits: 9002049, Misses: 997951
	// Elapsed time: 12357
	private final Map<Coord2D,Double> mapC = new TreeMap<>();
	
	
	private static Coord2D round( Coord2D coord ) {
		final double dX = (long)( coord.dX / 2.0 );
		final double dY = (long)( coord.dY / 2.0 );
		final Coord2D coordResult = new Coord2D( dX, dY );
		return coordResult;
	}
	
	private long getKey( final Coord2D coord ) {
//		final long lX = (long) coord.dX;
//		final Coord2D coordRounded = round( coord );
		final long lX = (long)Math.floor( coord.dX );
		final long lY = (long)Math.floor( coord.dY );
//		final long lX = (long)Math.floor( coordRounded.dX );
//		final long lY = (long)Math.floor( coordRounded.dY );
		final long lKey = lY << 32 + lX;
		return lKey;
	}
	
	public void put( 	final Coord2D coord,
						final double dValue ) {

		final Coord2D coordRounded = round( coord );

//		mapC.put( coord, dValue );
		mapC.put( coordRounded, dValue );
	}

	public void put_( 	final Coord2D coord,
						final double dValue ) {
		final long lKey = getKey( coord );
		map.put( lKey, dValue );
	}

	public Double get( final Coord2D coord ) {
		final Coord2D coordRounded = round( coord );
//		return mapC.get( coord );
		return mapC.get( coordRounded );
	}

	public Double get_( final Coord2D coord ) {
		final long lKey = getKey( coord );
		if ( map.containsKey( lKey ) ) {
			return map.get( lKey );
		} else {
			return null;
		}
	}

	public boolean contains( final Coord2D coord ) {
		final Coord2D coordRounded = round( coord );
//		return mapC.containsKey( coord );
		return mapC.containsKey( coordRounded );
	}

	
	public boolean contains_( final Coord2D coord ) {
		final long lKey = getKey( coord );
		return map.containsKey( lKey );
	}
	

	final static double RANGE = 1000;
	final static long ITERATIONS = 1000000;
//	final static long ITERATIONS = 100000;
	
	public static void main( final String[] args ) {
		
		System.out.println( "Starting.." );
		
		final AreaMap am = new AreaMap();
		final Random rand = new Random();
		
		int iHit = 0;
		int iMiss = 0;
		
		final long lTimeStart = System.currentTimeMillis();
		
		for ( int i=0; i<ITERATIONS; i++ ) {
			final double dX = rand.nextDouble() * RANGE * 2 - RANGE;
			final double dY = rand.nextDouble() * RANGE * 2 - RANGE;
			final Coord2D coord = new Coord2D( dX, dY );
			
			if ( am.contains( coord ) ) {
				iHit++;
			} else {
				iMiss++;
				final double dDist = Math.sqrt( dX * dX + dY * dY );
				am.put( coord, dDist );
			}
		}

		final long lTimeEnd = System.currentTimeMillis();
		final long lElapsed = lTimeEnd - lTimeStart;

		System.out.println( "Iterations: " + ITERATIONS + ", "
						+ "Hits: " + iHit + ", Misses: " + iMiss );
		System.out.println( "Elapsed time: " + lElapsed );
	}
	
}
