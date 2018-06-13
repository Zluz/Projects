package jmr.pr121.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;


public class CloudStorage {

	/*

		See:
	https://cloud.google.com/appengine/docs/standard/java/building-app/cloud-storage
		or:
	https://cloud.google.com/java/getting-started/using-cloud-storage
	 */
	
	private static final int BUFFER_SIZE = 2 * 1024 * 1024;
	
	
	private GcsService gcsService;

	
	public CloudStorage() {

		gcsService = GcsServiceFactory.createGcsService(
		    new RetryParams.Builder()
			        .initialRetryDelayMillis( 10 )
			        .retryMaxAttempts( 5 )
			        .totalRetryPeriodMillis( 5000 )
			        .build() );
	}
	
	private void copy(	final InputStream input, 
						final OutputStream output ) throws IOException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
				output.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
		} finally {
			input.close();
			output.close();
		}
	}
	

	private String storeData( 	final String strFilename,
								final File file ) throws IOException {

//		  String filename = uploadedFilename(image); // Extract filename
		  GcsFileOptions.Builder builder = new GcsFileOptions.Builder();

		  builder.acl( "public-read" ); // Set the file to be publicly viewable
		  GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
		  GcsOutputChannel outputChannel;
		  GcsFilename gcsFile = new GcsFilename( TestUtils.BUCKET_NAME, strFilename );
		  outputChannel = gcsService.createOrReplace( gcsFile, instance );
//		  copy(filePart.getInputStream(), Channels.newOutputStream(outputChannel));
		  
		  final InputStream stream = new FileInputStream( file );
//		  final InputStream stream = new ByteArrayInputStream(
//				  strData.getBytes( StandardCharsets.UTF_8 ) );
		  
//		  final InputStream is 
		  copy( stream, Channels.newOutputStream( outputChannel ) );
		  
		  return strFilename; // Return the filename without GCS/bucket appendage
	}

	
	public String test() {
//		final String strData = "This is a text string as sample data.";
		
		final CloudStorage storage = new CloudStorage();
		try {
			
			final File fileTest = TestUtils.getFile();
			
			storage.storeData( fileTest.getName(), fileTest );
			return "File saved (?)";
			
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			final String strError = ExceptionUtils.getStackTrace( e );
			return "Exception encountered: " + e.toString();
		}
	}
	
	
	public static void main( final String[] args ) throws IOException {
		
//		final String strData = "This is a text string as sample data.";
		
		final CloudStorage storage = new CloudStorage();
//		storage.storeData( "Test.txt", strData );
		storage.test();

	}


	
}
