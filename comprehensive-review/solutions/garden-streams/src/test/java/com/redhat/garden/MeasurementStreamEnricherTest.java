package com.redhat.garden;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.garden.entities.Sensor;
import com.redhat.garden.entities.SensorMeasurement;
import com.redhat.garden.entities.SensorMeasurementEnriched;
import com.redhat.garden.entities.SensorMeasurementType;

import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;


public class MeasurementStreamEnricherTest {

    TopologyTestDriver testDriver;

    TestInputTopic<Integer, Sensor> sensorsTopic;
    ObjectMapperSerde<Sensor> sensorSerde;

    TestInputTopic<Integer, SensorMeasurement> sensorMeasurementsTopic;
    ObjectMapperSerde<SensorMeasurement> sensorMeasurementSerde;

    TestOutputTopic<Integer, SensorMeasurementEnriched> enrichedMeasurementsTopic;
    ObjectMapperSerde<SensorMeasurementEnriched> enrichedMeasurementSerde;

    @BeforeEach
    public void setup() {
        MeasurementStreamEnricher topologyBuilder = new MeasurementStreamEnricher();
        testDriver = new TopologyTestDriver(topologyBuilder.build());

        sensorSerde = new ObjectMapperSerde<>(Sensor.class);
        sensorsTopic = testDriver.createInputTopic(
                    MeasurementStreamEnricher.SENSORS_TOPIC,
                    new IntegerSerializer(),
                    sensorSerde.serializer());

        sensorMeasurementSerde = new ObjectMapperSerde<>(SensorMeasurement.class);
        sensorMeasurementsTopic = testDriver.createInputTopic(
                    MeasurementStreamEnricher.SENSOR_MEASUREMENTS_TOPIC,
                    new IntegerSerializer(),
                    sensorMeasurementSerde.serializer());

        enrichedMeasurementSerde = new ObjectMapperSerde<>(SensorMeasurementEnriched.class);
        enrichedMeasurementsTopic = testDriver.createOutputTopic(
                    RulesProcessor.ENRICHED_SENSOR_MEASUREMENTS_TOPIC,
                    new IntegerDeserializer(),
                    enrichedMeasurementSerde.deserializer());
    }

    @AfterEach
    public void teardown() {
        testDriver.close();
        sensorMeasurementSerde.close();
        enrichedMeasurementSerde.close();
    }

    @Test
    public void testWritesEnrichedStreamToTopic() {
        // Given
        Sensor sensor = new Sensor(1, "Sensor 1", "Garden 1");
        SensorMeasurement measurement = new SensorMeasurement(1, SensorMeasurementType.TEMPERATURE, 4.5, 10L);

        // When
        sensorsTopic.pipeInput(sensor.id, sensor);
        sensorMeasurementsTopic.pipeInput(measurement.sensorId, measurement);

        // Then
        assertFalse(enrichedMeasurementsTopic.isEmpty());
    }

    @Test
    public void testEnrichedMeasurementIncludeGardenName() {
        // Given
        Sensor sensor = new Sensor(1, "Sensor 1", "Garden 1");
        SensorMeasurement measurement = new SensorMeasurement(1, SensorMeasurementType.TEMPERATURE, 4.5, 10L);

        // When
        sensorsTopic.pipeInput(sensor.id, sensor);
        sensorMeasurementsTopic.pipeInput(measurement.sensorId, measurement);

        // Then
        TestRecord<Integer, SensorMeasurementEnriched> record = enrichedMeasurementsTopic.readRecord();
        SensorMeasurementEnriched event = record.getValue();
        assertEquals("Garden 1", event.gardenName);
    }

    @Test
    public void testEnrichedMeasurementIncludeMeasurementType() {
        // Given
        Sensor sensor = new Sensor(1, "Sensor 1", "Garden 1");
        SensorMeasurement measurement = new SensorMeasurement(1, SensorMeasurementType.TEMPERATURE, 4.5, 10L);

        // When
        sensorsTopic.pipeInput(sensor.id, sensor);
        sensorMeasurementsTopic.pipeInput(measurement.sensorId, measurement);

        // Then
        TestRecord<Integer, SensorMeasurementEnriched> record = enrichedMeasurementsTopic.readRecord();
        SensorMeasurementEnriched event = record.getValue();
        assertEquals(SensorMeasurementType.TEMPERATURE, event.type);
    }

    @Test
    public void testEnrichedMeasurementIncludeValue() {
        // Given
        Sensor sensor = new Sensor(1, "Sensor 1", "Garden 1");
        SensorMeasurement measurement = new SensorMeasurement(1, SensorMeasurementType.TEMPERATURE, 4.5, 10L);

        // When
        sensorsTopic.pipeInput(sensor.id, sensor);
        sensorMeasurementsTopic.pipeInput(measurement.sensorId, measurement);

        // Then
        TestRecord<Integer, SensorMeasurementEnriched> record = enrichedMeasurementsTopic.readRecord();
        SensorMeasurementEnriched event = record.getValue();
        assertEquals(4.5, event.value);
    }

}
