package jmr.s2db.imprt;

import java.util.Map;

public interface Summarizer {
	
	boolean isMatch( final String strNodePath );
	
	Map<String,String> summarize( final Map<String,String> input );
	
}
