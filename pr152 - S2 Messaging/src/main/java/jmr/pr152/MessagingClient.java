package jmr.pr152;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmr.S2Properties;

public class MessagingClient {

	public final static Logger LOGGER = 
			LoggerFactory.getLogger( MessagingClient.class );


	public static void main( final String[] args ) throws Exception {
		LOGGER.info( "Starting.." );
		
		final Properties p = S2Properties.get();
		final String strServers = p.getProperty( "kafka.servers" );
		final String strTopic = p.getProperty( "kafka.topic.history-file" );
		final String strGroupId = UUID.randomUUID().toString();
		
		final MessageConsumerListener 
				consumer = new MessageConsumerListener(
						strTopic, strServers, strGroupId );
		consumer.addListener( ( strMessage, jn ) -> {
        	System.out.print( Instant.now().toString() );
        	System.out.print( ": " + jn.toString() );
        	System.out.println();
		});
		consumer.start();
		
    	Thread.sleep( Integer.MAX_VALUE );
		
		// https://kafka.apache.org/32/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html
		// https://kafka.apache.org/32/javadoc/org/apache/kafka/clients/consumer/package-summary.html
		
	}

}
