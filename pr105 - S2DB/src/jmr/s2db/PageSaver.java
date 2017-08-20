package jmr.s2db;

import java.util.HashMap;

import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.s2db.tables.Tables;

public class PageSaver extends HashMap<String,String> {

	/** Serialization */
	private static final long serialVersionUID = -6339379977543698677L;
	
//	private final String strPath;
	
	private final long seqPath;
	private Long seqPage;
	
	public PageSaver( final String strPath ) {
		if ( null==strPath ) throw new IllegalStateException( "null strPath" );
//		this.strPath = strPath;
		
		final Path tPath = ( (Path)Tables.PATH.get() );
		this.seqPath = tPath.get( strPath );

		final Page tPage = ( (Page)Tables.PAGE.get() );
		this.seqPage = tPage.get( seqPath );
		
		if ( null!=seqPage ) {
			this.putAll( tPage.getMap( this.seqPage ) );
		}
	}
	
	
	public Long save() {
		final Page tPage = ( (Page)Tables.PAGE.get() );
		tPage.addMap( seqPage, this, true );
		return seqPage;
	}
	
	public Long getSeq() {
		return seqPage;
	}
	
	
	
}
