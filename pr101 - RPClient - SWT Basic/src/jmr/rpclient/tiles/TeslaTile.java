package jmr.rpclient.tiles;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

import jmr.data.Conversion;
import jmr.pr102.Command;
import jmr.pr102.DataRequest;
import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.util.report.TraceMap;
import jmr.util.transform.DateFormatting;
import jmr.util.transform.JsonUtils;
import jmr.util.transform.Temperature;

public class TeslaTile extends TileBase {

	public static enum Mode { STATUS, CLIMATE, STAMP }; 
	
	
	private final static int BUTTON_CLIMATE_ON = 1;
	private final static int BUTTON_CLIMATE_OFF = 2;
	private final static int BUTTON_FLASH_LIGHTS = 3;
	
	
	final static EnumMap< DataRequest, Map<String,String> > 
							pages = new EnumMap<>( DataRequest.class );

	final static EnumMap< DataRequest, Long > 
							updated = new EnumMap<>( DataRequest.class );

	
//	private final boolean bClimateControl;
	private final Mode mode;
	
	private static Thread threadUpdater;
	
	private Point pointClick = null;
	
	private static boolean bRefreshRequest = false;
	private long lLastRefresh = 0;

	
	public TeslaTile() {
		this( Mode.STATUS );
	}

	
//	public TeslaTile( final boolean bClimateControl ) {
	public TeslaTile( final Mode mode ) {
//		this.bClimateControl = bClimateControl;
		this.mode = mode;
		if ( null==threadUpdater ) {
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
		}
//		threadUpdater.start();
	}
	
	
	

