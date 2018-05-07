package jmr.pr120;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import jmr.s2db.Client;
import jmr.util.NetUtil;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;

// also check
// https://www.developer.com/java/data/monitoring-email-accounts-imap-in-java.html

public class EmailControl {

	
	private final char[] cEmailAddress;
	private final char[] cEmailPassword;
	
	
	public static class EmailEvent {
		
		public final Command command;
		public final Parameter parameter;
		
		EmailEvent( final Command command,
					final Parameter parameter ) {
			this.command = command;
			this.parameter = parameter;
		}
	}
	
	public static interface EmailEventListener {
		public void incoming( final EmailEvent event );
	}
	
	
	public EmailControl(	final char[] cEmailAddress,
							final char[] cEmailPassword ) {
		this.cEmailAddress = cEmailAddress;
		this.cEmailPassword = cEmailPassword;
	}
	
	
	private void scanInbox() throws MessagingException, IOException {

		final Properties p = new Properties();
		p.setProperty( "mail.store.protocol",  "imaps" );
		final Session session = Session.getDefaultInstance( p );
		final Store store = session.getStore( "imaps" );
		store.connect( 	"imap.gmail.com", 
						new String( this.cEmailAddress ), 
						new String( this.cEmailPassword )  );
		
		final Folder folder = store.getFolder( "INBOX" );
		folder.open( Folder.READ_ONLY );
		
		final int count = folder.getMessageCount();
		System.out.println( "Message count: " + count );
//		final Message messages[] = folder.getMessages();
//		for ( final Message message : messages ) {
		for ( int i=count; i>count-4; i-- ) {
//			final Message message = messages[i];
			final Message message = folder.getMessage( i );
			
			final long lDate = message.getSentDate().getTime();
//			final ZoneOffset tz = ZoneOffset.of( "EST" );
			final ZoneOffset tz = ZoneOffset.UTC;
			final LocalDateTime ldt = LocalDateTime.ofEpochSecond( lDate, 0, tz ) ;
			
			System.out.println( "  Email " + i + ", "
						+ "size:" + message.getSize() + " bytes,  "
						+ "sent:" + ldt.toString() + ",  "
						+ "subject:" + message.getSubject() );
			final String strContent = message.getContent().toString();
			System.out.println( "      body: " + 
						strContent.substring( 0, Math.min( 60, strContent.length() ) ) );
		}
		
		folder.close();
		store.close();
	}
	
	
	
	
	
	public static void main( final String[] args ) throws MessagingException, IOException {

		System.out.println( "Registering S2 client.." );
		final String strSession = NetUtil.getSessionID();
		final String strClass = EmailControl.class.getName();
		Client.get().register( strSession, strClass );

		final char[] cUsername = SystemUtil.getProperty( 
						SUProperty.CONTROL_EMAIL_USERNAME ).toCharArray(); 
		final char[] cPassword = SystemUtil.getProperty( 
						SUProperty.CONTROL_EMAIL_PASSWORD ).toCharArray(); 

		final Client client = Client.get();
		client.register( "test", EmailControl.class.getName() );

		System.out.println( "Retrieving recent email.." );

		final EmailControl control = new EmailControl( cUsername, cPassword );
		control.scanInbox();

	}

}
