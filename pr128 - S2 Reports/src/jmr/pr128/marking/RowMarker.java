package jmr.pr128.marking;

import java.util.Map;

import jmr.pr128.reports.ReportColumn;

public interface RowMarker {

	public ReportColumn[] getRequiredColumns();
	
	public Mark evaluateMark( final Map<String,String> map );
	
}
