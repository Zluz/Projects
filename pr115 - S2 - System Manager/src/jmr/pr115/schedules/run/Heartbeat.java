package jmr.pr115.schedules.run;

import java.util.concurrent.TimeUnit;

public class Heartbeat extends JobWorker {
	
	final TimeUnit unit;
	
	public Heartbeat( final TimeUnit unit ) {
		this.unit = unit;
	}
	
	@Override
	public boolean run() {
//		System.out.println( "RunHeartbeat.run() - TimeUnit: " + unit.name() );
		return false;
	}
	
	public TimeUnit getTimeUnit() {
		return this.unit;
	}
	
	
	public static void main(String[] args) {
		// for testing
	}
}
