package jmr.pr138.training;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jmr.pr138.math.Functions;
import jmr.pr138.math.Point;

public class Optimizer {

	final static private Logger LOGGER = 
			Logger.getLogger( Optimizer.class.getName() );
	
	public static enum TargetState {
		/** has not yet been tested */
		UNTESTED,
		/** last trial improved the parameter (avg parabolic vertex) */
		IMPROVED_PARABOLIC,
		/** last trial improved the parameter (best of calculated scores) */
		IMPROVED_INCREMENTAL,
		/** minimum found, current value is optimal for parameter */
		MINIMUM,
		/** last trial did not improve, but does not appear to be a minimum */
		UNIMPROVED, 
	}
	
	private final Optimizable algorithm;
//	private final Parameter<?> parameter;
	
//	private double dCurrentValue;
	private double dCurrentRange;
	private double dCurrentScore = Double.NaN;
	private TargetState state = TargetState.UNTESTED;
	
	private Double dRangeLeft;
	private Double dRangeRight;
	
	private int iStep = 0;
	
	
	public Optimizer( final Optimizable algorithm ) {
		this.algorithm = algorithm;
//		this.parameter = this.algorithm.getParameter();
//		this.parameter = this.algorithm.getParameters().get( 0 );
		
//		this.dCurrentRange = this.parameter.getTestRange() * 0.01;
//		this.dCurrentValue = this.parameter.getInitial();
		this.dRangeLeft = null;
		this.dRangeRight = null;
	}
	
	
	public static boolean isLeftBetter( final double dLHS,
										final double dRHS ) {
		return Math.abs( dLHS ) < Math.abs( dRHS );
	}
	
	
	
//	public double testParam( final Parameter param ) {
//	public double testParam( final double dValue ) {
	public void step( int iParamIndex ) {
		iStep++;
//		this.algorithm.setParameter( param );
		
		final Parameter<Double> param0 = 
					this.algorithm.getParameters().get( iParamIndex );
//		final double dCurrentValue = param0.getCurrent();
		final double dCurrentValue = param0.asDouble();
		
		final double dCurrentRange = param0.getTestRange() * 0.01;

		final double dLeft, dRight;
		if ( null != this.dRangeLeft ) {
			dLeft = this.dRangeLeft;
		} else {
			dLeft = dCurrentValue * ( 1.0 - dCurrentRange );
		}
		if ( null != this.dRangeRight ) {
			dRight = this.dRangeRight;
		} else {
			dRight = dCurrentValue * ( 1.0 + dCurrentRange );
		}
		final double dRange = dRight - dLeft;

		final Double[] arrTestValues = new Double[ 4 ];
//		final Double[] arrResults = new Double[ 4 ];
		final Point[] arrResults = new Point[ 4 ];
		final Set<Double> set = new HashSet<>();

		for ( int i = 0; i < 4; i++ ) {
			final double dValue = (double) i / 3 * dRange + dLeft;
			arrTestValues[ i ] = dValue;

//			this.algorithm.setParamValue( dValue );
			param0.setCurrent( dValue );
			final double dScore = this.algorithm.getScore();
			if ( Double.isFinite( dScore ) ) {
				set.add( dScore );
			}
			
			final Point point = new Point( dValue, dScore );
			arrResults[ i ] = point;
		}
//		final boolean bAllGood = iFoundGood == 4;

		
		//TODO remove
		LOGGER.info( "Score array: " + Arrays.deepToString( arrResults ) );

		

		if ( set.isEmpty() ) {
			LOGGER.warning( "All scores were invalid." );
			LOGGER.info( "Doubling the current test range." );
			this.dCurrentRange = this.dCurrentRange * 2;
			this.state = TargetState.UNIMPROVED;
			return;
		} else if ( 1 == set.size() ) {
			LOGGER.warning( "All scores were equal." );
			LOGGER.info( "Current score: " + this.dCurrentScore );
			LOGGER.info( "Current range: " + this.dCurrentRange + " %" );
			LOGGER.info( "Score array: " + Arrays.deepToString( arrResults ) );
			LOGGER.info( "Doubling the current test range." );
			this.dCurrentRange = this.dCurrentRange * 2;
			this.state = TargetState.UNIMPROVED;
			return;
		}
		
		// attempt to calculate the optimal value
		// (by averaging minimums of 3 parabolas based on 4 points)
		
		final Point[] arrVertices = new Point[ 4 ];
		
		arrVertices[ 0 ] = Functions.getParabolicVertex( 
				arrResults[ 0 ], arrResults[ 1 ], arrResults[ 2 ] );
		arrVertices[ 1 ] = Functions.getParabolicVertex( 
				arrResults[ 0 ], arrResults[ 1 ], arrResults[ 3 ] );
		arrVertices[ 2 ] = Functions.getParabolicVertex( 
				arrResults[ 0 ], arrResults[ 2 ], arrResults[ 3 ] );
		arrVertices[ 3 ] = Functions.getParabolicVertex( 
				arrResults[ 1 ], arrResults[ 2 ], arrResults[ 3 ] );
		final Point ptVertex = Point.getAveragePoint( arrVertices );
		
		if ( ptVertex.isValid() ) {

			// initialize if not set, but should not really happen
			if ( Double.isNaN( this.dCurrentScore ) ) {
//				this.algorithm.setParamValue( this.dCurrentValue );
				param0.setCurrent( dCurrentValue );
				this.dCurrentScore = this.algorithm.getScore();
			}
			
			final double dCandidateValue = ptVertex.getX();
//			this.algorithm.setParamValue( dCandidateValue );
			param0.setCurrent( dCandidateValue );
			final double dCandidateScore = this.algorithm.getScore();
			
			// normal execution

			// double check that this value is actually an improvement
//			if ( this.parameter.isLeftBetter(
			if ( isLeftBetter( dCandidateScore, this.dCurrentScore ) ) {
				
				// all good. this is where we should end up..
				
				
			
//				final double dValuePrevious = dCurrentValue;
//				this.dCurrentValue = dCandidateValue;
				param0.setCurrent( dCandidateValue );
				this.dCurrentScore = dCandidateScore;
				
				if ( Double.isFinite( dCurrentValue ) ) {
					this.dRangeRight = Math.max( dCurrentValue, 
							dCandidateValue * ( 1.0 + dCurrentRange ) );
					this.dRangeLeft = Math.min( dCurrentValue, 
							dCandidateValue * ( 1.0 - dCurrentRange ) );
				}
	
				if ( dCurrentValue != dCandidateValue ) {
					this.state = TargetState.IMPROVED_PARABOLIC;
				} else {
					this.state = TargetState.MINIMUM;
				}
				
				return;
				
			}
		}


	
	
//		LOGGER.warning( "Failed to resolve a parametric vertex score." );
//		LOGGER.info( "Score array: " + Arrays.deepToString( arrResults ) );
		
		
		// slope of results may be too shallow (a line)
		// just take the best of the known scores 
		
		double dBestScore = this.dCurrentScore;
//		double dBestParam = this.dCurrentValue;
//		double dBestParam = param0.getCurrent();
		double dBestParam = param0.asDouble();
		boolean bChanged = false;
		
		for ( final Point point : arrResults ) {
			if ( ( ! Double.isFinite( dBestScore ) ) ||
//					parameter.isLeftBetter( point.dY, dBestScore ) ) {
					isLeftBetter( point.dY, dBestScore ) ) {

				bChanged = true;
				dBestScore = point.dY;
				dBestParam = point.dX;
			}
		}
		
		if ( ! bChanged ) {
			
			// could do no better..
//			this.state = TargetState.UNIMPROVED;
//			LOGGER.warning( "Failed to find a better score." );
			
			// maybe we've found the optimal value
			this.state = TargetState.MINIMUM;
			LOGGER.info( "Value appears to be optimized." );
			
			
			
		} else {
			this.state = TargetState.IMPROVED_INCREMENTAL;
			
//			this.dCurrentValue = dBestParam;
			param0.setCurrent( dBestParam );
			this.dCurrentScore = dBestScore;
		}
		
	}
	
	
	public void optimize( final double dValue ) {
		
	}
	
	
	
