package jmr.s2db.imprt;

import java.util.Date;

import jmr.s2db.Client;
import jmr.s2db.comm.JsonIngest;
import jmr.util.NetUtil;
import jmr.util.http.ContentRetriever;

public class WebImport {
	
	private final String strTitle;
	private final String strURL;
	
	private final String strPath;
	
	private String strJSON;
	
	
	
	public WebImport(	final String strTitle,
						final String strURL,
						final String strJSON ) {
		this.strTitle = strTitle;
		this.strURL = strURL;
		
		this.strPath = "/External/Ingest/" + strTitle;
		
		this.strJSON = strJSON;
	}
	

	public WebImport(	final String strTitle,
						final String strURL ) {
		this.strTitle = strTitle;
		this.strURL = strURL;
		
		this.strPath = "/External/Ingest/" + strTitle;
		
		this.strJSON = null;
	}
	
	
	
	
	
	public Long save() {
//		if ( null==strJSON ) return null;
//		if ( strJSON.isEmpty() ) return null;
		
		final Date now = new Date();
		
		if ( null==strJSON ) {
			final ContentRetriever retriever = new ContentRetriever( strURL );
			try {
				this.strJSON = retriever.getContent();
			} catch ( final Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		
		PageSaver saver = new PageSaver( strPath );
		saver.put( "timestamp", Long.toString( now.getTime() ) );
		saver.put( "URL", strURL );
		saver.put( "title", this.strTitle );
		final Long seq = saver.save();

		final JsonIngest ingest = new JsonIngest();
		/*final Long seqData =*/ ingest.saveJson( strPath + "/data", strJSON );

		saver.activate( now );
		
		System.out.println( "Result: seq = " + seq );
		return seq;
	}
						
	
	


	public static void main( final String[] args ) throws Exception {
		
		//--- JSON from file ------------------
		
//		final String strFilename = 
//				"C:\\Development\\SourceRepos\\git_20170719\\"
//				+ "Projects__20170719\\pr105 - S2DB\\files\\ingest\\"
//				+ "wunderground_forecast_example.json";
//		
//		final String strContents = 
//				FileUtil.readFromFile( new File( strFilename ) );

		
		//--- JSON from URL --------------------


		final String strSession = NetUtil.getSessionID();
		final String strClass = WebImport.class.getName();
		Client.get().register( strSession, strClass );
		
		
		final String strURL = 
				"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22glenelg%2C%20md%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

		final WebImport wi = new WebImport( "Test_Import", strURL );
		final Long seq = wi.save();
		
		System.out.println( "Result: seq = " + seq );
	}
		
	
	

}
