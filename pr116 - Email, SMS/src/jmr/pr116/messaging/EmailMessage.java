package jmr.pr116.messaging;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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
	
	
	private void sendJavaX( final Properties properties,
							final String strRecipient,
							final String strSubject,
							final String strBody,
							final File[] fileAttachments ) {

        final Session session = Session.getDefaultInstance( properties );
        final MimeMessage message = new MimeMessage( session );
    	final Multipart mp = new MimeMultipart();

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

        	if ( null!=strBody ) {
	        	final BodyPart bpMessage = new MimeBodyPart();
	        	bpMessage.setText( strBody );
	        	mp.addBodyPart( bpMessage );
        	}
            message.setContent(mp);
            
            if ( null!=fileAttachments && fileAttachments.length>0 ) {
            	for ( final File file : fileAttachments ) {
	            	final BodyPart bpAttachment = new MimeBodyPart();
	            	
	            	final DataSource source = new FileDataSource( file );
	            	bpAttachment.setDataHandler( new DataHandler( source ) );
	            	bpAttachment.setFileName( file.getAbsolutePath() );
	            	
	            	mp.addBodyPart( bpAttachment );
	                message.setContent(mp);
            	}
            }
            
            message.setSubject( ""+strSubject );
            
            try ( final Transport transport = session.getTransport("smtp") ) {
	            
            	System.out.println( "Connecting.." );
	            transport.connect( provider.getHost(), 
	            				strSender, new String( cPassword ) );
            	System.out.println( "Sending.." );
	            transport.sendMessage( message, message.getAllRecipients() );

            	System.out.println( "Closing.." );
	            transport.close();
            }
        } catch (AddressException ae) {
            ae.printStackTrace();
        } catch (MessagingException me) {
            me.printStackTrace();
        }
		
	}
	
	public void send(	final String strRecipient,
						final String strSubject,
						final String strBody,
						final File[] fileAttachments ) {
		
		System.out.println( "Sending message:" );
		System.out.println( "\tTo: " + strRecipient );
		System.out.println( "\tSubject: " + strSubject );
		if ( null!=strBody ) {
			System.out.println( "\tBody: " + strBody.substring( 0, 
										Math.min( 40, strBody.length() ) ) );
		} else {
			System.out.println( "\tBody: <null>" );
		}
		
		if ( EmailProvider.GMAIL_API.equals( this.provider ) ) {
			
		} else {
		
	        final Properties props = System.getProperties();
	        props.put( "mail.smtp.starttls.enable", "true" );
	        props.put( "mail.smtp.host", provider.getHost() );
	        props.put( "mail.smtp.user", strSender );
	        props.put( "mail.smtp.password", new String( cPassword ) );
	        props.put( "mail.smtp.port", provider.getPort() );
	        props.put( "mail.smtp.auth", "true");
	        
	        sendJavaX( props, strRecipient, strSubject, strBody, fileAttachments );
		}
	}
	
	
	public static void main( final String[] args ) {
		
		final LocalDateTime now = LocalDateTime.now();
		
		final Properties p = SystemUtil.getProperties();
		
		final EmailMessage message = new EmailMessage( 
					EmailProvider.GMAIL_JAVAX, 
					p.getProperty( "email.sender.address" ),
					p.getProperty( "email.sender.password" ).toCharArray() );
		
		final String strRecipient = p.getProperty( "email.receiver.address" );
		final String strFile01 = "S:\\Sessions\\B8-27-EB-4D-14-E2\\screenshot.png";
		final String strFile02 = "S:\\Sessions\\B8-27-EB-6A-F7-87\\screenshot.png";
		
		final File file01 = new File( strFile01 );
		final File file02 = new File( strFile02 );
		
		message.send(	strRecipient, 
						"pr116-subject", 
						"pr116-body test\n" + now.toString(),
						new File[] { file01, file02 } );
	}
	
}
