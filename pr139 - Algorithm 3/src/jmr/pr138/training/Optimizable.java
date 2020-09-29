package jmr.pr138.training;

public interface Optimizable {

//	public void setParameter( final Parameter parameter );
	public Parameter getParameter();
	
	public void setParamValue( final double dValue );
	
	/**
	 * zero is a perfect score.
	 * @return
	 */
	public double getScore();
	
}
