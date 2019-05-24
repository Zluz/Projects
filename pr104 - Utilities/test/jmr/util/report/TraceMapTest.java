package jmr.util.report;

import static org.junit.Assert.*;

import org.junit.Test;


public class TraceMapTest {

	@Test
	public void getPrefixFor_Test() {
		assertEquals( "07", TraceMap.getPrefixFor( 7 ) );
	}

	@Test
	public void getNextFramePrefix_Test() {
		assertEquals( "01", TraceMap.getNextFramePrefix( null ) );
	}

	@Test
	public void getInitials_Test() {
		assertEquals( "", TraceMap.getInitials( null ) );
		assertEquals( "OTTF", TraceMap.getInitials( "OneTwoThreeFour" ) );
		assertEquals( "TN1234", TraceMap.getInitials( "TestNumbers1234" ) );
		assertEquals( "TU", TraceMap.getInitials( "TestUnderscore__" ) );
		assertEquals( "TS", TraceMap.getInitials( "TestSpecial!@#$%^&*" ) );
	}

	@Test
	public void addFrame_Test() {
		assertNotNull( TraceMap.addFrame( null, null ) );
		
		TraceMap map = TraceMap.addFrame( null, null );
		assertEquals( "TMT.addFrame_Test()", map.get( "01-source" ) );
		
//		map = TraceMap.addFrame( map, "comment" );
		map.addFrame( "comment" );
		assertEquals( "comment", map.get( "02-comment" ) );
	}

}
