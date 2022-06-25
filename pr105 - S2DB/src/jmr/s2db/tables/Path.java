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

import jmr.s2db.Settings;
import jmr.s2db.comm.ConnectionProvider;

public class Path extends TableBase {


	private static final Logger 
			LOGGER = Logger.getLogger( Page.class.getName() );

		
	
	
	public Long get( final String strName ) {
		final Long lSeq = super.get(	"Path", 
										"name like '" + strName + "'", 
										"name", 
										"'" + strName + "'" );
		return lSeq;
	}

	

	/**
	 * 
	 * @param strParentPath ex:"/Sessions/"
	 * @param bFirstChildrenOnly
	 * @return 
	 */
	public Map<String, Long> getChildPages(	final String strParentPath,
											final boolean bFirstChildrenOnly ) {
		if ( ! Settings.SQL_ENABLED ) return Collections.emptyMap();

		final String strQuery = 
				 "SELECT " + 
				 "  page.seq as page_seq, " + 
				 "  path.name as path_name " + 
				 "FROM " + 
				 "  path," + 
				 "  page " + 
				 "WHERE" + 
				 "	( path.seq = page.seq_path )" + 
				 "	AND ( name like '" + strParentPath + "%' )" + 
				 "	AND ( page.state = 'A' );";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

			final Map<String,Long> map = new HashMap<>();
			
			stmt.executeQuery( strQuery );

			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				while ( rs.next() ) {
					try {
						final String strPageSeq = rs.getString( "page_seq" );
						final String strPathName = rs.getString( "path_name" );
						final long lPageSeq = Long.parseLong( strPageSeq );
						map.put( strPathName, lPageSeq );
					} catch ( final Exception e ) {
						// ignore..
					}
				}
			}
			
			return map;
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Query: " + strQuery, e );
		}
		return null;
	}
	
	
	
	
	
}
