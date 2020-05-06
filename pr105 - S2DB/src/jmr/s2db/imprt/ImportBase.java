package jmr.s2db.imprt;

import java.io.IOException;
import java.util.Map;

public abstract class ImportBase extends SummarizerBase {


	public abstract String getURL();
	
	public String getImportName() {
		return this.getClass().getSimpleName();
	}
	

	@Override
	public boolean isMatch( final String strNodePath ) {
		if ( null == strNodePath ) return false;

		// example, from TeslaSummarizer
//		return "/External/Ingest/Tesla - CHARGE_STATE/data/response".
//				equals( strNodePath );
		
		final String strMatch = "/" + this.getImportName();
		final boolean bMatch = strNodePath.contains( strMatch );
		return bMatch;
	}

	@Override
	public abstract Map<String, String> getJsonPaths();
	
	
	public Long doImportOnce() {
		SummaryRegistry.get().add( this );
		
		final WebImport wi = new WebImport( getImportName(), getURL() );
		try {
			final Long seq = wi.save();
			return seq;
		} catch ( final IOException e ) {
			e.printStackTrace();
			return null;
		}
	}

}
