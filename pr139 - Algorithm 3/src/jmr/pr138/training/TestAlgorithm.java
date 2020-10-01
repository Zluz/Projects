package jmr.pr138.training;

import java.util.logging.Logger;

public class TestAlgorithm implements Optimizable {

	final static private Logger LOGGER = 
			Logger.getLogger( TestAlgorithm.class.getName() );
	
	public final static double TEST_AREA = 100.0;
	
//	public final static double TARGET = 10.0;

	public final double dTargetValue;
	public double dCurrentValue;
	
//	private Parameter parameter;
	
	public static class TestParameter implements Parameter {

		@Override
		public double getInitial() {
			return TEST_AREA / 2.0;
		}

		@Override
		public double getTestRange() {
			return 10;
		}

		@Override
		public boolean isLinear() {
			return true;
		}
		
		@Override
		public double compare(	final double dLHS, 
								final double dRHS ) {
			final double dResult = Double.compare( 
											Math.abs( dLHS ),
											Math.abs( dRHS ) );
			return dResult;
		}
		
		@Override
		public boolean isLeftBetter( 	final double dLHS, 
										final double dRHS ) {
			return this.compare( dLHS, dRHS ) < 0;
		}
	}
	
	public static final Parameter PARAMETER = new TestParameter();
	
	
	public TestAlgorithm() {
//		this.dTargetValue = TEST_AREA * 2 * Math.random() - TEST_AREA;
		this.dTargetValue  = 14.0;
	}
	
//	@Override
//	public void setParameter( final Parameter parameter ) {
//		this.parameter = parameter;
//	}
	
	@Override
	public Parameter getParameter() {
		return PARAMETER;
	}
	
	// remember, zero is a perfect score, non-zero is less than ideal
	@Override
	public double getScore() {
//		return TARGET - this.parameter.getCurrent(); 
//		return this.dTarget - this.parameter.getCurrent();
//		final double dRandom = Math.random() * 4.0 - 2.0;
		final double dRandom = 0.0;
		final double dDiff = this.dTargetValue - this.dCurrentValue; 
		// score parabola must rise at x^1.4 or better
//		final double dResult = Math.pow( Math.abs( dDiff ), 1.00000 ) + dRandom;
//		final double dResult = Math.pow( Math.abs( dDiff ), 2.1 ) + dRandom;
		final double dResult = Math.pow( Math.abs( dDiff ), 3 ) + dRandom;
//		final double dResult = dDiff * dDiff; // easy. simple check.
		if ( ! Double.isFinite( dResult ) ) {
			LOGGER.warning( "Invalid result from getScore()" );
		}
		return dResult;
	}

	@Override
	public void setParamValue( final double dValue ) {
		this.dCurrentValue = dValue;
	}
	
}
