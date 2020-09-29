package jmr.pr138.training;

/**
 * an optimizable parameter.
 * maybe?
 * 	 must be linear
 *   must be zero-based
 */
public interface Parameter {

//	public String getName();
	
	public double getInitial();
	
//	public double getCurrent();
	
	/** default test range, as a percentage */
	public double getTestRange();
	
	public boolean isLinear();

	/** less than zero means dLHS is better (closer to zero) */
	public double compare( final double dLHS, final double dRHS );
	
	public boolean isLeftBetter( final double dLHS, final double dRHS );
}
