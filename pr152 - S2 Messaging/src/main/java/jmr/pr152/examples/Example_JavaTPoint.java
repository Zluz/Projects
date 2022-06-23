package jmr.pr152.examples;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

// see:
//	https://www.javatpoint.com/creating-kafka-consumer-in-java
public class Example_JavaTPoint {
	
    public static void main(String[] args) {
    	System.out.println( "Program started." );
//        Logger logger= LoggerFactory.getLogger(consumer1.class.getName());
    	final Logger logger = Logger.getLogger( 
    							Example_JavaTPoint.class.getName() );
    	
//        String bootstrapServers="127.0.0.1:9092";
        String bootstrapServers="192.168.6.20:9092";
//        String grp_id="third_app";
        String grp_id="client-test";
        String topic="quickstart-events";
        
        //Creating consumer properties
        final Properties props = new Properties();
        props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
						bootstrapServers);
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   
//        				StringDeserializer.class.getName());
//        				IntegerDeserializer.class.getName());
//						LongDeserializer.class.getName());
						ByteArrayDeserializer.class.getName());
        props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
//						StringDeserializer.class.getName());
						ByteArrayDeserializer.class.getName());
        props.put( ConsumerConfig.GROUP_ID_CONFIG,
						grp_id);
        props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
//						"earliest" );
						"latest" );
//    					"from-beginning" ); // not valid
        
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
//        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1 );
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest" );

        
        //creating consumer
//        final KafkaConsumer<String,String> consumer = 
//        final Consumer<String,String> consumer = 
//        final KafkaConsumer<Long,String> consumer = 
//        final KafkaConsumer<Object,Object> consumer = 
//        final KafkaConsumer<Long,String> consumer = 
        final Consumer<byte[],byte[]> consumer = 
								new KafkaConsumer<>( props );

        final Map<String, List<PartitionInfo>> map = consumer.listTopics();
//        System.out.println( "topics: " + map );

        //Subscribing
//        consumer.subscribe( Arrays.asList(topic) );
        final String strFirstTopic = map.keySet().iterator().next();
        logger.info( "strFirstTopic: " + strFirstTopic );
        consumer.subscribe( Arrays.asList( strFirstTopic ) );

//        consumer.
//        consumer.wakeup();
//        consumer.
        
        //polling
        System.out.println( "Polling.." );
        while (true) {
        	
//            final ConsumerRecords<String,String> records = 
            final ConsumerRecords<byte[],byte[]> records = 
            					consumer.poll( Duration.ofMillis(100) );
            
            for( final ConsumerRecord<?,?> record: records ){
            	
                logger.info( "Key: "+ record.key() + ", "
                			+ "Value:" +record.value());
                logger.info( "Partition:" + record.partition()+", "
                			+ "Offset:"+record.offset());
            }


        }
    }

}
