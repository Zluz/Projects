package jmr.pr134;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class FastFileChecker {

	public static List<String> resolveFiles( 
								final List<String> listInput,
								final boolean bDedup ) {
		final Hashtable<String,String> table = new Hashtable<>();
		final Boolean[] bRunning = { true }; 
		for ( final String strFile : listInput ) {
			final Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						final File file = new File( strFile );
						if ( bRunning[0] && file.canRead() ) {
//							vector.add( file.getAbsolutePath() );
							table.put( file.getAbsolutePath(), file.getName() );
						}
					} catch ( final Exception e ) {
						// just ignore, do not add the file.
					}
				}
			};
			thread.start();
		}
		try {
			Thread.sleep( 1000 );
			bRunning[0] = false;
		} catch ( final InterruptedException e ) {
			// interrupted. should re-throw but just return nothing.
			System.err.println( "Interrupted resolveFiles()." );
			return null;
		}
		final List<String> listOutput = new LinkedList<>();
		final List<String> listUnique = new LinkedList<>();
//		for ( final String strFile : vector ) {
		for ( final Entry<String, String> entry : table.entrySet() ) {
			if ( bDedup ) {
				final String strName = entry.getValue();
				if ( ! listUnique.contains( strName ) ) {
					listUnique.add( strName );
					listOutput.add( entry.getKey() );
				}
			} else {
				listOutput.add( entry.getKey() );
			}
		}
		return listOutput;
	}
	

}
