package jmr.s2db.tables;

import java.util.Date;

public class Device extends TableBase {


	public Long get(	final String strMAC,
						final String strName ) {
		final Long lSeq = super.get(	"device", 
										"mac like '" + strMAC + "'", 
										"mac, name", 
										"'" + strMAC + "', '" + strName + "'" );
		return lSeq;
	}
	
	
	public static String format( final Object value ) {
		if ( null==value ) return "''";
		if ( value instanceof String ) {
			return "'" + value + "'";
		} else if ( value instanceof Number ) {
			return "" + value.toString();
		} else if ( value instanceof Date ) {
			return "'" + ((Date) value).toGMTString() + "'";
		}
		return "";
	}

}
