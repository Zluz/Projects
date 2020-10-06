package jmr.pr138.training;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class TestAlgorithm implements Optimizable {

	final static private Logger LOGGER = 
			Logger.getLogger( TestAlgorithm.class.getName() );
	
	public final static double TEST_AREA = 100.0;
	
//	public final static double TARGET = 10.0;

//	public final double dTargetValue;
//	public double dCurrentValue;
	
	public final static double dHiddenTargetA = 14.0;
	
//	private Parameter parameter;
	
	public static class TestParameter extends Parameter<Double> {

		private static double INITIAL = TEST_AREA / 2.0; 
		
		@Override
		public Double getInitial() {
			return INITIAL;
		}

		@Override
		public double getTestRange() {
			return 10;
		}

//		@Override
//		public boolean isLinear() {
//			return true;
//		}


		// instance info
		
		private double dValue = INITIAL;
		
	
		@Override
		public void setCurrent( final Double dValue ) {
			this.dValue = dValue;
		}
		
		@Override
		public Double getCurrent() {
			return this.dValue;
		}
		
//		@Override
//		public double compare(	final double dLHS, 
//								final double dRHS ) {
//			final double dResult = Double.compare( 
//											Math.abs( dLHS ),
//											Math.abs( dRHS ) );
//			return dResult;
//		}
//		
//		@Override
//		public boolean isLeftBetter( 	final double dLHS, 
//										final double dRHS ) {
//			return this.compare( dLHS, dRHS ) < 0;
//		}
	}
	
	public static final Parameter PARAMETER = new TestParameter();
	
	
	public TestAlgorithm() {
//		this.dTargetValue = TEST_AREA * 2 * Math.random() - TEST_AREA;
//		this.dTargetValue  = 14.0;
	}
	
//	@Override
//	public void setParameter( final Parameter parameter ) {
//		this.parameter = parameter;
//	}
	
//	@Override
//	public Parameter getParameter() {
//		return PARAMETER;
//	}
	
	@Override
	public List<Parameter> getParameters() {
		final List<Parameter> list = new LinkedList<>();
		list.add( PARAMETER );
		return list;
	}
	
	// remember, zero is a perfect score, non-zero is less than ideal
	@Override
	public double getScore() {
		
		final Parameter<?> p0 = this.getParameters().get( 0 );
		
//		return TARGET - this.parameter.getCurrent(); 
//		return this.dTarget - this.parameter.getCurrent();
//		final double dRandom = Math.random() * 4.0 - 2.0;
		final double dRandom = 0.0;
//		final double dDiff = this.dTargetValue - this.dCurrentValue; 
//		final double dDiff = dHiddenTargetA - p0.getCurrent(); 
		final double dDiff = dHiddenTargetA - p0.asDouble(); 
		// score parabola must rise at x^1.4 or better
		final double dResult = Math.pow( Math.abs( dDiff ), 0.5 ) + dRandom;
//		final double dResult = Math.pow( Math.abs( dDiff ), 1.00000 ) + dRandom;
//		final double dResult = Math.pow( Math.abs( dDiff ), 2.1 ) + dRandom;
//		final double dResult = Math.pow( Math.abs( dDiff ), 3 ) + dRandom;
//		final double dResult = dDiff * dDiff; // easy. simple check.
		if ( ! Double.isFinite( dResult ) ) {
			LOGGER.warning( "Invalid result from getScore()" );
		}
		return dResult;
	}

//	@Override
//	public void setParamValue( final double dValue ) {
//		this.dCurrentValue = dValue;
//	}
	
}
