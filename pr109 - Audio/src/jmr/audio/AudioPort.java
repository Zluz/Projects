package jmr.audio;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * Represents a javax.sound.sampled.Line.Info
 */
public abstract class AudioPort {

	/** 
	 * Device index 
	 * (from <code>AudioSystem.getMixerInfo()</code>, zero-based)
	 */
	public final int iInfoIndex;

	/** Device name (from Info) */
	public final String strName;
	/** Device description (from Info) */
	public final String strDescription;
	/** Device vendor (from Info) */
	public final String strVendor;
	/** Device version (from Info) */
	public final String strVersion;
	
	/** Index of the associated Line.Info (non-null seems to mean playback) */
	public final Integer iSourceIndex;
	/** Index of the associated Line.Info (non-null seems to mean recording) */
	public final Integer iTargetIndex;
	
	public final String strLineInfo;
	
	private final Class<?> classLine;
	
	
	public AudioPort(	final int iInfoIndex,
						final Info info,
						final Integer iSourceIndex,
						final Integer iTargetIndex,
						final Line.Info li ) {
		// record the Info (audio device)
		this.iInfoIndex = iInfoIndex;
		
		this.strName = info.getName();
		this.strDescription = info.getDescription();
		this.strVendor = info.getVendor();
		this.strVersion = info.getVersion();
		
		this.iSourceIndex = iSourceIndex;
		this.iTargetIndex = iTargetIndex;
		
		this.strLineInfo = li.toString();
		this.classLine = li.getLineClass();
	}
	

	
	public abstract AudioFormat getDefaultFormat();
	
	
	public static AudioPort build(	final int iInfoIndex,	
									final Info info,
									final Integer iSourceIndex,
									final Integer iTargetIndex,
									final Line.Info li ) {
		if ( null==li ) throw new IllegalStateException( "Line.Info is null." );
		
		final AudioPort port;
		final Class<?> classLine = li.getLineClass();
		if ( classLine.equals( SourceDataLine.class ) ) {
			port = new PlaybackPort( iInfoIndex, info, iSourceIndex, li );
		} else if ( classLine.equals( TargetDataLine.class ) ) {
			port = new RecordPort( iInfoIndex, info, iTargetIndex, li );
		} else if ( classLine.equals( Port.class ) ) {
			port = null; // for use with a mixer (?)
		} else {
			port = null; // not sure what this may be
		}
		return port;
	}
	
	public Map<String,String> getDetails() {
		final Map<String,String> map = new HashMap<>();
		map.put( "Info.index", ""+this.iInfoIndex );
		map.put( "Info.Name", this.strName );
		map.put( "Info.Description", this.strDescription );
		map.put( "Info.Vendor", this.strVendor );
		map.put( "Info.Version", this.strVersion );
		map.put( "Line.Info.source_index", ""+this.iSourceIndex );
		map.put( "Line.Info.target_index", ""+this.iTargetIndex );
		map.put( "Line.Info.toString()", this.strLineInfo );
		map.put( "LineClass", this.classLine.getName() );

		final AudioFormat format = this.getDefaultFormat();
		if ( null!=format ) {

			map.put( "Format", format.toString() );
			
			map.put( "Format.Encoding", "" + format.getEncoding() );
			map.put( "Format.Channels", "" + format.getChannels() );
			map.put( "Format.SampleRate", "" + format.getSampleRate() );
			map.put( "Format.SampleSize", "" + format.getSampleSizeInBits() );
			map.put( "Format.BigEndian", "" + format.isBigEndian() );
			
		} else {
			map.put( "Format", "null" );
		}
		
		return map;
	}

	
	@Override
	public String toString() {
		final String strSuper = super.toString();
		final String strThis = strSuper 
					+ " (" + this.iInfoIndex 
					+ "/" + this.iSourceIndex 
					+ "/" + this.iTargetIndex + ")"; 
		return strThis;
	}

	
}
