package jmr.pr128.logical;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
			
			// order by the best connection. wired - wireless, etc
			// this happens to be alphabetical (eth0 - wlan0 - etc)
			final List<String> list = new LinkedList<>( map.keySet() );
			Collections.sort( list );
			
			for ( final String strKey : list ) {
//			for ( final Entry<String, String> entry : map.entrySet() ) {
				final String strValue = map.get( strKey );
				sb.append( strKey );
				sb.append( ": " );
				sb.append( strValue );
				sb.append( "\n" );
			}
			
			return sb.toString();
			
		} catch ( final SQLException e ) {
			return e.toString();
		}
	}

}