	public static void main( final String[] args ) {

		System.setProperty( "java.util.logging.SimpleFormatter.format", 
						"[%1$tF %1$tT] [%4$-7s] %5$s %n" );
		
		final TestAlgorithm ta = new TestAlgorithm();
		final Optimizer o = new Optimizer( ta );
		
		final Parameter<?> p = ta.getParameters().get( 0 );
		
		
		System.out.println( String.format( 
						"Actual target: %.4f", ta.dHiddenTargetA ) );
		System.out.println( String.format( 
//						"Current value: %.4f", o.dCurrentValue ) );
						"Current value: %.4f", p.getCurrent() ) );
		System.out.print( "[" + o.state.name() + "] " );
		System.out.println();
		
		for ( int i=0; i<100; i++ ) {
			o.step( 0 );
			System.out.print( "[" + o.state.name() + "] " );
			System.out.println( String.format( 
//						"Current value: %.30f", o.dCurrentValue ) );
						"Current value: %.30f", p.getCurrent() ) );
			if ( TargetState.MINIMUM.equals( o.state ) ) {
				System.out.println( String.format( 
//						"Found optimal value: %.30f", o.dCurrentValue ) );
						"Found optimal value: %.30f", p.getCurrent() ) );
				break;
			}
		}
		System.out.println( "Found in " + o.iStep + " steps." );
	}
	
	
}
