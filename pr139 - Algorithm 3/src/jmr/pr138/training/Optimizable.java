package jmr.pr138.training;

import java.util.List;

public interface Optimizable {

//	public void setParameter( final Parameter parameter );
//	public Parameter getParameter();
//	
//	public void setParamValue( final double dValue );
	
	List<Parameter> getParameters();
	
	/**
	 * zero is a perfect score.
	 * @return
	 */
	public double getScore();
	
}
