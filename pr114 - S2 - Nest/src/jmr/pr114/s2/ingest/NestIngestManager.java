package jmr.pr114.s2.ingest;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import jmr.S2Properties;
import jmr.SettingKey;
import jmr.pr113.FullStatus;
import jmr.pr113.Session;
import jmr.s2db.Client;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.util.NetUtil;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class NestIngestManager {

	
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
	
	
	
	final Session session;
	
	
	public NestIngestManager() {
		
		final S2Properties props = S2Properties.get();
		final char[] cUsername = props.getValue( SettingKey.NEST_USERNAME ).toCharArray();
		final char[] cPassword = props.getValue( SettingKey.NEST_PASSWORD ).toCharArray();

		System.out.println( "Creating Nest session.." );
		this.session = new Session( cUsername, cPassword );
	}
	
	public FullStatus callNest() {

		final Date now = new Date();
		
		System.out.println( "Requesting Nest (thermostat) status.." );
		final FullStatus status = session.getStatus();
		
		System.out.println( "Saving page.." );
		{
			final Path path = new Path();
			final Page page = new Page();

			final String strNodePath = "/External/Ingest/Nest - Thermostat";
			final Long seqPath = path.get( strNodePath );
			if ( null!=seqPath ) {
				final Long seqPage = page.create( seqPath );
				page.addMap( seqPage, status.getMap(), false );
				page.setState( seqPage, now, 'A' );
				System.out.println( "Page saved, seq=" + seqPage );
			} else {
				System.err.println( "Failed to get a Path seq." );
			}
		}
		
//		return status.getDeviceDetailJSON();
		return status;
	}
	
	
	
	
	public static void main( final String[] args ) throws Exception {

		System.out.println( "Registering S2 client.." );
		final String strSession = NetUtil.getSessionID();
		final String strClass = NestIngestManager.class.getName();
		Client.get().register( strSession, strClass );

		final char[] cUsername = 
				SystemUtil.getProperty( SUProperty.NEST_USERNAME ).toCharArray(); 
		final char[] cPassword = 
				SystemUtil.getProperty( SUProperty.NEST_PASSWORD ).toCharArray(); 

		System.out.println( "Creating Nest session.." );
		final Session session = new Session( cUsername, cPassword );

		
		for (;;) {
			
			final Date now = new Date();
			
			System.out.println( "Requesting Nest (thermostat) status.." );
			final FullStatus status = session.getStatus();
			
			System.out.println( "Saving page.." );
			{
				final Path path = new Path();
				final Page page = new Page();

				final String strNodePath = "/External/Ingest/Nest - Thermostat";
				final Long seqPath = path.get( strNodePath );
				if ( null!=seqPath ) {
					final Long seqPage = page.create( seqPath );
					page.addMap( seqPage, status.getMap(), false );
					page.setState( seqPage, now, 'A' );
					System.out.println( "Page saved, seq=" + seqPage );
				} else {
					System.err.println( "Failed to get a Path seq." );
				}
			}

			System.out.println();
			Thread.sleep( TimeUnit.MINUTES.toMillis( 30 ) );
		}
	}
}
