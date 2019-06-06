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

import java.util.List;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.cloud.vision.v1.NormalizedVertex;
import com.google.cloud.vision.v1.Vertex;

import jmr.pr123.vision.VisionService;

public class VisionGUI {

	final static String FILENAME_IMAGE_SOURCE = 
//			"S:/xfer/Vision testing/unsorted/capture_vid2 - yard sale 002.jpg";
			"S:/xfer/Vision testing/D035-v2 - car - plate.jpg";
	
	final static Display display = new Display();

	final private Shell shell;
//	final private Composite compImageSource;
//	final private Composite compImageAnalysis;
	final private Composite compTextLog;
	final private Composite compControls;
	final private Canvas canvasImageSource;
	final private Canvas canvasImageAnalysis;
	
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
		
		this.compTextLog = new Composite( compRight, SWT.NONE );
		this.compControls = new Composite( compRight, SWT.NONE );
		
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
	
	public void setAnalysisImage( final BatchAnnotateImagesResponse response ) {
		if ( null==response ) return;
		final Image image = this.imageSource;
		
		this.canvasImageAnalysis.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent e ) {
				final Rectangle rect = e.gc.getClipping();
				final ImageData id = image.getImageData();
				e.gc.drawImage( image, 
								0, 0, id.width, id.height, 
								0, 0, rect.width, rect.height );
				final Color color = display.getSystemColor( SWT.COLOR_GREEN );
				e.gc.setForeground( color );
				
				System.out.println( "response count: " + response.getResponsesCount() );
				
				final AnnotateImageResponse air = response.getResponses( 0 );
				
				final List<LocalizedObjectAnnotation> 
						listObjects = air.getLocalizedObjectAnnotationsList();
				final List<EntityAnnotation> 
						listText = air.getTextAnnotationsList();
				final List<EntityAnnotation> 
						listLabels = air.getLabelAnnotationsList();

				
				
				for ( final EntityAnnotation ea : listText ) {

					System.out.println( "Drawing Text: " + ea.getDescription() );

					final BoundingPoly poly = ea.getBoundingPoly();
					drawPoly( e.gc, poly, ea.getDescription(), 
									id.width, id.height,
									rect.width, rect.height );

				}

				for ( final LocalizedObjectAnnotation loa : listObjects ) {

					System.out.println( "Drawing Label: " + loa.getName() );
					
					final BoundingPoly poly = loa.getBoundingPoly();
					final boolean bPoly = drawPoly( 
									e.gc, poly, loa.getName(),
									id.width, id.height,
									rect.width, rect.height );
					
					if ( !bPoly ) {
//						e.gc.drawText( loa.getName(), 
//								vFirst.getX(), vFirst.getY(), false );
					}
				}
				
			}
		});
	}

	
	public static boolean drawPoly( final GC gc,
								 	final BoundingPoly poly,
								 	final String strName,
								 	final int iImageWidth,
								 	final int iImageHeight,
								 	final int iWidth,
								 	final int iHeight ) {
		if ( null==poly ) return false;
		
		final List<NormalizedVertex> listNorm = poly.getNormalizedVerticesList();
		if ( null!=listNorm && !listNorm.isEmpty() ) {
		
//			final int iWidth = gc.getClipping().width;
//			final int iHeight = gc.getClipping().height;
			
			final NormalizedVertex nvFirst = listNorm.get( 0 );
			Point ptLast = null;
			Point ptFirst = null;
			for ( final NormalizedVertex nv : listNorm ) {
				float fX = nv.getX();
				float fY = nv.getY();
				fX = fX * iWidth;
				fY = fY * iHeight;
				final int x = (int)fX;
				final int y = (int)fY;
				System.out.println( "\tfX: " + fX + ", fY:" + fY + "\tiX: " + x + ", iY:" + y );			
				gc.drawOval( x-2, y-2, 4, 4 );
				
				if ( null!=ptLast ) {
					gc.drawLine( x, y, ptLast.x, ptLast.y );
				}
				
				ptLast = new Point( x, y );
				if ( null==ptFirst ) ptFirst = ptLast;
			}
			gc.drawLine( ptFirst.x, ptFirst.y, ptLast.x, ptLast.y );

			gc.drawText( strName, ptFirst.x + 2, ptFirst.y, true );
		}
		
		final List<Vertex> listRaw = poly.getVerticesList();
		if ( null!=listRaw && !listRaw.isEmpty() ) {
			
			Point ptLast = null;
			Point ptFirst = null;
			for ( final Vertex vertex : listRaw ) {
				
				final int x = (int)((float)vertex.getX() / iImageWidth * iWidth);
				final int y = (int)((float)vertex.getY() / iImageHeight * iHeight);
	System.out.println( "\tX: " + x + ", Y:" + y );			
				gc.drawOval( x-2, y-2, 4, 4 );

				if ( null!=ptLast ) {
					gc.drawLine( x, y, ptLast.x, ptLast.y );
				}

				ptLast = new Point( x, y );
				if ( null==ptFirst ) ptFirst = ptLast;
			}
			
			gc.drawText( strName, ptFirst.x + 2, ptFirst.y, true );
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
