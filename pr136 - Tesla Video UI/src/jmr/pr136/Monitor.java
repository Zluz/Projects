package jmr.pr136;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.DoubleStream;

public class Monitor {

	final static int MAX_SAMPLES = 1000;
	final static int MAX_AGE = 20000;
	
	/*package*/ final TreeMap<Long,Double> mapData = new TreeMap<>();
	
	private final String strTitle;
	
	
	public Monitor( final String strTitle ) {
		this.strTitle = strTitle;
	}
	
	
	public String getTitle() {
		return this.strTitle;
	}
	

	public void add( final long lTime,
					 final double dValue ) {
		
		synchronized ( mapData ) {
			
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
	}

	private DoubleStream getDoubleStream() {
		final DoubleStream ds;
//		synchronized ( mapData ) {
			ds = mapData.values().stream().mapToDouble( d -> d );
//		}
		return ds;
	}
	
	public Double getAverageValue() {
		if ( mapData.isEmpty() ) return null;
		synchronized ( mapData ) {
			final DoubleStream ds = getDoubleStream();
			final double dAverage = ds.average().getAsDouble();
			return dAverage;
		}
	}
	
	public Double getMinimumValue() {
		if ( mapData.isEmpty() ) return null;
		synchronized ( mapData ) {
			final DoubleStream ds = getDoubleStream();
			final double dMin = ds.min().getAsDouble();
			return dMin;
		}
	}
	
	public Double getMaximumValue() {
		if ( mapData.isEmpty() ) return null;
		synchronized ( mapData ) {
			final DoubleStream ds = getDoubleStream();
			final double dMax = ds.max().getAsDouble();
			return dMax;
		}
	}
	
	public long getOldestTime() {
		return mapData.firstKey();
	}
	
	public Map<Long,Double> getData() {
//		return Collections.unmodifiableMap( this.mapData );
		synchronized ( this.mapData ) {
			final Map<Long,Double> map = new HashMap<>( this.mapData );
			return map;
		}
	}
	
	public List<Long> getDataKeys() {
		final List<Long> list; 
		synchronized ( this.mapData ) {
			list = new LinkedList<>( this.mapData.keySet() );
		}
		Collections.sort( list );
		return list;
	}
	
}
