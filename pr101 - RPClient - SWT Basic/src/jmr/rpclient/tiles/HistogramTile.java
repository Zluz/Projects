package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;

public class HistogramTile extends TileBase {

	
	public static class Graph {
		
		private final List<Float> listSamples = new LinkedList<>();
		private final List<Integer> listRecent = new LinkedList<>();
		
		private final int RECENT_COUNT = 10;
		
		private final int iWidth;
		private final int[] iPlot;
		
		private final int iSampleSize;
		
		private Float fMin;
		private Float fMax;
		private Float fWidth;
		
		public Graph( final int iWidth,
					  final int iSampleSize ) {
			this.iWidth = iWidth;
			this.iPlot = new int[ iWidth ];
			this.iSampleSize = iSampleSize;
		}
		
		private void pop() {
			final Float fSample = listSamples.remove( 0 );
			final int iX = getIndexOfSample( fSample );
			iPlot[ iX ]--;
		}
		
		private void recalibrate() {
			for ( int i=0; i<iWidth; i++ ) {
				iPlot[i] = 0;
			}
			listRecent.clear();
			
			fWidth = fMax - fMin;
			
			int i=0;
			for ( final Float fSample : listSamples ) {
				final int iX = getIndexOfSample( fSample );
				iPlot[ iX ]++;
				if ( i<RECENT_COUNT ) {
					listRecent.add( iX );
					i++;
				}
			}
		}
		
		private int getIndexOfSample( final float fSample ) {
			final float fX = ( fSample - fMin ) / fWidth;
			final int iIndex = (int)( fX * (iWidth-1) );
			return iIndex;
		}
		
		public synchronized void add( final float fSample ) {
			while ( listSamples.size() >= iSampleSize ) {
				pop();
			}
			while ( listRecent.size() >= RECENT_COUNT ) {
				listRecent.remove( 0 );
			}
			
			if ( null==fMin || null==fMax ) {
				fMin = fSample - 0.01f;
				fMax = fSample + 0.01f;
				recalibrate();
				
			} else if ( fSample < fMin ) {
				fMin = fSample;
				recalibrate();
			} else if ( fSample > fMax ) {
				fMax = fSample;
				recalibrate();
			}
			
			final int iX = getIndexOfSample( fSample );
			listSamples.add( fSample );
			listRecent.add( iX );
			iPlot[ iX ]++;
		}
		
		public synchronized int[] getPlot() {
			final int[] iPlotCopy = new int[ iWidth ];
			System.arraycopy( this.iPlot, 0, iPlotCopy, 0, this.iPlot.length );
			return iPlotCopy;
		}
		
		public synchronized Set<Integer> getRecent() {
			final Set<Integer> set = new HashSet<>();
			set.addAll( listRecent );
			return set;
		}
	}

	
	private final static Map<String,Graph> mapGraphs = new HashMap<>();
//	private final static Map<String,HistogramTile> mapTiles = new HashMap<>();
	
	
	public static Graph getGraph( final String strName ) {
		return mapGraphs.get( strName );
	}
	
	
	private final String strName;
	private Graph graph;
	
//	private Integer iSampleSize;
//	private Integer iPlotHeight;
	
	
	
