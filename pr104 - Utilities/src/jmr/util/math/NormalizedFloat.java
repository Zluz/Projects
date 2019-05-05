package jmr.util.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class NormalizedFloat extends FunctionBase {

	public final static int MAX_RAW_HISTORY = 200;
	
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
		this.iSize = 2 * iSize;
//		this.iSize = 1 * iSize;
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

	
	final static class Sample {
		final int iOrder;
		final float fValue;
		float fWeight;
		
		public Sample( final int iOrder,
					   final float fValue ) {
			this.iOrder = iOrder;
			this.fValue = fValue;
			this.fWeight = 1;
		}
	}
	
	
	public static void calcWeights_Age( final List<Sample> list ) {

		final int iSize = list.size();
		
		Collections.sort( list, 
				( lhs, rhs )-> Integer.compare( lhs.iOrder, rhs.iOrder ) );

		// assign weights for age
		// latest to half-age: 100%
		// half-age to oldest: 100% to 50%
		for ( int i=0; i<iSize; i++ ) {
//			final float fWeight = ( 2.0f * iSize ) - i;
//			listEval.get( i ).fWeight = 1;
			
			float fWeight = (float) iSize / 2.0f;
			fWeight = Math.min( fWeight, ( (float) iSize - i ) );
			fWeight = fWeight + (float) iSize / 2.0f;
			
			list.get( i ).fWeight = list.get( i ).fWeight * fWeight;
		}
	}

	
	public static void calcWeights_Distribution( final List<Sample> list ) {

		final int iSize = list.size();

		Collections.sort( list, 
				( lhs, rhs )-> Float.compare( lhs.fWeight, rhs.fWeight ) );
		
		// assign weights for top and bottom of distribution
		// 90%+ = 0%
		// 60% = 100%
		// 40% = 100%
		// 10%- = 0%
		
//		final float fBorderLow = 10f;  // 0.006794389867782593
//		final float fBorderLow = 0f;   // 0.004535465335845947
//		final float fBorderLow = -50f; // 0.002787771224975586
		final float fBorderLow = 30f;
		
		final float fBorderHigh = 100f - fBorderLow;
		for ( int i=0; i<iSize; i++ ) {
			final float fPos = (float) 100 * i / iSize;
//			float fWeight = 5; // 0.014151349091529847 (precision)
			float fWeight = 5;
			fWeight = Math.min( fWeight, ( fPos - fBorderLow ) );
			fWeight = Math.min( fWeight, ( fBorderHigh - fPos ) );
			fWeight = Math.max( fWeight, 0 );
			
			final Sample sample = list.get( i );
			sample.fWeight = sample.fWeight * fWeight;
		}
	}


	@Override
	public Double evaluate() {
//		return evaluate_001();
		return evaluate_002();
	}


	
	
	public Double evaluate_002() {
		final List<Sample> listEval;
		
		synchronized ( this.listGood ) {
//			if ( this.listGood.isEmpty() ) return null;
			if ( this.listGood.size() < this.iSize ) return null;
		
			listEval = new LinkedList<>();
			int i=0;
			for ( final float fValue : listGood ) {
				final Sample sample = new Sample( i, fValue );
				listEval.add( sample );
				i++;
			}
		}

		
		calcWeights_Age( listEval );

		calcWeights_Distribution( listEval );
		
		
		final int iSize = listEval.size();
		
		// add up samples, calculate weighted mean
		float fSum = 0;
		float fWeights = 0;
		for ( int i=0; i<iSize; i++ ) {
			final Sample sample = listEval.get( i );
			if ( sample.fWeight > 0 ) {
				fSum = fSum + ( sample.fValue * sample.fWeight );
				fWeights = fWeights + sample.fWeight;
			}
		}
		if ( 0==fWeights ) return null;
		final double dWeightedMean = fSum / fWeights;
		
		return dWeightedMean;
	}
		

	public Double evaluate_001() {
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
		
		if ( Double.isNaN( dMean ) ) {
			return null;
		} else {
			return dMean;
		}
	}
	
}
