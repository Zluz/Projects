package jmr.s2db.tables;

public class Path extends TableBase {

	public Long get( final String strName ) {
		final Long lSeq = super.get(	"path", 
										"name like '" + strName + "'", 
										"name", 
										"'" + strName + "'" );
		return lSeq;
	}

	
}
