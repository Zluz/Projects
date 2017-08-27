package jmr.s2db.comm;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.s2db.Client;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.util.NetUtil;
import jmr.util.http.ContentRetriever;
import jmr.util.report.Reporting;
import jmr.util.report.ZeroPad;

public class JsonIngest {

	
	final private List<Long> listPagesToActivate = new LinkedList<>();
	
	final private Path path = new Path();
	final private Page page = new Page();
	
	
	
	
	
	
	

	public Long saveJson(	final String strNodePath, 
							final String strJson ) {
		final Date now = new Date();
		final JsonElement element = new JsonParser().parse( strJson );
		final ProcessedJson result = processJsonElement( 
								strNodePath, element.getAsJsonObject() );
		activatePages( now );
		return result.seq;
	}
		
	
	private Long savePage(	final String strNodePath,
							final Map<String,String> map ) {
		if ( null==strNodePath ) throw new IllegalStateException( "null parameter" );
		if ( null==map ) throw new IllegalStateException( "null parameter" );
		
		if ( !map.isEmpty() ) {

			System.out.println( "Node: " + strNodePath );
			System.out.println( Reporting.print( map ) );

			final Long seqPath = path.get( strNodePath );
			final Long seqPage = page.create( seqPath );
			page.addMap( seqPage, map, false );
			listPagesToActivate.add( seqPage );
			
			return seqPage;
		} else {
			return null;
		}
	}
	
	
	private void activatePages( final Date date ) {
		for ( final Long seq : listPagesToActivate ) {
			page.setState( seq, date, 'A' );
		}
	}
	
	
	public Long saveJsonMap(	final String strNodePath,
								final JsonObject element ) {
		
        final Map<String,String> map = new HashMap<>();
        for ( final Entry<String, JsonElement> entry : element.entrySet() ) {
        	
        	final String key = entry.getKey();
        	final JsonElement value = entry.getValue();

    		final ProcessedJson result = 
    				processJsonElement( strNodePath + "/" + key, value );

        	if ( result.isSimple() ) {
//        		map.put( key, DataFormatter.format( result.strValue ) );
        		map.put( key, result.strValue );
        	} else {
//        		final Long seqElement = result.seq; // new seq
        	}
        }

        final Long seq = savePage( strNodePath, map );
        return seq;
	}

	
//	public static boolean isSimple( final JsonElement element ) {
//		if ( element.isJsonPrimitive() ) {
//			return true;
//		} else if ( element.isJsonNull() ) {
//			return true;
//		} else {
//			return false;
//		}
//	}
	
	
	public static class ProcessedJson {
		
		public final Long seq;
		public final String strValue;
		
		public ProcessedJson(	final Long seq, 
								final String strValue ) {
			this.seq = seq;
			this.strValue = strValue;
		}
		public ProcessedJson( final Long seq ) {
			this( seq, null );
		}
		public ProcessedJson( final String strValue ) {
			this( null, strValue );
		}
		public boolean isSimple() {
			return ( null!=strValue );
		}
	}
	

	public ProcessedJson processJsonElement(	final String strNodePath,
												final JsonElement element ) {
		if ( null==strNodePath ) throw new IllegalStateException( "null parameter" );
		if ( null==element ) throw new IllegalStateException( "null parameter" );

		
		if ( element.isJsonNull() ) {
		
			return new ProcessedJson( "(null)" );
			
		} else if ( element.isJsonObject() ) {
			
			final JsonObject jo = element.getAsJsonObject();
			final Long seq = saveJsonMap( strNodePath, jo );
			return new ProcessedJson( seq );

		} else if ( element.isJsonArray() ) {
			
			final JsonArray array = element.getAsJsonArray();
			final Long seq = saveJsonArray( strNodePath, array );
			return new ProcessedJson( seq );

		} else if ( element.isJsonPrimitive() ) {
			
			final String strValue = element.getAsString();
			return new ProcessedJson( strValue );
		
		} else { // unknown
			
			final String strValue = element.toString();
			return new ProcessedJson( strValue );
		}
	}
	

	public Long saveJsonArray(	final String strNodePath,
								final JsonArray array ) {

		final Map<String,String> map = new HashMap<>();
		
		final int iSize = array.size();
		final ZeroPad pad = new ZeroPad( iSize );
		for ( int i=0; i<iSize; i++ ) {
			
//			final String key = Integer.toString( i );
			final String key = pad.str( i );
			final JsonElement value = array.get( i );

			
    		final ProcessedJson result = 
    				processJsonElement( strNodePath + "/" + key, value );

			
//        	if ( isSimple( value ) ) {
    		if ( result.isSimple() ) {
//        		map.put( key, value.getAsString() );
    			map.put( key, result.strValue );
        	} else {
//        		final Long seqElement = result.seq; 
        	}
        				
		}

        final Long seq = savePage( strNodePath, map );
        return seq;
	}

	

	public static String report( final Object obj ) {
		if ( null==obj ) return "null";
		
		if ( obj instanceof Map ) {
			@SuppressWarnings( "unchecked" )
			final Map<String,Object> map = (Map<String,Object>)obj;
			return reportMap( map );
		} else {
			return obj.toString();
		}
	}
	
	
	public static String reportMap( final Map<String,Object> map ) {

		String str = "Map:[\n";
		for ( Entry<String, Object> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final Object objValue = entry.getValue();
			
			str += "\t" + strKey + "=\"" + report( objValue ) + "\"\n";
		}
		str += "]";
		return str;
	}
	
	public static void print( final Object obj ) {
		System.out.println( "\tReport: " + report( obj ) );
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
		
		final String strURL = 
				"https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22glenelg%2C%20md%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
		
		final ContentRetriever retriever = new ContentRetriever( strURL );
		final String strContents = retriever.getContent();
		
		
		//--- have JSON, now parse and save
		
		final String strNode = 
				"/tmp/" + JsonIngest.class.getName() 
				+ "_" + System.currentTimeMillis();

		final String strSession = NetUtil.getSessionID();
		final String strClass = JsonIngest.class.getName();
		Client.get().register( strSession, strClass );
		
		final JsonIngest ingest = new JsonIngest();
		final Long seq = ingest.saveJson( strNode, strContents );
		
		System.out.println( "Result: seq = " + seq );
	}
	
	
	
	
	
	
}
