package jmr.rpclient.tiles;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;

import jmr.pr102.DataRequest;
import jmr.rpclient.tiles.Theme.Colors;
import jmr.s2db.Client;
import jmr.util.transform.DateFormatting;

public class TeslaTile extends TileBase {


	final static EnumMap< DataRequest, Map<String,String> > 
							pages = new EnumMap<>( DataRequest.class );

	final static EnumMap< DataRequest, Long > 
							updated = new EnumMap<>( DataRequest.class );

	private Thread threadUpdater;

	public TeslaTile() {
		threadUpdater = new Thread( "TeslaTile Updater" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
		
					for (;;) {
						try {
							updatePages();
						} catch ( final Exception e ) {
							// ignore.. 
							// JDBC connection may have been dropped..
						}
		
						Thread.sleep( TimeUnit.SECONDS.toMillis( 4 ) );
		//						Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
//		threadUpdater.start();
	}
	
	
	
	
	@Override
	public MouseListener getMouseListener() {
		return null;
	}

	

	private void updatePage( final DataRequest type ) {

		final String strPath = 
//				"/External/Ingest/Tesla/" + type.name() + "/response";
				type.getResponsePath();
		
		final Map<String, String> map = 
					Client.get().loadPage( strPath );

		pages.put( type, map );
		
//		final String strModified = map.get( Page.ATTR_LAST_MODIFIED );
//		final Long lModified = Long.parseLong( strModified );
//		updated.put( type, lModified );
	}
	
	private void updatePages() {
		synchronized ( pages ) { 

			final DataRequest[] values = DataRequest.values();
			for ( final DataRequest type : values ) {
//				System.out.println( "Checking: " + type );
				if ( false
						|| DataRequest.VEHICLE_STATE.equals( type ) 
						|| DataRequest.CHARGE_STATE.equals( type )
//						|| DataRequest.DRIVE_STATE.equals( request ) 
//						|| DataRequest.GUI_SETTINGS_STATE.equals( request )
//						|| DataRequest.CLIMATE_STATE.equals( request )
												) {
					try {
						updatePage( type );
					} catch ( final Throwable t ) {
						t.printStackTrace();
					}
				}
			}
		}
	}
	
	
	
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
//		gc.fillRectangle( rect );
		
		synchronized ( pages ) { 
	
			gc.setFont( Theme.get().getFont( 25 ) );
			gc.drawText( "Tesla", 10, 10 );
			gc.setFont( Theme.get().getFont( 10 ) );


			try {
				final Map<String, String> 
						mapCharge = pages.get( DataRequest.CHARGE_STATE );
				final Map<String, String> 
						mapVehicle = pages.get( DataRequest.VEHICLE_STATE );
	
				if ( null!=mapCharge && null!=mapVehicle ) {
				
					final String strVersion = mapVehicle.get( "car_version" );
					final String strOdometer = mapVehicle.get( "odometer" );
					final String strRange = mapCharge.get( "battery_range" );
					final String strStatus = mapCharge.get( "+status" );
					final String strTimestamp = mapCharge.get( ".last_modified_uxt" );
//					final String strTime = mapCharge.get( "time_to_full_charge" );

					gc.drawText( strVersion, 10, 50 );
	
					gc.setFont( Theme.get().getFont( 16 ) );
//					gc.drawText( "Time to Charge:  " + strTime, 40, 112 );
					if ( null!=strStatus ) {
						gc.drawText( strStatus, 30, 112 );
					} else {
						gc.drawText( "(null status)", 30, 112 );
					}
					
					gc.setFont( Theme.get().getFont( 14 ) );
					gc.drawText( "Range:", 40, 86 );


					gc.setFont( Theme.get().getFont( 12 ) );
					gc.drawText( "Odometer" +Text.DELIMITER 
									+ strOdometer + " mi", 160, 26 );

					gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
					gc.setFont( Theme.get().getFont( 28 ) );
					gc.drawText( strRange + " mi", 110, 70 );

					final String strElapsed = 
							DateFormatting.getSmallTime( strTimestamp );
					
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
					gc.setFont( Theme.get().getFont( 6 ) );
					gc.drawText( strElapsed, 265, 10 );

				} else {
					gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
					gc.setFont( Theme.get().getFont( 16 ) );
					gc.drawText( "Missing data" + Text.DELIMITER 
							+ "mapCharge: " + mapCharge + Text.DELIMITER 
							+ "mapState: " + mapVehicle, 10, 50 );
				}

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

	}

}
