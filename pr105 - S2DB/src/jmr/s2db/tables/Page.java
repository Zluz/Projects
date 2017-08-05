package jmr.s2db.tables;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import jmr.s2db.Client;
import jmr.s2db.comm.ConnectionProvider;

public class Page extends TableBase {

	public Long get( final long seqPath ) {
		
		final Long lSession = Client.get().getSessionSeq();
		if ( null==lSession ) return null;
		
		final Long lSeq = super.get(	"page", 
					"seq_path = " + seqPath + " AND seq_session = " + lSession, 
					"seq_path, seq_session", 
					"" + seqPath + ", " + lSession );
		return lSeq;
	}
	
	public void addMap(	final Long seqPage,
						final Map<String,String> map ) {
		if ( null==seqPage ) return;
		if ( null==map ) return;

		try ( final Statement 
				stmt = ConnectionProvider.get().getStatement() ) {

			String strInsert = "INSERT INTO prop "
					+ "( seq_page, name, value ) "
					+ "VALUES ";
			
			for ( final Entry<String, String> entry : map.entrySet() ) {
				final String strKey = entry.getKey();
				final String strValue = entry.getValue();
				
				strInsert += "( " + seqPage + ","
							+ " '" + strKey + "',"
							+ " '" + strValue + "' ),";
			}
			
			strInsert = strInsert.substring( 0, strInsert.length() - 1 ) + ";";
			
			stmt.executeUpdate( strInsert );

			final Date now = new Date();
			
			final String strUpdate = "UPDATE page "
					+ "SET last_modified=" + TableBase.format( now ) + ", "
							+ "state='A' "
					+ "WHERE seq=" + seqPage + ";";

			stmt.executeUpdate( strUpdate );

			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
