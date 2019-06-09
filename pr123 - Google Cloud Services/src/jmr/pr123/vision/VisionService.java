package jmr.pr123.vision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

public class VisionService {

	final private String strSourceFilename;

	private BatchAnnotateImagesResponse response;
//	private final JsonObject jo = new JsonObject();
	
	public VisionService( final String strSourceFilename ) {
		this.strSourceFilename = strSourceFilename;
	}
	
	
	public boolean analyze() {

	    // Instantiates a client
	    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

	      // The path to the image file to annotate
	      Path path = Paths.get( this.strSourceFilename );
	      byte[] data = Files.readAllBytes(path);
	      ByteString imgBytes = ByteString.copyFrom(data);

	      
	      // Builds the image annotation request
	      List<AnnotateImageRequest> requests = new ArrayList<>();
	      
	      Image img = Image.newBuilder().setContent(imgBytes).build();
	      
	      Feature feat1 = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
	      Feature feat2 = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
//	      Feature feat3 = Feature.newBuilder().setType(Type.LOGO_DETECTION).build();
	      Feature feat4 = Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION).build();
	      
	      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
								              .addFeatures(feat1)
								              .addFeatures(feat2)
//								              .addFeatures(feat3)
								              .addFeatures(feat4)
								              .setImage(img)
								              .build();
	      requests.add(request);

	      // Performs label detection on the image file
	      this.response = vision.batchAnnotateImages(requests);
	      
	      return true;
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	public BatchAnnotateImagesResponse getAnnotationResponse() {
		return this.response;
	}
	
	
	public void printResponseReport() {
		if ( null==this.response ) return;
	
		List<AnnotateImageResponse> responses = response.getResponsesList();
	
		for (AnnotateImageResponse res : responses) {
			System.out.println( "\n\nres: vvvvvvvvvvv\n" + res.toString() + "\n^^^^^^^^^^^\n" );
			if (res.hasError()) {
				System.out.printf("Error: %s\n", res.getError().getMessage());
	        }
	
			for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
				System.out.println( "\n\nannotation: v v v v v v v\n" + annotation.toString() + "\n^ ^ ^ ^ ^ ^ ^\n" );
				System.out.println( "\tgetMid(): " + annotation.getMid() );
				System.out.println( "\tgetDescription(): " + annotation.getDescription() );
				System.out.println( "\tgetAllFields():" );
				annotation.getAllFields().forEach((k, v) ->
				System.out.printf("\t\t%s : %s\n", k, v.toString()));
	        }
		}
	}
	
	
//	public boolean toJsonFile( final File file ) {
//		final ObjectMapper om = new ObjectMapper();
//		try {
//			om.writeValue( file, this.response );
//			return true;
//		} catch ( final IOException e ) {
//			System.err.println( "Failed to write file. " + e.toString() );
//			return false;
//		}
//	}
	
	
//	public JsonObject toJson() {
//		return this.jo;
//	}
	
}
