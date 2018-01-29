package jmr.s2db;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.imprt.SummaryRegistry;
import jmr.s2db.job.JobManager;
import jmr.s2db.tables.Device;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.s2db.tables.Session;
import jmr.s2db.tables.Tables;
import jmr.util.NetUtil;

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
	

	public long register(	final String strName,
							final String strClass ) {
		final Date now = new Date();
	    final String strMAC = NetUtil.getMAC();
	    final String strIP = NetUtil.getIPAddress();
	    
	    String strRegex = strMAC.replaceAll( "-", "." );
	    strRegex = "/Sessions/.+" + strRegex + ".+";
	    
	    final long seqSession = 
	    		this.register( strMAC, strIP, strName, strClass, strRegex, 
	    					now );
	    
	    return seqSession;
	}
	
	
	public long register(	final String strMAC,
							final String strIP,
							final String strName,
							final String strClass,
							final String strExpireRegex,
							final Date now ) {

		System.out.println( "Registering: "
						+ "IP=" + strIP + ", "
						+ "Name=" + strName + ", "
						+ "Class=" + strClass );

		final Device tDevice = ( (Device)Tables.DEVICE.get() );
//		seqDevice = tDevice.get( strMAC, strName );
		seqDevice = tDevice.register( strMAC, strName, strIP );
		
		final Page tPage = ( (Page)Tables.PAGE.get() );
		tPage.expireAll( strExpireRegex );
		
		final Session tSession = ( (Session)Tables.SESSION.get() );
		seqSession = tSession.get( seqDevice, now, strIP, strClass );
		
//		System.out.println( "Session seq " + seqSession );
		
		new S2DBLogHandler();

//		tDevice.register( strMAC, strName, strIP );

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
		ConnectionProvider.get().close();
	}
	
	
	public Long getDeviceSeq() {
		return this.seqDevice;
	}
	
	public Long getSessionSeq() {
		if ( null==this.seqSession ) {
			throw new IllegalStateException( "Client not registered. "
					+ "Call Client.get().register(*) first." );
		}
		return this.seqSession;
	}
	
	
	
	public static final String PATH_VARIABLES = "/var/global";
	
	public String getString( final String strName ) {
		final Map<String, String> map = loadPage( PATH_VARIABLES );
		if ( null==map ) return null;
		return map.get( strName );
	}
	
	public void setString(	final String strName,
							final String strValue ) {
		final Map<String, String> map = loadPage( PATH_VARIABLES );
		if ( null==map ) throw new IllegalStateException( "null map" );
		map.put( strName, strValue );
	}
	
	
	public Long savePage(	final String strPath,
							final Map<String,String> map ) {
		if ( null==strPath ) return null;
		
		final Date now = new Date();
		
		final Path tPath = ( (Path)Tables.PATH.get() );
		final Long lPath = tPath.get( strPath );
		
		final Page tPage = ( (Page)Tables.PAGE.get() );
//		final Long lPage = tPage.get( lPath );
		final Long lPage = tPage.create( lPath );

		if ( null!=lPage ) {

			SummaryRegistry.get().summarize( strPath, map );
			
			tPage.addMap( lPage, map, true );
			tPage.setState( lPage, now, 'A' );
			LOGGER.log( Level.INFO, 
					"New page saved (seq=" + lPage+ "): " + strPath );
		}

		return lPage;
	}
	
	public Map<String,String> loadPage( final String strPath ) {
		if ( null==strPath ) return Collections.emptyMap();
		
		final Path tPath = ( (Path)Tables.PATH.get() );
		final Long lPath = tPath.get( strPath );
		if ( null==lPath ) return Collections.emptyMap();
		
		final Page tPage = ( (Page)Tables.PAGE.get() );
		final Long lPage = tPage.get( lPath );
		if ( null==lPage ) return Collections.emptyMap();

//		System.out.println( "Client.loadPage() - "
//				+ "path " + lPath + ", page " + lPage + ": " + strPath );
		
		final Map<String, String> map = tPage.getMap( lPage );
		return map;
	}


	public Device getThisDevice() {
		final Device tDevice = ( (Device)Tables.DEVICE.get() );
		return tDevice;
	}
	
	public JobManager getJobManager() {
		final JobManager manager = JobManager.getInstance();
		return manager;
	}
	
}
