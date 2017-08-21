package jmr.rpclient.tiles;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.UI;
import jmr.rpclient.tiles.Theme.Colors;

public class PerformanceMonitorTile extends TileBase {

	private final static int SAMPLE_COUNT = 130;


	private final Long[] arrTimes = new Long[ SAMPLE_COUNT ];
	
	private int iTimeIndex = 0;
	
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		try {
			
			arrTimes[ iTimeIndex ] = System.currentTimeMillis();
			if ( iTimeIndex<SAMPLE_COUNT-1 ) {
				iTimeIndex++;
			} else {
				iTimeIndex = 0;
			}
			
			gc.fillRectangle( image.getBounds() );
			
			int iXTime = iTimeIndex;
			Long lLastTime = null;
			Long lThisTime = null;
			long iTimeElapsed = 0;
			
			long iElapsedMax = 0;
			long iElapsedMin = 10000;
			long lTotal = 0;
			int iSampleCount = 0;
			
			gc.setForeground( UI.COLOR_GREEN );
			
			for ( int iX = 0; iX<SAMPLE_COUNT; iX++ ) {
				lThisTime = arrTimes[ iXTime ];
				
				if ( iXTime % 20 == 0 ) {
					gc.setForeground( UI.COLOR_DARK_BLUE );
					gc.drawLine( 10+iX, 140, 10+iX, 40 );
					gc.setForeground( UI.COLOR_GREEN );
				}
				
				if ( ( null!=lThisTime ) && ( null!=lLastTime ) 
								&& ( lLastTime > 0 ) ) {
					iTimeElapsed = lThisTime - lLastTime - TileCanvas.REFRESH_SLEEP;
					lTotal = lTotal + iTimeElapsed;
					iSampleCount++;
					
					iElapsedMax = Math.max( iElapsedMax, iTimeElapsed );
					iElapsedMin = Math.min( iElapsedMin, iTimeElapsed );
					
					final float fGraphY = ( iTimeElapsed * 150 / 1000 );
					gc.drawLine( 10+iX, 145, 10+iX, 130 - (int)fGraphY );
				}
				
				lLastTime = lThisTime;
	
				if ( iXTime<SAMPLE_COUNT-1 ) {
					iXTime++;
				} else {
					iXTime = 0;
				}
			}

			gc.setFont( Theme.get().getFont( 16 ) );
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

			final float fElapsedAvg;
			if ( iSampleCount>0 ) {
				fElapsedAvg = lTotal / iSampleCount;
				gc.drawText( "Avg: " + (int)fElapsedAvg + " ms", 10, 10 );
			} else {
				gc.drawText( "Avg: (N/A)", 10, 10 );
			}
			
			gc.drawText( "Max: " + iElapsedMax + " ms", 10, 36 );
	//		gc.drawText( "Min: " + iElapsedMin, 10, 40 );
			
	//		drawTextCentered( strText, 10 );
		} catch ( final Throwable t ) {
			// ignore
		}
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
