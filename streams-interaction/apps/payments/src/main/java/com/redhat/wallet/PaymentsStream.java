package com.redhat.wallet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.protocol.types.Field.Str;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;


@ApplicationScoped
public class PaymentsStream {

    // Deserializer for message keys.
    private final Serde<String> keySerde = Serdes.String();

    // Serializer for message values
    private final Serde<Integer> valueSerde = Serdes.Integer();

    @Produces
    public Topology buildTopology() {
        // TODO: Create the stream from the "payments" topic
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Integer> stream = builder.stream("payments", Consumed.with(keySerde, valueSerde));
        // TODO: use foreach to print each message
        stream.foreach((key, value) -> System.out.println("Received payment: " + value));
        // TODO: process the stream and send the result to the "large-payments" topic
        stream.filter((key, value) -> value > 1000).to("large-payments", Produced.with(keySerde, valueSerde));
        // TODO: return the topology
        return builder.build();
        stream.map(mapper)
    }
}
