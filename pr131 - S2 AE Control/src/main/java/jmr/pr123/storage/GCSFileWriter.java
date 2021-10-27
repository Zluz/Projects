package jmr.pr123.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BlobInfo.Builder;
import com.google.cloud.storage.Storage;

import jmr.pr104.util.http.ContentType;
import jmr.pr131.data.Logger;


public class GCSFileWriter implements jmr.pr131.Constants {

	/*
	 * https://github.com/GoogleCloudPlatform/getting-started-java/blob
	 * /master/bookshelf/3-binary-data/src/main/java/com/example/getstarted/util/
	 * CloudStorageHelper.java
	 */

	private final Map<String,String> map = new HashMap<>();
	
	private final String strBucketName;
	private final String strFilename;
	private final ContentType type;
	private final Storage storage;

	public GCSFileWriter(	final Storage storage,
							final String strBucketName,
							final String strFilename,
							final ContentType type ) {
		this.storage = storage;
		this.strBucketName = strBucketName;
		this.strFilename = strFilename;
		this.type = type;
	} 
	
	
	public void put(	final String strKey,
						final String strValue ) {
		this.map.put( strKey, strValue );
	}
	
	// remove this in the future. probably useful for now.
	public Map<String, String> getMap() {
		return this.map;
	}
	
	
	public String upload( final File file ) throws IOException {
		if ( ! APP_ENGINE_ENABLED ) return null;
		
		final InputStream stream = new FileInputStream( file );
		final String strResult = this.upload( stream );
		return strResult;
	}

	
	public String upload( final byte[] data ) {
		final InputStream stream = new ByteArrayInputStream( data );
		final String strResult = this.upload( stream );
		return strResult;
	}

	
	/**
	 * Uploads a file to Google Cloud Storage to the bucket specified in the
	 * BUCKET_NAME environment variable, appending a timestamp to end of the
	 * uploaded filename.
	 */

	
	public String upload( final InputStream stream ) {
		
		Logger.info( "--- GCSFileWriter.upload() <<<" );
		
		final ArrayList<Acl> arrACL = new ArrayList<>();
				
		Builder builder = BlobInfo.newBuilder( strBucketName, this.strFilename );
		builder = builder.setAcl( arrACL );
		builder = builder.setContentType( type.getMimeType() );
		if ( !map.isEmpty() ) {
			builder = builder.setMetadata( map );
		}
		
		try {
			@SuppressWarnings("deprecation")
			final BlobInfo info = storage.create( builder.build(), stream );
			// return the public download link
			return info.getMediaLink();
//		} catch ( final StorageException e ) {
		} catch ( final Exception e ) {
			System.err.println( e.toString() + " encountered while "
					+ "attempting to upload to Google Cloud Storage.\n"
					+ "File: " + this.strFilename + "\n" 
					+ "Aborting." );
			e.printStackTrace();
			return null;
		}
			
		// TODO catch exception from above, on storage.create():
		/*
Caused by: com.google.cloud.storage.StorageException: www.googleapis.com
	at com.google.cloud.storage.spi.v1.HttpStorageRpc.translate(HttpStorageRpc.java:220)
	at com.google.cloud.storage.spi.v1.HttpStorageRpc.create(HttpStorageRpc.java:291)
	at com.google.cloud.storage.StorageImpl.create(StorageImpl.java:148)
	at jmr.pr123.storage.GCSFileWriter.upload(GCSFileWriter.java:89)
	at jmr.pr123.storage.GCSFileWriter.upload(GCSFileWriter.java:58)
	at jmr.pr115.rules.drl.CloudUtilities.saveImage(CloudUtilities.java:40)
	
Caused by: com.google.cloud.storage.StorageException: Unexpected end of file from server
	at com.google.cloud.storage.spi.v1.HttpStorageRpc.translate(HttpStorageRpc.java:220)
	at com.google.cloud.storage.spi.v1.HttpStorageRpc.create(HttpStorageRpc.java:291)
	at com.google.cloud.storage.StorageImpl.create(StorageImpl.java:148)
	at jmr.pr123.storage.GCSFileWriter.upload(GCSFileWriter.java:89)
	at jmr.pr123.storage.GCSFileWriter.upload(GCSFileWriter.java:58)
	at jmr.pr115.rules.drl.CloudUtilities.saveImage(CloudUtilities.java:41)	
		 */
		
	}

	
	
	
	
}
