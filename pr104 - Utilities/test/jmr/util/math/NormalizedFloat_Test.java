package jmr.util.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import jmr.util.math.NormalizedFloat.Sample;

public class NormalizedFloat_Test {

	
	@Test
	public void calcWeights_Age_Test() {

		final List<Sample> list = new LinkedList<>();
		for ( int i=0; i<1000; i++ ) {
			final float fRand = (float)( Math.random() * 100.0f );
			final Sample sample = new Sample( i, fRand );
			list.add( sample );
		}
		
		NormalizedFloat.calcWeights_Age( list );
		System.out.println( "Weights, newest: " 
										+ list.get( 0  ).fWeight + ", "
										+ list.get( 10 ).fWeight + ", "
										+ list.get( 20 ).fWeight + " " );
		System.out.println( "Weights, middle: " 
										+ list.get( 490 ).fWeight + ", "
										+ list.get( 500 ).fWeight + ", "
										+ list.get( 510 ).fWeight + " " );
		System.out.println( "Weights, oldest: " 
										+ list.get( 980 ).fWeight + ", "
										+ list.get( 990 ).fWeight + ", "
										+ list.get( 999 ).fWeight + " " );

		assertEquals( 1000f, list.get( 0 ).fWeight, 0 );
		assertEquals( 1000f, list.get( 490 ).fWeight, 0 );
		assertEquals( 500f, list.get( 999 ).fWeight, 10 );
	}
	

	@Test
	public void calcWeights_Distribution_Test() {

		final List<Sample> list = new LinkedList<>();
		for ( int i=0; i<1000; i++ ) {
			final Sample sample = new Sample( i, (float) i );
			list.add( sample );
		}
		
		NormalizedFloat.calcWeights_Distribution( list );
		
		System.out.println( "Weights:" );
		for ( int i=1; i<20; i++ ) {
			final int iIndex = i * 50;
			final Sample sample = list.get( iIndex );
			System.out.println( "\tindex = " + iIndex + ", "
								+ "weight = " + sample.fWeight );
		}

		assertEquals( 0f, list.get( 0 ).fWeight, 0 );
//		assertEquals( 40f, list.get( 500 ).fWeight, 30 );
		assertEquals( 0f, list.get( 999 ).fWeight, 0.1f );
	}
	
	
	@Test
	public void evaluate_test() {
		final NormalizedFloat nf = new NormalizedFloat( 100, 10, 10 );
		
		for ( int i=0; i<100; i++ ) {
			final float fRand = (float)( Math.random() * 100.0f );
			nf.add( fRand );
		}
		
		final Double dAverage = nf.evaluate();
		System.out.println( "Average: " + dAverage );
		
		assertTrue( dAverage > 30 );
		assertTrue( dAverage < 70 );
		
		double dPrecisionSum = 0;
		double dAccuracySum = 0;
		
		double dLastValue = dAverage;
		
		for ( int i=50; i<100; i++ ) {
			for ( int j=0; j<10; j++ ) {
				nf.add( (float) ( Math.random() * 40.0f - 20.0f + i ) );
				nf.add( (float) i );
			}
			
			final Double dMovingAverage = nf.evaluate();
//			System.out.println( "Average (i=" + i + "): " + dMovingAverage );
			
			double dPrecisionHere = Math.abs( dLastValue - dMovingAverage );
			dPrecisionSum = dPrecisionSum + dPrecisionHere;
			double dAccuracyHere = Math.abs( i - dMovingAverage );
			dAccuracySum = dAccuracySum + dAccuracyHere;
			dLastValue = dMovingAverage;
		}

		for ( int k=0; k<10; k++ ) {
		
		for ( int i=100; i>0; i-- ) {
			for ( int j=0; j<10; j++ ) {
				nf.add( (float) ( Math.random() * 40.0f - 20.0f + i ) );
				nf.add( (float) i );
			}
			
			final Double dMovingAverage = nf.evaluate();
//			System.out.println( "Average (i=" + i + "): " + dMovingAverage );
			
			double dPrecisionHere = Math.abs( dLastValue - dMovingAverage );
			dPrecisionSum = dPrecisionSum + dPrecisionHere;
			double dAccuracyHere = Math.abs( i - dMovingAverage );
			dAccuracySum = dAccuracySum + dAccuracyHere;
			dLastValue = dMovingAverage;
		}

		for ( int i=0; i<100; i++ ) {
			for ( int j=0; j<10; j++ ) {
				nf.add( (float) ( Math.random() * 40.0f - 20.0f + i ) );
				nf.add( (float) i );
			}
			
			final Double dMovingAverage = nf.evaluate();
//			System.out.println( "Average (i=" + i + "): " + dMovingAverage );
			
			double dPrecisionHere = Math.abs( dLastValue - dMovingAverage );
			dPrecisionSum = dPrecisionSum + dPrecisionHere;
			double dAccuracyHere = Math.abs( i - dMovingAverage );
			dAccuracySum = dAccuracySum + dAccuracyHere;
			dLastValue = dMovingAverage;
		}

		}
		
		final int iTotalCount = 2050;
		
		final double dPrecisionScore = dPrecisionSum / iTotalCount;
		final double dAccuracyScore = dAccuracySum / iTotalCount;
		
		System.out.println( "Scores (lower is better)" );
		System.out.println( "Precision score: " + dPrecisionScore );
		System.out.println( "Accuracy score: " + dAccuracyScore );
	}


