package com.redhat.energy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.redhat.energy.records.MWattsMeasurement;
import com.redhat.energy.records.WindTurbine;
import com.redhat.energy.records.WindTurbineStats;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class StreamTopologyBuilder {

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // TODO: Create wind turbine serde
        ObjectMapperSerde<WindTurbine> turbineSerde = new ObjectMapperSerde<>(WindTurbine.class);
        // TODO: read the "turbines" topic as a KTable
        builder.table(
            "turbines",
            Consumed.with(Serdes.Integer(), turbineSerde),
            Materialized
                .<Integer, WindTurbine, KeyValueStore<Bytes, byte[]>>as("turbines-store")
                .withKeySerde(Serdes.Integer())
                .withValueSerde(turbineSerde)
        );
        // TODO: read the "turbine-generated-watts" topic as a KStream
        KStream<Integer, Integer> wattsValuesStream = builder.stream("turbine-generated-watts", 
        Consumed.with(Serdes.Integer(),Serdes.Integer()));
        // TODO: Create MWattsMeasurement serde
        ObjectMapperSerde<MWattsMeasurement> mwattsMeasurementSerde = new ObjectMapperSerde<>(MWattsMeasurement.class);
        // TODO: map the watts stream into a new mwatts stream
        wattsValuesStream.map((turbineId, watts) -> {
            Double megawatts = (double) watts / 1000000;
            MWattsMeasurement measurement = new MWattsMeasurement(turbineId, megawatts);
            System.out.println(
                "MAP - Turbine: " + turbineId +
                " | " + watts + " Watts -> " + megawatts + " MWatts"
            );
            return KeyValue.pair(turbineId, measurement);
        }).to(
            "turbine-generated-mwatts",
            Produced.with(Serdes.Integer(), mwattsMeasurementSerde)
        );
        // TODO: Create WindTurbineStats serde
        ObjectMapperSerde<WindTurbineStats> statsSerde = new ObjectMapperSerde<>(WindTurbineStats.class);
        // TODO: count measurements by turbine and write results to a new stream
        wattsValuesStream
        .groupByKey()
        .count()
        .toStream()
        .map((turbineId, count) -> {
            WindTurbineStats stats = new WindTurbineStats(turbineId, count);
            System.out.println("COUNT - Turbine: " + turbineId + " | Count:" + count);
            return KeyValue.pair(turbineId, stats);
        })
        .to(
            "turbine-stats",
            Produced.with(Serdes.Integer(), statsSerde)
        );
        return builder.build();
    }

}
