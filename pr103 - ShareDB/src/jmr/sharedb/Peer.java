package jmr.sharedb;

import java.io.File;

public class Peer {

	final private Server server;
	
	final private File fileSession;
	
	private long lLastActivity;
	
	public Peer(	final Server server,
					final File fileSession ) {
		this.server = server;
		this.fileSession = fileSession;
		this.lLastActivity = System.currentTimeMillis();
	}
	
	
	public long getLastActivity() {
		return this.lLastActivity;
	}


	public void processFile( final File file ) {
		this.lLastActivity = System.currentTimeMillis();
		
		if ( file.getName().endsWith(".tsv") ) {
			
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
