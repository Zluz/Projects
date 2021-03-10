package jmr.pr141;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class ScanFile {
	
	final RandomAccessFile raf;
	
	final Map<Long,Long> mapTacPos = new TreeMap<>();
	


	final int iBufferSize = 5000;
	final byte[] arrBytes = new byte[ iBufferSize ];
	
	final StringBuilder sbReader = new StringBuilder();
	
	
	public ScanFile( final File file ) throws Exception {
		raf = new RandomAccessFile( file, "r" );
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
	
	private boolean advanceTo( final String strTarget,
							   final int iMaxLength,
							   final boolean bRead ) {
		boolean bSearching = true;
		int iTravelled = 0;
		try {
			final long lPosStart = raf.getFilePointer();
			do {
				Arrays.fill( arrBytes, (byte)0 );
				final int iBytesRead = raf.read( arrBytes );
				iTravelled += iBytesRead;
				
				final Integer iFound = findCharInBuffer( strTarget );			
				if ( null != iFound ) {
					bSearching = false;
					final long lPosTarget = lPosStart + iFound + 1;
					raf.seek( lPosTarget );

					if ( bRead ) {
						final String strSubstr = 
								new String( arrBytes, 0, iFound + 1 );
						sbReader.append( strSubstr );
					}
//System.out.print( "+" );
					return true;
				} else if ( iBytesRead != arrBytes.length ) {
//System.out.print( "Y1" );
					bSearching = false;
				} else if ( iTravelled > iMaxLength ) {
//System.out.print( "Y2" );
					bSearching = false;
				}
				
				if ( bRead ) {
					sbReader.append( arrBytes );
				}
				
			} while ( bSearching );
//System.out.print( "X1" );
			return false;
		} catch ( final IOException e ) {
//System.out.print( "X2" );					
			return false;
		}
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
	
	
	public void scan_002() throws Exception {

		boolean bContinue = true;
		final long lLength = raf.length();
		
		int i = 0;

		while ( bContinue ) {
			i++;
			
			this.sbReader.setLength( 0 );
			
			final boolean bAdvHit1 = advanceTo( "\t\r", 40, true );
			final String str = clean( this.sbReader.toString() );
			
			final String strTrimmed = str.trim();
			final boolean bComment = ( '#' == strTrimmed.charAt( 0 ) );
			
			final boolean bHitLF = str.contains( "\n" ) 
					|| str.contains( "\f" ) || str.contains( "\r" );
			final boolean bHitTab = str.contains( "\t" );
			
			final long lPosition = raf.getFilePointer();

//			if ( ! bHitLF ) {
			this.sbReader.setLength( 0 );
			final boolean bAdvHit2 = advanceTo( "\r", 100000, true );
			
//			}
			
			System.out.print( "Read [" );
			if ( bAdvHit1 ) System.out.print( "1" );
			if ( bComment ) System.out.print( "C" );
			if ( bHitLF ) System.out.print( "L" );
			if ( bHitTab ) System.out.print( "T" );
			if ( bAdvHit2 ) System.out.print( "2" );
			System.out.print( "] (" + lPosition + "): " );
			final String strSafe = safe( str );
			System.out.print( "\"" + strSafe + "\"" );

			System.out.println( " >>> \"" 
						+ safe( clean( sbReader.toString() ) ) + "\"" );

			bContinue = i < 14;
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
	
	
	
	public static void main( final String[] args ) throws Exception {
		
//		final String strFile = "/data/Development/CM/test.tar";
		final String strFile = "D:\\Tasks\\20210309 - COSMIC-417 - Devices\\"
							+ "catalog.tsv";
		final File file = new File( strFile );
		
		final ScanFile sf = new ScanFile( file );
		
		sf.scan_002();
	}
	

}
