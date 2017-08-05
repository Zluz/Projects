package jmr.s2db.tables;

import java.util.Date;

public class Session extends TableBase {

	public Long get(	final Long lDevice,
						final Date dateStart,
						final String strIP,
						final String strClass ) {
		
		final String strDate = TableBase.format( dateStart );
		
		final Long lSeq = super.get(	"session", 
				"seq_device = " + lDevice + " "
						+ "AND start = " + strDate,
				"seq_device, start, ip_address, class", 
				"" + lDevice + ", "
						+ strDate + ", "
						+ "'" + strIP + "', "
						+ "'" + strClass + "'" );
		return lSeq;
	}

}
