package jmr.s2db.comm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jmr.pr126.comm.http.HostRegistry;
import jmr.pr126.comm.http.HttpListener;
import jmr.pr126.comm.http.HttpSender;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class Notifier {

	private static final Logger 
				LOGGER = Logger.getLogger( Notifier.class.getName() );

	
	public static final String EVENT_TABLE_UPDATE = "S2DB.table-update";

	public static final String HOME_SERVER = "HOME_SERVER";


	private static Notifier instance;
	
	private HttpSender sender = new HttpSender();
	
//	private String strLocalIP;
	
	private Notifier() {
		
		final SUProperty property = SUProperty.HOME_SERVER_IP;
		final String strIP = SystemUtil.getProperty( property );
		final String strURL = "http://" + strIP + ":" + HttpListener.PORT;
		
		try {
			final URL url = new URL( strURL );
			HostRegistry.getInstance().register( HOME_SERVER, url );
			
		} catch ( final MalformedURLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static synchronized Notifier getInstance() {
		if ( null==instance ) {
			instance = new Notifier();
		}
		return instance;
	}
	
	
	public boolean postRemoteAlias( final String strAlias,
									final String strURL ) {
		sender.postHostActivated( HOME_SERVER, strAlias, strURL);
		return true;
	}
	
	
	public boolean pushTableUpdate( final String strTable ) {
		final boolean bResult = pushTableUpdateTo( HOME_SERVER, strTable );
		return bResult;
	}

	
	public boolean pushTableUpdateTo( final String strAlias,
									  final String strTable ) {
		final Map<String,Object> map = new HashMap<>();
		map.put( "event", EVENT_TABLE_UPDATE );
		map.put( "table", strTable );
		
		LOGGER.info( ()-> "Sending notification to \"" + strAlias + "\" "
					+ "regarding update of table \"" + strTable + "\"" );
		
		final boolean bResult = sender.send( strAlias, map );
		return bResult;
	}

	
}
