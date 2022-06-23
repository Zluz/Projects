package jmr.pr153;

import java.awt.Image;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
	
	private KEF() {
		bActive = true;
		properties = S2Properties.get();
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
				new MessageConsumerListener( strTopic, strServers, "test" );
		consumer.addListener( strMessage -> {

        	System.out.print( Instant.now().toString() );
        	System.out.print( ": " );

			try {
				final JsonNode jn = MAPPER.readTree( strMessage );
	        	System.out.print( jn.toString() );
	        	
			} catch ( final IOException e ) {
				System.out.println( "EXCEPTION" );
				e.printStackTrace();
			}
			
        	System.out.println();
		});
		consumer.start();
	}
	
	
	public Image getLatestMotionImage() {
		return null;
	}
	
	

	public static void main( final String[] args ) throws Exception {
		KEF.get();
		TimeUnit.DAYS.sleep( 100 );
	}

}
