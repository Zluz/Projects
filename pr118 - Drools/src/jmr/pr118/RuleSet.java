package jmr.pr118;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;

public class RuleSet {

	final List<Resource> listResources = new LinkedList<>();
	
	final ClassLoader cl = Thread.currentThread().getContextClassLoader();
	
	
	public List<Resource> getResources() {
		return Collections.unmodifiableList( listResources );
	}
	
	
	public void addResource( final String strProjectResource ) {
		if ( null==strProjectResource ) return;
		if ( strProjectResource.isEmpty() ) return;
		
		final InputStream is = cl.getResourceAsStream( strProjectResource );

        final Resource resource = ResourceFactory.newInputStreamResource( is )
					        		.setResourceType( ResourceType.DRL )
					        		.setSourcePath( strProjectResource );
        listResources.add( resource );
	}
	
	
	public void addResources( final String strProjectPath ) {
		if ( null==strProjectPath ) return;
		
		final InputStream is = cl.getResourceAsStream( strProjectPath );
		
	    final InputStreamReader isr = new InputStreamReader( is );
		final BufferedReader br = new BufferedReader( isr );
		
		String strLine;

		try {
	        while( ( strLine = br.readLine()) != null ) {
		    	System.out.println( "File: " + strLine );

		    	final String strUpper = strLine.trim().toUpperCase();
		    	
		    	if ( strUpper.endsWith( ".DRL" ) ) {
			    	this.addResource( strLine );
		    	}
	        }
		} catch ( final IOException e ) {
			// just ignore for now
		}
	}
	
	
	
	
}
