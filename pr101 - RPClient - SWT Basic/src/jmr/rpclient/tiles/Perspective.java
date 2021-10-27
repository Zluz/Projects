package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.tiles.TeslaTile.Mode;

public enum Perspective {

	TOP_PAGE( 5, 3, true, false ),
	DAILY( 5, 3, true, false ),
	TESLA( 5, 3, true, false ),
	CAMERA_LOCAL( 5, 3, true, false ),
	CAMERA_REMOTE( 5, 3, true, false ),
	OFFICE_SILL( 5, 3, true, false ),
	DESKTOP( 5, 6, false, false ),
	DAILY_ROTATE( 3, 5, true, true ),
	REMOTE( 6, 2, false, false ),
	GPIO( 4, 5, false, false ),
	AUTO_HAT( 5, 3, false, false ),
	PROC_IMAGE( 8, 3, false, false ),
	WORKER_IMAGE( 7, 3, false, false ),
	SURVEILLANCE_STAMP( 5, 2, false, false ),
	
	TEST( 5, 3, false, false ),
	;
	
	

	private static final Logger 
			LOGGER = Logger.getLogger( Perspective.class.getName() );

	
	final int iCols;
	final int iRows;
	final boolean bFullscreen;
	final boolean bRotate;
	

	private final List<TileGeometry> 
							list = new LinkedList<TileGeometry>();
	
	
	Perspective(	final int iCols,
					final int iRows,
					final boolean bFullscreen,
					final boolean bRotate ) {
		this.iCols = iCols;
		this.iRows = iRows;
		this.bFullscreen = bFullscreen;
		this.bRotate = bRotate;
	}
	
	
	public int getColCount() {
		return this.iCols;
	}
	
	public int getRowCount() {
		return this.iRows;
	}
	
	public boolean isRotated() {
		return this.bRotate;
	}
	
	public boolean isFullscreen() {
		return this.bFullscreen;
	}


	
	
	public List<TileGeometry> getTiles( final Map<String, String> mapOptions ) {
		if ( list.isEmpty() ) {
			switch ( this ) {
				case TOP_PAGE: this.build_TopPage( mapOptions ); break;
				case DAILY: this.build_Daily(); break;
				case TESLA: this.build_Tesla( mapOptions ); break;
				case CAMERA_LOCAL: this.build_CameraLocal(); break;
				case CAMERA_REMOTE: this.build_CameraRemote(); break;
				case OFFICE_SILL: this.build_OfficeSill( mapOptions ); break;
				case TEST: this.build_Test(); break;
				case DAILY_ROTATE: this.build_DailyRotate( mapOptions ); break;
				case REMOTE: this.build_Remote( mapOptions ); break;
				case SURVEILLANCE_STAMP: this.build_SurvStamp( mapOptions ); break;
				case GPIO: this.build_GPIO( mapOptions ); break;
				case AUTO_HAT: this.build_AutoHAT( mapOptions ); break;
				case PROC_IMAGE: this.build_ProcImage( mapOptions ); break;
				case WORKER_IMAGE: this.build_ImageJobWorker( mapOptions ); break;
				case DESKTOP: this.build_Desktop( mapOptions ); break;
			}
			validate();
		}
		
		return list;
	}
	
	
	private void validate() {
		final Integer[][] grid = new Integer[iCols][iRows];
		
		for ( int iRow = 0; iRow<iRows; iRow++ ) {
			for ( int iCol = 0; iCol<iCols; iCol++ ) {
				grid[iCol][iRow] = 0;
			}
		}
		
		for ( final TileGeometry tile : list ) {
			final Rectangle r = tile.rect;
			for ( int iTX = 0; iTX<r.width; iTX++ ) {
				for ( int iTY = 0; iTY<r.height; iTY++ ) {

					final int iX = r.x + iTX;
					final int iY = r.y + iTY;

					if ( iY >= iRows ) {
						LOGGER.warning( "Tile hanging over Y bounds: " 
								+ tile.tile.toString() );
					} else if ( iX >= iCols ) {
						LOGGER.warning( "Tile hanging over X bounds: " 
								+ tile.tile.toString() );
					} else {
						grid[iX][iY]++;
					}
				}
			}
		}

		for ( int iRow = 0; iRow<iRows; iRow++ ) {
			for ( int iCol = 0; iCol<iCols; iCol++ ) {
				final int iCount = grid[iCol][iRow];
				if ( 0==iCount ) {
					list.add( new TileGeometry( new TextTile( null ),
							new Rectangle( iCol, iRow, 1, 1 ) ) );
				} else if ( iCount>1 ) {
					LOGGER.warning( "Overlapping tiles "
							+ "(at " + iRow + ", " + iCol + ")" );
				}
			}
		}
	}


