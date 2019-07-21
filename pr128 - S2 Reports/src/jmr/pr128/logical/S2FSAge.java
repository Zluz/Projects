package jmr.pr128.logical;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import jmr.pr128.reports.ReportColumn;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;

public class S2FSAge implements LogicalFieldEvaluation {

	@Override
	public String getTitle() {
		return ReportColumn.AGE_S2FS.getDisplayName();
	}

	@Override
	public ReportColumn[] getRequiredColumns() {
		return new ReportColumn[] { ReportColumn.MAC };
	}

	@Override
	public ReportColumn getLocationFollowing() {
		return ReportColumn.AGE_DB;
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object evaluateField( final ResultSet rs ) {
		if ( null==rs ) return null;
		
		try {
			final String strMAC = rs.getString( ReportColumn.MAC.name() );
			final FileSession fs = 
					FileSessionManager.getInstance().getFileSession( strMAC );
			
			if ( null==fs ) {
				return "<no session>";
			}
			
			final Long lModTime = fs.getUpdateTime();
			
			if ( null!=lModTime ) {
				final long lNow = System.currentTimeMillis();
				final long lElapsed = lNow - lModTime;
				final long lMinutes = TimeUnit.MILLISECONDS.toMinutes( lElapsed );
				return "" + lMinutes + " minutes";
			} else {
				return "<null>";
			}
			
		} catch ( final SQLException e ) {
			return e.toString();
		}
	}

}
