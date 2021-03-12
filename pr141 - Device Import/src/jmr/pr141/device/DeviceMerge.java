package jmr.pr141.device;

import java.util.List;

import jmr.pr141.device.Device.BooleanProperty;
import jmr.pr141.device.Device.IntegerProperty;

public class DeviceMerge {

	private final boolean bMoreData;
	
	public DeviceMerge( final boolean bMoreData ) {
		this.bMoreData = bMoreData;
	}
	
	public static String getBestOf( final String strA,
									final String strB ) {
		if ( null == strA ) return strB;
		if ( null == strB ) return strA;
		
		final String strTrimA = strA.trim();
		final String strTrimB = strB.trim();
		if ( strTrimA.isEmpty() ) return strTrimB;
		if ( strTrimB.isEmpty() ) return strTrimA;
		
		if ( strTrimA.length() > strTrimB.length() ) return strTrimA;
		if ( strTrimB.length() > strTrimA.length() ) return strTrimB;
		
		// examine more?
		return strA;
	}
	
	public static <T> T getBestOf( final T objA,
								   final T objB ) {
		if ( null == objA ) return objB;
		if ( null == objB ) return objA;
		
		// not super scientific..
		final String strA = objA.toString();
		final String strB = objB.toString();
		if ( strA.length() > strB.length() ) return objA;
		if ( strB.length() > strA.length() ) return objB;
		
		// check anything else? 
		return objA;
	}
	
	public Device merge( final long lTAC,
						 final List<Device> list ) {
		if ( null == list ) return null;
		if ( list.isEmpty() ) return null;
		
		if ( 1 == list.size() ) return list.get( 0 );
		
		final Device deviceResult = new Device( lTAC );
		
		for ( final TextProperty property : TextProperty.values() ) {
			String strBest = deviceResult.getProperty( property );
			for ( final Device deviceInput : list ) {
				final String strInput = deviceInput.getProperty( property );
				strBest = getBestOf( strBest, strInput );
			}
			deviceResult.set( property, strBest );
		}
		
		for ( final IntegerProperty property : IntegerProperty.values() ) {
			Integer iBest = deviceResult.getIntegerProperty( property );
			for ( final Device deviceInput : list ) {
				final Integer iInput = 
								deviceInput.getIntegerProperty( property );
				iBest = getBestOf( iBest, iInput );
			}
			deviceResult.setIntegerProperty( property, iBest );
		}

		for ( final BooleanProperty property : BooleanProperty.values() ) {
			Boolean bBest = deviceResult.getBooleanProperty( property );
			for ( final Device deviceInput : list ) {
				final Boolean bInput = 
								deviceInput.getBooleanProperty( property );
				bBest = getBestOf( bBest, bInput );
			}
			deviceResult.setBooleanProperty( property, bBest );
		}
		
		return deviceResult;

	}
	
}
