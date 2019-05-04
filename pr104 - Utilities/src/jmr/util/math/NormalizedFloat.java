package jmr.util.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class NormalizedFloat extends FunctionBase {

	public final static int MAX_RAW_HISTORY = 500;
	
	private final List<Float> listGood;
	private final List<Float> listRaw;
	private final int iSize;
	private final int iCutTop;
	private final int iCutBottom;
	
	public NormalizedFloat( final int iSize,
							final int iCutTop,
							final int iCutBottom ) {
		this.listGood = new LinkedList<Float>();
		this.listRaw = new LinkedList<Float>();
		this.iSize = iSize;
		this.iCutTop = iCutTop;
		this.iCutBottom = iCutBottom;
	}
	
	public void add( final Float fValue ) {
		synchronized ( listGood ) {
			this.listGood.add( fValue );
			if ( this.listGood.size() > iSize ) {
				this.listGood.remove( 0 );
			}
		}
		synchronized ( listRaw ) {
			this.listRaw.add( 0, fValue );
			if ( this.listRaw.size() > MAX_RAW_HISTORY ) {
				this.listRaw.remove( MAX_RAW_HISTORY );
			}
		}
	}
	
	public List<Float> getRawHistory() {
		final List<Float> list = new ArrayList<>( listRaw );
		return list;
	}

	@Override
	public Double evaluate() {
		final List<Float> listEval;
		
		synchronized ( this.listGood ) {
			if ( this.listGood.isEmpty() ) return null;
			if ( this.listGood.size() < ( iCutBottom + iCutTop ) ) return null;
		
			listEval = new LinkedList<>( this.listGood );
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
