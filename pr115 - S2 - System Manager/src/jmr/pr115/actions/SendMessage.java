package jmr.pr115.actions;

import java.io.File;
import java.util.Properties;

import jmr.S2Properties;
import jmr.pr116.messaging.EmailMessage;
import jmr.pr116.messaging.EmailProvider;

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

		final EmailMessage message = new EmailMessage( 
					EmailProvider.GMAIL, 
					p.getProperty( "email.sender.address" ),
					p.getProperty( "email.sender.password" ).toCharArray() );
		
		final String strRecipient = p.getProperty( "email.receiver.address" );

		message.send( strRecipient, strSubject, strBody, attachments );
	}
	

	public static void send(	final MessageType type,
								final String strSubject,
								final String strBody ) {
		send( type, strSubject, strBody, null );
	}

	
}
