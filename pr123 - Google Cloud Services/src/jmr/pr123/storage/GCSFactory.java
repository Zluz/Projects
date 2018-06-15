package jmr.pr123.storage;

import java.util.HashMap;
import java.util.Map;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;

import jmr.util.http.ContentType;

public class GCSFactory {

/*
	javadoc:
https://googlecloudplatform.github.io/google-cloud-java/google-cloud-clients/apidocs/index.html

	also good example/explanations:
		http://www.baeldung.com/java-google-cloud-storage

 */
	
	
	private static Storage storage = null;

	static {
		storage = StorageOptions.getDefaultInstance().getService();
	}
	
	private final String strBucketName;

	
	public GCSFactory( final String strBucketName ) {
		this.strBucketName = strBucketName;
	}
	
	
	public GCSFileWriter create(	final String strFilename,
							final ContentType type ) {
		final GCSFileWriter file = new GCSFileWriter( 
				GCSFactory.storage, this.strBucketName, strFilename, type );
		return file;
	}
	
	
	public Map<String,GCSFileReader> getListing() {
		final BlobListOption option = BlobListOption.currentDirectory();
		final Page<Blob> page = storage.list( this.strBucketName, option );
		final Iterable<Blob> iterator = page.iterateAll();
		final Map<String, GCSFileReader> map = new HashMap<>();
		for ( final Blob blob : iterator ) {
			final GCSFileReader file = new GCSFileReader( blob );
			
			final String strName = file.getName();
			map.put( strName, file );
		}
		return map;
	}
	
	
	public GCSFileReader getFile( final String strKey ) {
		final BlobId id = BlobId.of( strBucketName, strKey );
		final Blob blob = storage.get( id );
		final GCSFileReader file = new GCSFileReader( blob );
		return file;
	}
	
	
	public static void main( final String[] args ) {
		final GCSFactory factory = new GCSFactory( TestUtils.BUCKET_NAME );
		final Map<String, GCSFileReader> list = factory.getListing();
		for ( final GCSFileReader file : list.values() ) {
			
			System.out.print( "\t" + file.getContentType() );
			final byte[] data = file.getContent();
			System.out.print( "\t" + data.length );
			System.out.print( "\t" + file.getName() );
			System.out.println();
		}
	}
	
}
