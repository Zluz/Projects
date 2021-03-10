package jmr.pr141;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScanFile {
	
	final RandomAccessFile raf;
	
	final Map<Long,List<Long>> mapTacPos = new TreeMap<>();
	


	final int iBufferSize = 5000;
	final byte[] arrBytes = new byte[ iBufferSize ];
	
	final StringBuilder sbReader = new StringBuilder();
	

	long lCountLines = 0;
	long lCountLineTACs = 0;
	long lMaxRepeatedTACs = 0;
	long lMaxLineSize = 0;
	long lTacMostRepeated = 0;
	

	
	
	
	
	public ScanFile( final File file ) throws FileNotFoundException {
		raf = new RandomAccessFile( file, "r" );
	}
	
	public Map<Long,List<Long>> getTacPosMap() {
		return this.mapTacPos;
	}
	
	
	private Integer findCharInBuffer( final String strTarget ) {
		final byte[] arrTarget = strTarget.getBytes();
		for ( int i = 0; i < arrBytes.length; i++ ) {
			for ( int j = 0; j < arrTarget.length; j++ ) {
				if ( arrTarget[ j ] == arrBytes[ i ] ) {
					return i;
				}
			}
		}
		return null;
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
	
	
	public String readNextLine() {
		final StringBuilder sb = new StringBuilder();
		final int iSize = 10240;
		final byte[] arrBuffer = new byte[ iSize ];
		try {
			boolean bContinue = true;
			while ( bContinue ) {
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
			}
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

	
	public void scan_002( final long lMaxCount ) throws Exception {

		boolean bContinue = true;
		final long lLength = raf.length();
		
		int i = 0;

		while ( bContinue ) {
			i++;
			
			this.sbReader.setLength( 0 );
			
			final long lFilePos = raf.getFilePointer();
			
//			final boolean bAdvHit1 = advanceTo( "\t\r", 40, true );
//			final String str = clean( this.sbReader.toString() );
//			final String str = readNextLine();
			final String str = readNextSignificantLine();
			
			if ( null == str ) break; //TODO change
			
			lMaxLineSize = Math.max( lMaxLineSize, str.length() );
			
//			final String strTrimmed = str.trim();
//			final boolean bComment = ( 1 == strTrimmed.indexOf( '#' ) );
			
			final List<Long> listTACs = Util.getTACsFromLine( str );
			
			final boolean bHitLF = str.contains( "\n" ) 
					|| str.contains( "\f" ) || str.contains( "\r" );
			final boolean bHitTab = str.contains( "\t" );
			
			final long lPosition = raf.getFilePointer();

//			if ( ! bHitLF ) {
//			this.sbReader.setLength( 0 );
//			final boolean bAdvHit2 = advanceTo( "\r", 100000, true );
			
//			}
			
			System.out.print( "Read [" );
//			if ( bAdvHit1 ) System.out.print( "1" );
//			if ( bComment ) System.out.print( "C" );
			if ( bHitLF ) System.out.print( "L" );
			if ( bHitTab ) System.out.print( "T" );
//			if ( bAdvHit2 ) System.out.print( "2" );
			System.out.print( "] (" + lPosition + "): " );
			final String strSafe = safe( str );
			if ( strSafe.length() > 100 ) {
				System.out.print( "(" + strSafe.length() + " chars):" );
				System.out.print( "\"" + strSafe.substring( 0, 98 ) + "...\"" );
			} else {
				System.out.print( "\"" + strSafe + "\"" );
			}
			
			System.out.print( " TACs: " + listTACs.toString() );

			addTacPosition( lFilePos, listTACs );
			
//			System.out.print( " >>> \"" 
//						+ safe( clean( sbReader.toString() ) ) + "\"" );
			System.out.println();

			bContinue = i < lMaxCount;
		}

		System.out.println( "lCountLines      : " + lCountLines );
		System.out.println( "lCountLineTACs   : " + lCountLineTACs );
		System.out.println( "lMaxLineSize     : " + lMaxLineSize );
		System.out.println( "lMaxRepeatedTACs : " + lMaxRepeatedTACs );
		System.out.println( "lTacMostRepeated : " + lTacMostRepeated );
		
		
		final List<Long> list = mapTacPos.get( lTacMostRepeated );
		System.out.println( "Positions: " + list.toString() );
		
		System.out.println( "Seeking, Loading.." );
		for ( final Long lPos : list ) {
			raf.seek( lPos );
			final String strLine = readNextLine();
			System.out.println( "Read (pos:" + lPos + "): "
					+ "(len:" + strLine.length() + ") "
					+ "\"" + strLine.substring( 0, 40 ) + "...\"" );
		}
	}
	
	
	public void scan_001() throws Exception {
		
		boolean bContinue = true;
		final long lLength = raf.length();
		
		while ( bContinue ) {
//		for ( int i = 1; i < 1000; i++ ) {
			
//			final long lRand = (long)( Math.random() * lLength );
//			raf.seek( lRand );
			
			final int iBytesRead = raf.read( arrBytes );
			
			System.out.print( ""+ iBytesRead + " bytes read. " );
			
			int iCount = 0;
			for ( int j = 0; j < arrBytes.length; j++ ) {
				if ( 10 == arrBytes[ j ] ) { // LF
					iCount++;
				}
			}
			
			System.out.print( "getFilePointer() = " + raf.getFilePointer() + " " );
			System.out.print( "length() = " + raf.length() + " " );
			System.out.print( "iCount = " + iCount + " " );
			
//			bContinue = iBytesRead == arrBytes.length;
			bContinue = raf.getFilePointer() < 100000;
			System.out.println();
		}
		raf.close();
	}
	

	public List<String> getDeviceLines( final long lTAC ) {
		
		final List<String> listRecords = new LinkedList<>();

		final List<Long> listPositions = mapTacPos.get( lTAC );
		if ( null == listPositions ) return listRecords;
		
		System.out.println( "Positions: " + listPositions.toString() );
		
		System.out.println( "Seeking, Loading.." );
		for ( final Long lPos : listPositions ) {
			try {
				raf.seek( lPos );
				final String strLine = readNextLine();
				listRecords.add( strLine );
				System.out.println( "Read (pos:" + lPos + "): "
						+ "(len:" + strLine.length() + ") "
						+ "\"" + strLine.substring( 0, 40 ) + "...\"" );
			} catch ( final IOException e ) {
				// just ignore for now
			}
		}
		return listRecords;
	}
	
	
	public static void main( final String[] args ) throws Exception {
		
//		final String strFile = "/data/Development/CM/test.tar";
		final String strFile = "D:\\Tasks\\20210309 - COSMIC-417 - Devices\\"
							+ "catalog.tsv";
		final File file = new File( strFile );
		
		final ScanFile sf = new ScanFile( file );
		
		sf.scan_002( 10000 );
	}
	

}
