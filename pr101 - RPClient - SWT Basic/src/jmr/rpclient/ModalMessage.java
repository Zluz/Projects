package jmr.rpclient;

import java.util.LinkedList;
import java.util.List;

public class ModalMessage {

	public final static List<ModalMessage> MESSAGES = new LinkedList<>();
	
	
	final String strTitle;
	final String strBody;
	final long lTimeCreated;
	final long lTimeExpiration;
	boolean bActive;

	
	public ModalMessage(	final String strTitle,
						 	final String strBody,
						 	final long lDurationInSeconds ) {
		this.strTitle = strTitle;
		this.strBody = strBody;
		this.bActive = true;
		this.lTimeCreated = System.currentTimeMillis();
		this.lTimeExpiration = lTimeCreated + 1000 * lDurationInSeconds;
	}
	
	public String getTitle() {
		return this.strTitle;
	}
	
	public String getBody() {
		return this.strBody;
	}
	
	public long getRemainingMS( final long lTimeNow ) {
		return this.lTimeExpiration - lTimeNow;
	}
	
	public void close() {
		this.bActive = false;
		synchronized (MESSAGES) {
			MESSAGES.remove( this );
		}
	}
	
	
	public static void add( final ModalMessage message ) {
		synchronized (MESSAGES) {
			MESSAGES.add( message );
		}
	}
	
	public static ModalMessage getNext( final long lTimeNow ) {
//		final long lTimeNow = System.currentTimeMillis();
		synchronized (MESSAGES) {
			if ( MESSAGES.isEmpty() ) {
				return null;
			} else {
				while ( ! MESSAGES.isEmpty() ) {
					final ModalMessage message = MESSAGES.get( 0 );
					if ( ! message.bActive ) {
						MESSAGES.remove( message );
					} else if ( lTimeNow > message.lTimeExpiration ) {
						message.bActive = false;
						MESSAGES.remove( message );
					} else {
						return message;
					}
				}
			}
			return null;
		}
	}
	
}
