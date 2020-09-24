package jmr.pr138.input;

public interface MProvider {

	public boolean isReady();
	public boolean hasNext();
	public M getNext();
	
}
