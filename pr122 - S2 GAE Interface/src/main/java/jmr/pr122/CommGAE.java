package jmr.pr122;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;


//import org.apache.http.entity.ContentType;

import jmr.util.SUProperty;
import jmr.util.SystemUtil;
import jmr.util.http.ContentRetriever;


/**
 * Basic communication and simple local storage using Google App Engine.
 * <br><br>
 * This is not using advanced messaging or persistent storage.
 */
public class CommGAE {
	
	private final static Logger 
					LOGGER = Logger.getLogger( CommGAE.class.getName() );

	final String strGAEUrl;

	final JsonObject joConfig = new JsonObject();

	// build json only or send to gae?
	boolean bJsonOnly = false; 

	
	
	public CommGAE( final String strURL ) {
		this.strGAEUrl = strURL; 
	}

	public CommGAE() {
		this( SystemUtil.getProperty( SUProperty.GAE_URL ) ); 
	}
	
	

//	public void store(	final DocKey key,
//						final String strData ) {
//		this.store( key.name(), strData, key.getType() );
//	}

//	public void store(	final String strName,
//						final String strData,
//						final ContentType type ) {
//		this.store( strName, String.class, type,
//						strData.getBytes( UTF_8 ) );
//	}

	public void store(	final DocKey key,
						final String strData ) {
		this.store( key, null, null, strData.getBytes( UTF_8 ) );
	}

