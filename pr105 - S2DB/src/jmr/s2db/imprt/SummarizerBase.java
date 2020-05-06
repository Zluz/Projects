package jmr.s2db.imprt;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.SysexMessage;

import com.google.gson.JsonElement;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import net.minidev.json.JSONArray;

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
				
				try {
					final Object objValue = doc.read( strPath );
					
					boolean bSave = ( objValue != null );
					
//					if ( bSave ) {
//						if ( objValue instanceof AbstractCollection ) {
//							if ( ((AbstractCollection<?>)objValue).isEmpty() ) {
//								bSave = false;
//							}
//						}
//					}
					
					if ( bSave ) {

						final String strValue;

						if ( objValue instanceof AbstractCollection ) {
							
							final AbstractCollection<?> ac = 
											(AbstractCollection<?>)objValue;
							
							if ( 0 == ac.size() ) {
								strValue = null;
							} else if ( 1 == ac.size() ) {
								strValue = ac.iterator().next().toString();
							} else {
								strValue = objValue.toString();
							}
						} else {
							strValue = objValue.toString();
						}
							
						if ( null != strValue ) {
							final String strName = entry.getKey();
							map.put( strName, strValue );
						}
					}
					
				} catch ( final PathNotFoundException e ) {
					System.err.println( "Json Path not found: " + strPath );
					e.printStackTrace();
				}
			}
		}
		
		return map;
	}

}
