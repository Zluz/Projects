package jmr.pr128.marking;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import jmr.pr128.reports.ReportColumn;

public class CheckAge implements RowMarker {

	public final static long INTERVAL_NORMAL_REFRESH = 20;
	public final static long ELAPSED_NOT_REGULAR = TimeUnit.HOURS.toMinutes( 24 );
	
	@Override
	public ReportColumn[] getRequiredColumns() {
		return new ReportColumn[] { ReportColumn.AGE_S2FS };
	}

	@Override
	public Mark evaluateMark( final Map<String,String> map ) {
		if ( null==map ) return Mark.CRITICAL;
		for ( final Entry<String, String> entry : map.entrySet() ) {
			if ( ReportColumn.AGE_S2FS.match( entry.getKey() ) ) {
				final String strField = entry.getValue();
				
				if ( null==strField || strField.isEmpty() ) {
					return Mark.SUPPRESS;
				}
				if ( '<' == strField.charAt( 0 ) ) {
					return Mark.SUPPRESS;
				}
				final int iPos = strField.indexOf( ' ' );
				if ( -1 == iPos ) {
					return Mark.SUPPRESS;
				}
				
				final String strValue = strField.substring( 0, iPos );
				try {
					final long lValue = Long.parseLong( strValue );
					
					if ( lValue < ( 2 + INTERVAL_NORMAL_REFRESH ) ) {
						return Mark.HIGHLIGHT;
					} else if ( lValue < ELAPSED_NOT_REGULAR ) {
						return Mark.CRITICAL;
					} else {
						return Mark.SUPPRESS;
					}
					
				} catch ( final NumberFormatException e ) {
					return Mark.CRITICAL;
				}
			}
		}
		return Mark.CRITICAL;
	}

}
