package jmr.pr152.examples;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
//import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaTest_001 {

	public final static Logger LOGGER = 
//			Logger.getLogger( KafkaTest_001.class.getName() );
			LoggerFactory.getLogger( KafkaTest_001.class );

	
	public static Properties createProperties( final String strGroupName ) {

//      String bootstrapServers="127.0.0.1:9092";
      String bootstrapServers="***";
//      String grp_id="third_app";
//      String grp_id="client-test";
      String topic="quickstart-events";
      
      //Creating consumer properties
      final Properties props = new Properties();
      props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
						bootstrapServers);
//      props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   
//      				StringDeserializer.class.getName());
//      				IntegerDeserializer.class.getName());
//						LongDeserializer.class.getName());
//						ByteArrayDeserializer.class.getName());
//      props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
//						StringDeserializer.class.getName());
//						ByteArrayDeserializer.class.getName());
      props.put( ConsumerConfig.GROUP_ID_CONFIG,
    		  			strGroupName );
      props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
//						"earliest" );
						"latest" );
      
//      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
//      props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
//      props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
      
      props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1 );
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
//      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest" );

//      props.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
      
      return props;
	}
	
	public static void subscribe( final Consumer consumer ) {

        final Map<String, List<PartitionInfo>> map = consumer.listTopics();

        //Subscribing
//        consumer.subscribe( Arrays.asList(topic) );
        final String strFirstTopic = map.keySet().iterator().next();
        LOGGER.info( "strFirstTopic: " + strFirstTopic );
        consumer.subscribe( Arrays.asList( strFirstTopic ) );
	}
	
	
	public static void startBytes() {
		final String strName = "Bytes-Bytes";
    	final Properties props = createProperties( strName );
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   
				ByteArrayDeserializer.class.getName());
    	props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				ByteArrayDeserializer.class.getName());
    	
        final Consumer<byte[],byte[]> consumer = 
								new KafkaConsumer<>( props );
        subscribe( consumer );
        
        new Thread( ()-> {
        	Thread.currentThread().setName( strName );
        	LOGGER.info( "Monitoring " + strName );
        	for (;;) {
                final ConsumerRecords<byte[],byte[]> records = 
                					consumer.poll( Duration.ofMillis(100) );
                
                for( final ConsumerRecord<?,?> record: records ){
                    System.out.println( "Record: " + record );
                    LOGGER.info( "Key: "+ record.key() + ", "
                    			+ "Value:" +record.value());
                    LOGGER.info( "Partition:" + record.partition()+", "
                    			+ "Offset:"+record.offset());
                }
        	}
        } ).start();
	}
	

	public static void startStringString() {
		final String strName = "String-String";
    	final Properties props = createProperties( strName );
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   
				StringDeserializer.class.getName());
    	props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class.getName());
    	
        final Consumer<String,String> consumer = 
								new KafkaConsumer<>( props );
        subscribe( consumer );
        
        new Thread( ()-> {
        	Thread.currentThread().setName( strName );
        	LOGGER.info( "Monitoring " + strName );
        	for (;;) {
              final ConsumerRecords<String,String> records = 
                					consumer.poll( Duration.ofMillis(100) );
                
                for( final ConsumerRecord<?,?> record: records ){
                	
                	final String strValue = record.value().toString();
                	System.out.print( Instant.now().toString() );
                	System.out.print( ": " + strValue );
                	System.out.println();
                	
//                    System.out.println( "Record: " + record );
//                    LOGGER.info( "Key: "+ record.key() + ", "
//                    			+ "Value:" +record.value());
//                    LOGGER.info( "Partition:" + record.partition()+", "
//                    			+ "Offset:"+record.offset());
                }
        	}
        } ).start();
	}

	public static void startIntegerString() {
		final String strName = "Integer-String";
    	final Properties props = createProperties( strName );
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   
				IntegerDeserializer.class.getName());
    	props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class.getName());
    	
        final Consumer<Integer,String> consumer = 
								new KafkaConsumer<>( props );
        subscribe( consumer );
        
        new Thread( ()-> {
        	Thread.currentThread().setName( strName );
        	LOGGER.info( "Monitoring " + strName );
        	for (;;) {
              final ConsumerRecords<Integer,String> records = 
                					consumer.poll( Duration.ofMillis(100) );
                
                for( final ConsumerRecord<?,?> record: records ){
                    System.out.println( "Record: " + record );
                    LOGGER.info( "Key: "+ record.key() + ", "
                    			+ "Value:" +record.value());
                    LOGGER.info( "Partition:" + record.partition()+", "
                    			+ "Offset:"+record.offset());
                }
        	}
        } ).start();
	}
	
	
	public static void main( final String[] args ) throws Exception {
		LOGGER.info( "Starting.." );
    	
//    	startBytes();
//    	startIntegerString();
    	startStringString();
    	
    	Thread.sleep( Integer.MAX_VALUE );
	}

}
