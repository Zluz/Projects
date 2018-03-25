package jmr.pr115.schedules.run;

public class RunTest extends JobWorker {
	
	@Override
	public boolean run() {
		
		System.out.println( "RunTest.run()");
		
		return false;
	}
}
