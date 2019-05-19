package jmr.util.math;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

public abstract class FunctionBase {

//	private final ConnectionProvider connprov;
//	
//	public FunctionBase( final ConnectionProvider connprov ) {
//		this.connprov = connprov;
//	}
	

	protected final EnumMap<FunctionParameter,Double> 
					mapParamDouble = new EnumMap<>( FunctionParameter.class );
	
	
	protected List<String> listMessages = new LinkedList<>();
	
	protected Double dLastPosted = null;
	
	private final String strUnit;
	
	
	public FunctionBase( final String strUnit ) {
		this.strUnit = strUnit;
	}
	
	public abstract Double evaluate();
	
	public abstract boolean hasEnoughSamples();

	
	public String getUnit() {
		return this.strUnit;
	}
	
	
	public void setParamDouble( final FunctionParameter param,
								final Double dValue ) {
		if ( null!=dValue ) {
			mapParamDouble.put( param, dValue );
		} else {
			mapParamDouble.remove( param );
		}
	}

	public Double getParamDouble( final FunctionParameter param ) {
		if ( mapParamDouble.containsKey( param ) ) {
			return mapParamDouble.get( param );
		} else {
			return null;
		}
	}

	public double getParamDouble( final FunctionParameter param,
								  final double dDefault ) {
		final Double dValue = getParamDouble( param );
		if ( null!=dValue ) {
			return dValue.doubleValue();
		} else {
			return dDefault;
		}
	}
	
	
}
