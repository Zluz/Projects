package jmr.pr115.actions;

import jmr.s2db.event.EventMonitor.EventListener;

public abstract class EventMonitorAction implements EventListener {

	public abstract long getCooldownInterval();
	
	protected long lLastFiring = 0;
	
	public boolean checkCooldown( final long lNow ) {
		if ( lLastFiring + getCooldownInterval() < lNow ) {
			lLastFiring = lNow;
			return true;
		} else {
			return false;
		}
	}
	
}
