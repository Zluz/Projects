package jmr.pr123.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import jmr.util.http.ContentType;

public class CloudStorage02 {

	/*
	 * https://github.com/GoogleCloudPlatform/getting-started-java/blob
	 * /master/bookshelf/3-binary-data/src/main/java/com/example/getstarted/util/
	 * CloudStorageHelper.java
	 */

	private static Storage storage = null;

	// [START init]
	static {
		storage = StorageOptions.getDefaultInstance().getService();
	}
	// [END init]

	// [START uploadFile]

	/**
	 * Uploads a file to Google Cloud Storage to the bucket specified in the
	 * BUCKET_NAME environment variable, appending a timestamp to end of the
	 * uploaded filename.
	 */
	@SuppressWarnings("deprecation")
	public String uploadFile(	final File file, 
								final String bucketName ) throws IOException {
		
		final String strPattern = "YYYYMMdd-HHmmss__";
		final DateTimeFormatter dtf = DateTimeFormat.forPattern(strPattern);
		final DateTime dt = DateTime.now(DateTimeZone.UTC);
		final String strTime = dt.toString(dtf);
		final String fileName = strTime + file.getName();
//		final String fileName = file.getName();
		final InputStream stream = new FileInputStream( file );

//		final Entity entity = Project.ProjectRole; 
		// the inputstream is closed by default, so we don't need to close it here
		final ArrayList<Acl> arrACL = new ArrayList<>(
//				Arrays.asList( Acl.of( User.ofAllUsers(), Role.READER ) ) );
//				Arrays.asList( Acl.of( User.ofAllAuthenticatedUsers(), Role.READER ) ) );
//				Arrays.asList( Acl.of( User.ofAllAuthenticatedUsers(), Role.READER ) ) 
//				Arrays.asList( Acl.of( entity, Role.READER ) ) );
				);
				
		final Map<String, String> map = new HashMap<>();
		map.put( "test_name", "test_value" );
		map.put( "project", "pr123" );
		map.put( "filename", file.getName() );
		map.put( "file size", "" + file.length() );
		final BlobInfo blobInfo = storage.create(
				BlobInfo.newBuilder( bucketName, "test_dir/" + fileName )
				// Modify access list to allow all users with link to read file
					.setAcl( arrACL )
					.setContentType( ContentType.IMAGE_JPEG.getMimeType() )
					.setMetadata( map  )
					.build(),
//				filePart.getInputStream());
				stream );
		// return the public download link
		return blobInfo.getMediaLink();
	}
	// [END uploadFile]

	// [START getImageUrl]
	
	
	public String test() {
		final File file = TestUtils.getFile();
		try {
			
			this.uploadFile( file, TestUtils.BUCKET_NAME );
			return "File saved (?)";
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Exception encountered: " + e.toString();
		}
	}
	

	/**
	 * Extracts the file payload from an HttpServletRequest, checks that the file
	 * extension is supported and uploads the file to Google Cloud Storage.
	 */
	public String getImageUrl(HttpServletRequest req, HttpServletResponse resp, final String bucket)
			throws IOException, ServletException {
		Part filePart = req.getPart("file");
		final String fileName = filePart.getSubmittedFileName();
		String imageUrl = req.getParameter("imageUrl");
		// Check extension of file
		if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
			final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
			String[] allowedExt = { "jpg", "jpeg", "png", "gif" };
			for (String s : allowedExt) {
				if (extension.equals(s)) {
//					return this.uploadFile(filePart, bucket);
					return this.uploadFile( TestUtils.getFile(), bucket );
				}
			}
			throw new ServletException("file must be an image");
		}
		return imageUrl;
	}
	// [END getImageUrl]

}
