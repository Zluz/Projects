package jmr.pr153;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jmr.S2Properties;
import jmr.pr152.MessageConsumerListener;

/**
 * Interface combining Kafka, Elasticsearch, and Filesystem resources.
 */
public class KEF {

	public static final ObjectMapper MAPPER = new ObjectMapper();

	private static KEF instance;
	
	private boolean bActive;
	
	private Properties properties;
	
	private Display display;
	
	private KEF() {
		bActive = true;
		properties = S2Properties.get();
		display = Display.getDefault();
		startMotionListener();
	}
	
	public static synchronized KEF get() {
		if ( null == instance ) {
			instance = new KEF();
		}
		return instance;
	}
	
	
	
	private void startMotionListener() {
		
		final String strServers = properties.getProperty( "kafka.servers" );
		final String strTopic = properties.getProperty( "kafka.topic.history-file" );

		final MessageConsumerListener consumer = 
				new MessageConsumerListener( strTopic, strServers );
		consumer.addListener( ( strMessage, jn ) -> {

        	System.out.print( Instant.now().toString() );
        	System.out.print( ": " );

        	System.out.print( jn.toString() );
        	
        	final String strFilename = jn.at( "/dir-target" ).asText() 
        			+ jn.at("/filename" ).asText();
        	if ( strFilename.endsWith( ".jpg" ) ) {
        		final File file = new File( strFilename );
        		if ( file.exists() ) {
        			
        			final Image fileImageNew =
        					new Image( display, file.getAbsolutePath() );

        			if ( null != imageLatestMotion ) {
        				imageLatestMotion.dispose();
        			}
        			imageLatestMotion = fileImageNew;
        		}
        	}
	        	
        	System.out.println();
		});
		consumer.start();
	}
	
	
	private Image imageLatestMotion = null;
	
	
	public Image getLatestMotionImage() {
		return imageLatestMotion;
	}
	
	

	public static void main( final String[] args ) throws Exception {
		KEF.get();
		TimeUnit.DAYS.sleep( 100 );
	}

}
