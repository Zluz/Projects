package jmr.pr115.actions;

import java.util.concurrent.TimeUnit;

import jmr.pr115.actions.SendMessage.MessageType;
import jmr.s2db.event.EventMonitor;
import jmr.s2db.event.EventType;
import jmr.s2db.event.TimeEvent;
import jmr.s2db.tables.Event;
import jmr.util.transform.JsonUtils;

public class ReportNestLowTemperature extends EventMonitorAction {


	public static long TIME_COOLDOWN = TimeUnit.HOURS.toMillis( 4 );
	
	private static ReportNestLowTemperature instance;
	
	
	public ReportNestLowTemperature() {
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
		
		if ( TimeEvent.NEST_LOW_TEMPERATURE.name().equals( event.getSubject() ) ) {

//			if ( lLastReport + TIME_COOLDOWN < event.getTime() ) {
			if ( super.checkCooldown( event.getTime() ) ) {
				
				final String strPrettyJSON = JsonUtils.getPretty( event.getData() );
				
				final String strSubject = 
						"Nest: Low Temperature " + event.getValue() + "º F";
				
				final String strBody = 
						"Nest has reported low temperature.\n\n"
						+ "Temperture: " + event.getValue() + "º F\n"
						+ "Threshold: " + event.getThreshold() + "º F\n\n"
						+ "JSON:\n" + strPrettyJSON;
				
				System.out.println( "Sending email: \"" + strSubject + "\"" );
				SendMessage.send( MessageType.EMAIL, strSubject, strBody );
				System.out.println( "Email sent." );
			}
		}
	}
	
	
//	@Override
//	public void execute( final Event event ) {
	
}
