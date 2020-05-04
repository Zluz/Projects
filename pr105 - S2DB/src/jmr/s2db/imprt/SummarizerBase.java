package jmr.s2db.imprt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public abstract class SummarizerBase implements Summarizer {

//	final protected Map<String,String> mapJsonPaths = new HashMap<>();

	public SummarizerBase() {
//		mapExpressions.put( "test", );
	}
	
	
	public abstract Map<String,String> getJsonPaths();
	

	@Override
	public Map<String, String> summarize( final Map<String, String> map ) {
		// not implemented, but stubbed out.
		return null;
	}
	

	@Override
	public Map<String, String> summarize( final Object objRaw ) {
		if ( null == objRaw ) return null;
		
		final Map<String,String> map = new HashMap<>();
		
		if ( objRaw instanceof JsonElement ) {
			final JsonElement je = (JsonElement)objRaw;
			
			final DocumentContext doc = JsonPath.parse( je.toString() );
			
			final Map<String,String> mapPaths = this.getJsonPaths();
			for ( final Entry<String,String> entry : mapPaths.entrySet() ) {
				final String strPath = entry.getValue();
				
				final Object objValue = doc.read( strPath );
				
				if ( objValue != null ) {
					final String strValue = objValue.toString();
					final String strName = entry.getKey();
					map.put( strName, strValue );
				}
			}
		}
		
		return map;
	}

}
