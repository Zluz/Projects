package jmr.util.hardware;

import java.util.HashMap;
import java.util.Map;

import jmr.util.math.NormalizedFloat;


public abstract class PortInterface<T> {
	
	// fixed ?
//	final public Port port;
	final public T port;
	final public String strParameters;
//	public Listener listener;
	
	final public Map<String,Object> mapData = new HashMap<>();
	
	public PortInterface( final T port, 
						  final String strParameters ) {
		this.port = port;
		this.strParameters = strParameters;
	}
	
	
	

	public static class OutputDigitalInterface<T> extends PortInterface<T> {
		public boolean bValue;
		public OutputDigitalInterface( final T port ) {
			super( port, null );
		}
	}

	
	public abstract static class InputInterface<T> extends PortInterface<T> {
		
		public boolean bLogical;
		
		public InputInterface( final T port,
							   final String strParameters ) {
			super( port, strParameters );
		}
	}
	
	
	public static class InputDigitalInterface<T> extends InputInterface<T> {
		public InputDigitalInterface( final T port ) {
			super( port, null );
		}

		public Boolean bValue;
	}

	
	public static class InputAnalogInterface<T> extends InputInterface<T> {
		public InputAnalogInterface( final T port,
									 final String strParameters ) {
			super( port, strParameters );
		}
		
		public NormalizedFloat nfValue;
	}
	
}
