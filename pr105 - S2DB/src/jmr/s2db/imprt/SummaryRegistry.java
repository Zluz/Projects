package jmr.s2db.imprt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SummaryRegistry {

	public final static String PREFIX_SUMMARY = "+";

	private final static List<Summarizer> 
						SUMMARIZERS = new LinkedList<Summarizer>();
	
	private static SummaryRegistry instance;
	
	private SummaryRegistry() {};
	
	public synchronized static SummaryRegistry get() {
		if ( null==instance ) {
			instance = new SummaryRegistry();
		}
		return instance;
	}

	
	public void add( final Summarizer summarizer ) {
		SUMMARIZERS.add( summarizer );
	}
	
	
	public boolean summarize(	final String strNodePath,
								final Map<String,String> mapSource ) {
		if ( null==strNodePath ) return false;
		if ( null==mapSource ) return false;
	
		boolean bApplied = false;
		for ( final Summarizer summarizer : SUMMARIZERS ) {
			if ( summarizer.isMatch( strNodePath ) ) {
				final Map<String, String> 
							mapSummary = summarizer.summarize( mapSource );
				if ( null!=mapSummary && !mapSummary.isEmpty() ) {
					bApplied = true;
					for ( final Entry<String, String> 
											entry : mapSummary.entrySet() ) {
						final String strKey = PREFIX_SUMMARY + entry.getKey();
						final String strValue = entry.getValue();
						mapSource.put( strKey, strValue );
					}
				}
			}
		}
		
		return bApplied;
	}

}
