package jmr.pr115.actions;

import java.util.concurrent.TimeUnit;

import jmr.pr115.actions.SendMessage.MessageType;
import jmr.s2db.event.EventMonitor;
import jmr.s2db.event.EventType;
import jmr.s2db.event.TimeEvent;
import jmr.s2db.tables.Event;
import jmr.util.transform.JsonUtils;

public class ReportTeslaNotCharging extends EventMonitorAction {


	public static long TIME_COOLDOWN = TimeUnit.MINUTES.toMillis( 30 );
	
	private static ReportTeslaNotCharging instance;
	
	
	public ReportTeslaNotCharging() {
		if ( null==instance ) {
			instance = this;
			
			EventMonitor.get().addListener( this );
		}
	}
	
	
	@Override
	public long getCooldownInterval() {
		return TIME_COOLDOWN;
	}
	
	
	@Override
	public void process( final Event event ) {
		if ( null==event ) return;
		if ( !EventType.TIME.equals( event.getTriggerType() ) ) return;
		
		if ( TimeEvent.TESLA_LOW_BATTERY.name().equals( event.getSubject() ) ) {

//			if ( lLastReport + TIME_COOLDOWN < event.getTime() ) {
			if ( super.checkCooldown( event.getTime() ) ) {
				
				final String strPrettyJSON = JsonUtils.getPretty( event.getData() );
				
				final String strSubject = 
						"Tesla: Low Battery " + event.getValue() + " %";
				
				final String strBody = 
						"The Tesla has a low battery and is not charging.\n\n"
						+ "Battery level: " + event.getValue() + " %\n"
						+ "Alert threshold: " + event.getThreshold() + " %\n\n"
						+ "Combined JSON:\n" + strPrettyJSON;
				
				System.out.println( "Sending email: \"" + strSubject + "\"" );
				SendMessage.send( MessageType.EMAIL, strSubject, strBody );
				System.out.println( "Email sent." );
			}
		}
	}
	
	
//	@Override
//	public void execute( final Event event ) {
	
}