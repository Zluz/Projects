package jmr.p121.comm;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
//[END simple_includes]

/*
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
*/





import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.appengine.api.mail.MailService;

import jmr.pr121.config.Configuration;
import jmr.pr121.servlets.Log;

//import com.google.appengine.api.mail.MailService.Message;

public class GAEEmail {

	public static void sendTestEmail() {

		final String strSender = "test@pr121-s2gae.appspotmail.com";
		final String strTo = Configuration.get().get( "nest.username" );
		final String strSubject = "Test Subject";
		final String strBody = "Test Body";

		Log.add( "Starting GAEEmail.sendTestEmail()..(simple).." );
		Log.add( "\tAttempting to send email to " + strTo );

		try {
			
			
//			try {
//			    Class.forName("com.google.appengine.repackaged.com.google.common.base.internal.Finalizer");
////			    Class.forName("com.google.appengine.api.mail.MailServicePb$MailMessage");
////			    final com.google.appengine.api.mail.MailServicePb msp = null;
////			    final com.google.appengine.api.mail.MailServicePb.MailMessage mm = null;
//			    
//			    final com.google.appengine.repackaged.com.google.common.base.internal.Finalizer f = null;
//			    
//			} catch (ClassNotFoundException e) {
//			    e.printStackTrace();
//			}
			
			
			MailService service = null; 

			
			try {
				Log.add( "Experimenting with classes.." );

//			    Class.forName("com.google.appengine.repackaged.com.google.common.base.internal.Finalizer");
/*
java.lang.ClassNotFoundException: com.google.appengine.repackaged.com.google.common.base.internal.Finalizer
	at java.net.URLClassLoader.findClass(URLClassLoader.java:381)
	at com.google.apphosting.runtime.ApplicationClassLoader.findClass(ApplicationClassLoader.java:135)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:265)
	at jmr.p121.comm.GAEEmail.sendTestEmail(GAEEmail.java:106)
 */
//			    Class.forName("com.google.appengine.api.mail.MailServicePb$MailMessage");
//			    final com.google.appengine.api.mail.MailServicePb msp = null;
//			    final com.google.appengine.api.mail.MailServicePb.MailMessage mm = null;
			    
			    final com.google.appengine.repackaged.com.google.common.base.internal.Finalizer f = null;
//			    new com.google.appengine.repackaged.com.google.common.base.internal.Finalizer(null, null, null);
				
				Class.forName("com.google.appengine.api.mail.MailServicePb");
//			    Class.forName("com.google.appengine.api.mail.MailServicePb$MailMessage");
			    final com.google.appengine.api.mail.MailServicePb msp = null;
//			    final com.google.appengine.api.mail.MailServicePb.MailMessage mm = null;
			    
//			    final int service = com.google.appengine.api.mail.MailServiceFactory().getMailService();
			    service = com.google.appengine.api.mail.MailServiceFactory.getMailService();
				Log.add( "\tMailService: " + service );
				
				
				final com.google.appengine.api.mail.MailService.Message 
						message = new MailService.Message( strSender, strTo, strSubject, strBody );
				
				Log.add( "\tAbout to send (service).." );
				service.send( message ); // this works on Production
				Log.add( "\tSent! (service)" );
/*
java.lang.AssertionError: java.lang.NoSuchMethodException: com.google.appengine.repackaged.com.google.common.base.internal.Finalizer.startFinalizer(java.lang.Class, java.lang.Object)
	at com.google.appengine.repackaged.com.google.common.base.FinalizableReferenceQueue.getStartFinalizer(FinalizableReferenceQueue.java:313)
	at com.google.appengine.repackaged.com.google.common.base.FinalizableReferenceQueue.<clinit>(FinalizableReferenceQueue.java:105)
	at com.google.appengine.repackaged.com.google.common.collect.Interners$WeakInterner.<clinit>(Interners.java:118)
	at com.google.appengine.repackaged.com.google.common.collect.Interners.newWeakInterner(Interners.java:59)
	at com.google.appengine.repackaged.com.google.io.protocol.ProtocolSupport.<clinit>(ProtocolSupport.java:55)
	at com.google.appengine.api.mail.MailServicePb$MailMessage.<init>(MailServicePb.java:655)
	at com.google.appengine.api.mail.MailServicePb$MailMessage$1.<init>(MailServicePb.java:1618)
	at com.google.appengine.api.mail.MailServicePb$MailMessage.<clinit>(MailServicePb.java:1618)
	at com.google.appengine.api.mail.MailServiceImpl.doSend(MailServiceImpl.java:49)
	at com.google.appengine.api.mail.MailServiceImpl.send(MailServiceImpl.java:32)
	at jmr.p121.comm.GAEEmail.sendTestEmail(GAEEmail.java:88)
 */
				
				
				
				
				
			} catch ( final Throwable t ) {
				Log.add( "ERROR during GAEEmail.sendTestEmail() (class-loading): " + t.toString() );
				Log.add( ExceptionUtils.getStackTrace( t ) );
			}
			
			
			
			
			
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
	
			try {
			  Message msg = new MimeMessage(session);
	//		  msg.setFrom(new InternetAddress("admin@example.com", "Example.com Admin"));
			  msg.setFrom(new InternetAddress( strSender, "Test Email Sender"));
			  msg.addRecipient(Message.RecipientType.TO,
			                   new InternetAddress( strTo, "Test Email Recipient"));
			  msg.setSubject("Test Email (simple) - Subject");
			  msg.setText("Test Email (simple) - Text");
			  
			  
			  Log.add( "\tAbout to send (simple).." );
			  Transport.send(msg); // this works on Production
			  Log.add( "\tSent! (simple)" );

/*
java.lang.NoClassDefFoundError: Could not initialize class com.google.appengine.api.mail.MailServicePb$MailMessage
	at com.google.appengine.api.mail.MailServiceImpl.doSend(MailServiceImpl.java:49)
	at com.google.appengine.api.mail.MailServiceImpl.send(MailServiceImpl.java:32)
	at com.google.appengine.api.mail.stdimpl.GMTransport.sendMessage(GMTransport.java:247)
	at javax.mail.Transport.send(Transport.java:95)
	at javax.mail.Transport.send(Transport.java:48)
	at jmr.p121.comm.GAEEmail.sendTestEmail(GAEEmail.java:164)
 */
			} catch (AddressException e) {
			  // ...
			} catch (MessagingException e) {
			  // ...
			} catch (UnsupportedEncodingException e) {
			  // ...
			}
			
		} catch ( final Throwable t ) {
			Log.add( "ERROR during GAEEmail.sendTestEmail() (simple): " + t.toString() );
			Log.add( ExceptionUtils.getStackTrace( t ) );
		}
		
		Log.add( "Starting GAEEmail.sendTestEmail()..(multi-part).." );
		
		try {
//			
//			
//			try {
//			    Class.forName("com.google.appengine.api.mail.MailServicePb");
////			    Class.forName("com.google.appengine.api.mail.MailServicePb$MailMessage");
//			    final com.google.appengine.api.mail.MailServicePb msp = null;
////			    final com.google.appengine.api.mail.MailServicePb.MailMessage mm = null;
//			    
////			    final int service = com.google.appengine.api.mail.MailServiceFactory().getMailService();
//			    final MailService service = com.google.appengine.api.mail.MailServiceFactory.getMailService();
//			    
//				Log.add( "MailService: " + service );
//
//			} catch ( final Throwable t ) {
//				Log.add( "ERROR during GAEEmail.sendTestEmail() (class-loading): " + t.toString() );
//				Log.add( ExceptionUtils.getStackTrace( t ) );
//			}
//			
//			
			
			
//		    Properties props = new Properties();
//		    Session session = Session.getDefaultInstance(props, null);
//
//		    String msgBody = "Test Email (multi) - Text";
//
//		    try {
//		      Message msg = new MimeMessage(session);
////		      msg.setFrom(new InternetAddress("admin@example.com", "Example.com Admin"));
//			  msg.setFrom(new InternetAddress( strSender, "Test Email Sender"));
////		      msg.addRecipient(Message.RecipientType.TO,
////		                       new InternetAddress("user@example.com", "Mr. User"));
//			  msg.addRecipient(Message.RecipientType.TO,
//	                   new InternetAddress( strTo, "Test Email Recipient"));
//			  msg.setSubject("Test Email (multi) - Subject");
////		      msg.setSubject("Your Example.com account has been activated");
//		      msg.setText(msgBody);
//
//		      // [START multipart_example]
//		      String htmlBody = "<html>html body</html>";          // ...
//		      byte[] attachmentData = "Attachment data".getBytes();
//		      Multipart mp = new MimeMultipart();
//
//		      MimeBodyPart htmlPart = new MimeBodyPart();
//		      htmlPart.setContent(htmlBody, "text/html");
//		      mp.addBodyPart(htmlPart);
//
//		      MimeBodyPart attachment = new MimeBodyPart();
//		      InputStream attachmentDataStream = new ByteArrayInputStream(attachmentData);
//		      attachment.setFileName("manual.pdf");
//		      attachment.setContent(attachmentDataStream, "application/pdf");
//		      mp.addBodyPart(attachment);
//
//		      msg.setContent(mp);
//		      // [END multipart_example]
//
//			  Log.add( "\tAbout to send.." );
//
//		      Transport.send(msg);
///*
//com.google.apphosting.api.ApiProxy$OverQuotaException: The API call mail.Send() required more quota than is available.
//	at java.lang.Thread.getStackTrace(Thread.java:1556)
//	at com.google.apphosting.runtime.ApiProxyImpl.doSyncCall(ApiProxyImpl.java:328)
//	at com.google.apphosting.runtime.ApiProxyImpl.access$000(ApiProxyImpl.java:69)
//	at com.google.apphosting.runtime.ApiProxyImpl$1.run(ApiProxyImpl.java:242)
//	at com.google.apphosting.runtime.ApiProxyImpl$1.run(ApiProxyImpl.java:239)
//	at java.security.AccessController.doPrivileged(Native Method)
//	at com.google.apphosting.runtime.ApiProxyImpl.makeSyncCall(ApiProxyImpl.java:238)
//	at com.google.apphosting.runtime.ApiProxyImpl.makeSyncCall(ApiProxyImpl.java:69)
//	at com.google.apphosting.api.ApiProxy.makeSyncCall(ApiProxy.java:119)
//	at com.google.apphosting.api.ApiProxy.makeSyncCall(ApiProxy.java:66)
//	at com.google.appengine.api.mail.MailServiceImpl.doSend(MailServiceImpl.java:111)
//	at com.google.appengine.api.mail.MailServiceImpl.send(MailServiceImpl.java:34)
//	at com.google.appengine.api.mail.stdimpl.GMTransport.sendMessage(GMTransport.java:269)
//	at javax.mail.Transport.send(Transport.java:95)
//	at javax.mail.Transport.send(Transport.java:48)
//	at jmr.p121.comm.GAEEmail.sendTestEmail(GAEEmail.java:255)
// */
///*
//java.lang.NoClassDefFoundError: Could not initialize class com.google.appengine.api.mail.MailServicePb$MailMessage
//	at com.google.appengine.api.mail.MailServiceImpl.doSend(MailServiceImpl.java:49)
//	at com.google.appengine.api.mail.MailServiceImpl.send(MailServiceImpl.java:32)
//	at com.google.appengine.api.mail.stdimpl.GMTransport.sendMessage(GMTransport.java:247)
//	at javax.mail.Transport.send(Transport.java:95)
//	at javax.mail.Transport.send(Transport.java:48)
//	at jmr.p121.comm.GAEEmail.sendTestEmail(GAEEmail.java:240)
// */
//		    } catch (AddressException e) {
//		      // ...
//		    } catch (MessagingException e) {
//		      // ...
//		    } catch (UnsupportedEncodingException e) {
//		      // ...
//		}
			
			
			
		} catch ( final Throwable t ) {
			Log.add( "ERROR during GAEEmail.sendTestEmail() (multi): " + t.toString() );
			Log.add( ExceptionUtils.getStackTrace( t ) );
		}
		
	}
	
}