	@Test
	public void evaluate_test_02() {
		final NormalizedFloat nf = new NormalizedFloat( 30, 13, 13 );
		
		final List<Float> list = TestData.getSamples();

		double dPrecisionSum = 0;
		double dAccuracySum = 0;
		double dLastValue = TestData.APPROX_AVERAGE;
		
		double dAverageSum = 0;
		int iTotalCountSum = 0;

		for ( final Float fValue : list ) {
			nf.add( fValue );
			
			final Double dMovingAverage = nf.evaluate();

			if ( null!=dMovingAverage ) {
				
				dAverageSum = dAverageSum + dMovingAverage;
				iTotalCountSum++;
				
				double dPrecisionHere = Math.abs( 
								dLastValue - dMovingAverage );
				dPrecisionSum = dPrecisionSum + dPrecisionHere;
				double dAccuracyHere = Math.abs( 
								TestData.APPROX_AVERAGE - dMovingAverage );
				dAccuracySum = dAccuracySum + dAccuracyHere;
				dLastValue = dMovingAverage;
			}
		}
		
		final int iTotalCount = list.size();
		
		final double dPrecisionScore = dPrecisionSum / iTotalCount;
		final double dAccuracyScore = dAccuracySum / iTotalCount;
		
		final double dAverage = dAverageSum / iTotalCountSum;
		System.out.println( "Number of samples:  " + iTotalCount );
		System.out.println( "Normalized calcs:   " + iTotalCountSum );
		System.out.println( "Total average:      " 
								+ String.format( "%.5f", dAverage ) );

		System.out.println( "Scores (lower better)" );
		System.out.println( "  Precision score:  " 
								+ String.format( "%.5f", dPrecisionScore ) );
		System.out.println( "  Accuracy score:   " 
								+ String.format( "%.5f", dAccuracyScore ) );
	}

	
	/*
	 * 

specific fixed-number original algorithm:

Number of samples:  10000
Normalized calcs:   9974
Total average:      3.73776
Scores (lower better)
  Precision score:  0.00100
  Accuracy score:   0.01539


generalized function using weighted averages (with 2x samples)

Number of samples:  10000
Normalized calcs:   9941
Total average:      3.67723
Scores (lower better)
  Precision score:  0.01407
  Accuracy score:   0.07243


	 */
	
	
	
	
	
	
	
	
}
