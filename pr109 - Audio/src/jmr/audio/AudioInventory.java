package jmr.audio;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

public class AudioInventory {

	public final List<AudioPort> listPorts = new LinkedList<>();
	
	public List<AudioPort> scan() {

		listPorts.clear();
		
		final Info[] arrInfo = AudioSystem.getMixerInfo();
		int iInfoIndex = 0;
		for ( final Info info : arrInfo ) {
			
//			System.out.println();
//			System.out.println( "Info: " + info );
//			System.out.println( "\t\tInfo.getName(): " + info.getName() );
//			System.out.println( "\t\tInfo.getDescription(): " + info.getDescription() );
//			System.out.println( "\t\tInfo.getVendor(): " + info.getVendor() );
//			System.out.println( "\t\tInfo.getVersion(): " + info.getVersion() );
			
			final Mixer mixer = AudioSystem.getMixer( info );
			
			// recording (typically?)
			final Line.Info[] arrTLI = mixer.getTargetLineInfo();
			if ( arrTLI.length>0 ) {
				int iLIIndex = 0;
				for ( final javax.sound.sampled.Line.Info li : arrTLI ) {
					
					final AudioPort port = AudioPort.build( 
									iInfoIndex, info, null, iLIIndex, li );
					if ( null!=port ) {
						this.listPorts.add( port );
					}
					iLIIndex++;
				}
			}

			// playback (typically?)
			final Line.Info[] arrSLI = mixer.getSourceLineInfo();
			if ( arrSLI.length>0 ) {
				int iLIIndex = 0;
				for ( final javax.sound.sampled.Line.Info li : arrSLI ) {
					
					final AudioPort port = AudioPort.build( 
									iInfoIndex, info, iLIIndex, null, li );
					if ( null!=port ) {
						this.listPorts.add( port );
					}
					iLIIndex++;
				}
			}

			
			iInfoIndex++;
		}
		return this.listPorts;
	}

	
	private static boolean equals(	final Integer left,
									final Integer right ) {
		if ( null==left ) {
			if ( null==right ) {
				return true;
			} else {
				return false;
			}
		} else if ( left.equals( right ) ) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public AudioPort getPort(	final int iDeviceIndex,
								final Integer iSourceIndex,
								final Integer iTargetIndex ) {
		for ( final AudioPort port : listPorts ) {
			if ( iDeviceIndex == port.iInfoIndex 
					&& equals( iSourceIndex, port.iSourceIndex )
					&& equals( iTargetIndex, port.iTargetIndex ) ) {
				return port;
			}
		}
		return null;
	}
	
	
	public static void main( final String[] args ) {
		
		final AudioInventory inv = new AudioInventory();
		
		final List<AudioPort> list = inv.scan();
		
		final List<File> listClips = new LinkedList<>();

		for ( final AudioPort port : list ) {
			System.out.println( port );
			
			final Map<String, String> map = port.getDetails();
			final List<String> listNames = new LinkedList<>( map.keySet() );
			Collections.sort( listNames );
			for ( final String strName : listNames ) {
				final String strValue = map.get( strName );
				System.out.println( "\t" + strName + " = " + strValue );
			}
		}
		

		for ( final AudioPort port : list ) {
//			System.out.println( port );
			
			if ( port instanceof RecordPort ) {
				final File file = ((RecordPort) port).test();
				listClips.add( file );
				System.out.println( "Captured on " + port + " clip " + file );
			}
		}
		

		for ( final AudioPort port : list ) {
//			System.out.println( port );
			
			final List<File> files = Collections.unmodifiableList( listClips );
			if ( port instanceof PlaybackPort ) {
				for ( final File file : files ) {
					
					System.out.println( "Playing on " + port + " clip " + file );
					
					((PlaybackPort) port).test( file );
				}
			}
		}
		
		
	}
	
	
	
}
