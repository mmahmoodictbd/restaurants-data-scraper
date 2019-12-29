package com.unloadbrain.assignement.takeaway.scraper.dto.message;

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
public class ScrapedDataMessage {

    private String url;
    private String type;
    private String html;
    private long unixTimestamp;
    private Map<String, String> meta;
}
