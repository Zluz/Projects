package jmr.pr128.logical;

import java.sql.ResultSet;

import jmr.pr128.reports.ReportColumn;

public interface LogicalFieldEvaluation {

	public String getTitle();

	public ReportColumn[] getRequiredColumns();
	
	public ReportColumn getLocationFollowing();
	
	public Class<?> getType();
	
	public Object evaluateField( final ResultSet rs );
	
}
