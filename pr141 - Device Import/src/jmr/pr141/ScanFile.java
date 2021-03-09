package jmr.pr141;

import java.io.File;
import java.io.RandomAccessFile;

public class ScanFile {
	
	public static void main( final String[] args ) throws Exception {
		final String strFile = "/data/Development/CM/test.tar";
		final File file = new File( strFile );
		final int iBufferSize = 10240;
		final byte[] arrBytes = new byte[ iBufferSize ];
		
		final RandomAccessFile raf = new RandomAccessFile( file, "r" );
		
		boolean bContinue = true;
		final long lLength = raf.length();
		
//		while ( bContinue ) {
		for ( int i = 1; i < 1000; i++ ) {
			
			final long lRand = (long)( Math.random() * lLength );
			raf.seek( lRand );
			
			final int iBytesRead = raf.read( arrBytes );
			
			System.out.print( ""+ iBytesRead + " bytes read. " );
			
			int iCount = 0;
			for ( int j = 0; j < arrBytes.length; j++ ) {
				if ( 10 == arrBytes[ j ] ) {
					iCount++;
				}
			}
			
			System.out.print( "getFilePointer() = " + raf.getFilePointer() + " " );
			System.out.print( "length() = " + raf.length() + " " );
			System.out.print( "iCount = " + iCount + " " );
			
			bContinue = iBytesRead == arrBytes.length;
			System.out.println();
		}
		raf.close();
	}
	

}
