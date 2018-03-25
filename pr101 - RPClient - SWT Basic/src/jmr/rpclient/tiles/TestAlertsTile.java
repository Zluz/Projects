package jmr.rpclient.tiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jmr.S2Properties;
import jmr.pr116.messaging.EmailMessage;
import jmr.pr116.messaging.EmailProvider;
import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.tables.Job;

public class TestAlertsTile extends TileBase {

	
	public static enum AlertDetail {
		TEST_EMAIL( "Test Email", "/Local/scripts/test_email.sh" ),
		TEST_SMS( "Test SMS", "/Local/scripts/test_sms.sh" ),
//		TEST_EMAIL_ATTACHMENTS( "Attachments", "/Local/scripts/play_vlc.sh" ),
//		TEST_MMS( "MMS", "/Local/scripts/play_twit.sh" ),
//		TEST_AUDIO_CHIME( "Chime", "/Local/scripts/play_wtop.sh" ),
//		TEST_AUDIO_SIREN( "Siren", "/Local/scripts/play_npr.sh" ),
		;
		
		public final String strTitle;
		public final String strScript;
		
		AlertDetail(	final String strTitle,
						final String strScript ) {
			this.strTitle = strTitle;
			this.strScript = strScript;
		}
	}
	
	


	
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		try {

			final GCTextUtils text = new GCTextUtils( gc );
			text.setRect( gc.getClipping() );
		

			gc.setFont( Theme.get().getFont( 11 ) );

//			text.println( "Test Alerts" );
			
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );

			int iY = 16;
			int iX = 14;
			for ( final AlertDetail alert : AlertDetail.values() ) {
				super.addButton( gc, alert.ordinal(), 
						iX, iY,  132, 52, alert.strTitle );
				if ( iY > 50 ) {
					iY = 16;
					iX += 148;
				} else {
					iY += 70;
				}
			}
			

			gc.setForeground( Theme.get().getColor( Colors.LINE_FAINT ) );
			gc.drawLine( 299, 0, 299, 10 );
			gc.drawLine( 290, 0, 299, 0 );

			gc.drawLine( 299, 140, 299, 149 );
			gc.drawLine( 290, 149, 299, 149 );

		} catch ( final Throwable t ) {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
			gc.setFont( Theme.get().getFont( 16 ) );
			gc.drawText( t.toString(), 10, 50 );
		}
		
	}


	@Override
	public boolean clickCanvas( final Point point ) {
		return false;
	}


	private void play(	final S2Button button,
						final AlertDetail alert ) {

		System.out.println( "Selected alert: " + alert.strTitle );

		final Thread thread = new Thread( "Button action (TestAlertsTile)" ) {
			public void run() {

				final Map<String,String> map = new HashMap<String,String>();
				final Job job;
				
				final LocalDateTime now = LocalDateTime.now();	
				final String strDevice = Client.get().getThisDevice().getName();
						
				
				if ( AlertDetail.TEST_EMAIL.equals( alert ) ) {
					final String strSubject = "TEST_EMAIL " + now.toString();
					final String strBody = "TEST_BODY\n"
							+ "Device: " + strDevice + "\n"
							+ "Time (ISO): " + now.toString() + "\n"
							+ "Time (System): " + System.currentTimeMillis() + "\n"
							+ "Thread: " + Thread.currentThread().getName();
					

//					final Properties p = SystemUtil.getProperties();
//					final Properties p = FileSessionManager.getProperties();
					final Properties p = S2Properties.get();

					final EmailMessage message = new EmailMessage( 
								EmailProvider.GMAIL, 
								p.getProperty( "email.sender.address" ),
								p.getProperty( "email.sender.password" ).toCharArray() );
					
					final String strRecipient = p.getProperty( "email.receiver.address" );

					button.setState( ButtonState.WORKING );
					message.send( strRecipient, strSubject, strBody, null );
					button.setState( ButtonState.READY );
					job = null;

//					map.put( "subject", strSubject );
//					map.put( "body", strBody );
//					job = Job.add( JobType.SEND_EMAIL, map );
					
				} else if ( AlertDetail.TEST_SMS.equals( alert ) ) {
					map.put( "message", "TEST_TEXT_" + now.toString() );
//					job = Job.add( JobType.SEND_TEXT, map );
					job = null;

					button.setState( ButtonState.DISABLED );

				} else {
					job = null;
				}
				
//				if ( null!=job ) {
					button.setJob( job );
//				}
			};
		};
		thread.start();
	}
	
	
	@Override
	protected void activateButton( final S2Button button ) {
		for ( final AlertDetail program : AlertDetail.values() ) {
			if ( program.ordinal()==button.getIndex() ) {
				play( button, program );
			}
		}
	}
	
	
}