	public void store(	final DocKey key,
						final String strIndex,
//						final Class<?> classData,
						final Map<DocMetadataKey,String> map,
//						final ContentType type,
						final byte[] data ) {
		if ( null==this.strGAEUrl ) return;
		
		try {
			final StringBuilder sbURL = new StringBuilder();
			sbURL.append( ContentRetriever.cleanURL( 
						strGAEUrl + "/map?name=" ) );
//			sbURL.append( URLEncoder.encode( strName, UTF_8.name() ) );
			sbURL.append( key.name() );
			if ( null!=strIndex ) {
				sbURL.append( "/" + strIndex );
			}
			
//			if ( null!=classData ) {
//				sbURL.append( "&class=" + classData.getName() );
//			}
			if ( null!=map ) {
				for ( final Entry<DocMetadataKey, String> entry : map.entrySet() ) {
					final String strValue = entry.getValue();
					if ( null!=strValue ) {
						sbURL.append( "&" + entry.getKey().name() + "=" );
						sbURL.append( URLEncoder.encode( strValue, UTF_8.name() ) );
					}
				}
			}
			
//			if ( null!=type ) {
//				sbURL.append( "&type=" + type.getMimeType() );
//			}
			sbURL.append( "&type=" + key.getType().getMimeType() );
			
			final String strURL = sbURL.toString(); 
			final ContentRetriever retriever = new ContentRetriever( strURL );
			retriever.postContent( data );
			
		} catch ( final UnsupportedEncodingException e ) {
			throw new IllegalStateException( e );

		} catch ( final FileNotFoundException e ) {
			System.out.println( e.toString() + " encountered. "
					+ "Service may not be running or may not be accepting input." );
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static final String GCS_FILENAME = "CONFIG.txt";
	
	
	public void configure(	final String strName,
							final String strData ) {
		joConfig.addProperty( strName, strData );
		
		if ( null==this.strGAEUrl ) return;
		
		try {
			final StringBuilder sbURL = new StringBuilder();
			sbURL.append( ContentRetriever.cleanURL( 
						strGAEUrl + "/status?name=" ) );
			sbURL.append( URLEncoder.encode( strName, UTF_8.name() ) );
			
			final String strURL = sbURL.toString(); 
			final ContentRetriever retriever = new ContentRetriever( strURL );
			retriever.postContent( strData );
			
		} catch ( final UnsupportedEncodingException e ) {
			throw new IllegalStateException( e );
			
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void store(	final DocKey key,
						final String strIndex,
						final File file,
						final EnumMap<DocMetadataKey, String> map
//						final ContentType type 
						) {
		if ( null==file ) throw new IllegalStateException();
		if ( !file.isFile() ) return;
//		if ( null==type ) throw new IllegalStateException();
		if ( null==key ) throw new IllegalStateException();

		final Path path = Paths.get( file.toURI() );
		
		try {
			final byte[] data = Files.readAllBytes( path );

			final EnumMap<DocMetadataKey, String> mapMetadata; 
			if ( null==map ) {
				mapMetadata = new EnumMap<>( DocMetadataKey.class );
			} else {
				mapMetadata = map;
			}

			mapMetadata.put( DocMetadataKey.FILENAME, file.getName() );
			final Instant time = Instant.ofEpochMilli( file.lastModified() );
			mapMetadata.put( DocMetadataKey.FILE_DATE, time.toString() );

			//			store( strName, null, type, data );
			try {
				store( key, strIndex, mapMetadata, data );
			} catch ( final Exception e ) {
				// FileNotFoundException
				// service may not be accepting (because its not config'd) (?)
				// just ignore for now ..
				//TODO investigate, confirm, disable?
				LOGGER.log( Level.WARNING, "Exception encountered", e );
			}
			
		} catch ( final IOException e ) {
			LOGGER.log( Level.WARNING, "Exception encountered", e );
//			e.printStackTrace();
		}
	}
	
	
	
	/*

http://localhost:8080/map
http://localhost:8080/log

http://localhost:8080/map?name=test_name
http://localhost:8080/map?name=SCREENSHOT_B8-27-EB-13-8B-C0


	 */

	private void loadProperty( final SUProperty property ) {
		final String value = SystemUtil.getProperty( property );
		this.configure( property.getName(), value );
	}
	
	
	
	public void generateConfig() throws IOException {
		
//		final CommGAE comm = new CommGAE( "http://localhost:8080/" );
//		final CommGAE comm = new CommGAE();
		final CommGAE comm = this;
		
		final String strDate = new Date().toString();
		final String strMessage = 
				"test_value: this is the large stored data\n" + strDate;
		comm.store( DocKey.TEST, strMessage );
		
		
		final File file = new File( "S:\\Sessions\\B8-27-EB-13-8B-C0\\screenshot.png" );
		System.out.println( "File size: " + file.length() );
		final Path path = Paths.get( file.toURI() );
		
		final byte[] data = Files.readAllBytes( path );
		System.out.println( "Byte array: " + data.length );
//		comm.store( "SCREENSHOT_B8-27-EB-13-8B-C0", 
//						null, ContentType.IMAGE_PNG, data );
//		comm.store( DocKey.DEVICE_SCREENSHOT, "B8-27-EB-13-8B-C0", null, data );
		comm.store( DocKey.DEVICE_SCREENSHOT, "B8-27-EB-13-8B-C0/test-full.png", file, null );

		
		final File fileThumb = new File( "S:\\Sessions\\B8-27-EB-13-8B-C0\\screenshot-thumb.png" );
//		System.out.println( "File size: " + fileThumb.length() );
//		final Path path = Paths.get( file.toURI() );
//		
//		final byte[] data = Files.readAllBytes( path );
//		System.out.println( "Byte array: " + data.length );
//		comm.store( "SCREENSHOT_B8-27-EB-13-8B-C0", 
//						null, ContentType.IMAGE_PNG, data );
//		comm.store( DocKey.DEVICE_SCREENSHOT, "B8-27-EB-13-8B-C0", null, data );
		comm.store( DocKey.DEVICE_SCREENSHOT, "B8-27-EB-13-8B-C0/test-thumb.png", fileThumb, null );
		
//		final String strConfig_Accept001 = 
//						SystemUtil.getProperty( SUProperty.BROWSER_ACCEPT_001 );
//		comm.configure( SUProperty.BROWSER_ACCEPT_001.name(), strConfig_Accept001 );

		comm.loadProperty( SUProperty.GAE_USERNAME );
		comm.loadProperty( SUProperty.GAE_PASSWORD );

		comm.loadProperty( SUProperty.NEST_USERNAME );

//		comm.loadProperty( SUProperty.CONTROL_EMAIL_USERNAME );
//		comm.loadProperty( SUProperty.CONTROL_EMAIL_PROVIDER );
//		comm.loadProperty( SUProperty.CONTROL_EMAIL_PASSWORD );

		final List<String> listAccept = 
					SystemUtil.getProperties( SUProperty.BROWSER_ACCEPT_PRE );
		final String strAccept = String.join( "\n", listAccept );
		comm.configure( SUProperty.BROWSER_ACCEPT_PRE.getName(), strAccept );

		final List<String> listUser = 
				SystemUtil.getProperties( SUProperty.GAE_USER_PRE );
		final String strUsers = String.join( "\n", listUser );
		comm.configure( SUProperty.GAE_USER_PRE.getName(), strUsers );
		
		comm.configure( SUProperty.UPDATE_TIME.getName(), strDate );
	}
	
	

	public JsonObject getConfig() {
		if ( this.joConfig.size() > 0 ) {
			return this.joConfig;
		} else {
			try {
				this.generateConfig();
				return this.joConfig;
			} catch ( final IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}
	
	

	// NOTE: to update the GCS JSON config, run 
	// 			jmr.pr121.config.Configuration.main(String[])
	public static void main( final String[] args ) throws IOException {
//		final CommGAE comm = new CommGAE( "http://localhost:8080/" );
//		final CommGAE comm = new CommGAE();
		final CommGAE comm = new CommGAE( null );
		comm.generateConfig();
		System.out.println( CommGAE.class.getSimpleName() + " JSON:" );
		System.out.println( comm.getConfig().toString() );
	}


}
