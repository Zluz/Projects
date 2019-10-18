package jmr.pr128.reports;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import jmr.pr128.logical.IPDetails;
import jmr.pr128.logical.LogicalFieldEvaluation;
import jmr.pr128.logical.S2FSAge;
import jmr.pr128.logical.DeviceConfig;
import jmr.pr128.logical.DeviceReport;
import jmr.pr128.marking.RowMarker;
import jmr.pr128.marking.CheckAge;

public enum Report {

	
	DEVICES( 		"Devices",
			 		"Devices_Report",
			 		"/Device_Report.sql",
			 		new LogicalFieldEvaluation[] { 
			 					new IPDetails(),
			 					new S2FSAge(),
			 					new DeviceConfig(),
			 					new DeviceReport() },
			 		new RowMarker[] {
			 					new CheckAge() } ),
	
	RECENT_EVENTS( 	"Recent Events Report",
			 		"Recent_Events",
			 		"/Recent_Events.sql",
			 		null, null ),
	;
	

	
	private final static Logger 
					LOGGER = Logger.getLogger( Report.class.getName() );
	
	
	private final String strTitle;
	private final LogicalFieldEvaluation[] arrLFE;
	private final RowMarker[] arrMarkers;
	private final String strOutputFilename;
	private final String strConfigFilename;
	private String strSQL;
	
	private Report( final String strTitle,
					final String strOutputFilename,
					final String strConfigFilename,
					final LogicalFieldEvaluation[] arrLFE,
					final RowMarker[] arrMarkers ) {
		this.strTitle = strTitle;
		this.strOutputFilename = strOutputFilename;
		this.strConfigFilename = strConfigFilename;
		
		this.strSQL = loadSQL( strConfigFilename );
		this.arrLFE = arrLFE;
		this.arrMarkers = arrMarkers;
	}
	
	
	
	private String loadSQL( final String strFilename ) {
		
		final Charset cs = Charset.defaultCharset();
		String strContent;
		try {
			strContent = IOUtils.resourceToString( strConfigFilename, cs );
		} catch ( final IOException e ) {
			System.err.println( "Failed to load: " + strConfigFilename );
			strContent = null;
		}
		return strContent;
	}
	
	
	public String getTitle() {
		return this.strTitle;
	}

	
	public String getOutputFilename() {
		return this.strOutputFilename;
	}
	
	
	public String getSQL() {
		if ( null==this.strSQL ) {
//			this.strSQL = loadSQL( this.strConfigFilename );
			LOGGER.severe( ()-> "Failed to load: " + strConfigFilename );
		}
		return this.strSQL;
	}
	
	
	public List<LogicalFieldEvaluation> getLogicalFieldEvaluations() {
		final List<LogicalFieldEvaluation> list = new LinkedList<>();
		if ( null!=this.arrLFE ) {
			list.addAll( Arrays.asList( this.arrLFE ) );
		}
		return list;
	}
	
//	public RowMarker[] getRowMarkers() {
//		return this.arrMarkers;
//	}
	public RowMarker getRowMarker() {
		if ( null==arrMarkers ) {
			return null;
		} else if ( arrMarkers.length > 0 ) {
			return arrMarkers[0];
		} else {
			return null;
		}
	}
	
	
	public static void main( final String[] args ) throws IOException {
//		System.out.println( Report.DEVICES.getSQL() );

		final String strFile = "/Device_Report.sql";
		final Charset cs = Charset.defaultCharset();
		
		final String strContent = IOUtils.resourceToString( strFile, cs );
		System.out.println( strContent );
		
//		try ( final InputStream is = 
//						cl.getResourceAsStream( "Device_Report.sql" ) ) {
//			final String strContent = IOUtils.resourceToString(name, encoding)
//		}
	}
	
}
