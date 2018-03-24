package jmr.pr115.model;

import jmr.Field;

public enum ColumnMap {
	
	_01( Field.MAC ),
	_02( Field.IMAGE_SCREENSHOT ),
	_03( Field.IP ),
	_04( Field.SESSION_STATE ),

	
	_11( Field.DESCRIPTION ),
	_12( Field.OS_VERSION ),
	_13( Field.EXECUTABLE ),

	
	_20( Field.SESSION_START ),
	_21( Field.LAST_MODIFIED ),
	_22( Field.TIMEELP_SCREENSHOT ),
	
	
	_30( Field.PAGE_SOURCE_CLASS ),

	;
	
	final Field field;
	final int iWidth;
	
	ColumnMap( final Field field ) {
		this.field = field;
		this.iWidth = 0;
	}
	
	public Field getField() {
		return this.field;
	}
	
	
	

	public static ColumnMap get( final int index ) {
		if ( index < ColumnMap.values().length ) {
			return ColumnMap.values()[ index ];
		}
		return null;
	}
	
}
