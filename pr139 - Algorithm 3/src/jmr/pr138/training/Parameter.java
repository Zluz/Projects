package jmr.pr138.training;

/**
 * an optimizable parameter.
 * maybe?
 * 	 must be linear
 *   must be zero-based
 */
public abstract class Parameter<T> {

//	public String getName();
	
	public abstract T getInitial();
	
	/** default test range, as a percentage */
	public abstract double getTestRange();
	


	public abstract void setCurrent( final T tValue );
	
	public abstract T getCurrent();
	
	
	public double asDouble() {
		final T t = getCurrent();
		if ( t instanceof Double ) {
			return (double)t;
		} else {
			throw new IllegalStateException( "Parameter is not a double." );
		}
	}
}
