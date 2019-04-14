package jmr.s2db.comm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import jmr.pr126.comm.http.HostRegistry;
import jmr.pr126.comm.http.HttpListener;
import jmr.pr126.comm.http.HttpSender;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class Notifier {

	public static final String EVENT_TABLE_UPDATE = "S2DB.table-update";

	public static final String HOME_SERVER = "HOME_SERVER";


	private static Notifier instance;
	
	private HttpSender sender = new HttpSender();
	
	
	private Notifier() {
		
		final SUProperty property = SUProperty.HOME_SERVER_IP;
		final String strIP = SystemUtil.getProperty( property );
		final String strURL = "http://" + strIP + ":" + HttpListener.PORT;
		
		try {
			final URL url = new URL( strURL );
			HostRegistry.getInstance().register( HOME_SERVER, url );
			
		} catch (MalformedURLException e) {
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
	
	
	public boolean pushTableUpdate( final String strTable ) {
		final Map<String,Object> map = new HashMap<>();
		map.put( "event", EVENT_TABLE_UPDATE );
		map.put( "table", strTable );
		
		final boolean bResult = sender.send( HOME_SERVER, map );
		return bResult;
	}
	
	
}
