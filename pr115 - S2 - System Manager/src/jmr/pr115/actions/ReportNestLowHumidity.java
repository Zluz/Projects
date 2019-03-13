package jmr.pr115.actions;

import java.util.concurrent.TimeUnit;

import jmr.pr115.actions.SendMessage.MessageType;
import jmr.s2db.event.EventMonitor;
import jmr.s2db.event.EventType;
import jmr.s2db.event.TimeEvent;
import jmr.s2db.tables.Event;
import jmr.util.transform.JsonUtils;

public class ReportNestLowHumidity extends EventMonitorAction {


	public static long TIME_COOLDOWN = TimeUnit.MINUTES.toMillis( 60 );
	
	private static ReportNestLowHumidity instance;
	
	
	public ReportNestLowHumidity() {
		if ( null==instance ) {
			instance = this;
			
			EventMonitor.get().addListener( this, this.getClass().getName() );
		}
	}
	
	
	@Override
	public long getCooldownInterval() {
		return TIME_COOLDOWN;
	}
	
	
	@Override
	public void process( final Event event ) {
		if ( null==event ) return;
		if ( !EventType.TIME.equals( event.getEventType() ) ) return;
		
		if ( TimeEvent.NEST_LOW_HUMIDITY.name().equals( event.getSubject() ) ) {

//			if ( lLastReport + TIME_COOLDOWN < event.getTime() ) {
			if ( super.checkCooldown( event.getTime() ) ) {
				
				final String strPrettyJSON = JsonUtils.getPretty( event.getData() );
				
				final String strSubject = 
						"Nest: Low Humidity " + event.getValue() + " %";
				
				final String strBody = 
						"Nest has reported low humidity.\n\n"
						+ "Humidity: " + event.getValue() + " %\n"
						+ "Threshold: " + event.getThreshold() + " %\n\n"
						+ "JSON (device):\n" + strPrettyJSON;
				
				System.out.println( "Sending email: \"" + strSubject + "\"" );
				SendMessage.send( MessageType.EMAIL, strSubject, strBody );
				System.out.println( "Email sent." );
			}
		}
	}
	
	
//	@Override
//	public void execute( final Event event ) {
	
}
