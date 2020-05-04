package jmr.s2db.imprt;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;

import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.s2db.comm.JsonIngest;
import jmr.util.NetUtil;
import jmr.util.http.ContentRetriever;
import jmr.util.transform.JsonUtils;
import jmr.util.transform.TextUtils;

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
	
	
	public String getURL() {
		return this.strURL;
	}
	
	
	public Long save() throws IOException {
//		if ( null==strJSON ) return null;
//		if ( strJSON.isEmpty() ) return null;
		
		final Date now = new Date();
		
		if ( null==strJSON ) {
			final ContentRetriever retriever = new ContentRetriever( strURL );
//			try {
				this.strJSON = retriever.getContent();
//			} catch ( final Exception e ) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return null;
//			}
		}

		final JsonIngest ingest = new JsonIngest();
		final Long seqData = ingest.saveJson( strPath + "/data", strJSON, true );
		final Long seqSaver;
		
		if ( null != seqData ) {
			PageSaver saver = new PageSaver( strPath );
			saver.put( "timestamp", Long.toString( now.getTime() ) );
			saver.put( "URL", strURL );
			saver.put( "title", this.strTitle );
			saver.put( "seq_data", ""+ seqData );
			seqSaver = saver.save();
	
			saver.activate( now );
		} else {
			seqSaver = null;
		}
		
		System.out.println( "Result: seqData = " + seqData );
		System.out.println( "Result: seqSaver = " + seqSaver );
		return seqSaver;
	}
						


	public String getResponse() {
		return this.strJSON;
	}
		
	
	
	final static String JSON_TEST = ""
			+ "{\r\n" + 
			"  \"properties\": {\r\n" + 
			"    \"updated\": \"2020-05-04T05:33:44+00:00\",\r\n" + 
			"    \"elevation\": {\r\n" + 
			"      \"value\": 181.96560000000002,\r\n" + 
			"      \"unitCode\": \"unit:m\"\r\n" + 
			"    },\r\n" + 
			"    \"periods\": [\r\n" + 
			"      {\r\n" + 
			"        \"number\": 1,\r\n" + 
			"        \"name\": \"Overnight\",\r\n" + 
			"        \"startTime\": \"2020-05-04T04:00:00-04:00\",\r\n" + 
			"        \"isDaytime\": false,\r\n" + 
			"        \"temperature\": 53,\r\n" + 
			"        \"temperatureUnit\": \"F\",\r\n" + 
			"        \"shortForecast\": \"Slight Chance Rain Showers\"\r\n" + 
			"      },\r\n" + 
			"      {\r\n" + 
			"        \"number\": 2,\r\n" + 
			"        \"name\": \"Monday\",\r\n" + 
			"        \"startTime\": \"2020-05-04T06:00:00-04:00\",\r\n" + 
			"        \"isDaytime\": true,\r\n" + 
			"        \"temperature\": 65,\r\n" + 
			"        \"temperatureUnit\": \"F\",\r\n" + 
			"        \"shortForecast\": \"Sunny\"\r\n" + 
			"      }\r\n" + 
			"    ]\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"";
	
	


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

		
		final Summarizer summarizer = new SummarizerBase() {

			@Override
			public boolean isMatch( final String strNodePath ) {
				return true;
			}
			
			@Override
			public Map<String, String> getJsonPaths() {
				final Map<String,String> map = new HashMap<>();
//				map.put( "current.forecast.detailed", "$." );
				
				map.put( "elevation", 
								"$['properties'].['elevation'].['value']" );
				
				map.put( "period_1.name", 
					"$['properties'].['periods'].[?(@.number == 1)].['name']" );

				map.put( "period_1.temperature", 
					"$['properties'].['periods'].[?(@.number == 1)].['temperature']" );
				
				map.put( "period_1.forecast", 
					"$['properties'].['periods'].[?(@.number == 1)].['shortForecast']" );

				map.put( "period_2.name", 
					"$['properties'].['periods'].[?(@.number == 2)].['name']" );

				map.put( "period_2.temperature", 
					"$['properties'].['periods'].[?(@.number == 2)].['temperature']" );
				
				map.put( "period_2.forecast", 
					"$['properties'].['periods'].[?(@.number == 2)].['shortForecast']" );

				return map;
			}
		};

		
		final JsonElement je = JsonUtils.getJsonElementFor( JSON_TEST );
		if ( je.isJsonNull() ) System.err.println( "Invalid JSON" );
		
		final Map<String, String> map = summarizer.summarize( je );
		
		System.out.println( "Summarizer output:\n" + 
					TextUtils.convertMapToString( map ) );
		
		
		
		
		
//		if ( 1==1 ) return;
		


		final String strSession = NetUtil.getSessionID();
		final String strClass = WebImport.class.getName();
		Client.get().register( ClientType.TEST, strSession, strClass );
		Client.get().setDebug( true );

		SummaryRegistry.get().add( summarizer );
		

		
		final String strURL = 
					"https://api.weather.gov/gridpoints/LWX/95,87/forecast";

		final WebImport wi = new WebImport( "Test_Import", strURL );
		final Long seq = wi.save();

		
		System.out.println( "--- JSON Response --- START ---" );
		final String strJson = wi.getResponse();
		System.out.println( JsonUtils.getPretty( strJson ) );
		System.out.println( "--- JSON Response --- END ---" );
		
		
		System.out.println( "Result: seq = " + seq );
		Client.get().close();
	}

}
