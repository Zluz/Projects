package jmr.pr121.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class CloudStorage02 {

	/*
	 * https://github.com/GoogleCloudPlatform/getting-started-java/blob/master/
	 * bookshelf/3-binary-data/src/main/java/com/example/getstarted/util/
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
//	public String uploadFile(Part filePart, final String bucketName) throws IOException {
	public String uploadFile(	final File file, 
								final String bucketName ) throws IOException {
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern("-YYYY-MM-dd-HHmmssSSS");
		DateTime dt = DateTime.now(DateTimeZone.UTC);
		
		String dtString = dt.toString(dtf);
//		final String fileName = filePart.getSubmittedFileName() + dtString;
		final String fileName = file.getName() + dtString;
		final InputStream stream = new FileInputStream( file );

		// the inputstream is closed by default, so we don't need to close it here
		final ArrayList<Acl> arrACL = new ArrayList<>(
				Arrays.asList( Acl.of( User.ofAllUsers(), Role.READER ) ) );
		
		final BlobInfo blobInfo = storage.create(
				BlobInfo.newBuilder(bucketName, fileName)
				// Modify access list to allow all users with link to read file
					.setAcl(arrACL).build(),
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
