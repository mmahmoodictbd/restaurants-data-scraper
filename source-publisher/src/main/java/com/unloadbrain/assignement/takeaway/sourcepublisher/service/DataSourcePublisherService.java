package com.unloadbrain.assignement.takeaway.sourcepublisher.service;

import com.unloadbrain.assignement.takeaway.common.exception.KafkaRecordProducerException;
import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import com.unloadbrain.assignement.takeaway.sourcepublisher.config.SourcesProperties;
import com.unloadbrain.assignement.takeaway.sourcepublisher.dto.message.DataSourceMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@AllArgsConstructor
public class DataSourcePublisherService {

    private final String TOPIC = "SCRAPER_DATASOURCE";

    private final KafkaTemplate kafkaTemplate;
    private final SourcesProperties sourcesProperties;
    private final DateTimeUtil dateTimeUtil;

    public void publishSourcesToKafka() {

        for (SourcesProperties.Source source : sourcesProperties.getSources()) {

            DataSourceMessage dataSourceMessage = DataSourceMessage.builder()
                    .url(source.getUrl())
                    .type(source.getType())
                    .meta(source.getMeta())
                    .unixTimestamp(dateTimeUtil.getCurrentTimeEpochMilli())
                    .build();

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, dataSourceMessage);
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("Source data successfully sent to Kafka with offset = {}",
                            result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("Source data could not be sent to Kafka.", ex);
                    // TODO: Read this exception in global level and public error to Prometheus.
                    throw new KafkaRecordProducerException("Source data could not be sent to Kafka.");
                }
            });

            try {
                // Blocking call to return success status.
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                log.error("Apache Kafka ListenableFuture get() exception.", ex);
            }
        }
    }
}
