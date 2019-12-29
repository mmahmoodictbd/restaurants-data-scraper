package com.unloadbrain.assignement.takeaway.dataextractor.service;

import com.unloadbrain.assignement.takeaway.common.exception.KafkaRecordProducerException;
import com.unloadbrain.assignement.takeaway.dataextractor.dto.message.DataExtractionNotificationMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Service
@AllArgsConstructor
public class DataExtractionNotificationPublisherService {

    private final KafkaTemplate kafkaTemplate;

    public void publishDataExtractionNotificationToKafka(DataExtractionNotificationMessage dataExtractionNotificationMessage) {

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                dataExtractionNotificationMessage.getDataExtractionNotificationTopic(),
                dataExtractionNotificationMessage);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("DataExtractionNotificationMessage successfully sent to Kafka with offset = {}",
                        result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("DataExtractionNotificationMessage could not be sent to Kafka.", ex);
                // TODO: Read this exception in global level and public error to Prometheus.
                throw new KafkaRecordProducerException("DataExtractionNotificationMessage could not be sent to Kafka.");
            }
        });
    }
}
