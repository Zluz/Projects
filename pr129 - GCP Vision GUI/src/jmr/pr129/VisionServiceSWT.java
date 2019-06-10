package jmr.pr129;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.cloud.vision.v1.LocationInfo;
import com.google.cloud.vision.v1.NormalizedVertex;
import com.google.cloud.vision.v1.Vertex;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jmr.pr123.vision.VisionService;
import jmr.util.FileUtil;
import jmr.util.transform.JsonUtils;

public class VisionServiceSWT {


	@SuppressWarnings("unused")
	private final static Logger 
				LOGGER = Logger.getLogger( VisionServiceSWT.class.getName() );
	
	
	final String strFilename;
	
	final Device device;
	
	private VisionService vision;
	
	private final Image imageSource;
	private Image imageAnalysis;

	BatchAnnotateImagesResponse response;
	
	private final JsonObject jo = new JsonObject();
	
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
	

	public Image getAnalysisImage( final Integer iWidth ) {
		return getAnalysisImage( iWidth, null );
	}

	
	public Image getAnalysisImage( final Integer iWidth,
			 					   final Integer iHeight ) {
		
		if ( null==response ) return null;
		
		if ( null!=imageAnalysis && ! imageAnalysis.isDisposed() ) {
			imageAnalysis.dispose();
		}
		
		final JsonObject joSourceImage = new JsonObject();
		final JsonObject joAnalysisImage = new JsonObject();
		final JsonObject joAnnotations = new JsonObject();
		final JsonArray jaAObjects = new JsonArray();
		final JsonArray jaALabels = new JsonArray();
		final JsonArray jaAText = new JsonArray();
		
		joSourceImage.addProperty( "filename", this.strFilename );

		final ImageData id = imageSource.getImageData();

		joSourceImage.addProperty( "width", id.width );
		joSourceImage.addProperty( "height", id.height );
		joSourceImage.addProperty( "depth", id.depth );

		if ( null!=iWidth && null!=iHeight ) {
			imageAnalysis = new Image( device, iWidth, iHeight );
		} else if ( null!=iWidth ) {
			final float fCalcHeight = 
						(float)id.height / id.width * iWidth.intValue();
			final int iCalcHeight = (int)fCalcHeight;
			imageAnalysis = new Image( device, iWidth, iCalcHeight );
		} else {
			imageAnalysis = new Image( device, imageSource.getImageData() );
		}
		final ImageData idAnalysis = imageAnalysis.getImageData();
		final GC gc = new GC( imageAnalysis );
		
		joAnalysisImage.addProperty( "width", idAnalysis.width );
		joAnalysisImage.addProperty( "height", idAnalysis.height );
		joAnalysisImage.addProperty( "depth", idAnalysis.depth );
		
		final Rectangle rect = gc.getClipping();
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

		sbReport.append( "\n" );
		sbReport.append( "Label annotations:\n" );
		
		if ( ! listLabels.isEmpty() ) {
			for ( final EntityAnnotation ea : listLabels ) {
	
				final JsonObject jo = new JsonObject();
				
				final String strDescription = normalize( ea.getDescription() );
				final float fScore = ea.getScore();
				
				jo.addProperty( "description", strDescription );
				jo.addProperty( "score", fScore );
				
				sbReport.append( String.format( 
						"\tLabel (%.3f): %s", fScore, strDescription ) );
	
				final List<LocationInfo> list = ea.getLocationsList();
				if ( ! list.isEmpty() ) {
					
					sbReport.append( "  (LocationInfo in response)" );
				}
				
				jaALabels.add( jo );
				
				sbReport.append( "\n" );
			}
		} else {
			sbReport.append( "\t(none)\n" );
		}

		sbReport.append( "\n" );
		sbReport.append( "Localized Object annotations (shown in green):\n" );

		System.out.println( "Drawing Localized Object annotations" );
		
		if ( ! listObjects.isEmpty() ) {
			for ( final LocalizedObjectAnnotation loa : listObjects ) {

				final JsonObject jo = new JsonObject();

				final String strText = normalize( loa.getName() );
				final float fScore = loa.getScore();

				jo.addProperty( "name", strText );
				jo.addProperty( "score", fScore );

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
				
				jaAObjects.add( jo );
			}
		} else {
			sbReport.append( "\t(none)\n" );
		}
		
		
		sbReport.append( "\n" );
		sbReport.append( "Text annotations (shown in yellow):\n" );

		if ( ! listText.isEmpty() ) {
			for ( final EntityAnnotation ea : listText ) {

				final JsonObject jo = new JsonObject();

				final String strText = normalize( ea.getDescription() );
				
				jo.addProperty( "description", strText );
	
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
				
				jaAText.add( jo );
			}
		} else {
			sbReport.append( "\t(none)\n" );
		}

		
		final String strReport = sbReport.toString();
		System.out.println( strReport );


		joAnnotations.add( "labels", jaALabels );
		joAnnotations.add( "objects", jaAObjects );
		joAnnotations.add( "text", jaAText );
		
		this.jo.add( "image-source", joSourceImage );
		this.jo.add( "image-analysis", joAnalysisImage );
		this.jo.add( "annotations", joAnnotations );
		
		return this.imageAnalysis;
	}

	
	public String getAnalysisReport() {
		return this.sbReport.toString();
	}
	
	
	public static boolean saveImageToFile( final Image image,
										   final String strFilename ) {

		final ImageLoader saver = new ImageLoader();
		saver.data = new ImageData[] { image.getImageData() };
		saver.save( strFilename, SWT.IMAGE_JPEG );
		return true;
	}
	
	
//	public boolean saveToJSONFile( final File file ) {
//		return this.vision.toJsonFile( file );
//	}

	
	public JsonObject toJson() {
		return this.jo;
	}
	
	
	
	
	public static void main( final String[] args ) {
		
		final long lTimeStart = System.currentTimeMillis();
		
		String strFilename = null;
		Integer iWidth = null;
		Integer iHeight = null;
		boolean bShowHelpExit = false;
		
		if ( 0 == args.length ) {
			strFilename = VisionGUI.FILENAME_IMAGE_SOURCE;
		} else if ( args.length < 4 ) {
			strFilename = args[0];
			if ( args.length > 1 ) {
				try {
					iWidth = Integer.parseInt( args[1] );
				} catch ( final NumberFormatException e ) {
					System.err.println( "Error: bad width." );
					bShowHelpExit = true;
				}
			}
			if ( args.length > 2 ) {
				try {
					iHeight = Integer.parseInt( args[2] );
				} catch ( final NumberFormatException e ) {
					System.err.println( "Error: bad height." );
					bShowHelpExit = true;
				}
			}
		}
		
		if ( ! StringUtils.isBlank( strFilename ) ) {
			final File file = new File( strFilename );
			if ( ! file.canRead() ) {
				System.err.println( "Cannot open image file for reading: " 
									+ strFilename );
				bShowHelpExit = true;
			}
		} else {
			System.err.println( "Image file not specified." );
			bShowHelpExit = true;
		}
		
		if ( bShowHelpExit ) {
			System.out.println( "Syntax:" );
			System.out.println( "java -jar " 
						+ VisionServiceSWT.class.getSimpleName() 
						+ ".jar <image_filename> [<analysis_image_width> [<analysis_image_height]]" );
			System.exit( 1 );
		}

		final int iDotPos = strFilename.lastIndexOf( "." );
		final String strImageBase = strFilename.substring( 0, iDotPos );
		final String strAnalysisImageFile = strImageBase + "-analysis.jpg";
		final String strAnalysisTextFile = strImageBase + "-analysis.txt";
		final String strAnalysisJsonFile = strImageBase + "-analysis.json";

		System.out.println( strAnalysisJsonFile );

		System.out.println( "Analyzing image: " + strFilename );

		System.out.println( "Output image file: " + strAnalysisImageFile );
		System.out.println( "Output text file: " + strAnalysisTextFile );
		System.out.println( "Output JSON file: " + strAnalysisJsonFile );
		
		
		final Device device = Display.getDefault();
		final VisionServiceSWT vss = new VisionServiceSWT( device, strFilename );
		
		vss.analyze();
		final Image image = vss.getAnalysisImage( iWidth, iHeight );
		final String strReport = vss.getAnalysisReport();
		final JsonObject jo = vss.toJson();
		
		
		final long lTimeAnalysisComplete = System.currentTimeMillis();
		final long lElapsedAnalysis = lTimeAnalysisComplete - lTimeStart;
		
		
		final ImageData id = image.getImageData();
		System.out.println( "Image: " + id.width + " x " + id.height );
		System.out.println( "Report:\n" + strReport );
		
		saveImageToFile( image, strAnalysisImageFile );
		System.out.println( "Analysis image file saved." );
		
		final File fileReport = new File( strAnalysisTextFile );
		FileUtil.saveToFile( fileReport, strReport );
		System.out.println( "Analysis report file saved." );

		
		
		final File fileJson = new File( strAnalysisJsonFile );
		final JsonObject joAnalysis = jo.getAsJsonObject( "image-analysis" );
		joAnalysis.addProperty( "filename", strAnalysisImageFile );
		
		final JsonObject joReport = new JsonObject();
		joReport.addProperty( "filename", strAnalysisTextFile );
		joReport.addProperty( "time-start", lTimeStart );
		joReport.addProperty( "time-analysis-complete", lTimeAnalysisComplete );
		joReport.addProperty( "time-elapsed", lElapsedAnalysis );
		
		jo.add( "report", joReport );
		final String strJson = JsonUtils.getPretty( jo );
		FileUtil.saveToFile( fileJson, strJson );
		System.out.println( "Analysis JSON file saved." );
	}
	
}
