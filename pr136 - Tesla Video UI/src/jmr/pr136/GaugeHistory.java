package jmr.pr136;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import jmr.pr136.swt.UI;

public class GaugeHistory extends GaugeBase {

	private Font font = null;
	
	private final double dVMax; // max limit for data value
	

	protected GaugeHistory( final Monitor monitor,
							final Rectangle rect,
							final double dVMax ) {
		super( monitor, rect );
		this.dVMax = dVMax;
	}
	
	
	
	@Override
	public void paint(	final long lNow,
					   	final GC gc,
					   	final Image image ) {

		gc.setBackground( UI.getColor( SWT.COLOR_DARK_BLUE ) );
		gc.fillRectangle( rect );
		
		gc.setClipping( rect );
		
		if ( null == font ) {
			final Device device = image.getDevice();

			final Font fontSystem = device.getSystemFont();
		    
			final FontData fd = fontSystem.getFontData()[0];
		    fd.setHeight( 24 );
			font = new Font( device, fd );
		}

		final int iXRange = super.rect.width - 40;
		final int iYRange = super.rect.height;
		final int iXLeft = super.rect.x;
		final int iYTop = super.rect.y;
		final double dYBottom = iYTop + iYRange;

		double dVLast = 0.0;
		int iPrevX = 0;
		int iPrevY = 0;

		final Double dVMinTest = super.monitor.getMinimumValue();
		if ( null != dVMinTest ) {
	
	//		final double dVMin = dVMinTest.doubleValue();
	//		final double dVMax = super.monitor.getMaximumValue();
			final double dVMin = 0;
//			final double dVMax = 400;
			
			
			final double dVRange = dVMax - dVMin;
			
			final long lTMin = super.monitor.getOldestTime();
			final long lTRange = lNow - lTMin;
			if ( 0 == lTRange ) return;
			
			
			gc.setForeground( UI.getColor( SWT.COLOR_GREEN ) );
	
//			final List<Entry<Long, Double>> list = 
//						new ArrayList<>( super.monitor.getData().entrySet() );
//			for ( final Entry<Long, Double> entry : list ) {
//			final List<Long> list; 
//			synchronized ( super.monitor.mapData ) {
//				list = new LinkedList<>( super.monitor.getData().keySet() );
//			}
//			Collections.sort( list );
			final List<Long> list = super.monitor.getDataKeys();
			for ( final Long lTime : list ) {
				final Double dValue = super.monitor.getData().get( lTime );
				if ( null == dValue ) continue;
				
//				final double dTSince = entry.getKey() - lTMin;
//				dVLast = entry.getValue();
				final double dTSince = lTime - lTMin;
				dVLast = dValue;
				final double dVDiff = dVLast - dVMin;
				
				final double dDrawX = dTSince / lTRange * iXRange + iXLeft;
				final double dDrawY = dYBottom - ( dVDiff / dVRange * iYRange );
				
				final int iX = (int)dDrawX;
				final int iY = (int)dDrawY;
				gc.drawLine( iX, iY, iX, iYTop + iYRange );
				
				if ( iPrevX > 0 ) {
					gc.setLineWidth( 4 );
					gc.drawLine( iX, iY, iPrevX, iPrevY );
					gc.setLineWidth( 0 );
				}
				iPrevX = iX;
				iPrevY = iY;
			}
		}

		gc.setForeground( UI.getColor( SWT.COLOR_GREEN ) );

		gc.setFont( font );
		final int iY = iYTop + iYRange - 44;
		gc.drawText( monitor.getTitle(), iXLeft + 10, iY );

		gc.setForeground( UI.getColor( SWT.COLOR_YELLOW ) );
		
		final int iXRight = iXLeft + rect.width;
		if ( dVLast > 0 ) {
			gc.setLineWidth( 4 );
			gc.drawLine( iPrevX, iPrevY, iXRight, iPrevY );
			gc.setLineWidth( 0 );
			gc.drawText( String.format( "%05.2f", dVLast ), 
											iXRight - 100, iY );
		} else {
			gc.drawText( "(no data)", iXRight - 160, iY );
		}
		

		
		gc.setClipping( (Rectangle)null ); 
	}
	
}
