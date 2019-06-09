package jmr.pr129;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.cloud.vision.v1.LocationInfo;
import com.google.cloud.vision.v1.NormalizedVertex;
import com.google.cloud.vision.v1.Vertex;

import jmr.pr123.vision.VisionService;

public class VisionServiceSWT {

	
	final String strFilename;
	
	final Device device;
	
	private VisionService vision;
	
	private final Image imageSource;
	private Image imageAnalysis;

	BatchAnnotateImagesResponse response;

	
	final StringBuilder sbReport = new StringBuilder();


	public VisionServiceSWT( final Device device,
							 final String strFilename ) {
		this.strFilename = strFilename;
		this.device = device;
		imageSource = new Image( device, strFilename );
	}
	

	public static String normalize( final String strInput ) {
		String strOutput = strInput.trim();
		strOutput = StringUtils.replace( strOutput, "\n", " " );
		return strOutput;
	}

	
	

	public void analyze() {
		
		vision = new VisionService( this.strFilename );

		System.out.println( "Analyzing image.." );
		vision.analyze();
		System.out.println( "Examining.." );
		
		this.response = vision.getAnnotationResponse();
		
	}
	
	
	

	public static boolean drawPoly( final GC gc,
									final Color colorFore,
									final Color colorShadow,
								 	final BoundingPoly poly,
								 	final String strText,
								 	final int iImageWidth,
								 	final int iImageHeight,
								 	final int iWidth,
								 	final int iHeight ) {
		if ( null==poly ) return false;

		final List<Point> listPoints = new LinkedList<>();
		
		final List<NormalizedVertex> listNorm = poly.getNormalizedVerticesList();
		if ( null!=listNorm && !listNorm.isEmpty() ) {
		
			for ( final NormalizedVertex nv : listNorm ) {
				float fX = nv.getX() * iWidth;
				float fY = nv.getY() * iHeight;
				
				final Point pt = new Point( (int)fX, (int)fY );
				listPoints.add( pt );
			}
		}
		
		final List<Vertex> listRaw = poly.getVerticesList();
		if ( null!=listRaw && !listRaw.isEmpty() ) {
			
			for ( final Vertex vertex : listRaw ) {

				final float fX = (float)vertex.getX() / iImageWidth * iWidth;
				final float fY = (float)vertex.getY() / iImageHeight * iHeight;

				final Point pt = new Point( (int)fX, (int)fY );
				listPoints.add( pt );
			}
		}


//		final String strNormText = strText.trim();

		gc.setForeground( colorFore );

		if ( ! listPoints.isEmpty() ) {
			
			Point ptLast = null;
			Point ptFirst = null;
			for ( final Point pt : listPoints ) {
				
//				gc.drawOval( pt.x-2, pt.y-2, 4, 4 );

				if ( null!=ptLast ) {
					gc.drawLine( pt.x, pt.y, ptLast.x, ptLast.y );
				}

				ptLast = pt;
				if ( null==ptFirst ) ptFirst = pt;
			}
			
			final int iX = ptFirst.x;
			final int iY = ptFirst.y;
			gc.drawLine( iX, iY, ptLast.x, ptLast.y );

			gc.setForeground( colorShadow );
			gc.drawText( strText, iX + 1, iY - 1, true );
			gc.drawText( strText, iX + 3, iY - 1, true );
			gc.drawText( strText, iX + 1, iY + 1, true );
			gc.drawText( strText, iX + 3, iY + 1, true );
			gc.setForeground( colorFore );
			gc.drawText( strText, iX + 2, iY, true );
			
//			System.out.println( "Drawing at " + iX + ", " + iY 
//								+ " \"" + strText + "\"" );
		} else {
//			gc.drawText( "<no points> - " + strNormText, 4, 10, true );
//			System.out.println( "No location identified for" 
//								+ " \"" + strText + "\"" );
			return false;
		}
		

		return true;
	}
	
	
	public Image getAnalysisImage( final Integer iWidth,
			 					   final Integer iHeight ) {
		
		if ( null==response ) return null;
		
		if ( null!=imageAnalysis && ! imageAnalysis.isDisposed() ) {
			imageAnalysis.dispose();
		}
		
		if ( null!=iWidth && null!=iHeight ) {
			imageAnalysis = new Image( device, iWidth, iHeight );
		} else {
			imageAnalysis = new Image( device, imageSource.getImageData() );
		}
		final GC gc = new GC( imageAnalysis );
		
		final Rectangle rect = gc.getClipping();
		final ImageData id = imageSource.getImageData();
		gc.drawImage( imageSource, 
						0, 0, id.width, id.height, 
						0, 0, rect.width, rect.height );
		
		sbReport.setLength( 0 );
		
//		System.out.println( "response count: " + response.getResponsesCount() );
		
		final AnnotateImageResponse air = response.getResponses( 0 );
		
		final List<LocalizedObjectAnnotation> 
				listObjects = air.getLocalizedObjectAnnotationsList();
		final List<EntityAnnotation> 
				listText = air.getTextAnnotationsList();
		final List<EntityAnnotation> 
				listLabels = air.getLabelAnnotationsList();

		
		
		final Color colorObjects = device.getSystemColor( SWT.COLOR_GREEN );
		final Color colorText = device.getSystemColor( SWT.COLOR_YELLOW );
//		final Color colorShadow = display.getSystemColor( SWT.COLOR_DARK_GRAY );
		final Color colorShadow = device.getSystemColor( SWT.COLOR_BLACK );

		sbReport.append( "Label annotations:\n" );
		
		for ( final EntityAnnotation ea : listLabels ) {

//			final BoundingPoly poly = ea.getBoundingPoly();
//			drawPoly( e.gc, poly, ea.getDescription(), 
//							id.width, id.height,
//							rect.width, rect.height );
			
			final String strDescription = normalize( ea.getDescription() );
			final float fScore = ea.getScore();
//			final Map<FieldDescriptor, Object> map = ea.getAllFields();
			
			sbReport.append( String.format( 
					"\tLabel (%.3f): %s", fScore, strDescription ) );

			final List<LocationInfo> list = ea.getLocationsList();
			if ( ! list.isEmpty() ) {
				
				sbReport.append( "  (LocationInfo in response)" );
				
//				for ( final LocationInfo li : list ) {
//					final Map<FieldDescriptor, Object> mapFD = li.getAllFields();
////					System.out.println( "map: " + mapFD );
//				}
			}
			
			sbReport.append( "\n" );
		}


		sbReport.append( "Localized Object annotations (shown in green):\n" );

		System.out.println( "Drawing Localized Object annotations" );
		for ( final LocalizedObjectAnnotation loa : listObjects ) {

			final String strText = normalize( loa.getName() );
			final float fScore = loa.getScore();

			sbReport.append( String.format( 
						"\tObject (%.3f): %s", fScore, strText ) );

			final BoundingPoly poly = loa.getBoundingPoly();
			final boolean bDrawn = drawPoly( 
							gc, colorObjects, colorShadow,
							poly, strText,
							id.width, id.height,
							rect.width, rect.height );
			
			if ( ! bDrawn ) {
				sbReport.append( "  (not drawn)" );
			}
			sbReport.append( "\n" );
		}
		
		
		sbReport.append( "Text annotations (shown in yellow):\n" );

		for ( final EntityAnnotation ea : listText ) {

			final String strText = normalize( ea.getDescription() );
//			final float fScore = ea.getScore();

			sbReport.append( String.format( "\tText: %s", strText ) );

			final BoundingPoly poly = ea.getBoundingPoly();
			final boolean bDrawn = drawPoly( 
							gc, colorText, colorShadow,
							poly, strText, 
							id.width, id.height,
							rect.width, rect.height );
			if ( ! bDrawn ) {
				sbReport.append( "  (not drawn)" );
			}
			sbReport.append( "\n" );
		}

		final String strReport = sbReport.toString();
		System.out.println( strReport );
//		final String strUIReport = 
//				StringUtils.replace( strReport, "\n", Text.DELIMITER );
//		textReport.setText( strUIReport );
		
		
		return this.imageAnalysis;
	}
	
	public String getAnalysisReport() {
		return this.sbReport.toString();
	}
	
	
	public static void main( final String[] args ) {
		
		final Device device = Display.getDefault();
		final String strFilename = VisionGUI.FILENAME_IMAGE_SOURCE;
		
		final VisionServiceSWT vss = new VisionServiceSWT( device, strFilename );
		
		vss.analyze();
		final Image image = vss.getAnalysisImage( 160, 90 );
		final String strReport = vss.getAnalysisReport();
		
		
		final ImageData id = image.getImageData();
		System.out.println( "Image: " + id.width + " x " + id.height );
		System.out.println( "Report:\n" + strReport );
	}
	
}
