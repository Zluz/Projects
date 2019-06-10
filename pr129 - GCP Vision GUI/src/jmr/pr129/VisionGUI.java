package jmr.pr129;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang3.StringUtils;

import jmr.pr123.vision.VisionService;

public class VisionGUI {

	final static String FILENAME_IMAGE_SOURCE = 
//			"S:/xfer/Vision testing/unsorted/capture_vid2 - yard sale 002.jpg";
			"S:/xfer/Vision testing/unsorted/capture_vid2 - yard sale 001.1.jpg";
//			"S:/xfer/Vision testing/D035-v2 - car - plate.jpg";
//			"S:/xfer/Vision testing/D007-c - outside.jpg";
//			"S:/Sessions/94-C6-91-18-C8-33/20190605-085938_capture_vid2/previous.jpg";
//			"S:/Sessions/94-C6-91-18-C8-33/20190607-084254_capture_vid2/changed.jpg";
	
	
	final static Display display = new Display();

	final private Shell shell;
//	final private Composite compImageSource;
//	final private Composite compImageAnalysis;
//	final private Composite compTextLog;
//	final private Composite compControls;
	final private Canvas canvasImageSource;
	final private Canvas canvasImageAnalysis;
	final private Text textReport;
//	final private StringBuilder sbReport = new StringBuilder();

//	private BatchAnnotateImagesResponse response;
	
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
//		this.compTextLog = new Composite( compRight, SWT.NONE );
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

	public void setAnalysisImage( final Image image ) {
		if ( null!=imageAnalysis && !imageAnalysis.isDisposed() ) {
			imageAnalysis.dispose();
		}
		this.imageAnalysis = image;
		this.canvasImageAnalysis.addPaintListener( 
									createPaintListener( imageAnalysis ) );
		this.shell.setRedraw( true );
	}
	

	
	public Image getAnalysisImage() {
		return this.imageAnalysis;
	}
	
	
	

	static VisionService vision;


	public static void main( final String[] args ) {

		final VisionGUI gui = new VisionGUI();
		gui.open();
		
		gui.setSourceImage( FILENAME_IMAGE_SOURCE );

		final VisionServiceSWT 
				vss = new VisionServiceSWT( display, FILENAME_IMAGE_SOURCE );
		
		final Thread thread = new Thread( "Image analysis" ) {
			public void run() {
				
				System.out.print( "Analyzing image..." );
				vss.analyze();
				

				System.out.println( "Done." );
				

				display.asyncExec( new Runnable() {
					@Override
					public void run() {
						
						final Image image = vss.getAnalysisImage( 1600, 900 );
						gui.setAnalysisImage( image );
						
						String strReport = vss.getAnalysisReport();
						strReport = StringUtils.replace( 
										strReport, "\n", Text.DELIMITER );
						gui.textReport.setText( strReport );
						
						gui.shell.redraw();
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
