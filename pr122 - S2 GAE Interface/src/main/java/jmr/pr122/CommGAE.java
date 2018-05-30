package jmr.pr122;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

//import org.apache.http.entity.ContentType;

import jmr.util.SUProperty;
import jmr.util.SystemUtil;
import jmr.util.http.ContentRetriever;
import jmr.util.http.ContentType;

public class CommGAE {

	final String strGAEUrl;
	
	
	
	public CommGAE( final String strURL ) {
		this.strGAEUrl = strURL; 
	}

	public CommGAE() {
		this( SystemUtil.getProperty( SUProperty.GAE_URL ) ); 
	}
	

	public void store(	final DocKey key,
						final String strData ) {
		this.store( key.name(), strData );
	}

	public void store(	final String strName,
						final String strData ) {
		this.store( strName, String.class, ContentType.TEXT_PLAIN,
						strData.getBytes( UTF_8 ) );
	}

	public void store(	final String strName,
						final Class<?> classData,
						final ContentType type,
						final byte[] data ) {
		try {
			final StringBuilder sbURL = new StringBuilder();
			sbURL.append( ContentRetriever.cleanURL( 
						strGAEUrl + "/map?name=" ) );
			sbURL.append( URLEncoder.encode( strName, UTF_8.name() ) );
			
			if ( null!=classData ) {
				sbURL.append( "&class=" + classData.getName() );
			}
			
			if ( null!=type ) {
				sbURL.append( "&type=" + type.getMimeType() );
			}
			
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
	
	
	public void store(	final File file,
						final String strName,
						final ContentType type ) {
		if ( null==file ) throw new IllegalStateException();
		if ( !file.isFile() ) return;
		if ( null==type ) throw new IllegalStateException();

		final Path path = Paths.get( file.toURI() );
		
		try {
			final byte[] data = Files.readAllBytes( path );
			store( strName, null, type, data );
			
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
		
		final CommGAE comm = new CommGAE( "http://localhost:8080/" );
//		final CommGAE comm = new CommGAE();
		
		comm.store( "test_name", "test_value: this is the large stored data" );
		
		
		final File file = new File( "S:\\Sessions\\B8-27-EB-13-8B-C0\\screenshot.png" );
		System.out.println( "File size: " + file.length() );
		final Path path = Paths.get( file.toURI() );
		
		final byte[] data = Files.readAllBytes( path );
		System.out.println( "Byte array: " + data.length );
		comm.store( "SCREENSHOT_B8-27-EB-13-8B-C0", 
						null, ContentType.IMAGE_PNG, data );
		
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
