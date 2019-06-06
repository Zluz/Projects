package jmr.pr129;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

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

public class VisionGUI {

	final static String FILENAME_IMAGE_SOURCE = 
//			"S:/xfer/Vision testing/unsorted/capture_vid2 - yard sale 002.jpg";
//			"S:/xfer/Vision testing/unsorted/capture_vid2 - yard sale 001.1.jpg";
//			"S:/xfer/Vision testing/D035-v2 - car - plate.jpg";
			"S:/Sessions/94-C6-91-18-C8-33/20190605-085938_capture_vid2/previous.jpg";
	
	
	final static Display display = new Display();

	final private Shell shell;
//	final private Composite compImageSource;
//	final private Composite compImageAnalysis;
	final private Composite compTextLog;
//	final private Composite compControls;
	final private Canvas canvasImageSource;
	final private Canvas canvasImageAnalysis;
	final private Text textReport;
	final private StringBuilder sbReport = new StringBuilder();

	private BatchAnnotateImagesResponse response;
	
	private Image imageSource;
	private Image imageAnalysis;
	

	
	public VisionGUI() {
		shell = new Shell( display );

//		final GridLayout gl = new GridLayout();
//		gl.numColumns = 2;
//		shell.setLayout( gl );
		
		final FillLayout flOuter = new FillLayout( SWT.VERTICAL );
		shell.setLayout( flOuter );
		
		final Composite compLeft = new Composite( shell, SWT.NONE );
		final Composite compRight = new Composite( shell, SWT.NONE );
		
		final FillLayout flLeft = new FillLayout( SWT.HORIZONTAL );
		compLeft.setLayout( flLeft );

		final FillLayout flRight = new FillLayout( SWT.HORIZONTAL );
		compRight.setLayout( flRight );

		
		this.canvasImageSource = new Canvas( compLeft, SWT.FILL );
		this.canvasImageAnalysis = new Canvas( compLeft, SWT.FILL );
		
		this.textReport = new Text( compRight, SWT.MULTI );
		this.compTextLog = new Composite( compRight, SWT.NONE );
//		this.compControls = new Composite( compRight, SWT.NONE );
		
		this.shell.setText( VisionGUI.class.getName() );
		
//		this.canvasImageSource.addPaintListener( 
//								createPaintListener( imageSource ) );
//		this.canvasImageAnalysis.addPaintListener( 
//								createPaintListener( imageAnalysis ) );
		
		shell.layout();
	}
	
	
	private PaintListener createPaintListener( final Image image ) {
		final PaintListener listener = new PaintListener() {
			@Override
			public void paintControl( final PaintEvent event ) {
				if ( null!=image && !image.isDisposed() ) {
					final Rectangle rect = event.gc.getClipping();
					final ImageData id = image.getImageData();
					event.gc.drawImage( image, 
									0, 0, id.width, id.height, 
									0, 0, rect.width, rect.height );
				}
			}
		};
		return listener;
	}
	
	
	public void open() {
		shell.setVisible( true );
	}
	
	
	public Shell getShell() {
		return this.shell;
	}

	
	public void setSourceImage( final String strImageFilename ) {
		if ( null!=imageSource && !imageSource.isDisposed() ) {
			imageSource.dispose();
		}
		this.imageSource = new Image( display, strImageFilename );
		this.canvasImageSource.addPaintListener( 
									createPaintListener( imageSource ) );
		this.shell.setRedraw( true );
	}
	

	
	public Image getAnalysisImage() {
		return this.imageAnalysis;
	}
	
	public String getAnalysisReport() {
		return this.sbReport.toString();
	}
	
	public void analyze( final Integer iWidth,
						 final Integer iHeight ) {
		if ( null==response ) return;
		
		if ( null!=imageAnalysis && ! imageAnalysis.isDisposed() ) {
			imageAnalysis.dispose();
		}
		
		if ( null!=iWidth && null!=iHeight ) {
			imageAnalysis = new Image( display, iWidth, iHeight );
		} else {
			imageAnalysis = new Image( display, imageSource.getImageData() );
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

		

		final Color colorObjects = display.getSystemColor( SWT.COLOR_GREEN );
		final Color colorText = display.getSystemColor( SWT.COLOR_YELLOW );
//		final Color colorShadow = display.getSystemColor( SWT.COLOR_DARK_GRAY );
		final Color colorShadow = display.getSystemColor( SWT.COLOR_BLACK );

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
		final String strUIReport = 
				StringUtils.replace( strReport, "\n", Text.DELIMITER );
		textReport.setText( strUIReport );

		
	}
	
	
	public void setAnalysisImage( final BatchAnnotateImagesResponse response ) {
		if ( null==response ) return;
		this.response = response;
		
//		analyze();
//		final PaintListener listener = createPaintListener( this.imageAnalysis );
//		this.canvasImageAnalysis.addPaintListener( listener );
		
		this.canvasImageAnalysis.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent event ) {
				
				analyze( event.width, event.height );
				
				final Image image = imageAnalysis;
				
				if ( null!=image && !image.isDisposed() ) {
					final Rectangle rect = event.gc.getClipping();
					final ImageData id = image.getImageData();
					event.gc.drawImage( image, 
									0, 0, id.width, id.height, 
									0, 0, rect.width, rect.height );
				}
			}
			
		} );

	}
	
	
	public static String normalize( final String strInput ) {
		String strOutput = strInput.trim();
		strOutput = StringUtils.replace( strOutput, "\n", " " );
		return strOutput;
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
	

	static VisionService vision;

	public static void main( final String[] args ) {

		final VisionGUI gui = new VisionGUI();
		gui.open();
		
		gui.setSourceImage( FILENAME_IMAGE_SOURCE );

		
		final Thread thread = new Thread( "Image analysis" ) {
			public void run() {
				vision = new VisionService( FILENAME_IMAGE_SOURCE );
				
				System.out.print( "Analyzing image..." );
				vision.analyze();
				System.out.print( "Examining..." );
				gui.analyze( null, null );
				System.out.println( "Done." );
				
				System.out.println( vision.getAnnotationResponse().toString() );

				display.asyncExec( new Runnable() {
					@Override
					public void run() {
						gui.setAnalysisImage( vision.getAnnotationResponse() );
					}
				});
			};
		};
		thread.start();

	    while ( ! gui.getShell().isDisposed()) {
		      if ( display.readAndDispatch()) {
		    	  display.sleep();
		      }
		}
		display.dispose(); 
	}

}
