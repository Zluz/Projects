package jmr.pr114.s2.ingest;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import jmr.pr113.FullStatus;
import jmr.pr113.Session;
import jmr.s2db.Client;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.util.NetUtil;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class NestIngestManager {

	final static char[] cUsername = 
			SystemUtil.getProperty( SUProperty.NEST_USERNAME ).toCharArray(); 
	final static char[] cPassword = 
			SystemUtil.getProperty( SUProperty.NEST_PASSWORD ).toCharArray(); 

	
	public void printValues( final FullStatus status ) {

		final Map<String, String> map = status.getMap();

		for ( final Entry<String, String> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();
			
			System.out.println( "\t\"" + strKey + "\" = \"" + strValue + "\"" );
		}
		
		
		
		for ( final Entry<String, String> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();
			
			final String strUpper = strKey.toUpperCase();
			
//			System.out.println( "\t\"" + strKey + "\" = \"" + strValue + "\"" );
			System.out.println( strUpper + "( \"" + strKey + "\" ), // \"" + strValue + "\"" );
		}

	}
	
	
	
	
	public static void main( final String[] args ) throws Exception {

		System.out.println( "Registering S2 client.." );
		final String strSession = NetUtil.getSessionID();
		final String strClass = NestIngestManager.class.getName();
		Client.get().register( strSession, strClass );
		
		System.out.println( "Creating Nest session.." );
		final Session session = new Session( cUsername, cPassword );

		final Path path = new Path();
		final Page page = new Page();
		
		for (;;) {
			
			final Date now = new Date();
			
			System.out.println( "Requesting Nest (thermostat) status.." );
			final FullStatus status = session.getStatus();
			
			System.out.println( "Saving page.." );
			final String strNodePath = "/External/Ingest/Nest - Thermostat";
			final Long seqPath = path.get( strNodePath );
			if ( null!=seqPath ) {
				final Long seqPage = page.create( seqPath );
				page.addMap( seqPage, status.getMap(), false );
				page.setState( seqPage, now, 'A' );
				System.out.println( "Page saved, seq=" + seqPage );
			}

			System.out.println();
			Thread.sleep( 1000 * 60 * 30 );
		}
	}
}