	public static Perspective getPerspectiveFor( final String strName ) {
		if ( null==strName ) return Perspective.DAILY;
		
		final String strNormal = strName.trim().toUpperCase();
		for ( final Perspective perspective : Perspective.values() ) {
			if ( strNormal.equals( perspective.name() ) ) {
				return perspective;
			}
		}
		return Perspective.DAILY;
	}
	
	
	
	
	

	@SuppressWarnings("unused")
	private void build_Calibration() {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 1, 0, 3, 3 ) ) );

		list.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 0, 1, 1, 1 ) ) );
		
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 0, 1, 1 ) ) ); 
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 1, 1, 1 ) ) ); 
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 2, 1, 1 ) ) ); 
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 0, 0, 1, 1 ) ) ); 
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 0, 2, 1, 1 ) ) ); 
	}
	

	private void build_Test() {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 2, 1, 1, 1 ) ) );

		list.add( new TileGeometry( 
						new CameraTile( CameraTile.CameraLocation.LOCAL ), 
						new Rectangle( 3, 0, 2, 2 ) ) ); 

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 0, 1, 2, 1 ) ) ); 

		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 0, 1, 1 ) ) ); 

		list.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 4, 2, 1, 1 ) ) );
		
		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
	}
	

	private void build_CameraLocal() {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

//		list.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 1, 1, 1, 1 ) ) );
		
		list.add( new TileGeometry( new AudioSelectionTile(), 
						new Rectangle( 0, 1, 3, 1 ) ) ); 

		list.add( new TileGeometry( 
						new CameraTile( CameraTile.CameraLocation.LOCAL ), 
						new Rectangle( 3, 0, 2, 2 ) ) ); 

//		list.add( new TileGeometry( new PerformanceMonitorTile(), 
//						new Rectangle( 2, 1, 1, 1 ) ) );
		
		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
	}

	private void build_CameraRemote() {
		list.add( new TileGeometry( 
						new CameraTile( CameraTile.CameraLocation.ALL_CAPTURES ), 
						new Rectangle( 0, 0, 5, 3 ) ) ); 
	}

	


	private void build_OfficeSill( final Map<String, String> mapOptions ) {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 3, 0, 2, 1 ) ) );
		
		list.add( new TileGeometry( new JobListingTile( mapOptions ), 
						new Rectangle( 3, 1, 2, 1 ) ) ); 

		list.add( new TileGeometry( new AudioSelectionTile(), 
						new Rectangle( 0, 1, 3, 1 ) ) ); 

		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
	}

	

	private void build_TopPage( final Map<String, String> mapOptions ) {
//		list.add( new TileGeometry( new ClockTile(), 
//						new Rectangle( 0, 0, 3, 1 ) ) );

//		list.add( new TileGeometry( new PerformanceMonitorTile(), 
//						new Rectangle( 3, 0, 1, 1 ) ) );

		list.add( new TileGeometry( new PerspectiveSwitcherTile(), 
						new Rectangle( 4, 0, 1, 3 ) ) );
		
//		list.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 0, 1, 1, 1 ) ) );

//		list.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 4, 0, 1, 1 ) ) ); 
//		list.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 4, 1, 1, 1 ) ) ); 

//		list.add( new TileGeometry( new WeatherForecastTile(), 
//						new Rectangle( 0, 2, 4, 1 ) ) );
		
		list.add( new TileGeometry( new JobListingTile( mapOptions ), 
						new Rectangle( 0, 0, 4, 1 ) ) ); 

		list.add( new TileGeometry( new JobDetailTile( mapOptions ), 
				new Rectangle( 0, 1, 3, 2 ) ) ); 

		list.add( new TileGeometry( new EventListingTile(), 
				new Rectangle( 3, 1, 1, 2 ) ) ); 
	}

	private void build_Daily() {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 2, 1, 1, 1 ) ) );

		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 0, 1, 1 ) ) ); 
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 4, 1, 1, 1 ) ) ); 

		list.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 3, 1, 1, 1 ) ) );

		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
	}
	
	private void build_DailyRotate( final Map<String, String> mapOptions ) {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

//		list.add( new TileGeometry( new NetworkListTile(), 
//						new Rectangle( 0, 1, 3, 1 ) ) );

		list.add( new TileGeometry( new JobListingTile( mapOptions ), 
						new Rectangle( 0, 1, 3, 1 ) ) ); 

//		list.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 1, 2, 1, 1 ) ) );
//		list.add( new TileGeometry( new PerformanceMonitorTile(), 
//						new Rectangle( 2, 2, 1, 1 ) ) );
		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 0, 2, 2, 1 ) ) );
		list.add( new TileGeometry( new TeslaTile( Mode.CLIMATE ), 
						new Rectangle( 2, 2, 1, 1 ) ) );


		list.add( new TileGeometry( new AudioSelectionTile(), 
						new Rectangle( 0, 3, 3, 1 ) ) ); 

		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 4, 3, 1 ) ) ); 
		
