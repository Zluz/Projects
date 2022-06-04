package jmr.rpclient;

import java.util.LinkedList;
import java.util.List;

public class ModalMessage {

	public final static List<ModalMessage> MESSAGES = new LinkedList<>();
	
	
	final private String strTitle;
	final private String strBody;
	final private long lTimeCreated;
	final private long lTimeExpiration;
	boolean bActive;

	
	public ModalMessage(	final String strTitle,
						 	final String strBody,
						 	final long lDurationInSeconds ) {
		this.strTitle = strTitle;
		this.strBody = strBody;
		this.bActive = true;
		this.lTimeCreated = System.currentTimeMillis();
		if ( lDurationInSeconds > 0 ) {
			this.lTimeExpiration = lTimeCreated + 1000 * lDurationInSeconds;
		} else {
			this.lTimeExpiration = -1;
		}
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
	
	
	private boolean hasExpired( final long lTimeNow ) {
		if ( this.lTimeExpiration <= 0 ) return false;
		if ( lTimeNow > this.lTimeExpiration ) return true;
		return false;
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
					} else if ( message.hasExpired( lTimeNow ) ) {
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
