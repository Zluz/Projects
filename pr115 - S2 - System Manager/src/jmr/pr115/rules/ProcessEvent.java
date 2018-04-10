package jmr.pr115.rules;

import jmr.pr115.actions.EventMonitorAction;
import jmr.s2db.event.EventMonitor;
import jmr.s2db.tables.Event;

public class ProcessEvent extends EventMonitorAction {

	public ProcessEvent() {
		System.out.println( "--- ProcessEvent instantiated" );
		EventMonitor.get().addListener( this );
	}

	@Override
	public void process( final Event event ) {
		RulesProcessing.get().process( event );
	}

	@Override
	public long getCooldownInterval() {
		return 1;
	}
	
	
}
