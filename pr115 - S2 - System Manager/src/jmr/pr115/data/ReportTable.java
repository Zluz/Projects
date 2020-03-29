package jmr.pr115.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.pr128.logical.LogicalFieldEvaluation;
import jmr.pr128.marking.Mark;
import jmr.pr128.marking.RowMarker;
import jmr.pr128.reports.Report;
import jmr.pr128.reports.ReportColumn;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.tables.Event;
import jmr.util.transform.JsonUtils;

/**
 * Database queries may be run periodically and uploaded to GCS.
 */
public class ReportTable {

	private static final Logger 
			LOGGER = Logger.getLogger( ReportTable.class.getName() );

		
	public enum Format { HTML, JSON };

	private final String strTitle;
	
	private final String strFixedQuery;
	private final Report report;
	
	public ReportTable( final String strTitle,
						final String strQuery ) {
		this.strTitle = strTitle;
		this.report = null;
		this.strFixedQuery = strQuery;
	}

	public ReportTable( final Report report ) {
		this.strTitle = report.getTitle();
		this.report = report;
		this.strFixedQuery = null;
	}

	

	// not used? 'mac' column does not appear
	private String formatFirstLine( final String str ) {
		if ( str.toUpperCase().matches( "[0-9A-F]{2}-[0-9A-F]{2}"
				+ "-[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}" ) ) {
			final String strResult = str.replaceAll( "\\-", "&#8209;" );
			return strResult.toUpperCase();
		}
		return str;
	}

	
	public StringBuilder generateReport( final Format format, 
										 final Event event, 
									 	 final String strReason, 
										 final long lNow ) {
		
		LOGGER.info( "Generating report (format = " + format.toString() + ")" );
		
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
				"\n\n\n</style></head>\n\n"
				+ ""
				+ ""
				+ "<script type=\"text/javascript\">"
				+ "\r\n" + 
				"function toggleRowExpansion( row ) {\r\n" + 
				"	const strExpanded = row.dataset.expanded;\r\n" + 
				"\r\n" + 
				"	const cells = row.children;\r\n" + 
				"	for ( var i=0; i<cells.length; i++ ) {\r\n" + 
				"		const cell = cells[i];\r\n" + 
				"		var strText;\r\n" + 
				"		if ( '1' == strExpanded ) {\r\n" + 
				"			strText = cell.dataset.summary;\r\n" + 
				"		} else {\r\n" + 
				"			strText = cell.dataset.detail;\r\n" + 
				"		}\r\n" + 
				"		if ( undefined === strText ) {\r\n" + 
				"			// skip\r\n" + 
				"		} else {\r\n" + 
				"			cell.innerHTML = strText;\r\n" + 
				"		}\r\n" + 
				"	}\r\n" + 
				"\r\n" + 
				"	if ( '1' == strExpanded ) {\r\n" + 
				"		row.dataset.expanded = '0';\r\n" + 
				"	} else {\r\n" + 
				"		row.dataset.expanded = '1';\r\n" + 
				"	}\r\n" + 
				"}\r\n" + 
				"</script>"
				+ ""
				+ ""
				+ ""
				+ "" +
				"<body>\n" +
				"" );

		final String strQuery;
		final List<LogicalFieldEvaluation> listLFEs;
		
		if ( null!=report ) {
			strQuery = report.getSQL();
			listLFEs = report.getLogicalFieldEvaluations();
		} else {
			strQuery = strFixedQuery;
			listLFEs = null;
		}
		
		sb.append( "" 
				+ "<table class=\"table-blue\" width=\"100%\">\n"
				+ "<tr><th>Report</th><td>" + strTitle + "</td></tr>\r\n"
				+ "<tr><th>Time</th><td>" + new Date( lNow ) + "</td></tr>\r\n" );
		
		if ( null!=event ) {
			sb.append( "<tr><th>Event</th><td>" + event + "</td></tr>\r\n" );
			sb.append( "<tr><th>Event map</th><td>" 
					+ event.getDataAsMap()  + "</td></tr>\r\n" );
		} else {
			sb.append( "<tr><th>Event</th><td> &lt;null&gt; </td></tr>\r\n" );
		}
		sb.append( "" 
				+ "<tr><th>Reason</th><td>" + strReason + "</td></tr>\r\n"
				+ "</table>\n<br><br>\r\n"
				);

		sb.append( ""
				+ "<table class=\"table-blue\" width=\"100%\">\n" );
		
