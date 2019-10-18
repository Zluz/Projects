package jmr.pr128.logical;

import java.sql.ResultSet;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;

import jmr.pr128.reports.ReportColumn;
import jmr.util.transform.JsonUtils;

public interface LogicalFieldEvaluation {

	public String getTitle();

	public ReportColumn[] getRequiredColumns();
	
	public ReportColumn getLocationFollowing();
	
	public Class<?> getType();
	
	public Object evaluateField( final ResultSet rs );
	
	
	/*
	 * Pretty-print (and trim down) JSON data
	 */
	public static String formatJson( final String strInput ) {
		String strValue = strInput;
		strValue = StringUtils.remove( strValue, "\"\": \"\",\n" );
		if ( strValue.startsWith( "{\n" ) && strValue.endsWith( "\n}" ) ) {
			strValue = StringUtils.removeEnd( strValue, "}" );
			strValue = StringUtils.removeStart( strValue, "{" );
			strValue = strValue.trim();
		}
		return strValue;
	}
	
	/*
	 * Pretty-print (and trim down) JSON data
	 */
	public static String formatJson( final JsonElement jeValue ) {
		if ( null==jeValue ) return "<null>";
		
		String strValue = JsonUtils.getPretty( jeValue ).trim();
		return formatJson( strValue );
	}
	
}
