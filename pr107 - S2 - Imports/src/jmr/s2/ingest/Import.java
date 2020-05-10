package jmr.s2.ingest;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.imprt.Import_WeatherGov;
import jmr.s2db.imprt.Summarizer;
import jmr.s2db.imprt.WebImport;
import jmr.util.NetUtil;

public enum Import {

	/*=== WEATHER ===*/
	
	/** @see https://developer.yahoo.com/weather/ */
	WEATHER_FORECAST__YAHOO(
			"Weather_Forecast_Yahoo",
//			"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22glenelg%2C%20md%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys\" )",
			"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22glenelg%2C%20md%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys",
//https%3A%2F%2Fquery.yahooapis.com%2Fv1%2Fpublic%2Fyql%3Fq%3Dselect%2520*%2520from%2520weather.forecast%2520where%2520woeid%2520in%2520%28select%2520woeid%2520from%2520geo.places%281%29%2520where%2520text%253D%2522glenelg%252C%2520md%2522%29%26format%3Djson%26env%3Dstore%253A%252F%252Fdatatables.org%252Falltableswithkeys
			TimeUnit.HOURS.toMillis( 1 ),
			false ),


	/** @see https://www.weather.gov/documentation/services-web-api */
	WEATHER_FORECAST__WEATHER_GOV(
			"Import_WeatherGov",
			"https://api.weather.gov/gridpoints/LWX/95,87/forecast",
			new Import_WeatherGov(),
			TimeUnit.HOURS.toMillis( 1 ),
			true ),
	

	
	/*=== NEWS ===*/
	/*
	 * service index..
	 * 
	 * https://www.programmableweb.com/category/news-services/api
	 * 
	 * 
	 * 
	 * Microsoft?
	 * https://api.cognitive.microsoft.com/bing/v5.0/news/
	 * 		https://www.microsoft.com/cognitive-services/en-us/Bing-news-search-API/documentation
	 * 		https://www.programmableweb.com/api/bing-news-search
	 */
	
	NEWS_CURRENT__CNN_NEWSAPI(
			"News_Current_CNN_NewsAPI",
			"https://newsapi.org/v1/articles?source=cnn&sortBy=top&apiKey=3ac8d007fcb94645bb03bec513a0ebfc",
			TimeUnit.HOURS.toMillis( 4 ),
			true ),
	
	
	;
	
	
	
	
	private final String strTitle;
	
	private final String strURL;
	
	private final Summarizer summarizer;
	
	private final long lInterval;
	
	private final boolean bProcessJson;
	
	
	
	
	private Import( final String strTitle,
					final String strURL,
					final Summarizer summarizer,
					final long lInterval,
					final boolean bProcessJson ) {
		this.strTitle = strTitle;
		this.strURL = strURL;
		this.summarizer = summarizer;
		this.lInterval = lInterval;
		this.bProcessJson = bProcessJson;
	}

	private Import( final String strTitle,
					final String strURL,
					final long lInterval,
					final boolean bProcessJson ) {
		this( strTitle, strURL, null, lInterval, bProcessJson );
	}

	public String getTitle() {
		return this.strTitle;
	}
	
	public String getURL() {
		return this.strURL;
	}
	
	public Summarizer getSummarizer() {
		return this.summarizer;
	}
	
	public long getInterval() {
		return this.lInterval;
	}
	
	
	public static void main( final String[] args ) 
										throws InterruptedException, 
												IOException {

		ConnectionProvider.get();
		
		final String strSession = NetUtil.getSessionID();
		final String strClass = Import.class.getName();
		Client.get().register( ClientType.TEST, strSession, strClass );
		
//		final Import source = Import.NEWS_CURRENT__CNN_NEWSAPI;
		final Import source = Import.WEATHER_FORECAST__YAHOO;
		
		final String strURL = source.getURL();
		final String strTitle = source.getTitle();

		for (;;) {
			
			final WebImport wi = new WebImport( strTitle, strURL );
			final Long seq = wi.save( false );
			
			System.out.println( "Result: seq = " + seq + ", "
					+ "time now is " + new Date() );

			Thread.sleep( TimeUnit.HOURS.toMillis( 1 ) );
		}
	}
	
	
}
