package jmr.s2db.trigger;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonElement;

import jmr.s2db.tables.Trigger;

public class TriggerManager {

	
	private final List<Trigger> listTriggersHosted = new LinkedList<>();
	
	
	private static TriggerManager instance = null;
	
	private TriggerManager() {};
	
	
	public static synchronized TriggerManager getInstance() {
		if ( null==instance ) {
			instance = new TriggerManager();
		}
		return instance;
	}
	
	
	public Trigger registerTrigger( 	final TriggerType type,
										final TriggerDetail detail,
										final String strMatch,
										final Runnable runnable ) {
		if ( null==type ) return null;
		
		final String strURL = "http:\\\\localhost\\";
		
		final Trigger trigger = 
				Trigger.add( type, detail, strMatch, strURL );
		
		this.listTriggersHosted.add( trigger );
		return trigger;
	}
	
	
	public List<Trigger> getTriggersLike(
										final TriggerType event,
										final String strDetailLike ) {
		final List<Trigger> list = 
						Trigger.getTriggersLike( event, strDetailLike );
		return list;
	}
	
	
	public void fireTrigger( 	final Trigger trigger,
								final JsonElement data ) {
		//TODO
	}
	
	
	public void close() {
		for ( final Trigger trigger : this.listTriggersHosted ) {
			trigger.delete();
		}
	}
	
}
