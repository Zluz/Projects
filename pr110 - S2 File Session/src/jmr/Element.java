package jmr;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Element {

	public final String strValue;
	
	public final Long lValue;
	
	public final File file;
	
	public Element( final String strValue ) {
		this.strValue = strValue;
		this.lValue = getAsLong( strValue );
		this.file = null;
	}
	
	public Element( final long lValue ) {
		this.strValue = Long.toString( lValue );
		this.lValue = lValue;
		this.file = null;
	}
	
	public Element( final File file ) {
		if ( null!=file ) {
			this.strValue = file.getAbsolutePath();
		} else {
			this.strValue = "<null file>";
		}
		this.lValue = null;
		this.file = file;
	}
	
	
	public static Long getAsLong( final String strValue ) {
		try {
			final long lValue = Long.parseLong( strValue );
			return lValue;
		} catch ( final NumberFormatException e ) {
			// ignore
		}
		return null;
	}
	
	public String get() {
		return this.getAsString();
	}
	
	public String getAsString() {
		return this.strValue;
	}
	
	public Long getAsLong() {
		return this.lValue;
	}
	
	public File getAsFile() {
		return this.file;
	}
	
	public static Map<String,Element> 
					convertStringMap( final Map<String,String> mapString ) {
		final Map<String,Element> mapElement = new HashMap<>();
		for ( final Entry<String, String> entry : mapString.entrySet() ) {
			final String strValue = entry.getValue();
			mapElement.put( entry.getKey(), new Element( strValue ) );
		}
		return mapElement;
	}
	
}
