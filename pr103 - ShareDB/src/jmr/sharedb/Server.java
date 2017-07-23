package jmr.sharedb;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Server {

	public final ClientSession session;
	
	private final WatchService watchAll;
	private final WatchService watchEach;

	private Path dirParent;
	private File fileSession;
	
	private final Map<String,Peer> mapSessions = new HashMap<>();
	private final Map<String,Node> mapNodes = new HashMap<>();
	
	private final List<Listener> listeners = new LinkedList<Listener>();
	
	public interface Listener {
		public void changed();
	}
	
	
	public Server( final ClientSession session ) {

		this.session = session;
		final WatchService[] watches = init();
		this.watchAll = watches[0];
		this.watchEach = watches[1];
		this.watchAllSessions();
		this.watchEachSession();
	}
	
	
	private WatchService[] init() {
		fileSession = this.session.getSessionDir();
		final File fileParent = fileSession.getParentFile();
		dirParent = fileParent.toPath();
		WatchService watcherAllInit = null;
		WatchService watcherEachInit = null;
		try {
			watcherAllInit = FileSystems.getDefault().newWatchService();
			watcherEachInit = FileSystems.getDefault().newWatchService();
			dirParent.register( watcherAllInit, ENTRY_CREATE /*ENTRY_DELETE*/ );
			
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new WatchService[]{ watcherAllInit, watcherEachInit };
	}
	
	
	private void watchSessionDir( final Path path ) {
		if ( null==path ) return;
		final File file = path.toFile();
		if ( !file.isDirectory() ) return;
//		if ( !file.exists() ) return;
//		if ( !file.isDirectory() ) return;
//		if ( !path.toFile().isDirectory() ) return;

//		final String strPath = file.getAbsolutePath();
//		final String strPath = path.toString();
		final String strPath = file.getAbsolutePath();
		
		if ( mapSessions.containsKey( strPath ) ) {
			// already have it..
			return;
		}

		final Peer peer = new Peer( this, file );
		
		mapSessions.put( strPath, peer );

//		final File file = path.toFile();
//		System.out.println( "File: " + file );
//		System.out.println( "\tis dir: " + file.isDirectory() );
//		System.out.println( "\tfile path: " + file.getAbsolutePath() );
//
//		System.out.println( "\tpath parent: " + path.getParent() );

//		Path path = file.toPath();
		try {
			path.register( watchEach, 
					ENTRY_CREATE, /*ENTRY_DELETE,*/ ENTRY_MODIFY );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final File[] list = file.listFiles();
		for ( final File item : list ) {
			handleSessionEvent( item );
		}
	}
	
	
	public void postData(	final String strPath,
							final Map<String,String> map,
							final boolean inbound ) {
		if ( null==strPath ) return;
		if ( null==map ) return;
		
		final Node node = this.getNode( strPath );
		node.putAll( map );
		
		if ( !inbound ) { // meaning, data produced here, so share
			
			final StringBuilder sb = new StringBuilder();
			for ( final Entry<String, String> entry : map.entrySet() ) {
				sb.append( entry.getKey() );
				sb.append( "\t" );
				sb.append( entry.getValue() );
				sb.append( "\n" );
			}
			
			final String strSafe = convertNodeToFilePath( strPath );
//			String strSafe = URLEncoder.encode( strPath );
//			strSafe = strSafe.replace( "_", "%5F" );
//			strSafe = strSafe.replace( "%", "_" );
			
			fileSession.mkdirs();
			final File fileNEW = new File( fileSession, strSafe + ".new" );
			
			try (	final FileWriter fw = new FileWriter( fileNEW );
					final BufferedWriter bw = new BufferedWriter( fw ) ) {
				
				bw.write( sb.toString() );
				bw.flush();
				bw.close();
				
			} catch ( final IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			final File fileTSV = new File( fileSession, strSafe + ".tsv" );

			fileNEW.renameTo( fileTSV );
		}
	}
	
	
	public static String convertNodeToFilePath( final String strNodePath ) {
		String strFile = URLEncoder.encode( strNodePath );
		strFile = strFile.replace( "_", "%5F" );
		strFile = strFile.replace( "%", "_" );
		return strFile;
	}
	
	
	public static String convertFileToNodePath( final String strFilePath ) {
		String strNode = strFilePath;
		strNode = strNode.replace( "_", "%" );
//		strNode = strNode.replace( "%5F", "_" );
		strNode = URLDecoder.decode( strNode );
		return strNode;
	}
	
	
	
	
	private void handleSessionEvent( final File file ) {
//		if ( null==path ) return;
//		final File file = path.toFile();
		if ( !file.isFile() ) return;
		
		final String strFilename = file.getName();
		System.out.println( "(session event) new file: " + strFilename );
		
		final String strPath = file.getParentFile().getAbsolutePath();
		
		final Peer peer = mapSessions.get( strPath );
		if ( null!=peer ) {
			
//			final String strNode = convertFileToNodePath( strFilename );
			peer.processFile( /*strNode,*/ file );
		}
	}
	
	
	private void watchEachSession() {

		final Thread threadWatchEachSession = new Thread() {
			@Override
			public void run() {

				try { for (;;) {
					
					final WatchKey key = watchEach.take(); // this will block
					
					for ( final WatchEvent<?> event : key.pollEvents() ) {
						
						final Kind<?> kind = event.kind();
						final Object context = event.context();
						
						System.out.println(	"(Each) Event: " + event + ", "
											+ "kind: " + kind + ", "
											+ "context: " + context );
						if ( context instanceof Path ) {
							final Path path = (Path) context;
							
							final Path pathFull = 
									dirParent.toAbsolutePath().resolve( path );
							final File file = pathFull.toFile();
							
							handleSessionEvent( file );
						}
					}
					
					key.reset();
				} } catch ( final InterruptedException e ) {
					// interrupted? just quit.
				}
			}
		};
		threadWatchEachSession.start();
	}
	
	
	private void watchAllSessions() {
		
		final Thread threadWatchAllSessions = new Thread() {
			@Override
			public void run() {

				try { for (;;) {
					
					final WatchKey key = watchAll.take(); // this will block
					
					for ( final WatchEvent<?> event : key.pollEvents() ) {
						
						final Kind<?> kind = event.kind();
						final Object context = event.context();
						
						System.out.println(	"(All) Event: " + event + ", "
											+ "kind: " + kind + ", "
											+ "context: " + context );
						if ( context instanceof Path ) {
							final Path path = (Path) context;
							
							final Path pathFull = 
									dirParent.toAbsolutePath().resolve( path );
							
							watchSessionDir( pathFull );
						}
					}
					
					key.reset();
				} } catch ( final InterruptedException e ) {
					// interrupted? just quit.
				}
			}
		};
		threadWatchAllSessions.start();
		
		final File[] list = dirParent.toFile().listFiles();
		for ( final File file : list ) {
			if ( file.isDirectory() ) {
				final Path path = file.toPath();
				watchSessionDir( path );
			}
		}
		
	}
	
	
	
	public static void main( final String[] args ) 
						throws IOException, InterruptedException {

		final ClientSession session = ClientSession.get();
		
		final Server server = new Server( session );
		
		if (1==1) for (;;) {}
		
		final File fileSession = session.getSessionDir();
		
		final WatchService watcher = FileSystems.getDefault().newWatchService();

	    final WatchKey keyReg;
		final Path dir = fileSession.toPath();
//		try {
		    keyReg = dir.register(	watcher,
		    						ENTRY_CREATE,
		    					/*	ENTRY_DELETE, */
		    						ENTRY_MODIFY );
//		} catch ( final IOException x ) {
//		    System.err.println(x);
//		}
		    
		for (;;) {
			
			final WatchKey key = watcher.take(); // this will block
			
			for ( final WatchEvent<?> event : key.pollEvents() ) {
				
				final Kind<?> kind = event.kind();
				final Object context = event.context();
				
				System.out.println(	"Event: " + event + ", "
									+ "kind: " + kind + ", "
									+ "context: " + context );
			}
			
			key.reset();
		}
		    
	}


	private void notifyListeners() {
		for ( final Listener listener : listeners ) {
			listener.changed();
		}
	}
	
	public void addListener( final Listener listener ) {
		this.addListener( listener );
	}
	
	
	public Node getNode( final String strNode ) {
		final Node node;
		if ( mapNodes.containsKey( strNode ) ) {
			node = mapNodes.get( strNode );
		} else {
			node = new Node( this, strNode );
			node.addListener( new jmr.sharedb.Node.Listener() {
				public void changed() {
					notifyListeners();
				}
			} );
		}
		return node;
	}
	
	public Collection<Node> getNodes() {
		return Collections.unmodifiableCollection( this.mapNodes.values() );
	}
	
	
	
}
