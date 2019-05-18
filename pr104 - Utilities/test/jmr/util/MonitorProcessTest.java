package jmr.util;

import org.junit.Test;

public class MonitorProcessTest {

	@Test
	public void test() {
		
		final String strName = "Test";
		final String strCommand[] = { 
					"cmd.exe", 
					"/c \"for /l %x in (1, 1, 1000) do @echo %x\"" };
		final MonitorProcess proc = new MonitorProcess( strName, strCommand );
		final MonitorProcess.Listener listener = new MonitorProcess.Listener() {
			@Override
			public void process( final long lTime, 
								 final String strLine ) {
				System.out.println( lTime + " - " + strLine );
			}
		};
		proc.addListener( listener);
		proc.start();
	}

	
	public static void main( final String[] args ) {
		final MonitorProcessTest tester = new MonitorProcessTest();
		tester.test();
	}
	
}
