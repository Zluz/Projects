package jmr.util.math;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class NormalizedFloat extends FunctionBase {

	private final List<Float> list;
	private final int iSize;
	private final int iCutTop;
	private final int iCutBottom;
	
	public NormalizedFloat( final int iSize,
							final int iCutTop,
							final int iCutBottom ) {
		this.list = new LinkedList<Float>();
		this.iSize = iSize;
		this.iCutTop = iCutTop;
		this.iCutBottom = iCutBottom;
	}
	
	public void add( final Float fValue ) {
		synchronized ( list ) {
			this.list.add( fValue );
			if ( this.list.size() > iSize ) {
				this.list.remove( iSize );
			}
		}
	}

	@Override
	public Double evaluate() {
		final List<Float> listEval;
		
		synchronized ( this.list ) {
			if ( this.list.isEmpty() ) return null;
			if ( this.list.size() < ( iCutBottom + iCutTop ) ) return null;
		
			listEval = new LinkedList<>( this.list );
		}
		Collections.sort( listEval );
		
		try {
			for ( int i=0; i<iCutBottom; i++ ) {
				listEval.remove( 0 );
			}
			for ( int i=0; i<iCutTop; i++ ) {
				listEval.remove( listEval.size() - 1 );
			}
		} catch ( final IndexOutOfBoundsException e ) {
			return null;
		}
		
		final double[] arrData = new double[ listEval.size() ];
		for ( int i=0; i<listEval.size(); i++ ) {
			arrData[ i ] = listEval.get( i );
		}
		
		final DescriptiveStatistics ds = new DescriptiveStatistics( arrData );
		
		final double dMean = ds.getMean();
		
//		System.out.println( "Mean: " + dMean );
		
		return dMean;
	}
	
}
