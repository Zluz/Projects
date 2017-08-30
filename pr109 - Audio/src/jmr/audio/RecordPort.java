package jmr.audio;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import jmr.util.FileUtil;

import javax.sound.sampled.Mixer.Info;

public class RecordPort extends AudioPort {

	
	private AudioFormat format = null;
	
	private final Line.Info li;
	
//	private final Line line;
	

	public RecordPort(	final int iInfoIndex,
						final Info info,
						final int iTargetIndex,
						final Line.Info li ) {
		super( iInfoIndex, info, null, iTargetIndex, li );

		this.li = li;
		
		if ( ! li.getLineClass().equals( TargetDataLine.class ) ) {
			throw new IllegalStateException( 
								"Line.Info does not support record." );
		}
	}
	

	
	@Override
	public AudioFormat getDefaultFormat() {
		if ( null==this.format ) {
			try {
				final Line line = AudioSystem.getLine( li );
				try ( final TargetDataLine tdl = (TargetDataLine)line ) {
					this.format = tdl.getFormat();
				}
			} catch ( final LineUnavailableException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this.format;
	}

	
	public Map<String,String> getDetails() {
		final Map<String,String> map = super.getDetails();

		map.put( "Type", "Record" );

		return map;
	}

	
//	public static long lLastHash = 558638686;
	public static File fileBase = null;

	public File test() {
//		lLastHash = this.hashCode();

		final File fileBase;
		try {
			fileBase = File.createTempFile( 
//							"Clip_" + RecordPort.lLastHash, "" );
							"Clip_", "" );
		} catch ( final IOException e ) {
			e.printStackTrace();
			return null;
		}
		
		try {
			final Line line = AudioSystem.getLine( li );
			try ( final TargetDataLine tdl = (TargetDataLine)line; ) {
				final AudioFormat format = tdl.getFormat();
				tdl.open( format );
				
				tdl.start();
				
				final byte[] data = new byte[ tdl.getBufferSize() / 5 ];
				
//				System.out.print( "Recording (" + format + ", "
				System.out.print( "Recording ("
									+ "buf:" + data.length + ")..." );
//				System.out.println();
				
				int iMin = 0, iMax = 0;
				
				for ( int i=0; i<30; i++ ) {
					@SuppressWarnings("unused")
					final int iRead = tdl.read( data, 0, data.length );
					System.out.print( "." );
					
					
					final Clip clip = new Clip( format, data );
					final String strSerialized = clip.serialize();
					
//					System.out.print( "Raw data (" + data.length + " bytes): " );
					for ( int i2=0; i2<iRead; i2++ ) {
						final int iValue = data[i2];
//						System.out.print( " " + data[i2] );
						iMin = Math.min( iMin, iValue );
						iMax = Math.max( iMax, iValue );
					}
					
//					System.out.println( "Serialized "
//							+ "(" + strSerialized.length() + " bytes):" );
//					System.out.println( strSerialized );

//					System.out.println();
					
//					final File file = new File( "C:/TEMP/"
//							+ "CLIP_" + lLastHash + "_" + i );
					final File file = new File( 
//							fileBase.getAbsolutePath() + "_i.clip" );
							fileBase.getAbsolutePath() + "_" + i + ".clip" );
					FileUtil.saveToFile( file, strSerialized );
					file.deleteOnExit();
//					System.out.print( "Saved file: " + file + "   " );

//					System.out.println( 
//								"Sample range: " + iMin + " - " + iMax );
					

//					System.out.println();
				}
				System.out.print( "Done.  " );
				System.out.println( "Sample range: " + iMin + " - " + iMax );
			}
			
		} catch ( final LineUnavailableException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fileBase;
	}
	
	
	
	
}
