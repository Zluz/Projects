package jmr.pr138.processing;

import jmr.pr138.input.MDataSet;
import jmr.pr138.input.MProvider;

public class A3 implements A {

//	final private MProvider mp;
	
	public A3() {
		// NOP
	}
	
	public A3( final MProvider mp ) {
//		this.mp = mp;
		throw new RuntimeException( "Not implemented" );
	}
	
	@Override
	public Result calc( final MDataSet data ) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String calc() {
		throw new RuntimeException( "Not implemented" );
	}
	
	public static void main(String[] args) {
		final MProvider mp = null;
		final A a3 = new A3();
		
		System.out.println( a3 );
		
		final String strResult = a3.calc();
		System.out.println( strResult );
	}

}
