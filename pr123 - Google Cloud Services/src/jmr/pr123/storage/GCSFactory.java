package jmr.pr123.storage;

import java.util.HashMap;
import java.util.Map;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageException;
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

		final Map<String, GCSFileReader> map = new HashMap<>();
		final BlobListOption blo = BlobListOption.currentDirectory();

		Page<Blob> page = null;

		try {
			page = storage.list( this.strBucketName, blo );
		} catch ( final NoSuchMethodError t ) {
			/*
				Can run into java.lang.NoSuchMethodError through 
				com.google.cloud.storage.StorageImpl.list(StorageImpl.java:262)
			 	ignore for now, try the call below..
			 */ 
		}
		
		if ( null==page || !page.hasNextPage() ) {
			try {
				// final BucketGetOption bgo = BucketGetOption.;
				final Bucket bucket = storage.get( this.strBucketName );
				page = bucket.list( blo );
			} catch ( final StorageException e ) {
				/*
				exception through
				jmr.pr123.storage.GCSFactory.getListing(GCSFactory.java:70)
				 */
			} catch ( final NoSuchMethodError t ) {
				/*
				Can run into java.lang.NoSuchMethodError through
				com.google.cloud.storage.Bucket.list(Bucket.java:732)
			 	ignore for now, try the call below..
				 */ 
			}
		}

		if ( null==page || !page.hasNextPage() ) {
			try {
				final Bucket bucket = storage.get( this.strBucketName );
				page = bucket.list();
			} catch ( final StorageException e ) {
				/*
				exception through
				com.google.cloud.storage.StorageImpl.get(StorageImpl.java:172)
				// this happened while deploying to App Engine standard..
				 */
			} catch ( final NoSuchMethodError t ) {
				/*
				Can run into java.lang.NoSuchMethodError through
				com.google.cloud.storage.Bucket.list(Bucket.java:732)
			 	ignore for now, try the call below..
			 */ 
			}
		}

		
		if ( null!=page ) {
			try {
				final Iterable<Blob> iterator = page.iterateAll();
				for ( final Blob blob : iterator ) {
					final GCSFileReader file = new GCSFileReader( blob );
					
					final String strName = file.getName();
					map.put( strName, file );
				}
				return map;
			} catch ( final Exception e ) {
				// bummer..
			}
		}
		
		return map;
	}
	
	
	public GCSFileReader getFile( final String strKey ) {
		if ( null==strKey ) return null;
		final BlobId id = BlobId.of( strBucketName, strKey );
		if ( null==id ) return null;
		final Blob blob = storage.get( id );
		if ( null==blob ) return null;
		final GCSFileReader file = new GCSFileReader( blob );
		return file;
	}
	
	
	public static void main( final String[] args ) {
		final GCSFactory factory = new GCSFactory( TestUtils.BUCKET_NAME );
		final Map<String, GCSFileReader> list = factory.getListing();
		for ( final GCSFileReader file : list.values() ) {
			
			if ( file.getName().toUpperCase().contains( "TESLA" ) ) {
				System.out.print( "\t" + file.getContentType() );
				final byte[] data = file.getContent();
				System.out.print( "\t" + data.length );
				System.out.print( "\t" + file.getName() );
				System.out.println();
				final String strContent = new String( data );
				int i=0;
				for ( final String strLine : strContent.split( "\\n" ) ) {
					System.out.println( "\t\t" + strLine );
					if ( i>10 ) {
						break;
					} else {
						i++;
					}
				}
			}
		}
	}
	
}
