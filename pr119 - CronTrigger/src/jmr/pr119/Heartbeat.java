package jmr.pr119;

import java.util.concurrent.TimeUnit;

public class Heartbeat extends JobWorker {
	
	final TimeUnit unit;
	
	public Heartbeat( final TimeUnit unit ) {
		this.unit = unit;
	}

	public TimeUnit getTimeUnit() {
		return this.unit;
	}
	
	
	public static void main(String[] args) {
		// for testing
	}
}