	public HistogramTile( final String strName ) {
		this.strName = strName;
	}
	
	
	private Integer iLastMaxRecent;
	private Integer iLastMinRecent;
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		try {

			gc.setFont( Theme.get().getFont( 10 ) );
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
			gc.drawText( this.strName, 2, 2 );

			final int iWidth = image.getBounds().width;
			final int iHeight = image.getBounds().height - 14;

			if ( null != iLastMaxRecent ) {
				gc.setBackground( UI.COLOR_BLUE );
				gc.fillRectangle( iLastMinRecent, iHeight, 
						  		  iLastMaxRecent - iLastMinRecent, 8 );
			}

			if ( null == this.graph ) {
				this.graph = new Graph( iWidth, 500 );
				mapGraphs.put( this.strName, this.graph );
			}
			
			final int[] iPlot = this.graph.getPlot();
			final Set<Integer> set = this.graph.getRecent();

			iLastMinRecent = null;
			iLastMaxRecent = null;
			
			for ( int i = 0; i<iWidth; i++ ) {
				final int iY2;
				if ( set.contains( i ) ) {
					gc.setForeground( UI.COLOR_WHITE );
					iY2 = iHeight + 8;
					if ( null==iLastMaxRecent || null==iLastMinRecent ) {
						iLastMaxRecent = i;
						iLastMinRecent = i;
					} else if ( iLastMaxRecent.intValue() < i ) {
						iLastMaxRecent = i;
					} else if ( iLastMinRecent.intValue() > i ) {
						iLastMinRecent = i;
					}
				} else {
					iY2 = iHeight;
					gc.setForeground( UI.COLOR_GREEN );
				}
				final float fY = (float)( iPlot[i] ^ ( 1/4 ) ) * 4;
				gc.drawLine( i, iY2, i, iHeight - (int)fY );
			}
			
			gc.setFont( Theme.get().getFont( 7 ) );
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

			final String strMin = String.format( "%.2f", graph.fMin );
			gc.drawText( strMin, 2, iHeight + 2, true );
			final String strMax = String.format( "%.2f", graph.fMax );
			gc.drawText( strMax, iWidth - 24, iHeight + 2, true );

			
////			arrTimes[ iTimeIndex ] = System.currentTimeMillis();
//			arrTimes[ iTimeIndex ] = System.nanoTime() / 100000;
//			arrTemp[ iTimeIndex ] = cpu.getTemperature();
//			if ( iTimeIndex<SAMPLE_COUNT-1 ) {
//				iTimeIndex++;
//			} else {
//				iTimeIndex = 0;
//			}
//			
//			final boolean bBar = image.getImageData().width < 100;
//			
//			gc.fillRectangle( image.getBounds() );
//			
//			int iXTime = iTimeIndex;
//			Long lLastTime = null;
//			Long lThisTime = null;
//			int iTimeElapsed = 0;
//			
//			long iElapsedMax = 0;
//			long iElapsedMin = 10000;
//			long lTotal = 0;
//			int iSampleCount = 0;
//			
//			gc.setForeground( UI.COLOR_GREEN );
//			
//			for ( int iX = 0; iX<SAMPLE_COUNT; iX++ ) {
//				lThisTime = arrTimes[ iXTime ];
//				
//				if ( !bBar ) {
//					if ( iXTime % 20 == 0 ) {
//						gc.setForeground( UI.COLOR_DARK_BLUE );
//						gc.drawLine( 10+iX, 140, 10+iX, 40 );
//						gc.setForeground( UI.COLOR_GREEN );
//					}
//				}
//				
//				if ( ( null!=lThisTime ) && ( null!=lLastTime ) 
//								&& ( lLastTime > 0 ) ) {
//					iTimeElapsed = (int)( lThisTime - lLastTime 
//												- TileCanvas.REFRESH_SLEEP );
//					lTotal = lTotal + iTimeElapsed;
//					iSampleCount++;
//					
//					iElapsedMax = Math.max( iElapsedMax, iTimeElapsed );
//					iElapsedMin = Math.min( iElapsedMin, iTimeElapsed );
//
//					final int iY;
//					final int iXframe;
//					gc.setForeground( UI.COLOR_GREEN );
//					if ( bBar ) {
//						iXframe = iTimeElapsed * 2/100;
//						iY = 47 + iX;
//						gc.drawLine( 0, iY, iXframe, iY );
//					} else {
//						final float fGraphY = ( iTimeElapsed * 150 / 10000 );
//						gc.drawLine( 10+iX, 145, 10+iX, 130 - (int)fGraphY );
//						iY = 0;
//						iXframe = 0;
//					}
//					
//					final Double dTemp = arrTemp[ iXTime ];
//					if ( bBar && null!=dTemp ) {
//						
//						final double dX = ( dTemp.doubleValue() - 10.0 ) / 2; 
//						
//						gc.setForeground( UI.COLOR_RED );
//						
////						if ( dTemp.doubleValue() > TEMP_ALERT_THRESHOLD ) {
////							gc.drawLine( iXframe, iY, (int)dX, iY );
////						} else {
//							gc.drawPoint( (int)dX, iY );
////						}
//					}
//				}
//				
//				lLastTime = lThisTime;
//	
//				if ( iXTime<SAMPLE_COUNT-1 ) {
//					iXTime++;
//				} else {
//					iXTime = 0;
//				}
//			}
//			
//			final int iAvg;
//			if ( iSampleCount>0 ) {
//				iAvg = (int) ( lTotal / iSampleCount ) /10;
//			} else {
//				iAvg = 0;
//			}
//
//			if ( !bBar ) {
//				
//				gc.setFont( Theme.get().getFont( 16 ) );
//				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
//	
////				final float fElapsedAvg;
////				if ( iSampleCount>0 ) {
////					fElapsedAvg = lTotal / iSampleCount;
////					gc.drawText( "Avg: " + (int)fElapsedAvg/10 + " ms", 10, 10, true );
////				} else {
////					gc.drawText( "Avg: (N/A)", 10, 10, true );
////				}
//				gc.drawText( "Avg: " + iAvg + " ms", 10, 10, true );
//
////				gc.drawText( "Max: " + iElapsedMax/2 + " ms", 10, 36, true );
//		//		gc.drawText( "Min: " + iElapsedMin, 10, 40 );
//	
//				final boolean bDebug = gc.getDevice().getDeviceData().debug;
//				if ( bDebug ) {
//					gc.setFont( Theme.get().getFont( 8 ) );
//	//				final boolean bTracking = gc.getDevice().getDeviceData().tracking;
//					final int iLength = gc.getDevice().getDeviceData().objects.length;
//					final String strResCount = "" + iLength;
//					gc.drawText( "SWT #: " + strResCount, 10, 62 );
//				} else {
//					gc.setFont( Theme.get().getFont( 8 ) );
//					gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
//					
//	//				gc.drawText( "No SWT Debug", 10, 62, true );
//	//				gc.drawText( "[X]  SWT Debug", 10, 80, true );
//				}
//			} else {
//				gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
//				gc.drawLine( 0, 179, 50, 179 );
//				
//
//				gc.setFont( Theme.get().getFont( 10 ) );
//				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
//				gc.drawText( "" + iAvg + " ms", 2, 30, true );
//			}

	//		drawTextCentered( strText, 10 );
		} catch ( final Throwable t ) {
			t.printStackTrace();
		}
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
