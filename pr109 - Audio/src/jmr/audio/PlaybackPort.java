package jmr.audio;

import java.io.File;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

import jmr.util.FileUtil;

public class PlaybackPort extends AudioPort {

	
	private AudioFormat format = null;
	
	private final Line.Info li;
	
//	private final Line line;
	

	public PlaybackPort(	final int iInfoIndex,
							final Info info,
							final int iSourceIndex,
							final Line.Info li ) {
		super( iInfoIndex, info, iSourceIndex, null, li );

		this.li = li;
		
		if ( ! li.getLineClass().equals( SourceDataLine.class ) ) {
			throw new IllegalStateException( 
								"Line.Info does not support playback." );
		}
	}
	

	
	@Override
	public AudioFormat getDefaultFormat() {
		if ( null==this.format ) {
			try {
				final Line line = AudioSystem.getLine( li );
				try ( final SourceDataLine tdl = (SourceDataLine)line ) {
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

		map.put( "Type", "Playback" );

		return map;
	}

	

	public File test( final File fileBase ) {
		
//		final File fileBase;
////		try {
////			fileBase = File.createTempFile( 
////							"Clip_" + RecordPort.lLastHash + "_", "" );
//			fileBase = RecordPort.fileBase;
//			if ( null==fileBase ) return null;
//			
////		} catch ( final IOException e ) {
////			e.printStackTrace();
////			return null;
////		}
		
		try {
			final Line line = AudioSystem.getLine( li );
			try ( final SourceDataLine sdl = (SourceDataLine)line; ) {
				final AudioFormat format = sdl.getFormat();
				sdl.open( format );
				
				sdl.start();
				
				int iMin = 0, iMax = 0;

//				final byte[] data = new byte[ sdl.getBufferSize() / 5 ];
				
				System.out.print( "Playing (" + format + ")..." );
				int i=0;
				while ( i>=0 ) {
//				for ( int i=0; i<30; i++ ) {
					

//					System.out.println();
//					final File file = new File( "C:/TEMP/"
//							+ "CLIP_" + RecordPort.lLastHash + "_" + i + ".clip" );
					final File file = new File( 
							fileBase.getAbsolutePath() + "_" + i + ".clip" );
//					System.out.println( "Loaded file: " + file );
					
//					final Clip clip = new Clip( format, data.length );
					final String strContent = FileUtil.readFromFile( file );
					if ( null!=strContent ) {
						final Clip clip = new Clip( strContent );
//					final String[] strings = strContent.split( "\n" );

//					final byte[] data = new byte[  ];

//					System.arraycopy( clip.data, 0, data, 0, data.length );
					
					
//					@SuppressWarnings("unused")
//					final int iRead = tdl.read( data, 0, data.length );
						System.out.print( "." );
						sdl.write( clip.data, 0, clip.data.length );
						
						for ( int i2=0; i2<clip.data.length; i2++ ) {
							final int iValue = clip.data[i2];
//							System.out.print( " " + data[i2] );
							iMin = Math.min( iMin, iValue );
							iMax = Math.max( iMax, iValue );
						}
						
					} else {
						i = -10;
						System.out.print( "x-" );
					}

					
////					final Clip clip = new Clip( format, data );
//					final String strSerialized = clip.serialize();
//					
//					System.out.print( "Raw data (" + data.length + " bytes): " );
////					for ( int i2=0; i2<iRead; i2++ ) {
////						System.out.print( " " + data[i2] );
////					}
//					System.out.println();
//					
//					System.out.println( "Serialized "
//							+ "(" + strSerialized.length() + " bytes):" );
////					System.out.println( strSerialized );
//
//					System.out.println();
//					
////					final File file = new File( "C:/TEMP/"
//							+ "CLIP_" + this.hashCode() + "_" + i );
//					FileUtil.saveToFile( file, strSerialized );
//					System.out.println( "Saved file: " + file );

//					System.out.println();
					i++;
				}
				System.out.print( "Done.  " );
				System.out.println( "Sample range: " + iMin + " - " + iMax );
			}
//			System.out.println( "Done." );
			
		} catch ( final LineUnavailableException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fileBase;
	}
	
	
	
	
	
}
