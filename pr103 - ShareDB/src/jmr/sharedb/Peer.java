package jmr.sharedb;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Peer {

	private static final Charset UTF_8 = Charset.forName( "UTF-8" );

	final private Server server;
	
	final private File fileSession;
	
	private long lLastActivity;
	
	public Peer(	final Server server,
					final File fileSession ) {
		this.server = server;
		this.fileSession = fileSession;
		this.lLastActivity = System.currentTimeMillis();
		System.out.println( "New Peer: " + this.toString() );
	}
	
	@Override
	public String toString() {
		return "Peer[" + fileSession.toString() + "]";
	}
	
	
	public long getLastActivity() {
		return this.lLastActivity;
	}

	
	public String getSessionName() {
		final String strName = this.fileSession.getName();
		return strName;
	}
	

	public void processFile( // final String strNodePath,
								final File file ) {
		this.lLastActivity = System.currentTimeMillis();

		final String strFilename = file.getName();
		
		if ( strFilename.endsWith(".tsv") ) {
			
			final int iLen = strFilename.length();
			final String strFile = strFilename.substring( 0, iLen - 4 );
			final String strNode = Server.convertFileToNodePath( strFile );

			System.out.println( "New data to Node: " + strNode );
			
			final Node node = this.server.getNode( strNode );
			
			final Path path = file.toPath();
			try {
				final Map<String,String> map = new HashMap<>();
				for ( final String strLine : Files.readAllLines( path, UTF_8 ) ) {
					final String[] arr = strLine.split( "\t" );
					map.put( arr[0], arr[1] );
				}
				node.putAll( map );
			} catch ( final Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	/*
 
files:
	all files are only written once (never edited)
	last modified is relevant

	?:
	*.info - details about the session
	*.out - data posted by the client
	*.in - action intended for the client

	not processed:
	*.new - file being created. renamed when finished.
	*.old - file is outdated. may be deleted.

filename:
	<node_name>.tsv - data posted by the client
		<name> \t <value>

	 */
	
	
}
