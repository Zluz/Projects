package jmr.pr129;

import org.eclipse.swt.graphics.Point;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.NormalizedVertex;
import com.google.cloud.vision.v1.Vertex;

public class DrawablePoly {

	final private BoundingPoly bp;
	
	final private List<Point> listPoints = new LinkedList<>();
	
	private final int iImageWidth;
	private final int iImageHeight;
	private final int iWidth;
	private final int iHeight;
	
	private Point ptTop = null;
	private Point ptAverage = null;
	
	private boolean bOrthogonal;
	
	public DrawablePoly( final BoundingPoly poly,
						 final int iImageWidth,
						 final int iImageHeight,
						 final int iWidth,
						 final int iHeight ) {
		this.bp = poly;
		this.iWidth = iWidth;
		this.iHeight = iHeight;
		this.iImageWidth = iImageWidth;
		this.iImageHeight = iImageHeight;
		
		this.evalVertices();
		this.evalPoints();
	}
	
	
	public boolean isOrthogonal() {
		return this.bOrthogonal;
	}
	
	
	public Point getTopmostPoint() {
		return this.ptTop;
	}

	
	public Point getAveragePoint() {
		return this.ptAverage;
	}

	
	private void evalPoints() {
		
		final Set<Integer> setX = new HashSet<>();
		final Set<Integer> setY = new HashSet<>();
		int iSumX = 0;
		int iSumY = 0;
		
		for ( final Point pt : listPoints ) {
			setX.add( pt.x );
			setY.add( pt.y );
			iSumX += pt.x;
			iSumY += pt.y;
			if ( null==ptTop || pt.y < ptTop.y ) {
				ptTop = pt;
			}
		}
		final int iCount = listPoints.size();
		this.ptAverage = new Point( 
				(int)((float)iSumX / iCount ), 
				(int)((float)iSumY / iCount ) );
		
		this.bOrthogonal = ( 2==setX.size() && 2==setY.size() ); 
	}

	
	private void evalVertices() {

		final List<NormalizedVertex> listNorm = bp.getNormalizedVerticesList();
		if ( null!=listNorm && !listNorm.isEmpty() ) {
		
			for ( final NormalizedVertex nv : listNorm ) {
				float fX = nv.getX() * iWidth;
				float fY = nv.getY() * iHeight;
				
				final Point pt = new Point( (int)fX, (int)fY );
				listPoints.add( pt );
			}
		}
		
		final List<Vertex> listRaw = bp.getVerticesList();
		if ( null!=listRaw && !listRaw.isEmpty() ) {
			
			for ( final Vertex vertex : listRaw ) {

				final float fX = (float)vertex.getX() / iImageWidth * iWidth;
				final float fY = (float)vertex.getY() / iImageHeight * iHeight;

				final Point pt = new Point( (int)fX, (int)fY );
				listPoints.add( pt );
			}
		}
	}
	
	public List<Point> getVertices() {
		return this.listPoints;
	}
	
	
}
