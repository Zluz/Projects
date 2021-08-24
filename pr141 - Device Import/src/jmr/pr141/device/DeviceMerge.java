package jmr.pr141.device;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	

	private String combineMapValues( final String strA,
									 final String strB ) {
		if ( null == strA ) return strB;
		if ( null == strB ) return strA;
		
		final String strConcat = strA + "," + strB;
		final String[] arrFields = strConcat.split( "," );
		final List<String> list = Arrays.asList( arrFields );
//		final Set<String> set = new HashSet<>( list );
		final Set<String> set = new HashSet<>();
		list.forEach( str-> set.add( str.trim() ) );
		final String strResult = String.join( ",", set );
		return strResult;
	}
	
	
	public Device merge( final long lTAC,
						 final List<Device> list ) {
		if ( null == list ) return null;
		if ( list.isEmpty() ) return null;
		
		if ( 1 == list.size() ) return list.get( 0 );
		
		final Device deviceResult = new Device( lTAC );
		
		for ( final TextProperty property : TextProperty.values() ) {
			String strBest = null;
			for ( final Device deviceInput : list ) {
				final String strInput = deviceInput.getProperty( property );
				strBest = getBestOf( strBest, strInput );
			}
			deviceResult.set( property, strBest );
		}
		
		for ( final IntegerProperty property : IntegerProperty.values() ) {
			Integer iBest = null;
			for ( final Device deviceInput : list ) {
				final Integer iInput =  deviceInput.getProperty( property );
				iBest = getBestOf( iBest, iInput );
			}
			deviceResult.setProperty( property, iBest );
		}

		for ( final BooleanProperty property : BooleanProperty.values() ) {
			Boolean bBest = null;
			for ( final Device deviceInput : list ) {
				final Boolean bInput = deviceInput.getProperty( property );
				bBest = getBestOf( bBest, bInput );
			}
			deviceResult.setProperty( property, bBest );
		}

		final Map<String,String> mapResult = new HashMap<>();
		for ( final Device deviceInput : list ) {
			final Map<String, String> mapInput = 
									deviceInput.getCharacteristicsMap();
			for ( Entry<String, String> entry : mapInput.entrySet() ) {
				final String strKey = entry.getKey();
				final String strInputValue = entry.getValue();
				if ( mapResult.containsKey( strKey ) ) {
					final String strResultValue = mapResult.get( strKey );
					final String strNewValue = 
							combineMapValues( strResultValue, strInputValue );
					mapResult.put( strKey, strNewValue );
				} else {
					mapResult.put( strKey, strInputValue );
				}
			}
		}
		deviceResult.getCharacteristicsMap().putAll( mapResult );
		
		return deviceResult;

	}

}
