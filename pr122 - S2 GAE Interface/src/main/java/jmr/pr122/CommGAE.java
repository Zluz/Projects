package jmr.pr122;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

//import org.apache.http.entity.ContentType;

import jmr.util.SUProperty;
import jmr.util.SystemUtil;
import jmr.util.http.ContentRetriever;

public class CommGAE {

	final String strGAEUrl;
	
	
	
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
						final EnumMap<DocMetadataKey,String> map,
//						final ContentType type,
						final byte[] data ) {
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
					sbURL.append( "&" + entry.getKey().name() );
					sbURL.append( "=" + entry.getValue() );
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
			
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void configure(	final String strName,
							final String strData ) {
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
			store( key, strIndex, map, data );
			
		} catch ( final IOException e ) {
			e.printStackTrace();
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
	
	
	public static void main( final String[] args ) throws IOException {
		
//		final CommGAE comm = new CommGAE( "http://localhost:8080/" );
		final CommGAE comm = new CommGAE();
		
		comm.store( DocKey.TEST, "test_value: this is the large stored data" );
		
		
		final File file = new File( "S:\\Sessions\\B8-27-EB-13-8B-C0\\screenshot.png" );
		System.out.println( "File size: " + file.length() );
		final Path path = Paths.get( file.toURI() );
		
		final byte[] data = Files.readAllBytes( path );
		System.out.println( "Byte array: " + data.length );
//		comm.store( "SCREENSHOT_B8-27-EB-13-8B-C0", 
//						null, ContentType.IMAGE_PNG, data );
		comm.store( DocKey.DEVICE_SCREENSHOT, "B8-27-EB-13-8B-C0", null, data );
		
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
	}

}
