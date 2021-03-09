package jmr.pr141;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

public class Device {
	
	public static enum TextProperties {
		MARKETING_NAME( 20 ),
		MANUFACTURER( 50 ),
		BRAND_NAME( 16 ),
		MODEL_NAME( 20 ),
		
		BANDS( 50 ),
		BANDS_5G( 50 ),
		RADIO_INTERFACE( 20 ),
		OPERATING_SYSTEM( 12 ),
		DEVICE_TYPE( 10 ),
		;
		
		final int iPadding;
		
		private TextProperties( final int iPadding ) {
			this.iPadding = iPadding;
		}
	}
	
	final List<Long> listTACs = new LinkedList<>();
	
	final EnumMap<TextProperties,String> 
					mapProperties = new EnumMap<>( TextProperties.class );
	
	Integer iCountryCode;
	Integer iSimCount;
	
	Boolean bBluetooth;
	Boolean bWLAN;
	
	
	public Device( final List<Long> listTACs ) {
		this.listTACs.addAll( listTACs );
	}
	
}
