package jmr.pr115.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.comm.ConnectionProvider;

/**
 * Database queries may be run periodically and uploaded to GCS.
 */
public class ReportTable {

	private static final Logger 
			LOGGER = Logger.getLogger( ReportTable.class.getName() );

		
	public enum Format { HTML, JSON };

	private final String strTitle;
	
	private final String strQuery;
	
	public ReportTable( final String strTitle,
						final String strQuery ) {
		this.strTitle = strTitle;
		this.strQuery = strQuery;
	}
	
	
	public StringBuilder generateReport( Format format ) {
		
		final StringBuilder sb = new StringBuilder();
		
		sb.append( "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head> "
				+ "<title>" + strTitle + "</title> "
				+ "</head>\n\n"
				+ "<body>\n"
				+ "<table width='100%' border='1'>\n" );
		
		try ( final Connection conn = ConnectionProvider.get().getConnection();
			  final Statement stmt = conn.createStatement();
			  final ResultSet rs = stmt.executeQuery( strQuery ) ) {
			
			final ResultSetMetaData md = rs.getMetaData();
			
			sb.append( "<tr>\n" );
			final int iColCount = md.getColumnCount();
			for ( int i=1; i<=iColCount; i++ ) {
				final String strLabel = md.getColumnLabel( i );
				sb.append( "\t<th> " + strLabel + " </th>\n" );
			}
			sb.append( "</tr>\n" );
			
			while ( rs.next() ) {
				sb.append( "<tr>\n" );
				for ( int i=1; i<=iColCount; i++ ) {
					sb.append( "\t<td> " );
					final String strField = rs.getString( i );
					sb.append( strField );
					sb.append( " </td>\n" );
				}
				sb.append( "</tr>\n" );
			}

			sb.append( "</table>\n" );
			sb.append( "</body>\n"
					+ "</html>" );
			return sb;
			
		} catch ( final SQLException e ) {
			final String strMessage = "Failed to generate report. "
							+ "Encountered " + e.toString();
			LOGGER.log( Level.SEVERE, strMessage, e );
			
			sb.setLength( 0 );
			sb.append( strMessage );
		}

		return sb;
	}
	
	
	public static void main( final String[] args ) {
		final ReportTable report = new ReportTable( 
				ReportTable.class.getName() + " main",
				"SELECT VERSION();" );
		final StringBuilder sb = report.generateReport( Format.HTML );
		System.out.println( sb.toString() );
	}	
	
}
