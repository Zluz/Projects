package jmr.pr120;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMultipart;

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
	
	private Thread threadMonitorInbox = null;
	
	private boolean bActive = false;
	
	private final EmailEventListener listener;
	
	
	
	public EmailControl(	final char[] cEmailAddress,
							final char[] cEmailPassword,
							final EmailEventListener listener ) {
		this.cEmailAddress = cEmailAddress;
		this.cEmailPassword = cEmailPassword;
		this.listener = listener;
	}

	
	public void start() {
		try {
			this.initializeInbox();
		} catch ( final MessagingException | IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void stop() {
		this.bActive = false;
	}
	
	
	private void submitCommand(	final Command command ) {
		if ( null==command ) return;
		if ( null==listener ) return;
		
		final EmailEvent event = new EmailEvent( command, null );
		listener.incoming( event );
	}
	
	
	private void processContent(	final Message message,
									final String strContent ) {
		if ( null==message ) return;
		if ( null==strContent ) return;
		
		System.out.println( "Processing content.. "
				+ "(" + strContent.length() + " chars)" );
		
		int iNonBlankCount = 0;
		int iCommandLineCount = 0;
		
		for ( String strLine : strContent.split( "\n" ) ) {
			strLine = strLine.trim();
			
			if ( !strLine.isEmpty() ) {
				final char cFirst = strLine.charAt( 0 );
				
				if ( Command.COMMAND_PREFIX==cFirst ) {
					// interpret as command
					System.out.println( "Examine as command: " + strLine );
					iCommandLineCount++;
					
					final Command command = Command.getCommandFrom( strLine );
					submitCommand( command );
					
				} else if ( Character.isAlphabetic( cFirst ) ) {
					if ( strLine.startsWith( "On " ) 
//							&& strLine.endsWith( " wrote:" ) ) {
							&& strLine.contains( " at " ) ) {
						// this is the first of the quoted section. ignore.
					} else if ( strLine.endsWith( "wrote:" ) ) {
						// ignore this line also
					} else {
						iNonBlankCount++;
						System.out.println( "Non-blank line ignored: " + strLine );
					}
				} else if ( ( '>'==cFirst ) || ( '|'==cFirst ) ) {
					// ignore
				} else {
					// probably not important.. ignore also
				}
			}
		}
		
		
		if ( iCommandLineCount>0 ) {
			// commands issued (possibly)
		} else if ( iNonBlankCount>0 ) {
			// some text. ignore.
		} else { // no commands, blank content, return HELP
			System.out.println( "No text, handle as HELP request" );
			submitCommand( Command.HELP );
		}
		
	}
	
	
	private void processMessage( final Message message ) {

		final int iNumber = message.getMessageNumber();
		
		try {

			final long lDate = message.getSentDate().getTime();
//				final ZoneOffset tz = ZoneOffset.of( "EST" );
			final ZoneOffset tz = ZoneOffset.UTC;
			final LocalDateTime ldt = LocalDateTime.ofEpochSecond( lDate, 0, tz ) ;
			
			
			System.out.println( "  Email message #" + iNumber + ", "
						+ "size:" + message.getSize() + " bytes,  "
						+ "sent:" + ldt.toString() + ",  "
						+ "subject:" + message.getSubject() );
			final String strContent = message.getContent().toString();
			final Object objContent = message.getContent();
			
			if ( objContent instanceof MimeMultipart ) {
				final MimeMultipart multi = (MimeMultipart) message.getContent();
				final int iCount = multi.getCount();
				
				System.out.println( "\tContent is MimeMultipart, " 
								+ iCount + " part(s)" );
				
				for ( int i=0; i<iCount; i++ ) {
					final BodyPart part = multi.getBodyPart( i );
					
					final Object objPart = part.getContent();
					final String strPart = objPart.toString();
					final String strType = part.getContentType();
					final String strClass = objPart.getClass().getName();
					
					/*
					 * Typically 2 parts (text and html)
					 * strType will typically be 
					 * 		(1) "type TEXT/PLAIN; charset=UTF-8" and 
					 * 		(2) "type TEXT/HTML; charset=UTF-8" 
					 * strClass will typically be java.lang.String
					 */
					
					System.out.println( "\t\tpart " + i + " content details: "
								+ "type " + strType + ", "
								+ "class " + strClass + ", "
								+ "string length " + strPart.length() );
//					System.out.println( "\t\t  content toString(): " + 
//							strPart.substring( 0, 
//									Math.min( 60, strPart.length() ) ) );
					
//					if ( "TEXT/PLAIN".equalsIgnoreCase( strType ) ) {
					if ( strType.toUpperCase().startsWith( "TEXT/PLAIN" ) ) {
						processContent( message, strPart );
					}
				}
			} else {
				System.out.println( "      body: " + 
							strContent.substring( 0, 
									Math.min( 60, strContent.length() ) ) );
				processContent( message, strContent );
			}
			
		} catch ( final MessagingException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	private void initializeInbox() throws MessagingException, IOException {
		if ( this.bActive ) return;
		if ( null!=this.threadMonitorInbox ) return;
		
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
		
		
		if ( !Boolean.TRUE.equals( bSupportsIDLE ) ) {
			throw new IllegalStateException( 
					"Email (" + new String( this.cEmailAddress ) 
					+ ") does not seem to support IMAP IDLE." );
		}
		
		
	
	
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

				System.out.println( "\tprocessing " 
							+ event.getMessages().length + " message(s)" );

				for ( final Message message : event.getMessages() ) {
					processMessage( message );
				}
			}
		} );
		
		this.bActive = true;
		
		this.threadMonitorInbox = new Thread( "Monitor IMAP Inbox" ) {
			@Override
			public void run() {
				try {
					while ( bActive ) {

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
										new String( cEmailAddress ), 
										new String( cEmailPassword )  );
								
							}
							
						} catch ( final Exception e ) {
							if ( e.toString().contains( " * BYE" ) ) {
								System.out.println( "IMAP connection dropped." );
								// normal for the server to drop the 
								// connection periodically. just reconnect.
							} else {
								System.err.println( 
	//									"Exception during IMAPFolder.idle(): " 
										"Exception while maintaining IMAPFolder: " 
														+ e.toString() );
								e.printStackTrace();
							}
						}

						Thread.sleep( 100 );
						
					}
				} catch ( final InterruptedException e ) {
					// quitting..
					bActive = false;
				}
			}
		};
		
		threadMonitorInbox.start();
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

		final EmailEventListener listener = new EmailEventListener() {
			@Override
			public void incoming( final EmailEvent event ) {
				System.out.println( "EmailEventListener.incoming() - " 
													+ event.command );
			}
		};
		
		final EmailControl 
				control = new EmailControl( cUsername, cPassword, listener );
		control.start();

		for (;;) {
			Thread.sleep( 100 );
		}
	}

}
