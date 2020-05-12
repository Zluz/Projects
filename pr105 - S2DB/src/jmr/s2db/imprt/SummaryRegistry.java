package jmr.s2db.imprt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;

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
	

	public boolean summarize(	final String strNodePath,
								final JsonElement je,
								final Map<String,String> mapOutput ) {
		if ( null==strNodePath ) return false;
		if ( null==je ) return false;
		if ( null==mapOutput ) return false;

		boolean bApplied = false;
		for ( final Summarizer summarizer : SUMMARIZERS ) {
			if ( summarizer.isMatch( strNodePath ) ) {
				
				final Map<String,String> mapSummary = new HashMap<>();
				mapSummary.putAll( summarizer.summarize( je ) );
				mapSummary.putAll( summarizer.summarize( mapSummary ) );
				
				if ( null!=mapSummary && !mapSummary.isEmpty() ) {
					bApplied = true;
					for ( final Entry<String,String> 
											entry : mapSummary.entrySet() ) {
						final String strKey = PREFIX_SUMMARY + entry.getKey();
						final String strValue = entry.getValue();
						mapOutput.put( strKey, strValue );
					}
				}
			}
		}
		
		return bApplied;
	}


}
