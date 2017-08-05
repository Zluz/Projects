package jmr.s2db.tables;

public class Device extends TableBase {


	public Long get(	final String strMAC,
						final String strName ) {
		final Long lSeq = super.get(	"device", 
										"mac like '" + strMAC + "'", 
										"mac, name", 
										"'" + strMAC + "', '" + strName + "'" );
		return lSeq;
	}
	
	
}
