package jmr.s2db.tables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import jmr.s2db.comm.ConnectionProvider;

public class Device extends TableBase {
	
	public static Long seqDevice = null;
	public static String strName = null;
	public static final Map<String,String> mapOptions = new HashMap<>();


	public Long get(	final String strMAC,
						final String strName ) {
		final Long lSeq = super.get(	"device", 
										"mac like '" + strMAC + "'", 
										"mac, name", 
										"'" + strMAC + "', '" + strName + "'" );
		seqDevice = lSeq;
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
		
		tPage.addMap( lPage, map, true );
		
	    return seq;
	}
	
	private void loadDetails() {

		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final String strQuery = 
			 "SELECT  "
			 + "	* "
			 + "FROM  "
			 + "	device "
			 + "WHERE "
			 + "	seq = " + seqDevice + ";";
			 
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return mapOptions;
	}
	
	
}
