package jmr.pr121.doc;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DocumentMap {

	final static DocumentMap instance = new DocumentMap();
	
	final Map<String,DocumentData> map = new HashMap<>();
	
	private DocumentMap() {};
	
	public static DocumentMap get() {
		return instance;
	}
	
	public void put( 	final String strKey,
						final DocumentData data ) {
		map.put( strKey, data );
	}
	
	public DocumentData get( final String strKey ) {
		return map.get( strKey );
	}
	
	public Set<Entry<String, DocumentData>> entrySet() {
		return map.entrySet();
	}
	
	public List<String> getOrderedKeys() {
		final List<String> list = new LinkedList<>( map.keySet() );
		Collections.sort( list );
		return list;
	}
	
}
