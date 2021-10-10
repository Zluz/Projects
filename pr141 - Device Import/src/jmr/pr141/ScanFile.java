package jmr.pr141;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jmr.pr141.conversion.TSVRecord;
import jmr.pr141.conversion.Utils;
import jmr.pr141.device.Device;


public class ScanFile implements DeviceProvider {

	public final static boolean DEBUG_SHOW_LOAD_DETAIL = true;
	public final static boolean DEBUG_RUN_TAC_SEEK_TEST = true;

	/** key of entry used to validate an index file to its source */
	private final static Long KEY_CHECK = 0L;
	/** future-proofing (maybe) */
	private final static Long CHECK_VERSION = 1L;
	
	
	final RandomAccessFile raf;
	
	// index zero: <source file-size>
	final Map<Long,List<Long>> mapTacPos = new TreeMap<>();
	


	final StringBuilder sbReader = new StringBuilder();
	
	final String strName;
	

	long lCountLines = 0;
	long lCountLineTACs = 0;
	long lMaxRepeatedTACs = 0;
	long lMaxLineSize = 0;
	long lTacMostRepeated = 0;
	

	
	
	
	public ScanFile( final File file ) throws FileNotFoundException {
		this.raf = new RandomAccessFile( file, "r" );
		this.strName = "ScanFile: " + file.getName();
	}
	
	private class SCDReference extends DeviceReference {
		
		final long lFilePosition;
		
		public SCDReference( final long lFilePosition ) {
			super( ScanFile.this );
			this.lFilePosition = lFilePosition;
		}
		
		@Override
		public String getName() {
			return "Position " + lFilePosition;
		}
		
		public long getFilePosition() {
			return lFilePosition;
		}
	}
	
	
	@Override
	public String getName() {
		return this.strName; 
	}
	
	@Override
	public Device getDevice( final DeviceReference ref ) {
		if ( ! ( ref instanceof SCDReference ) ) return null; 

		final SCDReference scdr = (SCDReference)ref;
		
		try {
			final long lPosition = scdr.getFilePosition();
			this.raf.seek( lPosition + 1 );
		} catch ( final IOException e ) {
			return null;
		}
		final String strLine = this.readNextLine();
		final Device device = TSVRecord.fromTSV( strLine );
		return device;
	}
	
	
	@Override
	public List<DeviceReference> findReferences( final long lTAC ) {
		final List<DeviceReference> listRefs = new LinkedList<>();
		
		if ( lTAC != KEY_CHECK 
				&& mapTacPos.containsKey( lTAC ) ) {
			final List<Long> listPositions = mapTacPos.get( lTAC );
			for ( final Long lPosition : listPositions ) {
				final DeviceReference ref = new SCDReference( lPosition );
				listRefs.add( ref );
			}
		}
		return listRefs;
	}
	

	@Override
	public long getTotalReferences() {
		return this.lCountLineTACs;
	}

	@Override
	public long getTotalUniqueTACs() {
		return this.mapTacPos.size();
	}
	
	
	public void addCheckEntry( final File fileSource ) {
		final List<Long> list = new LinkedList<>();
		
		list.add( CHECK_VERSION );
		list.add( (long)fileSource.length() );
		list.add( fileSource.lastModified() );
		list.add( (long)fileSource.getName().hashCode() );
		
		this.mapTacPos.put( KEY_CHECK, list );
	}
	
