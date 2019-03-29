package jmr.pr126.comm.http;


import java.net.URLEncoder;


// from
// StarHost:jmr/home/comm/http/URLReader.java
public class HttpSender {
	
	public enum Type { 
		CLICK, TEXTEDIT, 
		SHUTDOWN, 
	};

	private static HttpSender instance;

	private String strHost;

	
	
	public static HttpSender get() {
		if ( null==instance ) {
			instance = new HttpSender();
		}
		return instance;
	}
	
	
	public void setHost( final String strHost ) {
		if ( null==strHost ) return;
		if ( strHost.isEmpty() ) return;
		this.strHost = strHost;
	}
	
	
	public boolean send(	final Type type,
							final String strID,
							final String strData ) {
		if ( null==strHost ) return false;
		
		@SuppressWarnings("deprecation")
		final String strURL = 
				"http://" + strHost + "/atom?Type=" + type.name() 
						+ "&id=" + URLEncoder.encode( strID )
						+ "&data=" + URLEncoder.encode( strData );
		
		final URLReader reader = new URLReader( strURL );
		final String strResult = reader.getContent();
		return ( null!=strResult );
	}
	
}