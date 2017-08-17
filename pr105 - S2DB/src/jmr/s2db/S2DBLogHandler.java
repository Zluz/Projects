package jmr.s2db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.tables.Session;

public class S2DBLogHandler extends Handler {

	private static final Logger 
			LOGGER = Logger.getLogger( S2DBLogHandler.class.getName() );
	
	static {
		final S2DBLogHandler handler = new S2DBLogHandler();
		final LogManager manager = LogManager.getLogManager();
		manager.getLogger( "" ).addHandler( handler );
		
		LOGGER.log( Level.FINER, "S2DBLogHandler registered." );
	}
	
	
	private static Client s2db = null;
	
//	private static Long seqSession = null;
	

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
	}

	private void check() {
		if ( null==s2db ) {
			s2db = Client.get();
		}
	}
	
//	public static void setSession( final long seqSession ) {
//		S2DBLogHandler.seqSession = seqSession;
//	}
	
	@Override
	public void publish( final LogRecord record ) {
		check();
		
//		try ( final Statement 
//				stmt = ConnectionProvider.get().getStatement() ) {
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {
			
			final Date time = new Date( record.getMillis() );
			final String strLevel = record.getLevel().getName();
			
			final String strClass = record.getSourceClassName();
			final String strMethod = record.getSourceMethodName();
			final String strSource;
			if ( null!=strClass && null!=strMethod ) {
				strSource = strClass + "." + strMethod + "()";
			} else if ( null!=strClass ) {
				strSource = strClass + ".<unknown method>";
			} else {
				strSource = null;
			}
			
			final String strInsert = "INSERT INTO log "
					+ "( seq_session, time, level, text, source ) "
					+ "VALUES ( " 
							+ Session.getSessionSeq() + ", " 
							+ DataFormatter.format( time.getTime() ) + ", "
							+ DataFormatter.format( strLevel ) + ", "
							+ DataFormatter.format( record.getMessage() ) + ", " 
							+ DataFormatter.format( strSource ) + " "
							+ " );";
			
			stmt.executeUpdate( strInsert );
//			stmt.executeUpdate( strInsert, Statement.RETURN_GENERATED_KEYS );
//			try ( final ResultSet rs = stmt.getGeneratedKeys() ) {
//				
//				if ( rs.next() ) {
//					final long lSeq = rs.getLong( 1 );
//					return lSeq;
//				}
//			}
			
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
//	public static void registerLoggers() {
//		final S2DBLogHandler handler = new S2DBLogHandler();
//		
//		final LogManager manager = LogManager.getLogManager();
//		
////		final Enumeration<String> names = manager.getLoggerNames();
////		for ( ; names.hasMoreElements(); ) {
////			final String name = names.nextElement();
////			System.out.println( "Logger found: " + name );
////			final Logger logger = manager.getLogger( name );
////			logger.addHandler( handler );
////		}
////		manager.getLogger( "global" ).addHandler( handler );
//		manager.getLogger( "" ).addHandler( handler );
//	}
	
	
	public static void main( final String[] args ) {
		
//		final S2DBLogHandler handler = new S2DBLogHandler();
//		
//		final LogManager manager = LogManager.getLogManager();
//		final Enumeration<String> names = manager.getLoggerNames();
//		for ( ; names.hasMoreElements(); ) {
//			final String name = names.nextElement();
//			final Logger logger = manager.getLogger( name );
//			logger.addHandler( handler );
//		}
		
//		registerLoggers();
		
//		final LogRecord record = new LogRecord( Level.INFO, "S2DBLogHandler main() test." );
//		handler.publish( record );
		
		LOGGER.log( Level.INFO, "S2DBLogHandler test using LOGGER." );
		
	}
	
}
