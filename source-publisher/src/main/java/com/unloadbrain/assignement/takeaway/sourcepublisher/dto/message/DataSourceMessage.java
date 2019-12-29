package com.unloadbrain.assignement.takeaway.sourcepublisher.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Message DTO class to put data to Apache Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceMessage {

    private String url;
    private String type;
    private long unixTimestamp;
    private Map<String, String> meta;
}
