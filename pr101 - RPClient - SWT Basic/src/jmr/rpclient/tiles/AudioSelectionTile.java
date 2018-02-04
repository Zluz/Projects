package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;

public class AudioSelectionTile extends TileBase {

	
	public static enum AudioProgram {
		PLAY_SPOTIFY( "Spotify (ext)", "/Local/scripts/play_stop.sh" ),
		PLAY_LOVELINE( "Classic Loveline", "/Local/scripts/play_vlc.sh" ),
		PLAY_TWIT( "TWiT (Twitch)", "/Local/scripts/play_twit.sh" ),
		PLAY_NPR( "NPR stream", "/Local/scripts/play_npr.sh" ),
		;
		
		public final String strTitle;
		public final String strScript;
		
		AudioProgram(	final String strTitle,
						final String strScript ) {
			this.strTitle = strTitle;
			this.strScript = strScript;
		}
	}
	
	


	
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		try {

			final GCTextUtils text = new GCTextUtils( gc );
			text.setRect( gc.getClipping() );
		

			gc.setFont( Theme.get().getFont( 11 ) );

//			text.println( "Select audio program" );
			
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );

			int iY = 10;
			int iX = 20;
			for ( final AudioProgram program : AudioProgram.values() ) {
				super.addButton( gc, program.ordinal(), 
						iX, iY,  120, 50, program.strTitle );
				if ( iY > 50 ) {
					iY = 10;
					iX += 140;
				} else {
					iY += 70;
				}
			}
			
//			super.addButton( gc, AudioProgram.PLAY_LOVELINE.ordinal(), 
//								10, 30,  125, 55, 
//								AudioProgram.PLAY_LOVELINE.strTitle );
//			
//			super.addButton( gc, AudioProgram.PLAY_TWIT.ordinal(), 
//								10, 100, 125, 55, 
//								AudioProgram.PLAY_TWIT.strTitle );
//			
//			super.addButton( gc, AudioProgram.PLAY_NPR.ordinal(), 
//								10, 170, 125, 55, 
//								AudioProgram.PLAY_NPR.strTitle );

			gc.setForeground( Theme.get().getColor( Colors.LINE_FAINT ) );
			gc.drawLine( 299, 0, 299, 10 );
			gc.drawLine( 290, 0, 299, 0 );

			gc.drawLine( 299, 140, 299, 149 );
			gc.drawLine( 290, 149, 299, 149 );

		} catch ( final Throwable t ) {
			gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
			gc.setFont( Theme.get().getFont( 16 ) );
			gc.drawText( t.toString(), 10, 50 );
		}
		
	}


	@Override
	public boolean clickCanvas( final Point point ) {
//		this.pointClick = point;
//
//		bRefreshRequest = true;
//		Job.add( JobType.TESLA_READ, DataRequest.CHARGE_STATE.name() );
//		Job.add( JobType.TESLA_READ, DataRequest.VEHICLE_STATE.name() );
//		Job.add( JobType.TESLA_READ, DataRequest.CLIMATE_STATE.name() );
//		return true;
		return false;
	}


	private void play( final AudioProgram program ) {

		System.out.println( "Selected audio program: " + program.strTitle );

		final Thread thread = new Thread( "Button action (AudioSelectionTile)" ) {
			public void run() {

				final Map<String,String> map = new HashMap<String,String>();
				map.put( "remote", "media" );
				map.put( "command", program.strScript );
					
				Job.add( JobType.REMOTE_EXECUTE, map );
			};
		};
		thread.start();
	}
	
	
	@Override
	protected void activateButton( final S2Button button ) {
		for ( final AudioProgram program : AudioProgram.values() ) {
			if ( program.ordinal()==button.getIndex() ) {
				play( program );
			}
		}
	}
	
	
}
