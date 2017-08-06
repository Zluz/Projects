package jmr.s2db;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.tables.Device;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.s2db.tables.Session;
import jmr.s2db.tables.Tables;

public class Client {


	private static final Logger 
			LOGGER = Logger.getLogger( Client.class.getName() );

	private static Client instance;
	
	private Client() {};
	
	private Long seqDevice;
	private Long seqSession;
	
	
	public static synchronized Client get() {
		if ( null==instance ) {
			new S2DBLogHandler();
			instance = new Client();
		}
		return instance;
	}
	
	
	
	public long register(	final String strMAC,
							final String strIP,
							final String strName,
							final String strClass,
							final Date now ) {
		
		final Device tDevice = ( (Device)Tables.DEVICE.get() );
		seqDevice = tDevice.get( strMAC, strName );
		
		final Session tSession = ( (Session)Tables.SESSION.get() );
		seqSession = tSession.get( seqDevice, now, strIP, strClass );
		
		new S2DBLogHandler();
		
		return seqSession.longValue();
	}
	
	private Long seqSessionPage = null;
	
	public void setSessionPage( final Long seqPage ) {
		this.seqSessionPage = seqPage;
	}
	
	
	public void close() {
		final Date now = new Date();
		if ( null!=seqSessionPage ) {
			new Page().setState( seqSessionPage, now, 'E' );
		}
	}
	
	
	public Long getDeviceSeq() {
		return this.seqDevice;
	}
	
	public Long getSessionSeq() {
		if ( null==this.seqSession ) {
			throw new IllegalStateException( "Client not registered." );
		}
		return this.seqSession;
	}
	
	
	public Long savePage(	final String strPath,
							final Map<String,String> map ) {
		if ( null==strPath ) return null;
		
		final Path tPath = ( (Path)Tables.PATH.get() );
		final Long lPath = tPath.get( strPath );
		
		final Page tPage = ( (Page)Tables.PAGE.get() );
		final Long lPage = tPage.get( lPath );
		
		tPage.addMap( lPage, map );
		
		LOGGER.log( Level.INFO, 
				"New page saved (seq=" + lPage+ "): " + strPath );

		return lPage;
	}
	
}
