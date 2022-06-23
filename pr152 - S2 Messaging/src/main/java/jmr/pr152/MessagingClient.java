package jmr.pr152;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingClient {

	public final static Logger LOGGER = 
			LoggerFactory.getLogger( MessagingClient.class );

	public final static String KAFKA_SERVER = "192.168.6.20:9092";
	public final static String TOPIC_HISTORY_FILES = "history-file"; 

	public static void main( final String[] args ) throws Exception {
		LOGGER.info( "Starting.." );
		
		final MessageConsumerListener 
				consumer = new MessageConsumerListener(
						TOPIC_HISTORY_FILES, KAFKA_SERVER, "test" );
		consumer.addListener( strMessage -> {
        	System.out.print( Instant.now().toString() );
        	System.out.print( ": " + strMessage );
        	System.out.println();
		});
		consumer.start();
		
    	Thread.sleep( Integer.MAX_VALUE );
		
		// https://kafka.apache.org/32/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html
		// https://kafka.apache.org/32/javadoc/org/apache/kafka/clients/consumer/package-summary.html
		
	}

}
