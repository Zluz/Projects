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
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import jmr.util.SUProperty;
import jmr.util.SystemUtil;

// good example, this works..
// https://stackoverflow.com/questions/23424003/right-way-to-poll-gmail-inbox-for-incoming-mail-from-stand-alone-application
//
// (Nevermind this link; says Monitor but has nothing to do with monitoring)
// XXXXX https://www.developer.com/java/data/monitoring-email-accounts-imap-in-java.html

public class EmailControl {

	
	private final char[] cEmailAddress;
	private final char[] cEmailPassword;
	
	private IMAPFolder imapInbox = null;
	
	
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
		p.setProperty( "mail.imaps.host",  "imap.gmail.com" );
		p.setProperty( "mail.store.port",  "993" );
		p.setProperty( "mail.store.timeout",  "10000" );
		
//		final Session session = Session.getDefaultInstance( p );
		final Session session = Session.getInstance( p );
		
		final Store store = session.getStore( "imaps" );

		store.connect( 	"imap.gmail.com", 
						new String( this.cEmailAddress ), 
						new String( this.cEmailPassword )  );
		
		
		final Boolean bSupportsIDLE;

		final IMAPStore imapstore;

		if ( store instanceof IMAPStore ) {
			imapstore = (IMAPStore) store;
			
			if ( imapstore.hasCapability( "IDLE" ) ) {
				bSupportsIDLE = true;
			} else {
				bSupportsIDLE = false;
			}
		} else {
			imapstore = null;
			bSupportsIDLE = false;
		}
		
		
		
		
		
		if ( bSupportsIDLE ) {
			this.imapInbox = (IMAPFolder) imapstore.getFolder( "INBOX" );
			
			System.out.println( "Email supports IDLE. Adding listener.." );
			
			imapInbox.addMessageCountListener( new MessageCountListener() {
				
				@Override
				public void messagesRemoved( final MessageCountEvent event ) {
					System.out.println( "MessageCountListener.messagesRemoved()" );
				}
				
				@Override
				public void messagesAdded( final MessageCountEvent event ) {
					System.out.println( "MessageCountListener.messagesAdded()" );
				}
			} );
			
//			final IdleThread threadIdle = new IdleThread( inbox );
			
			try {
				for (;;) {
					
					try {
						
						if ( !imapInbox.isOpen() ) {
							System.out.println( "Opening IMAPFolder.." );
							imapInbox.open( Folder.READ_ONLY );
						}
						
						final Store storeTest = imapInbox.getStore();
						if ( null!=storeTest && storeTest.isConnected() ) {
						
							imapInbox.idle();
							
						} else {
							
							System.out.println( "Reconnecting Store.." );
							storeTest.connect( 	"imap.gmail.com", 
									new String( this.cEmailAddress ), 
									new String( this.cEmailPassword )  );
							
						}
					} catch ( final Exception e ) {
						System.err.println( 
//								"Exception during IMAPFolder.idle(): " 
								"Exception while maintaining IMAPFolder: " 
												+ e.toString() );
						e.printStackTrace();
					}

					Thread.sleep( 100 );
				}
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
			
			
			
		} else {
			System.out.println( "Email does NOT support IDLE." );
		
			
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
	}
	
	
	
	
	
	public static void main( final String[] args ) 
				throws MessagingException, IOException, InterruptedException {

		System.out.println( "Registering S2 client.." );
//		final String strSession = NetUtil.getSessionID();
//		final String strClass = EmailControl.class.getName();
//		Client.get().register( strSession, strClass );

		final char[] cUsername = SystemUtil.getProperty( 
						SUProperty.CONTROL_EMAIL_USERNAME ).toCharArray(); 
		final char[] cPassword = SystemUtil.getProperty( 
						SUProperty.CONTROL_EMAIL_PASSWORD ).toCharArray(); 

//		final Client client = Client.get();
//		client.register( "test", EmailControl.class.getName() );

		System.out.println( "Retrieving recent email.." );

		final EmailControl control = new EmailControl( cUsername, cPassword );
		control.scanInbox();

		for (;;) {
			Thread.sleep( 100 );
		}
	}

}
