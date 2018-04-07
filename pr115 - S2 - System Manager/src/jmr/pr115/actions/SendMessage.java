package jmr.pr115.actions;

import java.io.File;
import java.util.Properties;

import jmr.S2Properties;
import jmr.pr116.messaging.EmailMessage;
import jmr.pr116.messaging.EmailProvider;
import jmr.pr116.messaging.TextMessage;
import jmr.pr116.messaging.TextProvider;

public class SendMessage {

	public static enum MessageType {
		EMAIL,
		TEXT, // MMS or SMS
	}
	
	
	public static void send(	final MessageType type,
								final String strSubject,
								final String strBody,
								final File[] attachments ) {

//		final Properties p = SystemUtil.getProperties();
//		final Properties p = FileSessionManager.getProperties();
		final Properties p = S2Properties.get();

		final EmailMessage email = new EmailMessage( 
				EmailProvider.GMAIL, 
				p.getProperty( "email.sender.address" ),
				p.getProperty( "email.sender.password" ).toCharArray() );

		if ( MessageType.EMAIL.equals( type ) ) {
			
			final String strRecipient = p.getProperty( "email.receiver.address" );
			email.send( strRecipient, strSubject, strBody, attachments );
			
		} else if ( MessageType.TEXT.equals( type ) ) {
			
			final TextMessage text = 
							new TextMessage( email, TextProvider.VERIZON );
			
			final String strRecipient = p.getProperty( "sms.recipient" );
			
			text.send( strRecipient, strSubject );
		}
	}
	

	public static void send(	final MessageType type,
								final String strSubject,
								final String strBody ) {
		send( type, strSubject, strBody, null );
	}

	public static void send(	final MessageType type,
								final String strSubject ) {
		send( type, strSubject, null, null );
	}
	
}
