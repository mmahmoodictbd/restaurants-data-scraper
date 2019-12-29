package com.unloadbrain.assignement.takeaway.scraper.util;


import com.unloadbrain.assignement.takeaway.common.util.MessageDeserializer;
import com.unloadbrain.assignement.takeaway.scraper.dto.message.DataSourceMessage;

/**
 * Datasource Apache Kafka message deserializer class.
 */
public class DataSourceMessageDeserializer extends MessageDeserializer<DataSourceMessage> {

    public DataSourceMessageDeserializer() {
        super(DataSourceMessage.class);
    }
}