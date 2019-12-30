package com.unloadbrain.assignement.takeaway.kpipublisher.util;


import com.unloadbrain.assignement.takeaway.common.util.MessageDeserializer;
import com.unloadbrain.assignement.takeaway.kpipublisher.dto.message.DataExtractionNotificationMessage;

/**
 * DataExtractionNotificationMessageDeserializer Apache Kafka message deserializer class.
 */
public class DataExtractionNotificationMessageDeserializer extends MessageDeserializer<DataExtractionNotificationMessage> {

    public DataExtractionNotificationMessageDeserializer() {
        super(DataExtractionNotificationMessage.class);
    }
}