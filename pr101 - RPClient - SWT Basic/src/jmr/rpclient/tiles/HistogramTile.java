package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jmr.rpclient.swt.GCTextUtils;
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
		
		private Integer iThresholdMax;
		private Integer iThresholdMin;
		private Double dThresholdMax;
		private Double dThresholdMin;
		
		private Float fMark;
		private long lTimeLastSample;
		private long lElapsedLastSample;
		
		public boolean bEnabled;
		
		public Graph( final int iWidth,
					  final int iSampleSize ) {
			this.iWidth = iWidth;
			this.iPlot = new int[ iWidth ];
			this.iSampleSize = iSampleSize;
			bEnabled = false;
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
		
		public Float getValueForIndex( final int iX ) {
			if ( null == fMin ) return null;
			if ( null == fWidth ) return null;
			
			final float fValue = 
					( fWidth * ( (float)iX / ( iWidth - 1 ) ) ) + fMin;
			return fValue;
		}
		
		public synchronized void add( final float fSample ) {
			
			final long lNow = System.currentTimeMillis();
			if ( this.lTimeLastSample > 0 ) {
				this.lElapsedLastSample = lNow - this.lTimeLastSample;
			}
			this.lTimeLastSample = lNow;
			
			this.bEnabled = true;
			while ( listSamples.size() >= iSampleSize ) {
				pop();
			}
			while ( listRecent.size() >= RECENT_COUNT ) {
				listRecent.remove( 0 );
			}
			
			if ( null==fMin || null==fMax ) {
				fMin = fSample - 0.001f;
				fMax = fSample + 0.001f;
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
		
		public Float getLatestSample() {
			if ( listSamples.isEmpty() ) return null;
			final Float fValue = listSamples.get( listSamples.size() - 1 );
			return fValue;
		}
		
		public long getLatestElapsed() {
			return this.lElapsedLastSample;
		}

		private Integer calcSafeValue( final Double dValue ) {
			if ( null!=dValue ) {
				final int iValue = this.getIndexOfSample( dValue.floatValue() );
				if ( iValue < 0 ) {
					return null;
				} else if ( iValue >= iWidth ) {
					return null;
				} else {
					return iValue;
				}
			} else {
				return null;
			}
		}
		
		public void setThresholdMax( final Double fThresholdMax ) {
			this.dThresholdMax = fThresholdMax;
			this.iThresholdMax = this.calcSafeValue( fThresholdMax );
		}
		
		public void setThresholdMin( final Double fThresholdMin ) {
			this.dThresholdMin = fThresholdMin;
			this.iThresholdMin = this.calcSafeValue( fThresholdMin );
		}
		
		public Integer getThresholdIndexMax() {
			return this.iThresholdMax;
		}

		public Integer getThresholdIndexMin() {
			return this.iThresholdMin;
		}
		
		public Double getThresholdValueMax() {
			return this.dThresholdMax;
		}

		public Double getThresholdValueMin() {
			return this.dThresholdMin;
		}
		
		public void setEnabled( final boolean bEnabled ) {
			this.bEnabled = bEnabled;
		}
		
		public void setMark( final Float fValue ) {
			this.fMark = fValue;
		}

		public Float getMark() {
			return this.fMark;
		}
	}

	
	private final static Map<String,Graph> mapGraphs = new HashMap<>();
//	private final static Map<String,HistogramTile> mapTiles = new HashMap<>();
	
	
	public static Graph getGraph( final String strName ) {
		return mapGraphs.get( strName );
	}
	
	
	private final String strName;
	private final boolean bIsElapsedGraph;
	private Graph graph;
	
	
	
//	private Integer iSampleSize;
//	private Integer iPlotHeight;
	
	
	
	public HistogramTile( final String strName,
						  final boolean bIsElapsedGraph ) {
		this.strName = strName;
		this.bIsElapsedGraph = bIsElapsedGraph;

//		if ( bIsElapsedGraph && "CHANGE_INTERVAL_3".equals( strName ) ) {
//			System.out.println( "Histogram instantiated: CHANGE_INTERVAL_3" );
//			System.out.println( ExceptionUtils.getStackTrace( new Throwable() ) );
//		}
	}

	public HistogramTile( final String strName ) {
		this( strName, false );
	}

	
	private Integer iLastMaxRecent;
	private Integer iLastMinRecent;
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		final long lNow = System.currentTimeMillis();
		try {

			final int iWidth = image.getBounds().width;
			final int iHeight = image.getBounds().height - 14;
			final int iWidthHalf = iWidth / 2;

			if ( null != iLastMaxRecent ) {
				gc.setBackground( UI.COLOR_BLUE );
				gc.fillRectangle( iLastMinRecent, iHeight, 
						  		  iLastMaxRecent - iLastMinRecent, 8 );
				gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
			}

			gc.setForeground( UI.COLOR_DARK_GRAY );
			gc.drawLine( 0, iHeight, iWidth, iHeight );
			gc.drawLine( 0, iHeight, 0, iHeight / 2 );
			gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

			if ( null == this.graph ) {
				this.graph = new Graph( iWidth, 500 );
				mapGraphs.put( this.strName, this.graph );
			}
			
			final int[] iPlot = this.graph.getPlot();
			final Set<Integer> set = this.graph.getRecent();
			final Integer iThresholdMin = this.graph.getThresholdIndexMin();
			final Integer iThresholdMax = this.graph.getThresholdIndexMax();
			
			gc.setFont( Theme.get().getFont( 7 ) );
			
			if ( null!=iThresholdMin ) {

				gc.setForeground( UI.COLOR_DARK_BLUE );
				gc.drawLine( iThresholdMin, iHeight, iThresholdMin, iHeight/4 );
				if ( null!=iLastMinRecent ) {
					gc.drawLine( iThresholdMin, 20, iLastMinRecent, iHeight );
				}

				gc.setForeground( UI.COLOR_GRAY );
				gc.drawLine( iThresholdMin, 20, iThresholdMin, iHeight/4 );
			} 
			final Double dThresholdMin = this.graph.getThresholdValueMin();
			if ( null!=dThresholdMin ) {
				final String strMin = String.format( 
						"<  %.6f", dThresholdMin.doubleValue() );
				gc.drawText( strMin, 40, iHeight + 6 );
			}
			if ( null!=iThresholdMax ) {

				gc.setForeground( UI.COLOR_DARK_BLUE );
				gc.drawLine( iThresholdMax, iHeight, iThresholdMax, iHeight/4 );
				if ( null!=iLastMaxRecent ) {
					gc.drawLine( iThresholdMax, 20, iLastMaxRecent, iHeight );
				}
				
				gc.setForeground( UI.COLOR_GRAY );
				gc.drawLine( iThresholdMax, 20, iThresholdMax, iHeight/4 );
			}
			final Double dThresholdMax = this.graph.getThresholdValueMax();
			if ( null!=dThresholdMax ) {
				final String strMax = String.format( 
						"%.6f  >", dThresholdMax.doubleValue() );
				gc.drawText( strMax, iWidth - 90, iHeight + 6 );
			}
			

			gc.setFont( Theme.get().getFont( 10 ) );
			if ( graph.bEnabled ) {
				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
			} else {
				gc.setForeground( UI.COLOR_DARK_GRAY );
				
				gc.drawLine( 30, 30, iWidth - 30, iHeight - 30 );
				gc.drawLine( 30, iHeight - 30, iWidth - 30, 30 );
			}
//			gc.drawText( "Histogram:  " + this.strName, 2, 2 );
			gc.drawText( this.strName, 2, 2 );

			
			iLastMinRecent = null;
			iLastMaxRecent = null;
			
			int iCountLeft = 0;
			int iCountRight = 0;
			boolean bLow = true;
			
			for ( int i = 0; i<iWidth; i++ ) {
				final int iY2;
				if ( set.contains( i ) ) {
					gc.setForeground( UI.COLOR_WHITE );
					iY2 = iHeight + 6;
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
				
				if ( iWidthHalf == i ) {
					bLow = false;
				}
				if ( bLow ) {
					iCountLeft = iCountLeft + iPlot[i];
				} else {
					iCountRight = iCountRight + iPlot[i];
				}
			}
			
			final boolean bMoreEmptyLeft = iCountRight > iCountLeft;
			
			final Float fMark = graph.getMark();
			if ( null!=fMark ) {
				gc.setForeground( UI.COLOR_RED );
				final int iX = graph.getIndexOfSample( fMark.floatValue() );
				gc.drawLine( iX, 0, iX, iHeight + 10 );
				gc.setForeground( UI.COLOR_WHITE );
				final String strValue = String.format( "%.4f", fMark.floatValue() );
				gc.drawText( strValue, iWidthHalf - 12, iHeight + 2 );
			}
			
			gc.setFont( Theme.get().getFont( 8 ) );

			if ( graph.bEnabled ) {
				gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
			} else {
				gc.setForeground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
			}
			final String strMin = String.format( "%.3f", graph.fMin );
			gc.drawText( strMin, 2, iHeight + 4 );
			final String strMax = String.format( "%.3f", graph.fMax );
			gc.drawText( strMax, iWidth - 34, iHeight + 4 );
			
			
			final Float fLatest = graph.getLatestSample();
			if ( null!=fLatest ) {
				
				final GCTextUtils text = new GCTextUtils( gc );
				text.setRect( gc.getClipping() );
				text.setRightAligned( ! bMoreEmptyLeft );
				text.addSpace( 14 );

				if ( ! bIsElapsedGraph ) {
					text.println( "last" );
					final String strLatest = String.format( "%.3f", fLatest );
					text.println( strLatest );
				}
				
				final float fElapsed = (float) graph.getLatestElapsed() / 1000;
				final String strElapsed = String.format( "%.3f", fElapsed );
				text.println( "elapsed" );
				text.println( strElapsed );
				
				if ( bIsElapsedGraph ) {
					final float fSince = lNow - graph.lTimeLastSample;
					text.println( "since" );
					text.println( String.format( "%.3f", fSince / 1000 ) );
				}
			}
			
		} catch ( final Throwable t ) {
			t.printStackTrace();
		}
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}

	
	@Override
	public boolean clickCanvas( final Point point ) {
		if ( null != this.graph ) {
			final Float fValue;
			if ( point.y < 110 ) {
				fValue = this.graph.getValueForIndex( point.x );
			} else {
				fValue = null;
			}
			this.graph.setMark( fValue );
		}
		return super.clickCanvas( point );
	}
	

}
