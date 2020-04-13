package jmr.pr136;

import java.util.TreeMap;

public class Monitor {

	final static int MAX_SAMPLES = 100;
	final static int MAX_AGE = 2000;
	
	private final TreeMap<Long,Double> mapData = new TreeMap<>();
	
	public void add( final long lTime,
					 final double dValue ) {
		mapData.put( lTime, dValue );
		
		//TODO go back and see if keyset.remove would work
		
		while ( mapData.size() > MAX_SAMPLES ) {
			final long lOldest = mapData.firstKey();
			mapData.remove( lOldest );
		}
		
		final long lAgeLimit = lTime - MAX_AGE;
		while ( mapData.firstKey() < lAgeLimit ) {
			mapData.remove( mapData.firstKey() );
		}
	}
	
	public Double getAverageValue() {
		if ( mapData.isEmpty() ) return null;
		
		final double dAverage = mapData.values().stream()
								.mapToDouble( d -> d )
								.average()
								.getAsDouble();
		return dAverage;
	}
	
}
