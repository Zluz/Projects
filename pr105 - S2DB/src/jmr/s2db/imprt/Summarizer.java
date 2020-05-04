package jmr.s2db.imprt;

import java.util.Map;

public interface Summarizer {
	
	boolean isMatch( final String strNodePath );
	
	Map<String,String> summarize( final Map<String,String> input );
	
	/**
	 * NOTE: objRaw would typically be a JsonElement. If this is the case
	 * and an implementation is provided, then it must not be over 64KB 
	 * (in equivalent string form) before returning. Prune data as necessary.
	 * 
	 * @param objRaw - Typically a JsonElement
	 * @return
	 */
	Map<String,String> summarize( final Object objRaw );
	
}
