package jmr.pr116.messaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
//import java.util.List;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users;
import com.google.api.services.gmail.Gmail.Users.Messages;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;


public class GmailQuickstart {

	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying
	 * these scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections
			.singletonList(GmailScopes.GMAIL_LABELS);
	private static final String CREDENTIALS_FILE_PATH 
					//= "/credentials.json";
			= "C:\\Development\\File\\Google_Credentials\\"
					+ "credentials_20191021_2305.json";
//	= "/Development/File/Google_Credentials/"
//			+ "credentials_20191021_2305.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(
			final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
//		InputStream in = GmailQuickstart.class
//				.getResourceAsStream(CREDENTIALS_FILE_PATH);
//		if (in == null) {
//			throw new FileNotFoundException(
//					"Resource not found: " + CREDENTIALS_FILE_PATH);
//		}
		
		final File fileCreds = new File( CREDENTIALS_FILE_PATH );
		final FileInputStream ifs = new FileInputStream( fileCreds );
		final InputStreamReader isr = new InputStreamReader( ifs );
		
		GoogleClientSecrets clientSecrets = GoogleClientSecrets
//				.load(JSON_FACTORY, new InputStreamReader(in));
				.load( JSON_FACTORY, isr );

		// Build flow and trigger user authorization request.
		final GoogleAuthorizationCodeFlow 
				flow = new GoogleAuthorizationCodeFlow.Builder(
						HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(
								new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder()
				.setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver)
				.authorize("user");
	}

	
	public static void main(String... args)
							throws IOException, GeneralSecurityException {
		
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport
				.newTrustedTransport();
		final Gmail service = new Gmail.Builder(
									HTTP_TRANSPORT, JSON_FACTORY,
									getCredentials(HTTP_TRANSPORT) )
						.setApplicationName(APPLICATION_NAME)
						.build();

		// Print the labels in the user's account.
//		final String user = "me";
		final String user = "hazysky.s2";
		final Users users = service.users();
		final ListLabelsResponse 
						listResponse = users.labels().list(user).execute();
		final List<Label> labels = listResponse.getLabels();
		
		if (labels.isEmpty()) {
			System.out.println("No labels found.");
		} else {
			System.out.println("Labels:");
			for (Label label : labels) {
				System.out.printf("- %s\n", label.getName());
			}
		}
		
		final Messages messages = users.messages();
		final com.google.api.services.gmail.Gmail.Users.Messages.List 
							list = messages.list( user );
		final ListMessagesResponse response = list.execute();
		final List<Message> listMessages = response.getMessages();
		int i = 0;
		System.out.println( "Message IDs:" );
		for ( final Message msg : listMessages ) {
			i++;
			final String strId = msg.getId();
			System.out.println( strId );
			if ( i>10 ) break;
		}
//		List<Message> result = new List<>();
//		result.addRange( response.getMessages() );
	}
}
