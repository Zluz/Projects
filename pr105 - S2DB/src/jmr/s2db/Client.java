package jmr.s2db;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

import jmr.pr126.comm.http.HttpListener;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.comm.Notifier;
import jmr.s2db.event.EventMonitor;
import jmr.s2db.event.EventType;
import jmr.s2db.event.SystemEvent;
import jmr.s2db.imprt.SummaryRegistry;
import jmr.s2db.job.JobManager;
import jmr.s2db.tables.Device;
import jmr.s2db.tables.Event;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.s2db.tables.Session;
import jmr.s2db.tables.Tables;
import jmr.util.NetUtil;
import jmr.util.report.TraceMap;

public class Client {


	private static final Logger 
			LOGGER = Logger.getLogger( Client.class.getName() );

	
	public enum ClientType {
	
			TILE_GUI( 8090 ),
			TRAY_GUI( 8092 ),
			TEST( 8094 ),
			;
		
		private final int iPort;
		
		private ClientType( final int iPort ) {
			this.iPort = iPort;
		}
		
		public int getPort() {
			return this.iPort;
		}
	};

	
	
	private static Client instance;
	
	private Client() {};
	
	private Long seqDevice;
	private Long seqSession;
	
	// allow other instances of the program.
	// this means do not fail if ports cannot be reserved
	private boolean bDebug = false;
	
	
	
	
	public static synchronized Client get() {
		if ( null==instance ) {
			new S2DBLogHandler();
			instance = new Client();
		}
		return instance;
	}
	
	public void setDebug( final boolean bDebug ) {
		this.bDebug = bDebug;
	}
	
	public boolean isDebugEnabled() {
		return this.bDebug;
	}
	

	public Long register(	final ClientType type,
							final String strName,
							final String strClass ) {
		final Long lResult = this.register( type, strName, strClass, false );
		return lResult;
	}
	
	
	public Long register(	final ClientType type,
						    final String strName,
							final String strClass,
							final boolean bQuiet ) {
		final Date now = new Date();
		final long lNow = System.currentTimeMillis();
	    final String strMAC = NetUtil.getMAC();
	    final Map<String, String> mapNICs = NetUtil.getIPAddresses();
	    final String strIP = NetUtil.getIPAddress();
	    
    	final EventMonitor monitor = EventMonitor.get( type );
    	if ( null==monitor ) {
    		LOGGER.warning( ()-> "Failed to initialize EventMonitor." );
    		return null;
    	}
	    
	    String strRegex = strMAC.replaceAll( "-", "." );
	    strRegex = "/Sessions/.+" + strRegex + ".+";
	    
	    final long seqSession = 
	    		this.register( strMAC, strIP, mapNICs, 
	    				strName, strClass, strRegex, now );
	    
	    final JsonObject jo = new JsonObject();
	    jo.addProperty( "IP", strIP );
	    jo.addProperty( "name", strName );
	    jo.addProperty( "class", strClass );
	    jo.addProperty( "MAC", strMAC );
	    final String strData = jo.toString();
	    
	    if ( ! bQuiet ) {
			final Event event = Event.add(
					EventType.SYSTEM, 
					SystemEvent.CLIENT_REGISTERED.name(), 
					strIP, null, 
					strData, lNow, null, null, null );
			System.out.println( "Client registered. "
					+ "Session " + seqSession + ", "
					+ "Event " + event.getEventSeq() + "." );
	    } else {
			System.out.println( "Client registered. "
					+ "Session " + seqSession );
	    }
		
	    return seqSession;
	}
	
	
	public long register(	final String strMAC,
							final String strIP,
							final Map<String,String> mapNICs,
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
		seqDevice = tDevice.register( strMAC, strName, strIP, mapNICs );
		
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
		
		// can get this exception if in debug:
		// 		java.lang.IllegalStateException: Client not registered. 
		// 			Call Client.get().register(*) first.
		if ( bDebug ) {
			return Collections.emptyMap();
		}
		
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


	public boolean registerAsRemote( final ClientType type,
									 final String strRemoteName, 
								  	 final String strIP ) {
		if ( StringUtils.isBlank( strRemoteName ) ) return false;
		if ( StringUtils.isBlank( strIP ) ) return false;
		
		LOGGER.info( ()-> "Client.registerAsRemote(), "
									+ "strRemoteName = " + strRemoteName );
		
		HttpListener.getInstance( type.iPort ).setIP( strIP );

		final long lTime = System.currentTimeMillis();
		final String strURL = 
					HttpListener.getInstance( type.iPort ).getHostedURL();
		final TraceMap map = new TraceMap( true );
		map.put( "URL", strURL );
		map.put( "REMOTE_NAME", strRemoteName );
		
		final Event event = Event.add(	EventType.SYSTEM, 
										SystemEvent.LISTENER_ACTIVATED.name(), 
										strIP, null, map, lTime, 
										null, null, null );
		LOGGER.fine( ()-> "Registered as Remote, "
						+ "event seq " + event.getEventSeq() );
		

		
		Notifier.getInstance().postRemoteAlias( strRemoteName, strURL );
		
		return true;
	}
	
	
	public Map<String,String> getDetails() {
		final Map<String,String> map = new HashMap<>();
		map.put( "device_seq", Long.toString( this.getDeviceSeq() ) );
		map.put( "session_seq", Long.toString( this.getSessionSeq() ) );
		map.put( "net_mac", NetUtil.getMAC() );
		map.put( "net_ip", NetUtil.getIPAddress() );
		map.put( "process_name", NetUtil.getProcessName() );
		return map;
	}
	
	
}
