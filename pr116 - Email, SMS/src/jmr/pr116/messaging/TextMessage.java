package jmr.pr116.messaging;

import java.time.LocalDateTime;
import java.util.Properties;

import jmr.util.SystemUtil;

public class TextMessage {

	final TextProvider provider;
	private final EmailMessage email;
	
	public TextMessage(	final EmailMessage email,
						final TextProvider provider ) {
		this.provider = provider;
		this.email = email;
	}
	
	public void send( 	final String strRecipient,
						final String strNote,
						final String strText ) {
		final LocalDateTime now = LocalDateTime.now();
		final String strRecipientEmail = 
									strRecipient + "@" + provider.getHost();
		final String strSubject;
		if ( null!=strNote ) {
			strSubject = strNote;
		} else {
			strSubject = "Text_" + now.toString();
		}
		System.out.println( "Sending text\n\tto: " + strRecipientEmail 
				+ "\n\tText: " + strText );
		email.send( strRecipientEmail, strSubject, strText, null );
	}
	
	public void send( 	final String strRecipient,
						final String strText ) {
		this.send( strRecipient, strText, strText );
	}

	

	public static void main( final String[] args ) {
		
//		final LocalDateTime now = LocalDateTime.now();
		
		final Properties p = SystemUtil.getProperties();
		
		final EmailMessage email = new EmailMessage( 
					EmailProvider.GMAIL, 
					p.getProperty( "email.sender.address" ),
					p.getProperty( "email.sender.password" ).toCharArray() );
		
		final TextMessage message = 
							new TextMessage( email, TextProvider.VERIZON );

		message.send(	p.getProperty( "sms.recipient" ),
						"pr116_subject",
//						"Project 116\ntest sms message\n" + now.toString()
						"Project 116 - body" 
								);
	}
	
}
