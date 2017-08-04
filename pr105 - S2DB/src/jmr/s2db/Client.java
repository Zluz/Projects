package jmr.s2db;

import java.util.Date;

import jmr.s2db.tables.Device;
import jmr.s2db.tables.Session;
import jmr.s2db.tables.Tables;

public class Client {


	private static Client instance;
	
	private Client() {};
	
	
	public static synchronized Client get() {
		if ( null==instance ) {
			instance = new Client();
		}
		return instance;
	}
	
	
	public long register(	final String strMAC,
							final String strName,
							final Date now ) {
		
		final Device tDevice = ( (Device)Tables.DEVICE.get() );
		final Long lDevice = tDevice.get( strMAC, strName );
		
		final Session tSession = ( (Session)Tables.SESSION.get() );
		final Long lSession = tSession.get( lDevice, now );
		
		return lSession.longValue();
	}
	
}
