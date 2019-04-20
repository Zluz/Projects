package jmr.pr128.reports;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public enum Report {


//	final String strSQL = "SELECT max_start, d.name, ip_address, d.mac, "
//			+ "class, options, comment FROM device d, "
//			+ "( SELECT MAX( start ) max_start FROM session "
//			+ "GROUP BY seq_device ) recent_session, session s "
//			+ "WHERE TRUE AND ( recent_session.max_start = s.start ) "
//			+ "AND ( s.seq_device = d.seq ) ORDER BY "
//			+ "( recent_session.max_start ) DESC;";

//	final String strSQL = "SELECT max_start, ip_address, class, d.name "
//			+ "FROM device d, "
//			+ "( SELECT MAX( start ) max_start FROM session "
//			+ "GROUP BY seq_device ) recent_session, session s "
//			+ "WHERE TRUE AND ( recent_session.max_start = s.start ) "
//			+ "AND ( s.seq_device = d.seq ) ORDER BY "
//			+ "( recent_session.max_start ) DESC;";
	
	DEVICES( 		"Devices",
			 		"Devices_Report",
			 		"/Device_Report.sql" ),
	
	RECENT_EVENTS( 	"Recent Events Report",
			 		"Recent_Events",
			 		"/Recent_Events.sql" ),

	;
	

	
	private final static Logger 
					LOGGER = Logger.getLogger( Report.class.getName() );
	
	
	private final String strTitle;
	private final String strOutputFilename;
	private final String strConfigFilename;
//	private final String strSQL;
	private String strSQL;
	
	private Report( final String strTitle,
					final String strOutputFilename,
					final String strConfigFilename ) {
		this.strTitle = strTitle;
		this.strOutputFilename = strOutputFilename;
		this.strConfigFilename = strConfigFilename;
		
//		final Charset cs = Charset.defaultCharset();
//		String strContent;
//		try {
//			strContent = IOUtils.resourceToString( strConfigFilename, cs );
//		} catch ( final IOException e ) {
//			System.err.println( "Failed to load: " + strConfigFilename );
//			strContent = null;
//		}
//		this.strSQL = strContent;
		
//		this.strSQL = null;
		
		this.strSQL = loadSQL( strConfigFilename );
		
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
