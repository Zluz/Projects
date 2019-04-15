package jmr.util.math;

import java.util.LinkedList;
import java.util.List;

public abstract class FunctionBase {

//	private final ConnectionProvider connprov;
//	
//	public FunctionBase( final ConnectionProvider connprov ) {
//		this.connprov = connprov;
//	}
	
	protected List<String> listMessages = new LinkedList<>();
	
	
	public abstract Double evaluate();
	
}
