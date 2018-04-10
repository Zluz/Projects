package jmr.pr118;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
	
	public List<String> getResourcesAsText() {
		final List<String> list = new LinkedList<>();
		for ( final Resource resource : listResources ) {
			list.add( resource.getSourcePath() );
		}
		return list;
	}
	
	
	public void addResource( final String strProjectResource ) {
		if ( null==strProjectResource ) return;
		if ( strProjectResource.isEmpty() ) return;
		
		final InputStream is = cl.getResourceAsStream( strProjectResource );

		if ( null==is ) {
			System.err.println( 
					"Resource appears to be invalid: " + strProjectResource );
			return;
		}
		
        final Resource resource = ResourceFactory.newInputStreamResource( is )
					        		.setResourceType( ResourceType.DRL )
					        		.setSourcePath( strProjectResource );
        listResources.add( resource );
	}
	
	
	public void addResources(	final String strProjectPath,
								final boolean bOnlyLatest ) {
		if ( null==strProjectPath ) return;
		
		final InputStream is = cl.getResourceAsStream( strProjectPath );
		
		if ( null==is ) {
			System.err.println( 
					"Resource appears to be invalid: " + strProjectPath );
			return;
		}
		
	    final InputStreamReader isr = new InputStreamReader( is );
		final BufferedReader br = new BufferedReader( isr );
		
		String strLine;

		try {
			
			long lLatestModified = 0;
			String strLatestFile = null;
			
	        while( ( strLine = br.readLine()) != null ) {
		    	System.out.println( "Scanning resource: " + strLine );

		    	final String strUpper = strLine.trim().toUpperCase();
		    	
		    	if ( strUpper.endsWith( ".DRL" ) ) {
			    	final String strPath = strProjectPath + "/" + strLine;
			    	try {
				    	final URL url = cl.getResource( strPath );
			    		final File file = new File( url.toURI() );
			    		if ( file.isFile() ) {
//			    			System.out.println( "Rules file found: " + file );
			    			final long lThisModified = file.lastModified();
			    			if ( lThisModified > lLatestModified ) {
			    				lLatestModified = lThisModified;
			    				strLatestFile = strPath;
			    			}
			    		}
			    	} catch ( final Exception e ) {
			    		// just ignore
			    	}
			    	
			    	if ( !bOnlyLatest ) {
			    		this.addResource( strPath );
			    	}
		    	}
	        }
	        
	        if ( bOnlyLatest && null!=strLatestFile ) {
	        	System.out.println( "Loading latest DRL file: " + strLatestFile );
	        	this.addResource( strLatestFile );
	        }
	        
		} catch ( final IOException e ) {
			// just ignore for now
		}
	}

	
//	public void addResources( final String strProjectPath ) {
//		this.addResources( strProjectPath, false );
//	}

	
	
	public static void main( final String[] args ) {
		final RuleSet rules = new RuleSet();
		// oops, this only works from pr115
		rules.addResources( "jmr/pr115/rules/drl", false );
	}
	
	
	
}
