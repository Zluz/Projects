package jmr.pr122;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import jmr.util.SUProperty;
import jmr.util.SystemUtil;
import jmr.util.http.ContentRetriever;

public class GAEComm {

	final String strGAEUrl;
	
	
	
	public GAEComm() {
		this.strGAEUrl = SystemUtil.getProperty( SUProperty.GAE_URL ); 
	}
	

	public void store(	final String strName,
						final String strData ) {
		try {
			final String strURL = ContentRetriever.cleanURL( 
						strGAEUrl + "/map?name=" 
						+ URLEncoder.encode( strName, "UTF-8" ) );
			final ContentRetriever retriever = new ContentRetriever( strURL );
			retriever.postContent( strData );
			
		} catch ( final UnsupportedEncodingException e ) {
			throw new IllegalStateException( e );
			
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void main( final String[] args ) {
		final GAEComm comm = new GAEComm();
		
		comm.store( "test_name", "test_value: this is the large stored data" );
	}

}