//		list.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 2, 4, 1, 1 ) ) ); 
//		list.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 0, 4, 1, 1 ) ) ); 
	}
	

	private void build_Remote( final Map<String, String> mapOptions ) {
		
		list.add( new TileGeometry( new JobListingTile( mapOptions ), 
						new Rectangle( 0, 0, 6, 1 ) ) ); 

		list.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 0, 1, 1, 1 ) ) );

		list.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 1, 1, 1, 1 ) ) );

		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 5, 1, 1, 1 ) ) ); 
	}


	private void build_SurvStamp( final Map<String, String> mapOptions ) {
		
		list.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 0, 1, 1, 1 ) ) );

		list.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 1, 1, 1, 1 ) ) );

		list.add( new TileGeometry( new TeslaTile( Mode.STAMP ), 
						new Rectangle( 0, 0, 2, 1 ) ) );

		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 2, 0, 3, 2 ) ) ); 

	}

	
	private void build_GPIO( final Map<String, String> mapOptions ) {
		
		list.add( new TileGeometry( new IO_GPIOTile( mapOptions ), 
						new Rectangle( 0, 0, 4, 5 ) ) ); 
	}

	private void build_AutoHAT( final Map<String, String> mapOptions ) {
		
		list.add( new TileGeometry( new IO_AutomationHatTile( 
					IO_AutomationHatTile.TileType.DISPLAY, mapOptions ), 
						new Rectangle( 0, 0, 3, 3 ) ) ); 

		list.add( new TileGeometry( new IO_AutomationHatTile(  
					IO_AutomationHatTile.TileType.CONTROL, mapOptions ), 
						new Rectangle( 3, 1, 2, 2 ) ) ); 

		list.add( new TileGeometry( new HistogramTile( "IN_A_1" ), 
						new Rectangle( 3, 0, 2, 1 ) ) );
		
		list.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 3, 2, 1, 1 ) ) );
		list.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 4, 2, 1, 1 ) ) );
