package jmr.pr128.logical;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import jmr.pr128.reports.ReportColumn;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSessionManager;

public class IPDetails implements LogicalFieldEvaluation {

	@Override
	public String getTitle() {
		return ReportColumn.IP_ADDRESSES.getDisplayName();
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
			
			final Map<String, String> map = fs.getIPs();
			
			final StringBuilder sb = new StringBuilder();
			for ( final Entry<String, String> entry : map.entrySet() ) {
				sb.append( entry.getKey() );
				sb.append( ": " );
				sb.append( entry.getValue() );
				sb.append( "\n" );
			}
			
			return sb.toString();
			
		} catch ( final SQLException e ) {
			return e.toString();
		}
	}

}
