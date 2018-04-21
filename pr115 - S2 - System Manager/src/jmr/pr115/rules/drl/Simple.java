package jmr.pr115.rules.drl;

import com.google.gson.JsonObject;

import jmr.pr115.schedules.run.TeslaJob;
import jmr.s2.ingest.Import;
import jmr.s2db.imprt.WebImport;
import jmr.util.TimeUtil;

public class Simple {
	
	static {
		TimeUtil.isHourOfDay(); // just to get the import
	}


	public static void doRefreshWeather() {

		System.out.println( "--- doRefreshWeather()" );

		
//		ConnectionProvider.get();
//		
//		final String strSession = NetUtil.getSessionID();
//		final String strClass = Import.class.getName();
//		Client.get().register( strSession, strClass );
		
//		final Import source = Import.NEWS_CURRENT__CNN_NEWSAPI;
		final Import source = Import.WEATHER_FORECAST__YAHOO;
		
		final String strURL = source.getURL();
		final String strTitle = source.getTitle();

//		System.out.println( "Refreshing weather import.." );
		
		final WebImport wi = new WebImport( strTitle, strURL );
		final Long seq = wi.save();
		
		System.out.println( "Weather refreshed. Result: seq = " + seq );
	}
	

	public static JsonObject doCheckTeslaState() {

		System.out.println( "--- doCheckTeslaState()" );
		
		final TeslaJob job = new TeslaJob();
		final JsonObject jo = job.request();
		
		System.out.println( "Combined JsonObject from Tesla (size): " + jo.size() );
		return jo;
	}
	
}
