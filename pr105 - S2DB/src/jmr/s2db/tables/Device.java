package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.imprt.SummaryRegistry;

public class Device extends TableBase {
	
	private static final Logger 
			LOGGER = Logger.getLogger( Device.class.getName() );

	private static Device deviceThis = null;
	
	private static Long seqDevice = null;
	private static String strName = null;
	private static final Map<String,String> mapOptions = new HashMap<>();


	public Long get(	final String strMAC,
						final String strName ) {
		final Long lSeq = super.get(	"device", 
										"mac like '" + strMAC + "'", 
										"mac, name", 
										"'" + strMAC + "', '" + strName + "'" );
		seqDevice = lSeq;
		final Device device = new Device();
		deviceThis = device;
		return lSeq;
	}
	
	public Long register(	final String strMAC,
							final String strName,
							final String strIP ) {
		final Long seq = this.get( strMAC, strName );
		
	    final String strPath = "/var/Device/" + strMAC;
	    final Map<String,String> map = new HashMap<>();
	    map.put( "ip", strIP );
	    map.put( "name", strName );
//	    Client.get().savePage( strPath, map );
	    
		final Path tPath = ( (Path)Tables.PATH.get() );
		final Long lPath = tPath.get( strPath );
		
		final Page tPage = ( (Page)Tables.PAGE.get() );
//		final Long lPage = tPage.get( lPath );
		final Long lPage = tPage.getNoSession( lPath );
		
		SummaryRegistry.get().summarize( strPath, map );

		tPage.addMap( lPage, map, true );
		
	    return seq;
	}
	
	private void loadDetails() {

		final String strQuery = 
		 "SELECT  "
		 + "	* "
		 + "FROM  "
		 + "	device "
		 + "WHERE "
		 + "	seq = " + seqDevice + ";";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			stmt.executeQuery( strQuery );

			mapOptions.clear();
			String strOptions = null;
			
			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				while ( rs.next() ) {
					strName = rs.getString( "name" );
					strOptions = rs.getString( "options" );
				}
			}
			
			if ( null!=strOptions ) {
				for ( final String strLine : strOptions.split( "\\n" ) ) {
					final String[] parts = strLine.split( "=" );
					if ( parts.length > 1 ) {
						mapOptions.put( parts[0], parts[1] );
					}
				}
			}
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query: " + strQuery, e );
		}
	}
	
	public String getName() {
		if ( null==strName ) {
			loadDetails();
		}
		return strName;
	}
	
	public Map<String,String> getOptions() {
		if ( null==mapOptions ) {
			loadDetails();
		}
		return Collections.unmodifiableMap( mapOptions );
	}
	
	public static Device getDevice( final long seqDevice ) {
		return null; //TODO
	}
	
	public static Device getThisDevice() {
		return deviceThis;
	}
	
	
}
