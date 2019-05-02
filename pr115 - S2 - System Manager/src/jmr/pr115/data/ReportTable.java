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
				+ "<title>" + strTitle + "</title>\n"
				+ "<style>\n\n\n"
				+ ""
				+ ""
				+ "\r\n" + 
				"\r\n" +  // table style created at:
				"\r\n" +  //    https://divtable.com/table-styler/
				"table.table-blue {\r\n" + 
				"  font-family: \"Lucida Sans Unicode\", \"Lucida Grande\", sans-serif;\r\n" + 
				"  border: 1px solid #1C6EA4;\r\n" + 
				"  background-color: #F4F4F4;\r\n" + 
				"  width: 100%;\r\n" + 
				"  text-align: left;\r\n" + 
				"  border-collapse: collapse;\r\n" + 
				"}\r\n" + 
				"table.table-blue td, table.table-blue th {\r\n" + 
				"  border: 1px solid #FFFFFF;\r\n" + 
				"  padding: 1px 3px;\r\n" + 
				"}\r\n" + 
				"table.table-blue tbody td {\r\n" + 
				"  font-size: 13px;\r\n" + 
				"}\r\n" + 
				"table.table-blue tr:nth-child(even) {\r\n" + 
				"  background: #D0E4F5;\r\n" + 
				"}\r\n" + 
				"table.table-blue thead {\r\n" + 
				"  background: #1C6EA4;\r\n" + 
				"  background: -moz-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n" + 
				"  background: -webkit-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n" + 
				"  background: linear-gradient(to bottom, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n" + 
				"  border-bottom: 2px solid #444444;\r\n" + 
				"}\r\n" + 
				"table.table-blue thead th {\r\n" + 
				"  font-size: 17px;\r\n" + 
				"  font-weight: bold;\r\n" + 
				"  color: #FFFFFF;\r\n" + 
				"  border-left: 2px solid #D0E4F5;\r\n" + 
				"}\r\n" + 
				"table.table-blue thead th:first-child {\r\n" + 
				"  border-left: none;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"table.table-blue tfoot {\r\n" + 
				"  font-size: 14px;\r\n" + 
				"  font-weight: bold;\r\n" + 
				"  color: #FFFFFF;\r\n" + 
				"  background: #D0E4F5;\r\n" + 
				"  background: -moz-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n" + 
				"  background: -webkit-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n" + 
				"  background: linear-gradient(to bottom, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n" + 
				"  border-top: 2px solid #444444;\r\n" + 
				"}\r\n" + 
				"table.table-blue tfoot td {\r\n" + 
				"  font-size: 14px;\r\n" + 
				"}\r\n" + 
				"table.table-blue tfoot .links {\r\n" + 
				"  text-align: right;\r\n" + 
				"}\r\n" + 
				"table.table-blue tfoot .links a{\r\n" + 
				"  display: inline-block;\r\n" + 
				"  background: #1C6EA4;\r\n" + 
				"  color: #FFFFFF;\r\n" + 
				"  padding: 2px 8px;\r\n" + 
				"  border-radius: 5px;\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				""
				+ ""
				+ ""
				+ "\n\n\n</style></head>\n\n"
				+ "<body>\n"
				+ "<table class=\"table-blue\" width=\"100%\">\n" );
		
		try ( final Connection conn = ConnectionProvider.get().getConnection();
			  final Statement stmt = conn.createStatement();
			  final ResultSet rs = stmt.executeQuery( strQuery ) ) {
			
			final ResultSetMetaData md = rs.getMetaData();
			
			sb.append( "<thead><tr>\n" );
			final int iColCount = md.getColumnCount();
			for ( int i=1; i<=iColCount; i++ ) {
				final String strLabel = md.getColumnLabel( i );
				sb.append( "\t<th> " + strLabel + " </th>\n" );
			}
			sb.append( "</tr></thead>\n<tbody>\n" );
			
			while ( rs.next() ) {
				sb.append( "<tr>\n" );
				for ( int i=1; i<=iColCount; i++ ) {
					sb.append( "\t<td> " );
					final String strFieldRaw = rs.getString( i );
					String strFieldNorm = ( strFieldRaw != null ) 
									? strFieldRaw.trim() : "<null>";
//					strFieldNorm = StringUtils.replaceAll( 
//										strFieldNorm, "\\\\n", "<BR>" );
					strFieldNorm = strFieldNorm.replaceAll( "\\n", "<BR>" );
					sb.append( strFieldNorm );
					sb.append( " </td>\n" );
				}
				sb.append( "</tr>\n" );
			}

			sb.append( "</tbody>\n</table>\n" );
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
