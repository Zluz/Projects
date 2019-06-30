package jmr.s2db.tables;

import java.util.Date;

import jmr.s2db.DataFormatter;

public class Session extends TableBase {

	private static Long seqSession = null;
//	private static Long seqSessionPage;
	
	public Long get(	final Long lDevice,
						final Date dateStart,
						final String strIP,
						final String strClass ) {
		
		final String strDate = DataFormatter.format( dateStart );
		
		final Long lSeq = super.get(	"session", 
				"seq_device = " + lDevice + " "
						+ "AND start = " + strDate,
				"seq_device, start, ip_address, class", 
				"" + lDevice + ", "
						+ strDate + ", "
						+ "'" + strIP + "', "
						+ "'" + strClass + "'" );
		seqSession = lSeq;
		return lSeq;
	}
	
	public static Long getSessionSeq() {
		return Session.seqSession;
	}
	
//	public void setSessionPageSeq( final long seqPage ) {
//		Session.seqSessionPage = seqPage;
//	}
//	
//	public void expireSession() {
//		
//	}
	

}