//		list.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 4, 2, 1, 1 ) ) ); 
	}

	private void build_ProcImage( final Map<String, String> mapOptions ) {
		
		list.add( new TileGeometry( new HistogramTile( "FILE_INTERVAL_1", true ), 
						new Rectangle( 0, 0, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "CHANGE_INTERVAL_1", true ), 
						new Rectangle( 2, 0, 1, 1 ) ) );
		list.add( new TileGeometry( new ProcImageTile( "1", mapOptions ), 
						new Rectangle( 3, 0, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "IMAGE_CHANGE_VALUE_1" ), 
						new Rectangle( 5, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new HistogramTile( "FILE_INTERVAL_2", true ), 
						new Rectangle( 0, 1, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "CHANGE_INTERVAL_2", true ), 
						new Rectangle( 2, 1, 1, 1 ) ) );
		list.add( new TileGeometry( new ProcImageTile( "2", mapOptions ), 
						new Rectangle( 3, 1, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "IMAGE_CHANGE_VALUE_2" ), 
						new Rectangle( 5, 1, 3, 1 ) ) );

		list.add( new TileGeometry( new HistogramTile( "FILE_INTERVAL_3", true ), 
						new Rectangle( 0, 2, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "CHANGE_INTERVAL_3", true ), 
						new Rectangle( 2, 2, 1, 1 ) ) );
		list.add( new TileGeometry( new ProcImageTile( "3", mapOptions ), 
						new Rectangle( 3, 2, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "IMAGE_CHANGE_VALUE_3" ), 
						new Rectangle( 5, 2, 3, 1 ) ) );

//		list.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 2, 2, 1, 1 ) ) );
//		list.add( new TileGeometry( new PerformanceMonitorTile(), 
//						new Rectangle( 3, 2, 1, 1 ) ) );
//		list.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 3, 2, 1, 1 ) ) ); 
	}
	

	private void build_ImageJobWorker( final Map<String, String> mapOptions ) {
		
		list.add( new TileGeometry( new HistogramTile( "FILE_INTERVAL_1", true ), 
						new Rectangle( 0, 0, 2, 1 ) ) );
//		list.add( new TileGeometry( new HistogramTile( "CHANGE_INTERVAL_1", true ), 
//						new Rectangle( 2, 0, 1, 1 ) ) );
		list.add( new TileGeometry( new ImageJobWorkerTile( "1", mapOptions ), 
						new Rectangle( 2, 0, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "IMAGE_CHANGE_VALUE_1" ), 
						new Rectangle( 4, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new HistogramTile( "FILE_INTERVAL_2", true ), 
						new Rectangle( 0, 1, 2, 1 ) ) );
//		list.add( new TileGeometry( new HistogramTile( "CHANGE_INTERVAL_2", true ), 
//						new Rectangle( 2, 1, 1, 1 ) ) );
		list.add( new TileGeometry( new ImageJobWorkerTile( "2", mapOptions ), 
						new Rectangle( 2, 1, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "IMAGE_CHANGE_VALUE_2" ), 
						new Rectangle( 4, 1, 3, 1 ) ) );
//
		list.add( new TileGeometry( new HistogramTile( "FILE_INTERVAL_3", true ), 
						new Rectangle( 0, 2, 2, 1 ) ) );
//		list.add( new TileGeometry( new HistogramTile( "CHANGE_INTERVAL_3", true ), 
//						new Rectangle( 2, 2, 1, 1 ) ) );
		list.add( new TileGeometry( new ImageJobWorkerTile( "3", mapOptions ), 
						new Rectangle( 2, 2, 2, 1 ) ) );
		list.add( new TileGeometry( new HistogramTile( "IMAGE_CHANGE_VALUE_3" ), 
						new Rectangle( 4, 2, 3, 1 ) ) );

//		list.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 2, 2, 1, 1 ) ) );
//		list.add( new TileGeometry( new PerformanceMonitorTile(), 
//						new Rectangle( 3, 2, 1, 1 ) ) );
//		list.add( new TileGeometry( new CalibrationTile(), 
//						new Rectangle( 3, 2, 1, 1 ) ) ); 
	}


	private void build_Tesla( final Map<String, String> mapOptions ) {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 2, 1 ) ) );
		list.add( new TileGeometry( new EventListingTile(), 
						new Rectangle( 2, 0, 1, 1 ) ) );
//		list.add( new TileGeometry( new PerspectiveSwitcherTile(), 
//						new Rectangle( 2, 0, 1, 1 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 4, 0, 1, 2 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 2, 1, 2, 1 ) ) );
		list.add( new TileGeometry( new TeslaTile( Mode.CLIMATE ), 
						new Rectangle( 3, 0, 1, 1 ) ) );
		
		list.add( new TileGeometry( new JobListingTile( mapOptions ), 
						new Rectangle( 0, 1, 2, 1 ) ) ); 
		
		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 2, 1 ) ) );
		list.add( new TileGeometry( new TestAlertsTile(), 
						new Rectangle( 2, 2, 1, 1 ) ) );
		list.add( new TileGeometry( new NestTile(), 
						new Rectangle( 3, 2, 2, 1 ) ) ); 
	}

	private void build_Desktop( final Map<String, String> mapOptions ) {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 2, 1, 1, 2 ) ) );
		
		list.add( new TileGeometry( new TeslaTile( Mode.CLIMATE ), 
						new Rectangle( 2, 3, 1, 1 ) ) );
		
//		list.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 2, 3, 1, 1 ) ) );

		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 0, 3, 1, 1 ) ) ); 

		list.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 1, 3, 1, 1 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 0, 1, 2, 1 ) ) ); 
		
//		list.add( new TileGeometry( new NetworkListTile(), 
		list.add( new TileGeometry( new JobListingTile( mapOptions ), 
						new Rectangle( 0, 2, 2, 1 ) ) ); 

		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 5, 5, 1 ) ) ); 
		
		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 4, 3, 1 ) ) ); 

		list.add( new TileGeometry( new SessionListTile( false ), 
						new Rectangle( 3, 0, 2, 5 ) ) ); 

	}

	
	
	
	
	
}
