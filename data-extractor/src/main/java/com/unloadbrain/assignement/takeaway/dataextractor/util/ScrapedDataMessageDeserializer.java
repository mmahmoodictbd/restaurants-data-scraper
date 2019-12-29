package com.unloadbrain.assignement.takeaway.dataextractor.util;


import com.unloadbrain.assignement.takeaway.common.util.MessageDeserializer;
import com.unloadbrain.assignement.takeaway.dataextractor.dto.message.ScrapedDataMessage;

/**
 * ScrapedDataMessage Apache Kafka message deserializer class.
 */
public class ScrapedDataMessageDeserializer extends MessageDeserializer<ScrapedDataMessage> {

    public ScrapedDataMessageDeserializer() {
        super(ScrapedDataMessage.class);
    }
}