	private void updatePage( final DataRequest type ) {

		final String strPath = 
//				"/External/Ingest/Tesla/" + type.name() + "/response";
//				"/External/Ingest/Tesla/" + type.name() + "/data";
//				"/External/Ingest/Tesla/" + type.name() + "/data/response";
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
	
	
	public static Map<String,String> nullCheck( final Map<String,String> map ) {
		if ( null!=map ) return map;
		return Collections.emptyMap();
	}
	
	
	@Override
	public void paint( 	final GC gc, 
						final Image image ) {
		
		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
//		gc.fillRectangle( rect );
		
		final boolean bSingleCell = 150 == gc.getClipping().width;

//		if ( !bSingleCell ) {
////			gc.setFont( Theme.get().getFont( 25 ) );
//			gc.setFont( Theme.ThFont._25_SSCM_V.getFont() );
//			gc.drawText( "Tesla", 10, 10 );
//		}

		try {
			String strError = null;
			String strAlert = null;

			final Map<String, String> mapCharge = new HashMap<>();
			final Map<String, String> mapVehicle = new HashMap<>(); 
			final Map<String, String> mapClimate = new HashMap<>(); 

			synchronized ( pages ) { 
				mapCharge.putAll( nullCheck( 
								pages.get( DataRequest.CHARGE_STATE ) ) );
				mapVehicle.putAll( nullCheck( 
								pages.get( DataRequest.VEHICLE_STATE ) ) );
				mapClimate.putAll( nullCheck( 
								pages.get( DataRequest.CLIMATE_STATE ) ) );
			}

			if ( null!=mapCharge && null!=mapVehicle ) {
				
				final String strVersion = mapVehicle.get( "car_version" );
//					final String strOdometer = mapVehicle.get( "odometer" );
				final String strRange = mapCharge.get( "battery_range" );
//				final String strStatus = mapCharge.get( "+status" );
				final String strChargeStatus = mapCharge.get( "charging_state" );
				final String strSoftware = mapVehicle.get( "software_update" );
				final String strUpdate = JsonUtils.getJsonValue( 
												strSoftware, "status" );
				
//				final String strTimestampCharge = mapCharge.get( ".last_modified_uxt" );
//				
//				final int iTimestampCharge = Conversion.getIntFromString( 
//						mapCharge.get( ".last_modified_uxt" ), Integer.MIN_VALUE );
//				final int iTimestampVehicle = Conversion.getIntFromString( 
//						mapVehicle.get( ".last_modified_uxt" ), Integer.MIN_VALUE );
//				final int iTimestampClimate = Conversion.getIntFromString( 
//						mapClimate.get( ".last_modified_uxt" ), Integer.MIN_VALUE );
				
				final Long lTimestampLatest = Conversion.getMaxLongFromStrings(
									mapCharge.get( ".last_modified_uxt" ),
									mapVehicle.get( ".last_modified_uxt" ),
									mapClimate.get( ".last_modified_uxt" ) );
				
				final String strHome = mapVehicle.get( "homelink_nearby" );
				final String strAPIVersion = mapVehicle.get( "api_version" );
			
				final String strPortOpen = mapCharge.get( "charge_port_door_open" );
//					final String strTime = mapCharge.get( "time_to_full_charge" );
				final boolean bHome = "true".equalsIgnoreCase( strHome );
//					final boolean bLatch = "Engaged".equalsIgnoreCase( strLatch );
				final boolean bPortOpen = "true".equalsIgnoreCase( strPortOpen );
				final String strInsideTemp = mapClimate.get( "inside_temp" );
				final String strSetTemp = mapClimate.get( "driver_temp_setting" );

				final String strLatch = mapCharge.get( "charge_port_latch" );
				final boolean bLatched = "Engaged".equalsIgnoreCase( strLatch );

				final boolean bClimateOn = "true".equalsIgnoreCase( 
										mapClimate.get( "is_climate_on" ) );
				final boolean bFanOn = !"0".equalsIgnoreCase( 
										mapClimate.get( "fan_status" ) );
				
				final String strBatteryLevel = mapCharge.get( "battery_level" );
				boolean bLowBattery = false;
				int iBatteryLevel = -1;
				try {
					iBatteryLevel = Integer.parseInt( strBatteryLevel );
					bLowBattery = iBatteryLevel < 70;
				} catch ( final NumberFormatException e ) {
					bLowBattery = true;
					strError = "Invalid battery_level";
				}

				final String strChargeState = mapCharge.get( "charging_state" );
				final String strChargerPower = mapCharge.get( "charger_power" );
				final boolean bChargeComplete = 
							"Complete".equalsIgnoreCase( strChargeState );
				final boolean bCharging = 
							"Charging".equalsIgnoreCase( strChargeState );

//				final boolean bAlertCycle = 
//						Math.floor(System.currentTimeMillis()/500) % 2 == 0;
//				final boolean bAlert = bAlertCycle && bHome && !bPortOpen;
//				final boolean bAlert = bHome && !bPortOpen;
//				final boolean bError = null!=strError;
//				if ( null!=strError ) {
//					strAlert = "(See Error)";
//				}
				
//				if ( bHome && bLowBattery && !bCharging ) {
//				if ( bHome && bLowBattery && !bLatched ) {
				if ( bHome && bLowBattery && !bPortOpen ) {
					strAlert = "Plug in! " + strBatteryLevel + "%";
				}
				
				final boolean bAlert = ( null!=strError || null!=strAlert ); 
				if ( bAlert ) { // && bAlertCycle ) {
//						gc.setForeground( Theme.get().getColor( Colors.BACK_ALERT ) );
					gc.setBackground( Theme.get().getColor( 
									Colors.BACKGROUND_FLASH_ALERT ) );
					gc.fillRectangle( 0, 0, 299, 149 );
				}

				try {
//					final Long lLastModified = Long.parseLong( strTimestampCharge );
//					if ( lLastRefresh != lLastModified ) {
					if ( null!=lTimestampLatest 
									&& lLastRefresh != lTimestampLatest ) {
						bRefreshRequest = false;
						lLastRefresh = lTimestampLatest;
						setButtonState( TeslaTile.BUTTON_CLIMATE_OFF, 
													ButtonState.READY );
						setButtonState( TeslaTile.BUTTON_CLIMATE_ON, 
													ButtonState.READY );
						setButtonState( TeslaTile.BUTTON_FLASH_LIGHTS, 
													ButtonState.READY );
					}
				} catch ( final NumberFormatException e ) {
					// just ignore
				}


				final double dFahrenheitInside = Temperature
						.getFahrenheitFromCelsius( strInsideTemp );

				if ( Mode.STAMP.equals( this.mode ) ) {
					

//					gc.setFont( Theme.get().getFont( 11 ) );
					gc.setFont( Theme.ThFont._42_RCB.getFont() );
//					gc.setFont( Theme.ThFont._42_PR.getFont() );

					final String strInsideTempF = 
								String.format( "%.1f", dFahrenheitInside );
					
					final GCTextUtils text = new GCTextUtils( gc );
					text.setRect( gc.getClipping() );

					final String strMode;
					if ( ! bClimateOn ) {
						strMode = "off";
					}
					
					text.println( "In: " + strInsideTempF + "�F" );

					text.println( "Batt: " + iBatteryLevel + " %" );

					
				} else if ( bSingleCell ) {

					final GCTextUtils text = new GCTextUtils( gc );
					text.setRect( gc.getClipping() );
					
//					if ( this.bClimateControl ) {
					if ( Mode.CLIMATE.equals( this.mode ) ) {

//						gc.setFont( Theme.get().getFont( 11 ) );
						gc.setFont( Theme.ThFont._11_SSCM_V.getFont() );

						final String strInsideTempF = 
									String.format( "%.1f", dFahrenheitInside );
						
						text.println( "Inside temp: " + strInsideTempF + " �F" );
						
						gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );

						super.addButton( gc, BUTTON_CLIMATE_ON, 
											10, 30, 125, 55, "Climate ON" );
						super.addButton( gc, BUTTON_CLIMATE_OFF, 
											10, 100, 60, 40, "C OFF" );
						super.addButton( gc, BUTTON_FLASH_LIGHTS, 
											85, 100, 50, 40, "Flash" );
						
					} else {

						gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
//						gc.setFont( Theme.get().getFont( 11 ) );
						gc.setFont( Theme.ThFont._11_SSCM_V.getFont() );

						text.setSpacingAdjust( -5 );
						text.println( bHome, "Location: Home" );
						text.println( bPortOpen, "Port open" );
						text.println( bLatched, "   Port latched" );
						text.addSpace( 8 );
						text.println( bChargeComplete, "Charge complete" );
						text.println( bCharging, "   Charging" );
						text.println( bLowBattery, "   Low battery: " 
															+ strBatteryLevel );
						text.println( "        State: " + strChargeState );
						text.println( "        Power: " + strChargerPower );
						text.addSpace( 8 );
						text.println( bClimateOn, "Climate On" );
						text.println( bFanOn, "   Fan On" );
						if ( bRefreshRequest ) {
							gc.setBackground( Theme.get().getColor( 
											Colors.BACKGROUND_FLASH_ALERT ) );
						}
						text.println( bRefreshRequest, "Refresh request" );
						text.addSpace( 12 );

						if ( null != strAlert ) {
							gc.setBackground( Theme.get().getColor( 
									Colors.BACKGROUND_FLASH_ALERT ) );
							text.println( null!=strAlert, "Alr> " + strAlert );
						} else {
							gc.setBackground( Theme.get().getColor( 
									Colors.BACKGROUND ) );
							text.println( null!=strAlert, "Alr>  -" );
						}

						if ( null != strError ) {
							gc.setBackground( Theme.get().getColor( 
									Colors.BACKGROUND_FLASH_ALERT ) );
							text.println( null!=strError, "Err> " + strError );
						} else {
							gc.setBackground( Theme.get().getColor( 
									Colors.BACKGROUND ) );
							text.println( null!=strError, "Err>  -" );
						}
					}
					
				} else {
	
//					gc.setFont( Theme.get().getFont( 25 ) );
					gc.setFont( Theme.ThFont._25_SSCM_V.getFont() );
					gc.drawText( "Tesla", 10, 10 );
//					gc.setFont( Theme.get().getFont( 10 ) );
					gc.setFont( Theme.ThFont._10_SSCM_V.getFont() );
					
					gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
					gc.drawText( strVersion, 10, 50 );
					gc.drawText( "API  " + strAPIVersion, 10, 68 );
	
					gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

//					gc.setFont( Theme.get().getFont( 16 ) );
					gc.setFont( Theme.ThFont._16_SSCM_V.getFont() );
	//					gc.drawText( "Time to Charge:  " + strTime, 40, 112 );
					
					if ( null!=strError ) {
						gc.drawText( "Error: " + strError, 10, 112 );
					} else if ( StringUtils.isNotBlank( strUpdate ) ) {
						gc.drawText( "Software: " + strUpdate, 10, 112 );
					} else {
						gc.drawText( "Charge: " + strChargeStatus, 10, 112 );
					}
					
//					gc.setFont( Theme.get().getFont( 14 ) );
					gc.setFont( Theme.ThFont._16_SSCM_V.getFont() );
					gc.drawText( "Range:", 50, 84 );
	
	
//					gc.setFont( Theme.get().getFont( 12 ) );
					gc.setFont( Theme.ThFont._11_SSCM_V.getFont() );
	//					gc.drawText( "Odometer" +Text.DELIMITER 
	//									+ strOdometer + " mi", 160, 26 );
//					gc.drawText( "Location" + Text.DELIMITER 
//							+ ( bHome ? "Home" : "Away" ), 160, 26 );
					gc.drawText( "Location", 200, 26 );
					gc.drawText( ( bHome ? "Home" : "Away" ), 200, 46 );
	
					gc.setForeground( Theme.get().getColor( Colors.TEXT_BOLD ) );
//					gc.setFont( Theme.get().getFont( 28 ) );
					gc.setFont( Theme.ThFont._25_PR.getFont() );
					gc.drawText( strRange + " mi", 120, 80 );
	
					if ( null!=lTimestampLatest ) {
						final String strElapsed = 
	//							DateFormatting.getSmallTime( strTimestampCharge );
								DateFormatting.getSmallTime( 
										super.iNowPaint - lTimestampLatest );
						
						gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
//						gc.setFont( Theme.get().getFont( 7 ) );
						gc.setFont( Theme.ThFont._7_O_V.getFont() );
						gc.drawText( strElapsed, 265, 10 );
					}
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
	public boolean clickCanvas( final Point point ) {
		this.pointClick = point;

//		if ( !this.bClimateControl ) {
		if ( Mode.CLIMATE.equals( this.mode ) ) {
			
			final Thread thread = new Thread( ()-> {
				
				bRefreshRequest = true;
				final Job.JobSet set = new Job.JobSet( 3 );
	
	//			final Map<String,Object> map = new HashMap<>();
				final TraceMap map = new TraceMap();
				map.put( "reason", "user-click.canvas" );
				map.put( "job-set.first", set.lFirstSeq );
				map.put( "job-set.count", 3 );
	
				Job.add( JobType.TESLA_READ, set, null, 
										DataRequest.CHARGE_STATE.name(), map );
				Job.add( JobType.TESLA_READ, set, null, 
										DataRequest.VEHICLE_STATE.name(), map );
				Job.add( JobType.TESLA_READ, set, null, 
										DataRequest.CLIMATE_STATE.name(), map );
			} );
			thread.start();
			
			return true;
		} else {
			return false;
		}
	}


	private void activateButtonThreaded( final S2Button button ) 
									throws InterruptedException {
		if ( null!=button ) {
			button.setState( ButtonState.WORKING );
		}
		Job job = null;
		
//		final Map<String,Object> map = new HashMap<>();
		final TraceMap map = new TraceMap();
		map.put( "reason", "user-click.button" );
		map.put( "button-name", button.getName() );
		map.put( "button-index", button.getIndex() );

		switch ( button.getIndex() ) {
			case BUTTON_CLIMATE_ON: {
				System.out.println( "Climate ON" );
				job = Job.add( JobType.TESLA_WRITE, null, null, 
									Command.HVAC_START.name(), map );
				Thread.sleep( 1000 );
				Job.add( JobType.TESLA_READ, null, null, 
									DataRequest.CLIMATE_STATE.name(), map );
				
				break;
			}
			
			case BUTTON_CLIMATE_OFF: {
				System.out.println( "Climate OFF" );
				job = Job.add( JobType.TESLA_WRITE, null, null, 
									Command.HVAC_STOP.name(), map );
				Thread.sleep( 1000 );
				Job.add( JobType.TESLA_READ, null, null, 
									DataRequest.CLIMATE_STATE.name(), map );
				break;
			}
			case BUTTON_FLASH_LIGHTS: {
				System.out.println( "Flash lights" );
				job = Job.add( JobType.TESLA_WRITE, null, null, 
									Command.FLASH_LIGHTS.name(), map );
				break;
			}
		}
		if ( null!=button ) {
			button.setJob( job );
		}
	}
	
	
	
	@Override
	protected void activateButton( final S2Button button ) {
		final Thread thread = new Thread( "Button action (TeslaTile)" ) {
			public void run() {
				try {
					activateButtonThreaded( button );
				} catch ( final InterruptedException e ) {
					// just quit
				}
			};
		};
		thread.start();
	}
	
	
}
