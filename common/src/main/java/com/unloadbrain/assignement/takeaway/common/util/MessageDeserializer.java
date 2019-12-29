package com.unloadbrain.assignement.takeaway.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * Abstract sensor data Apache Kafka message deserializer class.
 */
@Slf4j
public abstract class MessageDeserializer<T> implements Deserializer<T> {

    protected Class<T> clazz;
    private ObjectMapper mapper = new ObjectMapper();

    public MessageDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {

        T obj = null;
        try {
            obj = mapper.readValue(data, clazz);
        } catch (Exception ex) {
            log.error("Could not deserialize string to object {}", obj, ex);
        }

        return obj;
    }

    @Override
    public void close() {
    }
}