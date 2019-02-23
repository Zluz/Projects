package jmr.audio;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.sound.sampled.AudioFormat;

public class Clip {

	final byte[] data;

	final static String DELIM_FORMAT = "@";
	

	final AudioFormat format;
	
	public Clip(	final AudioFormat format,
					final int iBufferSize ) {
		this.format = format;
		data = new byte[ iBufferSize ];
	}

	
	public Clip(	final AudioFormat format,
					final byte[] buffer ) {
		this.format = format;
		data = new byte[ buffer.length ];
		System.arraycopy( buffer, 0, data, 0, buffer.length );
	}
	
	
	public Clip( final String strContent ) {
		if ( null==strContent ) 
			throw new IllegalStateException( "Content is null" );
		
		final String[] arr = strContent.split( "\n" );
		final String strFormat = arr[1];
		final String strLength = arr[2];
		final String strData = arr[3];
		
		final String[] arrFormat = strFormat.split( DELIM_FORMAT );
		
		this.format = new AudioFormat( 
//				fRate, iSampleSize, iChannels, bSigned, bBigEndian );
				Float.parseFloat( arrFormat[0] ),
				Integer.parseInt( arrFormat[1] ),
				Integer.parseInt( arrFormat[2] ),
				true,
				Boolean.parseBoolean( arrFormat[4] ) );
		
		@SuppressWarnings("unused")
		final String strOriginalFormat = arrFormat[5];
		@SuppressWarnings("unused")
		final String strResolvedFormat = this.format.toString();
		
		final int iLength = Integer.parseInt( strLength );
		this.data = new byte[ iLength ];
		
		final Decoder decoder = Base64.getDecoder();
//		final byte[] decoded = decoder.decode( strData.getBytes() );
		final byte[] decoded = decoder.decode( strData );
		
		final int iDataLength = Math.min( decoded.length, iLength );
		
		System.arraycopy( decoded, 0, this.data, 0, iDataLength );
	}

	
	public String serialize() {
		StringBuilder sb = new StringBuilder();
		sb.append( "\n" );
//		sb.append( "CD" );
		sb.append( "" + format.getFrameRate() + DELIM_FORMAT
					+ format.getSampleSizeInBits() + DELIM_FORMAT
					+ format.getChannels() + DELIM_FORMAT
					+ true + DELIM_FORMAT 
					+ format.isBigEndian() + DELIM_FORMAT
					+ format.toString() ); 
		sb.append( "\n" );
		
		final Encoder encoder = Base64.getEncoder();
		
		final byte[] encoded = encoder.encode( data );
		final String strEncoded = new String( encoded );
		

		
		final Decoder decoder = Base64.getDecoder();
//		final byte[] decoded = decoder.decode( strData.getBytes() );
		@SuppressWarnings("unused")
		final byte[] decoded = decoder.decode( strEncoded );
		
		
		
		
		
		
		sb.append( "" + strEncoded.length() + "\n" );
		sb.append( strEncoded );
		sb.append( "\n" );
		
		return sb.toString();
	}
	
	
	public static void main( final String[] args ) {
		
		final byte[] original = new byte[ 100 ]; // { 1, 2, 3, 4, 5 };
		for ( byte i=0; i<100; i++ ) {
			original[ i ] = i;
		}

		System.out.print( "Bytes (" + original.length + "): [");
		for ( byte b : original ) {
			System.out.print( " " + b );
		}
		System.out.println( " ]" );

//		final byte[] source  = "Test".getBytes();
		
		final byte[] encodedBytes = Base64.getEncoder().encode( original );
		final String strEncoded = new String( encodedBytes );
		System.out.println( "encodedBytes " + strEncoded );
		
		final byte[] strEncodedBytes = strEncoded.getBytes();
		final byte[] decodedBytes = Base64.getDecoder().decode( strEncodedBytes );
//		System.out.println( "decodedBytes " + new String( decodedBytes ) );
		
		System.out.print( "Bytes (" + decodedBytes.length + "): [");
		for ( byte b : decodedBytes ) {
			System.out.print( " " + b );
		}
		System.out.println( " ]" );
	}
	
	
}
