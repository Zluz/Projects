package jmr.pr128.logical;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonObject;

import jmr.pr128.reports.ReportColumn;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;

public class DeviceReport implements LogicalFieldEvaluation {

	@Override
	public String getTitle() {
		return ReportColumn.DEVICE_REPORT.getDisplayName();
	}

	@Override
	public ReportColumn[] getRequiredColumns() {
		return new ReportColumn[] { ReportColumn.MAC };
	}

	@Override
	public ReportColumn getLocationFollowing() {
		return ReportColumn.OPTIONS;
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
			
			final JsonObject json = fs.getDeviceReport();
			
			final String strJson = LogicalFieldEvaluation.formatJson( json );
			return strJson;
			
		} catch ( final SQLException e ) {
			return e.toString();
		}
	}

}