		try ( final Connection conn = ConnectionProvider.get().getConnection();
			  final Statement stmt = conn.createStatement();
			  final ResultSet rs = stmt.executeQuery( strQuery ) ) {
			
			final ResultSetMetaData md = rs.getMetaData();
			
			sb.append( "<thead><tr>\n" );
			final int iColCount = md.getColumnCount();
			final List<String> listHeaders = new LinkedList<>();
			for ( int i=1; i<=iColCount; i++ ) {
				final String strLabel = md.getColumnLabel( i );
				listHeaders.add( strLabel );
			}

			final Map<String,LogicalFieldEvaluation> mapLFEs = new HashMap<>();
			if ( null!=listLFEs ) {
				Collections.reverse( listLFEs );
				for ( int iHeader = listHeaders.size() - 1; iHeader > -1; iHeader-- ) {
					final String strHeader = listHeaders.get( iHeader );

					for ( final LogicalFieldEvaluation lfe : listLFEs ) {
						final ReportColumn column = lfe.getLocationFollowing();
						if ( column.match( strHeader ) ) {
							
							final String strLFETitle = lfe.getTitle();
							listHeaders.add( iHeader, strLFETitle );
							mapLFEs.put( strLFETitle, lfe );
						}
					}
				}
			}

			for ( final String strHeader : listHeaders ) {
				sb.append( "\t<th> " + strHeader + " </th>\n" );
			}
			sb.append( "</tr></thead>\n<tbody>\n" );
			
			while ( rs.next() ) {

//				final List<String> listFields = new LinkedList<>();
				final Map<String,String> mapFields = new HashMap<>();
				
				for ( final String strHeader : listHeaders ) {
					
					final LogicalFieldEvaluation lfe = mapLFEs.get( strHeader );
					if ( null!=lfe ) {
						try {
							final Object objValue = lfe.evaluateField( rs );
							final String strValue;
							if ( null!=objValue ) {
								strValue = objValue.toString();
							} else {
								strValue = "<null>";
							}
//							listFields.add( strValue );
							mapFields.put( strHeader, strValue );
						} catch ( final Exception e ) {
//							listFields.add( e.toString() );
							mapFields.put( strHeader, e.toString() );
							e.printStackTrace();
						}
					} else {
						final String strFieldRaw = rs.getString( strHeader );
//						listFields.add( strFieldRaw );
						mapFields.put( strHeader, strFieldRaw );
					}
				}
				
				final Mark mark;
				final RowMarker marker;
				if ( null==report ) {
					mark = Mark.NORMAL;
				} else {
					marker = report.getRowMarker();
					if ( null!=marker ) {
						mark = marker.evaluateMark( mapFields );
					} else {
						mark = Mark.NORMAL;
					}
				}
				
				sb.append( "<tr style='" + mark.getHtmlStyle() + "' "
								+ "onClick='toggleRowExpansion( this );' "
								+ "data-expanded='0' >\n" );
				
				for ( final String strHeader : listHeaders ) {
					final String strFieldRaw = mapFields.get( strHeader );

					String strFieldNorm;
					if ( strFieldRaw != null ) {
						strFieldNorm = strFieldRaw.trim();
						strFieldNorm = strFieldNorm.replaceAll( "<BR>", "\n" );
						strFieldNorm = strFieldNorm.replaceAll( "<br>", "\n" );
					} else {
						strFieldNorm = "<null>";
					}
							
					if ( strHeader.contains( "json" ) ) {
						strFieldNorm = JsonUtils.getPretty( strFieldNorm );
						strFieldNorm = LogicalFieldEvaluation
												.formatJson( strFieldNorm );
					}

					strFieldNorm = strFieldNorm.replaceAll( "<", "&lt;" );
					strFieldNorm = strFieldNorm.replaceAll( ">", "&gt;" );

					final String[] strLines = strFieldNorm.trim().split( "\\n" );
					final int iLineCount = strLines.length;
					final String strSummary;
					if ( iLineCount > 1 ) {
						
						final String strFirstLine = 
												formatFirstLine( strLines[0] );
						
//						strSummary = "<font color='silver' size='2'>[+" + iLineCount + "]</font>"
						strSummary = "<small>[+" + iLineCount + "]</small>"
										+ "&nbsp;&nbsp;" + strFirstLine;

						strFieldNorm = strFieldNorm.replaceAll( "\\n", "<BR>" );

						sb.append( "\t<td data-detail='" + strFieldNorm + "' "
								+ "data-summary='" + strSummary + "'> " );
						
						sb.append( strSummary );
					} else {

						sb.append( "\t<td> " );
						sb.append( strFieldNorm );
					}

//					sb.append( "\t<td> " );
//					sb.append( strFieldNorm );
					sb.append( " </td>\n" );
				}
				sb.append( "</tr>\n" );

			}

			sb.append( "</tbody>\n</table>\n" );
			sb.append( "</body>\n"
					+ "</html>" );
			return sb;
			
		} catch ( final SQLException e ) {
			final String strMessage = "Failed to generate report.\n"
							+ "SQL: " + strQuery + "\n"
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
//				"SELECT VERSION();"
//				"SELECT VERSION(), 1,2,3,4, '' + NOW() + '\\n' + NOW();"
				"SELECT "
					+ "table_schema, "
					+ "table_name, "
					+ "REPLACE( table_name, '_', '\\n' ) "
				+ "FROM sys.schema_table_statistics;"
				);
		final long lNow = System.currentTimeMillis();
		final StringBuilder sb = report.generateReport( 
										Format.HTML, null, "Reason", lNow );
		System.out.println( sb.toString() );
	}	
	
}
