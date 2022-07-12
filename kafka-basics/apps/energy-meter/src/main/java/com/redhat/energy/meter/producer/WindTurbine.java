package com.redhat.energy.meter.producer;

import com.redhat.energy.meter.common.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;

public class WindTurbine extends Config {
    private static final int[] energyProductionSequence = {300, 400, 500, 600, 700};

    private static void printRecord(ProducerRecord<Void, Integer> record) {
        System.out.println("Sent record:");
        System.out.println("\tTopic = " + record.topic());
        System.out.println("\tPartition = " + record.partition());
        System.out.println("\tKey = " + record.key());
        System.out.println("\tValue = " + record.value());
    }

    private static Properties configureProperties() {
        Properties props = new Properties();

        configureProducer(props);
        configureConnectionSecurity(props);

        return props;
    }

    private static void configureProducer(Properties props) {
        // TODO: configure the bootstrap server
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-cluster-kafka-bootstrap-vjuvzs-kafka-cluster.apps.na46a.prod.ole.redhat.com:443");
        // TODO: configure the key serializer
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        // TODO: configure the value serializer
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerSerializer");        
    }

    public static void main(String[] args) {
        // TODO: implement the business logic
        Producer<Void,Integer> producer = new KafkaProducer<>(configureProperties());

        for(int energyProduction : energyProductionSequence){
            ProducerRecord<Void,Integer> record = new ProducerRecord<Void,Integer>("wind-turbine-production", energyProduction);

            producer.send(record);
            printRecord(record);

        }
        producer.close();
    }
}
