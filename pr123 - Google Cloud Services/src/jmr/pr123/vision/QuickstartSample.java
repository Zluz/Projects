
package jmr.pr123.vision;

// Imports the Google Cloud client library

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
 * reference
 * 
 * https://cloud.google.com/vision/docs/quickstart-client-libraries#client-libraries-install-java
 * 
 * https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/vision/cloud-client/src/main/java/com/example/vision/QuickstartSample.java
 *
 */
public class QuickstartSample {
	
  public static void main(String... args) throws Exception {
	  
    // Instantiates a client
    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

      // The path to the image file to annotate
//      String fileName = "./resources/wakeupcat.jpg";
      String fileName = 
//    		  		"S:/xfer/Vision testing/unsorted/"
		  			"S:/xfer/Vision testing/"
//		    					+ "capture_vid2 - yard sale 002.jpg";
//      						+ "capture_vid2 - car, front right.jpg";
    		  					+ "D035-v2 - car - plate.jpg";

      // Reads the image file into memory
      Path path = Paths.get(fileName);
      byte[] data = Files.readAllBytes(path);
      ByteString imgBytes = ByteString.copyFrom(data);

      
      // Builds the image annotation request
      List<AnnotateImageRequest> requests = new ArrayList<>();
      
      Image img = Image.newBuilder().setContent(imgBytes).build();
      
      Feature feat1 = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
      Feature feat2 = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
//      Feature feat3 = Feature.newBuilder().setType(Type.LOGO_DETECTION).build();
      Feature feat4 = Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION).build();
      
      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
							              .addFeatures(feat1)
							              .addFeatures(feat2)
//							              .addFeatures(feat3)
							              .addFeatures(feat4)
							              .setImage(img)
							              .build();
      requests.add(request);

      // Performs label detection on the image file
      BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
    	  System.out.println( "\n\nres: vvvvvvvvvvv\n" + res.toString() + "\n^^^^^^^^^^^\n" );
        if (res.hasError()) {
          System.out.printf("Error: %s\n", res.getError().getMessage());
//          return;
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
  }
}