	public Map<Long,List<Long>> getTacPosMap() {
		return this.mapTacPos;
	}
	
	
	private String clean( final String strInput ) {
		final StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < strInput.length(); i++ ) {
			final char c = strInput.charAt( i );
//			if ( '\f' == c ) {
//				// discard
//			} else if ( '\r' == c ) {
//				// discard
//			} else {
				sb.append( c );
//			}
		}
		return sb.toString();
	}
	
	public String safe( final String str ) {
		final String strSafe = str
				.replace( '\t', '>' )
				.replace( '\f', 'f' )
				.replace( '\r', 'r' )
				.replace( '\n', 'n' ); 
		return strSafe;
	}
	
	
	public String readNextLine_001() { // Average time: ~1.5 s
		// uses a fresh buffer every time and calls raf.seek()
		// very strange that this is faster than not using seek()
		final StringBuilder sb = new StringBuilder();
		final int iSize = 10240;
		final byte[] arrBuffer = new byte[ iSize ];
		try {
			boolean bContinue = true;
			do {
				Arrays.fill( arrBuffer, (byte)0 );
				final long lFilePosLast = raf.getFilePointer();
				final int iRead = raf.read( arrBuffer );
				
				if ( -1 == iRead && 0 == sb.length() ) {
					// already at the end of the file
					return null;
				}
				if ( iRead != iSize ) {
					// just reached the end of the file
					bContinue = false;
				}
				
				int iPos = -1;
				for ( int i = 0; i < iRead; i++ ) {
					final char c = (char)arrBuffer[ i ];
					if ( 10 == c || 13 == c ) {
						iPos = i;
						bContinue = false;
						raf.seek( lFilePosLast + iPos + 1 );
						break;
					} else {
						sb.append( c );
					}
				}
			} while ( bContinue );
			return sb.toString();
		} catch ( final IOException e ) {
			return null;
		}
	}
	
	
	public String readNextLine() {
		return readNextLine_001();
	}

	
	final int iBufSize = 10240;
	final byte[] arrBuffer = new byte[ iBufSize ];
	int iBufPos = -1;
	long lFilePosLast;
	
	// never calls seek()
	public String readNextLine_002() { // Average time: 1.6+ s ???
		final StringBuilder sb = new StringBuilder();
		try {
			boolean bContinue = true;
			do {
				
				if ( -1 == iBufPos ) {
					
					Arrays.fill( arrBuffer, (byte)0 );
					lFilePosLast = raf.getFilePointer();
					final int iRead = raf.read( arrBuffer );
					
					if ( -1 == iRead && 0 == sb.length() ) {
						// already at the end of the file
						return null;
					}
					if ( iRead != iBufSize ) {
						// just reached the end of the file
						bContinue = false;
					}
					
					iBufPos = 0;
				}
				
				int iPos = -1;
				for ( int i = iBufPos; i < iBufSize; i++ ) {
					final char c = (char)arrBuffer[ i ];
					if ( 10 == c || 13 == c ) {
						iPos = i;
						bContinue = false;
//						raf.seek( lFilePosLast + iPos + 1 );
						iBufPos = iPos + 1;
						break;
					} else if ( 0 != c ) {
						sb.append( c );
					}
				}
				if ( bContinue ) {
					iBufPos = -1;
				}
			} while ( bContinue );
			return sb.toString();
		} catch ( final IOException e ) {
			return null;
		}
	}
	
	
	public String readNextSignificantLine() {
		boolean bAbort = false;
		boolean bKeepSearching = true;
		String strLine;
		do {
			strLine = readNextLine();
			bAbort = ( null == strLine );
			bKeepSearching = ( null == strLine
							|| strLine.isEmpty()
							|| '#' == strLine.charAt( 0 ) );
		} while ( bKeepSearching && ! bAbort );
		return strLine;
	}
	
	
	
	private void addTacPosition( final long lFilePos, 
								 final List<Long> listTACs ) {
		if ( null == listTACs ) return;
		if ( listTACs.isEmpty() ) return;
		if ( -1 == lFilePos ) return;
		
		lCountLines++;
		
		for ( final Long lTAC : listTACs ) {
			lCountLineTACs++;
			final List<Long> listPositions;
			if ( this.mapTacPos.containsKey( lTAC ) ) {
				listPositions = mapTacPos.get( lTAC );
				final int iSize = listPositions.size() + 1;
				if ( iSize > lMaxRepeatedTACs ) {
					lMaxRepeatedTACs = iSize;
					lTacMostRepeated = lTAC;
				}
			} else {
				listPositions = new LinkedList<>();
				mapTacPos.put( lTAC, listPositions );
			}
			listPositions.add( lFilePos );
		}
	}

	
	public void scan( final long lMaxCount ) throws Exception {

		boolean bContinue = true;
		
		int i = 0;

		while ( bContinue ) {
			i++;
			
			this.sbReader.setLength( 0 );
			
			final long lFilePos = raf.getFilePointer();
			
			final String str = readNextSignificantLine();
			
			if ( null == str ) break; //TODO change
			
			lMaxLineSize = Math.max( lMaxLineSize, str.length() );
			
			final List<Long> listTACs = 
//					TSVRecord.getTACsFromLine( str );
					Utils.getNumbersFromLine( str );
			
			
			if ( DEBUG_SHOW_LOAD_DETAIL ) {
				final boolean bHitLF = str.contains( "\n" ) 
						|| str.contains( "\f" ) || str.contains( "\r" );
				final boolean bHitTab = str.contains( "\t" );
				
//				final long lPosition = raf.getFilePointer();
				final long lPosition = this.lFilePosLast + this.iBufPos;
	
				System.out.print( "Read [" );
				if ( bHitLF ) System.out.print( "L" );
				if ( bHitTab ) System.out.print( "T" );
				System.out.print( "] (" + lPosition + "): " );
				final String strSafe = safe( str );
				if ( strSafe.length() > 100 ) {
					System.out.print( "(" + strSafe.length() + " chars):" );
					System.out.print( "\"" + strSafe.substring( 0, 98 ) + "...\"" );
				} else {
					System.out.print( "\"" + strSafe + "\"" );
				}
				System.out.print( " TACs: " + listTACs.toString() );

				System.out.println();
			}

			addTacPosition( lFilePos, listTACs );
			
			bContinue = i < lMaxCount;
		}

		if ( DEBUG_SHOW_LOAD_DETAIL ) {
			System.out.println( "lCountLines      : " + lCountLines );
			System.out.println( "lCountLineTACs   : " + lCountLineTACs );
			System.out.println( "lMaxLineSize     : " + lMaxLineSize );
			System.out.println( "lMaxRepeatedTACs : " + lMaxRepeatedTACs );
			System.out.println( "lTacMostRepeated : " + lTacMostRepeated );
		}
		
	}
	
	private void runSeekTest( final long lTAC ) throws IOException {

		if ( DEBUG_RUN_TAC_SEEK_TEST ) {
			final List<Long> list = mapTacPos.get( lTAC );
			System.out.println( "Positions: " + list.toString() );
			
			System.out.println( "Seeking, Loading.." );
			for ( final Long lPos : list ) {
				raf.seek( lPos );
//				final String strLine = readNextLine();
				final String strLine = readNextSignificantLine();
				final int iLen = strLine.length();
				final String strSafe;
				if ( iLen > 1000 ) {
					strSafe = strLine.substring( 0, 1000 ) + "..";
				} else {
					strSafe = strLine;
				}
				System.out.println( "Read (pos:" + lPos + "): "
						+ "(len:" + strLine.length() + ") "
						+ "\"" + strSafe + "\"" );
			}
		}
	}
	
	public List<String> getDeviceLines( final long lTAC ) {
		
		final List<String> listRecords = new LinkedList<>();

		final List<Long> listPositions = mapTacPos.get( lTAC );
		if ( null == listPositions ) return listRecords;
		
//		System.out.println( "Positions: " + listPositions.toString() );
		
//		System.out.println( "Seeking, Loading.." );
		for ( final Long lPos : listPositions ) {
			try {
				raf.seek( lPos );
				final String strLine = readNextLine();
				listRecords.add( strLine );
//				System.out.println( "Read (pos:" + lPos + "): "
//						+ "(len:" + strLine.length() + ") "
//						+ "\"" + strLine.substring( 0, 40 ) + "...\"" );
			} catch ( final IOException e ) {
				// just ignore for now
			}
		}
		return listRecords;
	}
	
	//from https://beginnersbook.com/2013/12/how-to-serialize-hashmap-in-java/
	public void saveIndex( final File file ) {
		 try {
		 	final FileOutputStream fos = new FileOutputStream( file );
            final ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject( this.mapTacPos );
            oos.close();
            fos.close();
            System.out.println( "Saved to: " + file.getAbsolutePath() );
	     } catch(IOException ioe) {
            ioe.printStackTrace();
	     }
	}

	public void loadIndex( final File file ) throws ClassNotFoundException {
		try {
			final FileInputStream fis = new FileInputStream( file );
			final ObjectInputStream ois = new ObjectInputStream( fis );
			final Map<Long, List<Long>> 
					map = (Map<Long, List<Long>>) ois.readObject();
			this.mapTacPos.putAll( map );
			ois.close();
			fis.close();
			System.out.println("Saved to: " + file.getAbsolutePath());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void main( final String[] args ) throws Exception {
		
//		final String strFile = "/data/Development/CM/test.tar";
//		final String strFile = "D:\\Tasks\\20210309 - COSMIC-417 - Devices\\"
//							+ "catalog.tsv";
		final String strFile = "/home/jmraben/Tasks/20210805_Devices/"
				+ "TAC_Lookup_202107.csv";
		final File file = new File( strFile );

//		final String strFileIndex = "D:\\Tasks\\"
//						+ "20210309 - COSMIC-417 - Devices\\catalog.idx";
		final String strFileIndex = "/home/jmraben/Tasks/20210805_Devices/"
				+ "catalog_202107.idx";
		final File fileIndex = new File( strFileIndex );

		final ScanFile sf = new ScanFile( file );
		
		final long lTimeStart = System.currentTimeMillis();
		sf.loadIndex( fileIndex );
//		sf.scan( 1000 );
		final long lTimeEnd = System.currentTimeMillis();
		final long lElapsed = lTimeEnd - lTimeStart;
		System.out.println( "Elapsed: " + lElapsed / 1000 );
		
//		sf.runSeekTest( sf.lTacMostRepeated );
		sf.runSeekTest( 1318400 );
		
//		sf.saveIndex( fileIndex );
	}
	

}
