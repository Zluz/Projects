package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;

public class PerspectiveSwitcherTile extends TileBase {


//	public static enum AudioProgram {
//		PLAY_SPOTIFY( "Spotify (ext)", "/Local/scripts/stop_all.sh" ),
//		CHANNEL_A( "Channel A", "/Local/scripts/stop_all.sh" ),
//		PLAY_LOVELINE( "Classic Loveline", "/Local/scripts/play_vlc.sh" ),
//		PLAY_TWIT( "TWiT (Twitch)", "/Local/scripts/play_twit.sh" ),
//		PLAY_WTOP( "WTOP stream", "/Local/scripts/play_wtop.sh" ),
//		PLAY_NPR( "NPR stream", "/Local/scripts/play_npr.sh" ),
//		;
//		
//		//	Spotify		Loveline	WTOP live
//		//	Channel A	TWiT live	NPR live
//
//		public final String strTitle;
//		public final String strScript;
//		
//		AudioProgram(	final String strTitle,
//						final String strScript ) {
//			this.strTitle = strTitle;
//			this.strScript = strScript;
//		}
//	}
	
	


	
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		try {

			final GCTextUtils text = new GCTextUtils( gc );
			text.setRect( gc.getClipping() );
		

			gc.setFont( Theme.get().getFont( 11 ) );

			text.println( "Available Perspectives" );
			
			gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );

			int iY = 26;
			int iX = 14;
			for ( final Perspective perspective : Perspective.values() ) {
				
				final String strTitle = perspective.name();
				
				super.addButton( gc, perspective.ordinal(), 
						iX, iY,  134, 34, strTitle );
				iY += 44;
				
				if ( iY >= gc.getClipping().height ) break;
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


	private void setPerspective(	final S2Button button,
									final Perspective perspective ) {

		System.out.println( "Selected perspective: " + perspective.name() );

		final Thread thread = new Thread( "Button action (PerspectiveSwitcherTile)" ) {
			public void run() {

				button.setState( ButtonState.WORKING );

				Display.getDefault().asyncExec( new Runnable() {
					@Override
					public void run() {
						TileCanvas.getInstance().setPerspective( perspective );
						button.setState( ButtonState.READY );
					}
				});

//				final Map<String,String> map = new HashMap<String,String>();
//				map.put( "remote", "media" );
//				map.put( "command", program.strScript );
//					
//				final Job job = Job.add( JobType.REMOTE_EXECUTE, map );
//				button.setJob( job );
				try {
					Thread.sleep( 200 );
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			};
		};
		thread.start();
//		button.setState( ButtonState.READY );

	}


	@Override
	protected void activateButton( final S2Button button ) {
		for ( final Perspective perspective : Perspective.values() ) {
			if ( perspective.ordinal()==button.getIndex() ) {
				setPerspective( button, perspective );
			}
		}
	}
	
	
	
	
	
}
