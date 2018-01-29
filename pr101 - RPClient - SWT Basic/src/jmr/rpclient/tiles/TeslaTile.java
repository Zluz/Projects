package jmr.rpclient.tiles;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

import jmr.pr102.DataRequest;
import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.tables.Job;
import jmr.util.transform.DateFormatting;

public class TeslaTile extends TileBase {


	final static EnumMap< DataRequest, Map<String,String> > 
							pages = new EnumMap<>( DataRequest.class );

	final static EnumMap< DataRequest, Long > 
							updated = new EnumMap<>( DataRequest.class );

	private Thread threadUpdater;
	
	private Point pointClick = null;
	
	private static boolean bRefreshRequest = false;
	private static long lLastRefresh = 0;

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
//		synchronized ( pages ) { 

			final DataRequest[] values = DataRequest.values();
			for ( final DataRequest type : values ) {
//				System.out.println( "Checking: " + type );
				if ( false
						|| DataRequest.VEHICLE_STATE.equals( type ) 
						|| DataRequest.CHARGE_STATE.equals( type )
//						|| DataRequest.DRIVE_STATE.equals( request ) 
//						|| DataRequest.GUI_SETTINGS_STATE.equals( request )
						|| DataRequest.CLIMATE_STATE.equals( type )
												) {
					try {
						updatePage( type );
					} catch ( final Throwable t ) {
						t.printStackTrace();
					}
				}
			}
//		}
	}
	
	
	
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
//		gc.fillRectangle( rect );
		
		final boolean bSingleCell = 150 == gc.getClipping().width;

		if ( !bSingleCell ) {
			gc.setFont( Theme.get().getFont( 25 ) );
			gc.drawText( "Tesla", 10, 10 );
		}

		try {

			final Map<String, String> mapCharge = new HashMap<>();
			final Map<String, String> mapVehicle = new HashMap<>(); 
			final Map<String, String> mapClimate = new HashMap<>(); 

			synchronized ( pages ) { 
				mapCharge.putAll( pages.get( DataRequest.CHARGE_STATE ) );
				mapVehicle.putAll( pages.get( DataRequest.VEHICLE_STATE ) );
				mapClimate.putAll( pages.get( DataRequest.CLIMATE_STATE ) );
			}

			if ( null!=mapCharge && null!=mapVehicle ) {
			
				final String strVersion = mapVehicle.get( "car_version" );
//					final String strOdometer = mapVehicle.get( "odometer" );
				final String strRange = mapCharge.get( "battery_range" );
				final String strStatus = mapCharge.get( "+status" );
				final String strTimestamp = mapCharge.get( ".last_modified_uxt" );
				final String strHome = mapVehicle.get( "homelink_nearby" );
				final String strAPIVersion = mapVehicle.get( "api_version" );
//					final String strLatch = mapCharge.get( "charge_port_latch" );
				final String strPortOpen = mapCharge.get( "charge_port_door_open" );
//					final String strTime = mapCharge.get( "time_to_full_charge" );
				final boolean bHome = "true".equalsIgnoreCase( strHome );
//					final boolean bLatch = "Engaged".equalsIgnoreCase( strLatch );
				final boolean bPortOpen = "true".equalsIgnoreCase( strPortOpen );

				final boolean bClimateOn = "true".equalsIgnoreCase( 
										mapClimate.get( "is_climate_on" ) );
				final boolean bFanOn = !"0".equalsIgnoreCase( 
										mapClimate.get( "fan_status" ) );

				final boolean bChargeComplete = 
						"Complete".equalsIgnoreCase( 
								mapCharge.get( "charging_state" ) );

				final boolean bAlertCycle = Math.floor(System.currentTimeMillis()/500) % 2 == 0;
//				final boolean bAlert = bAlertCycle && bHome && !bPortOpen;
				final boolean bAlert = bHome && !bPortOpen;
				if ( bAlert && bAlertCycle ) {
//						gc.setForeground( Theme.get().getColor( Colors.BACK_ALERT ) );
					gc.setBackground( Theme.get().getColor( Colors.BACK_ALERT ) );
					gc.fillRectangle( 0, 0, 299, 149 );
				}

				try {
					final Long lLastModified = Long.parseLong( strTimestamp );
					if ( lLastRefresh != lLastModified ) {
						bRefreshRequest = false;
						lLastRefresh = lLastModified;
					}
				} catch ( final NumberFormatException e ) {
					// just ignore
				}

				

				
				if ( bSingleCell ) {

					gc.setFont( Theme.get().getFont( 11 ) );
					final GCTextUtils text = new GCTextUtils( gc );
					text.setRect( gc.getClipping() );
					
					text.println( bHome, "Location: Home" );
					text.println( bPortOpen, "Port open" );
					text.println( bChargeComplete, "Charging complete" );
					text.println( bClimateOn, "Climate On" );
					text.println( bFanOn, "    Fan On" );
					text.println( bRefreshRequest, "Refresh requested" );
					text.println( bAlert, "Alert" );
					
				} else {
	
					gc.setFont( Theme.get().getFont( 25 ) );
					gc.drawText( "Tesla", 10, 10 );
					gc.setFont( Theme.get().getFont( 10 ) );
					
					gc.drawText( strVersion, 10, 50 );
					gc.drawText( "API  " + strAPIVersion, 10, 64 );
	
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
	//					gc.drawText( "Odometer" +Text.DELIMITER 
	//									+ strOdometer + " mi", 160, 26 );
					gc.drawText( "Location" + Text.DELIMITER 
							+ ( bHome ? "Home" : "Away" ), 160, 26 );
	
					gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
					gc.setFont( Theme.get().getFont( 28 ) );
					gc.drawText( strRange + " mi", 110, 70 );
	
					final String strElapsed = 
							DateFormatting.getSmallTime( strTimestamp );
					
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
					gc.setFont( Theme.get().getFont( 6 ) );
					gc.drawText( strElapsed, 265, 10 );
				}
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
		
		if ( null!=pointClick ) {
			final int x = pointClick.x;
			final int y = pointClick.y;
			gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
			gc.drawOval( x-10, y-10, 20, 20 );
			if ( !bRefreshRequest ) {
				pointClick = null;
			}
		}

	}


	@Override
	public void click( final Point point ) {
		this.pointClick = point;
		
		bRefreshRequest = true;
		Job.add( "TESLA:" + DataRequest.CHARGE_STATE.name() );
		Job.add( "TESLA:" + DataRequest.VEHICLE_STATE.name() );
		Job.add( "TESLA:" + DataRequest.CLIMATE_STATE.name() );
	}
	
	
}
