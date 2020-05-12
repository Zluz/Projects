package jmr.rpclient.tiles;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;
import jmr.util.hardware.rpi.CPUMonitor;

public class PerformanceMonitorTile extends TileBase {

//	private static final double TEMP_ALERT_THRESHOLD = 65.0;


	private final static int SAMPLE_COUNT = 130;


	private final Long[] arrTimes = new Long[ SAMPLE_COUNT ];
	private final Double[] arrTemp = new Double[ SAMPLE_COUNT ];
	private final CPUMonitor cpu = CPUMonitor.get();
	
	private int iTimeIndex = 0;
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		try {
			
//			arrTimes[ iTimeIndex ] = System.currentTimeMillis();
			arrTimes[ iTimeIndex ] = System.nanoTime() / 100000;
			arrTemp[ iTimeIndex ] = cpu.getTemperature();
			if ( iTimeIndex<SAMPLE_COUNT-1 ) {
				iTimeIndex++;
			} else {
				iTimeIndex = 0;
			}
			
			final boolean bBar = image.getImageData().width < 100;
			
			if ( bBar && cpu.isHeatWarning() ) {
				gc.setBackground( Theme.get().getColor( 
											Colors.BACKGROUND_FLASH_ALERT ) );
			}
			gc.fillRectangle( image.getBounds() );
			
			int iXTime = iTimeIndex;
			Long lLastTime = null;
			Long lThisTime = null;
			int iTimeElapsed = 0;
			
			long iElapsedMax = 0;
			long iElapsedMin = 10000;
			long lTotal = 0;
			int iSampleCount = 0;
			
			gc.setForeground( UI.COLOR_GREEN );
			
			for ( int iX = 0; iX<SAMPLE_COUNT; iX++ ) {
				lThisTime = arrTimes[ iXTime ];
				
				if ( !bBar ) {
					if ( iXTime % 20 == 0 ) {
						gc.setForeground( UI.COLOR_DARK_BLUE );
						gc.drawLine( 10+iX, 140, 10+iX, 40 );
						gc.setForeground( UI.COLOR_GREEN );
					}
				}
				
				if ( ( null!=lThisTime ) && ( null!=lLastTime ) 
								&& ( lLastTime > 0 ) ) {
					iTimeElapsed = (int)( lThisTime - lLastTime 
												- TileCanvas.REFRESH_SLEEP );
					lTotal = lTotal + iTimeElapsed;
					iSampleCount++;
					
					iElapsedMax = Math.max( iElapsedMax, iTimeElapsed );
					iElapsedMin = Math.min( iElapsedMin, iTimeElapsed );

					final int iY;
					final int iXframe;
					gc.setForeground( UI.COLOR_GREEN );
					if ( bBar ) {
						iXframe = iTimeElapsed * 2/100;
						iY = 47 + iX;
						gc.drawLine( 0, iY, iXframe, iY );
					} else {
						final float fGraphY = ( iTimeElapsed * 150 / 10000 );
						gc.drawLine( 10+iX, 145, 10+iX, 130 - (int)fGraphY );
						iY = 0;
						iXframe = 0;
					}
					
					final Double dTemp = arrTemp[ iXTime ];
					if ( bBar && null!=dTemp ) {
						
						final double dX = ( dTemp.doubleValue() - 10.0 ) / 2; 
						
						gc.setForeground( UI.COLOR_RED );
						
//						if ( dTemp.doubleValue() > TEMP_ALERT_THRESHOLD ) {
//							gc.drawLine( iXframe, iY, (int)dX, iY );
//						} else {
							gc.drawPoint( (int)dX, iY );
//						}
					}
				}
				
				lLastTime = lThisTime;
	
				if ( iXTime<SAMPLE_COUNT-1 ) {
					iXTime++;
				} else {
					iXTime = 0;
				}
			}
			
			final int iAvg;
			if ( iSampleCount>0 ) {
				iAvg = (int) ( lTotal / iSampleCount ) /10;
			} else {
				iAvg = 0;
			}

			if ( !bBar ) {
				
				gc.setFont( Theme.get().getFont( 16 ) );
				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
	
//				final float fElapsedAvg;
//				if ( iSampleCount>0 ) {
//					fElapsedAvg = lTotal / iSampleCount;
//					gc.drawText( "Avg: " + (int)fElapsedAvg/10 + " ms", 10, 10, true );
//				} else {
//					gc.drawText( "Avg: (N/A)", 10, 10, true );
//				}
				gc.drawText( "Avg: " + iAvg + " ms", 10, 10, true );

//				gc.drawText( "Max: " + iElapsedMax/2 + " ms", 10, 36, true );
		//		gc.drawText( "Min: " + iElapsedMin, 10, 40 );
	
				final boolean bDebug = gc.getDevice().getDeviceData().debug;
				if ( bDebug ) {
					gc.setFont( Theme.get().getFont( 8 ) );
	//				final boolean bTracking = gc.getDevice().getDeviceData().tracking;
					final int iLength = gc.getDevice().getDeviceData().objects.length;
					final String strResCount = "" + iLength;
					gc.drawText( "SWT #: " + strResCount, 10, 62 );
				} else {
					gc.setFont( Theme.get().getFont( 8 ) );
					gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
					
	//				gc.drawText( "No SWT Debug", 10, 62, true );
	//				gc.drawText( "[X]  SWT Debug", 10, 80, true );
				}
			} else {
				gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
				gc.drawLine( 0, 179, 50, 179 );
				

				gc.setFont( Theme.get().getFont( 10 ) );
				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
				gc.drawText( "" + iAvg + " ms", 2, 30, true );
			}

	//		drawTextCentered( strText, 10 );
		} catch ( final Throwable t ) {
			// ignore
		}
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
