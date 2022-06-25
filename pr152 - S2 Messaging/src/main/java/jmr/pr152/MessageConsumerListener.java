package jmr.pr152;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageConsumerListener {

	public final static Logger LOGGER = 
			LoggerFactory.getLogger( MessageConsumerListener.class );
	
	public static final ObjectMapper MAPPER = new ObjectMapper();

	public static interface Listener {
		void event( final String strContent,
					final JsonNode jnContent );
	}
	
	private final String strTopic;
	private final String strServers;
	private final String strClientName;
    private Consumer<String,String> consumer;
    private boolean bActive = false;
    
    private final List<Listener> listListeners = new LinkedList<>();
    
	public MessageConsumerListener( final String strTopic,
									final String strServers,
									final String strClientName ) {
		this.strTopic = strTopic;
		this.strServers = strServers;
		this.strClientName = strClientName;
	}

	public MessageConsumerListener( final String strTopic,
									final String strServers ) {
		this( strTopic, strServers, UUID.randomUUID().toString() );
	}
	
	
	public void addListener( final Listener listener ) {
		this.listListeners.add( listener );
	}
	
	
	public Properties createProperties() {
		final Properties props = new Properties();
		props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, strServers );
		props.put( ConsumerConfig.GROUP_ID_CONFIG, strClientName );
		props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest" );
      
		props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   
									StringDeserializer.class.getName());
		props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
									StringDeserializer.class.getName());

		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1 );
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

//      props.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
		return props;
	}
	
	private void processRecord( final ConsumerRecord<?,?> record ) {
		if ( listListeners.isEmpty() ) return;

    	System.out.print( Instant.now().toString() );
    	System.out.print( ": " + StringUtils.abbreviate( record.toString(), 100 ) );

    	final String strValue = record.value().toString();
//    	System.out.print( ": " + strValue );
    	System.out.println();
    	
		try {
			final JsonNode jn = MAPPER.readTree( strValue );
			if ( null != jn && ! jn.isNull() ) {
	        	for ( final Listener listener : listListeners ) {
	        		new Thread( ()-> listener.event( strValue, jn ) ).start();
	        	}
			}
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		final Properties props = createProperties();
		this.consumer = new KafkaConsumer<>( props );
        consumer.subscribe( Arrays.asList( strTopic ) );
        
        bActive = true;
        new Thread( ()-> {
        	Thread.currentThread().setName( "Kafka listener - " + strTopic );
        	while ( bActive ) {
                final ConsumerRecords<String,String> records = 
            					consumer.poll( Duration.ofMillis( 200 ) );
                
                for( final ConsumerRecord<?,?> record: records ){
                	processRecord( record );
                }
        	}
        } ).start();
	}
	
	
}
