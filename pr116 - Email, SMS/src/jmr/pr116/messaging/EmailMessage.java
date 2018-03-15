package jmr.pr116.messaging;

import java.time.LocalDateTime;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import jmr.util.SystemUtil;

public class EmailMessage {

	private final EmailProvider provider;
	private final String strSender;
	private final char[] cPassword;
	
	public EmailMessage(	final EmailProvider provider,
							final String strSender,
							final char[] cPassword ) {
		this.provider = provider;
		this.strSender = strSender;
		this.cPassword = cPassword;
	}
	
	public void send(	final String strRecipient,
						final String strSubject,
						final String strBody ) {
		
        final Properties props = System.getProperties();
        props.put( "mail.smtp.starttls.enable", "true" );
        props.put( "mail.smtp.host", provider.getHost() );
        props.put( "mail.smtp.user", strSender );
        props.put( "mail.smtp.password", new String( cPassword ) );
        props.put( "mail.smtp.port", provider.getPort() );
        props.put( "mail.smtp.auth", "true");

        final Session session = Session.getDefaultInstance( props );
        final MimeMessage message = new MimeMessage( session );

        try {
            message.setFrom( new InternetAddress( strSender ) );
            InternetAddress[] toAddress = new InternetAddress[ 1 ];

            // To get the array of addresses
//            for( int i = 0; i < to.length; i++ ) {
//                toAddress[i] = new InternetAddress(to[i]);
//            }
            toAddress[0] = new InternetAddress( strRecipient );

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject( strSubject );
            message.setText( strBody );
            try ( final Transport transport = session.getTransport("smtp") ) {
	            
            	System.out.println( "Connecting.." );
	            transport.connect( provider.getHost(), 
	            				strSender, new String( cPassword ) );
            	System.out.println( "Sending.." );
	            transport.sendMessage( message, message.getAllRecipients() );

            	System.out.println( "Closing.." );
	            transport.close();
            }
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
	}
	
	
	public static void main( final String[] args ) {
		
		final LocalDateTime now = LocalDateTime.now();
		
		final Properties p = SystemUtil.getProperties();
		
		final EmailMessage message = new EmailMessage( 
					EmailProvider.GMAIL, 
					p.getProperty( "email.sender.address" ),
					p.getProperty( "email.sender.password" ).toCharArray() );
		
		final String strRecipient = p.getProperty( "email.receiver.address" );
		message.send(	strRecipient, 
						"pr116-subject", 
						"pr116-body test\n" + now.toString() );
	}
	
}
