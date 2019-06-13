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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.cloud.vision.v1.LocationInfo;
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
									final DrawablePoly poly,
								 	final String strText,
								 	final int iBoxAlpha ) {
		if ( null==poly ) return false;

		final List<Point> listPoints = poly.getVertices();
		

		gc.setForeground( colorFore );
		gc.setAlpha( iBoxAlpha );

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
			
			gc.drawLine( ptFirst.x, ptFirst.y, ptLast.x, ptLast.y );
			
			final Point ptText;
			if ( poly.isOrthogonal() ) {
				ptText = ptFirst;
			} else {
				ptText = poly.getTopmostPoint();
			}

			final Point ptExtent = gc.textExtent( strText );
			final Point ptTop = poly.getTopmostPoint();

			final int iY = Math.max( ptText.y - ptExtent.y + 2, 4 );
			final int iX;
			if ( poly.isOrthogonal() ) {
				iX = ptText.x;
			} else if ( ptTop.x > poly.getAveragePoint().x ) {
				iX = ptText.x - ptExtent.x;
			} else {
				iX = ptText.x;
			}

			gc.setForeground( colorShadow );
			gc.setAlpha( 80 );
			
			gc.drawText( strText, iX + 1, iY - 1, true );
			gc.drawText( strText, iX + 3, iY - 1, true );
			gc.drawText( strText, iX + 1, iY + 1, true );
			gc.drawText( strText, iX + 3, iY + 1, true );
			
			gc.setForeground( colorFore );
			gc.setAlpha( 255 );

			gc.drawText( strText, iX + 2, iY, true );
			
			gc.drawOval( ptTop.x-1, ptTop.y-1, 2, 2 );
			
			return true;
			
		} else {
			// no points
			return false;
		}
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
		
		gc.setAdvanced( true );
		gc.setAntialias( SWT.ON );
		
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
				listAObjects = air.getLocalizedObjectAnnotationsList();
		final List<EntityAnnotation> 
				listAText = air.getTextAnnotationsList();
		final List<EntityAnnotation> 
				listALabels = air.getLabelAnnotationsList();

		
		
		final Color colorObjects = device.getSystemColor( SWT.COLOR_GREEN );
		final Color colorText = device.getSystemColor( SWT.COLOR_YELLOW );
//		final Color colorShadow = display.getSystemColor( SWT.COLOR_DARK_GRAY );
		final Color colorShadow = device.getSystemColor( SWT.COLOR_BLACK );

		sbReport.append( "\n" );
		sbReport.append( "Label annotations:\n" );
		
		if ( ! listALabels.isEmpty() ) {
			for ( final EntityAnnotation ea : listALabels ) {
	
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
		
		if ( ! listAObjects.isEmpty() ) {
			for ( final LocalizedObjectAnnotation loa : listAObjects ) {

				final JsonObject jo = new JsonObject();

				final String strText = normalize( loa.getName() );
				final float fScore = loa.getScore();

				jo.addProperty( "name", strText );
				jo.addProperty( "score", fScore );

				sbReport.append( String.format( 
							"\tObject (%.3f): %s", fScore, strText ) );
	
				final DrawablePoly poly = new DrawablePoly(
								loa.getBoundingPoly(), 
								id.width, id.height,
								rect.width, rect.height );
				final boolean bDrawn = drawPoly( 
								gc, colorObjects, colorShadow,
								poly, strText, 220 );
				
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
		
		
		final Map<String,Integer> mapNormText = new HashMap<>();
		if ( ! listAText.isEmpty() ) {
			for ( final EntityAnnotation ea : listAText ) {

				final String strText = normalize( ea.getDescription() );
				
				if ( mapNormText.keySet().contains( strText ) ) {
					final int iCount = mapNormText.get( strText );
					mapNormText.put( strText, 1 + iCount );
				} else {
					mapNormText.put( strText, 1 );
				}
			}
		}


		if ( ! listAText.isEmpty() ) {
			for ( final EntityAnnotation ea : listAText ) {

				final JsonObject jo = new JsonObject();

				final String strText = normalize( ea.getDescription() );
				
				jo.addProperty( "description", strText );
	
				sbReport.append( String.format( "\tText: %s", strText ) );
	
				final DrawablePoly poly = new DrawablePoly(
										ea.getBoundingPoly(), 
										id.width, id.height,
										rect.width, rect.height );
				final String strDrawText;
				final int iBoxAlpha;
				if ( poly.isOrthogonal() 
								&& ( mapNormText.get( strText ) > 1 ) ) {
					strDrawText = "";
					iBoxAlpha = 40;
				} else {
					strDrawText = StringUtils.abbreviate( strText, 40 );
					iBoxAlpha = 160;
				}
				final boolean bDrawn = drawPoly( 
										gc, colorText, colorShadow,
										poly, strDrawText,
										iBoxAlpha );


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
