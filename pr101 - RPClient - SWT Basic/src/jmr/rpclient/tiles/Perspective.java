package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Rectangle;

public enum Perspective {

	DAILY( 5, 3, true, false ),
	TESLA( 5, 3, true, false ),
	CAMERA( 5, 3, true, false ),
	DESKTOP( 5, 6, false, false ),
	DAILY_ROTATE( 3, 5, true, true ),
	REMOTE( 6, 2, false, false ),
	GPIO( 4, 5, false, false ),
	
	TEST( 5, 3, false, false ),
	;
	
	

	@SuppressWarnings("unused")
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
				case DAILY: this.build_Daily(); break;
				case TESLA: this.build_Tesla( mapOptions ); break;
				case CAMERA: this.build_Camera(); break;
				case TEST: this.build_Test(); break;
				case DESKTOP: this.build_Desktop( mapOptions ); break;
				case DAILY_ROTATE: this.build_DailyRotate(); break;
				case REMOTE: this.build_Remote( mapOptions ); break;
				case GPIO: this.build_GPIO( mapOptions ); break;
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
		
		//TODO add  java.lang.ArrayIndexOutOfBoundsException check
		for ( final TileGeometry tile : list ) {
			final Rectangle r = tile.rect;
			for ( int iTX = 0; iTX<r.width; iTX++ ) {
				for ( int iTY = 0; iTY<r.height; iTY++ ) {
					final int iX = r.x + iTX;
					final int iY = r.y + iTY;
					grid[iX][iY]++; 
				}
			}
		}

		for ( int iRow = 0; iRow<iRows; iRow++ ) {
			for ( int iCol = 0; iCol<iCols; iCol++ ) {
				final int iCount = grid[iCol][iRow];
				if ( 0==iCount ) {
					list.add( new TileGeometry( new TextTile( null ),
							new Rectangle( iCol, iRow, 1, 1 ) ) );
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

		list.add( new TileGeometry( new CameraTile(), 
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
	
	
	private void build_Camera() {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

//		list.add( new TileGeometry( new SystemInfoTile(), 
//						new Rectangle( 1, 1, 1, 1 ) ) );
		
		list.add( new TileGeometry( new AudioSelectionTile(), 
						new Rectangle( 0, 1, 3, 1 ) ) ); 

		list.add( new TileGeometry( new CameraTile(), 
						new Rectangle( 3, 0, 2, 2 ) ) ); 

//		list.add( new TileGeometry( new PerformanceMonitorTile(), 
//						new Rectangle( 2, 1, 1, 1 ) ) );
		
		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
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

	private void build_DailyRotate() {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new NetworkListTile(), 
						new Rectangle( 0, 1, 3, 1 ) ) );

		
		list.add( new TileGeometry( new SystemInfoTile(), 
						new Rectangle( 1, 2, 1, 1 ) ) );

		list.add( new TileGeometry( new PerformanceMonitorTile(), 
						new Rectangle( 2, 2, 1, 1 ) ) );

		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 3, 3, 1 ) ) ); 

		
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 2, 4, 1, 1 ) ) ); 
		list.add( new TileGeometry( new CalibrationTile(), 
						new Rectangle( 0, 4, 1, 1 ) ) ); 
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

	private void build_GPIO( final Map<String, String> mapOptions ) {
		
		list.add( new TileGeometry( new GPIOTile( mapOptions ), 
						new Rectangle( 0, 0, 4, 5 ) ) ); 
	}

	private void build_Tesla( final Map<String, String> mapOptions ) {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 4, 0, 1, 2 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 2, 1, 2, 1 ) ) );
		list.add( new TileGeometry( new TeslaTile( true ), 
						new Rectangle( 3, 0, 1, 1 ) ) );
		
		list.add( new TileGeometry( new JobListingTile( mapOptions ), 
						new Rectangle( 0, 1, 2, 1 ) ) ); 
		
		list.add( new TileGeometry( new WeatherForecastTile(), 
						new Rectangle( 0, 2, 5, 1 ) ) ); 
	}

	private void build_Desktop( final Map<String, String> mapOptions ) {
		list.add( new TileGeometry( new ClockTile(), 
						new Rectangle( 0, 0, 3, 1 ) ) );

		list.add( new TileGeometry( new TeslaTile(), 
						new Rectangle( 2, 1, 1, 2 ) ) );
		
		list.add( new TileGeometry( new TeslaTile( true ), 
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
