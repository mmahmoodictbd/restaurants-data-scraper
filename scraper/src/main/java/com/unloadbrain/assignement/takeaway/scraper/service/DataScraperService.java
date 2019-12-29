package com.unloadbrain.assignement.takeaway.scraper.service;

import com.unloadbrain.assignement.takeaway.common.exception.KafkaRecordProducerException;
import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import com.unloadbrain.assignement.takeaway.scraper.dto.message.DataSourceMessage;
import com.unloadbrain.assignement.takeaway.scraper.dto.message.ScrapedDataMessage;
import com.unloadbrain.assignement.takeaway.scraper.util.JsoupUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Service
@AllArgsConstructor
public class DataScraperService {

    private final int TIMEOUT = 60_000;
    private final String TOPIC = "SCRAPED_DATA";

    private final KafkaTemplate kafkaTemplate;
    private final DateTimeUtil dateTimeUtil;
    private final JsoupUtil jsoupUtil;

    @KafkaListener(topics = "SCRAPER_DATASOURCE", groupId = "scraper")
    public void scrapeAndPublishToKafka(DataSourceMessage dataSourceMessage) {

        log.info("Received message {} DataSource topic from group 'scraper'", dataSourceMessage);

        Document doc = jsoupUtil.fetch(dataSourceMessage.getUrl(), TIMEOUT);

        ScrapedDataMessage scrapedDataMessage = ScrapedDataMessage.builder()
                .url(dataSourceMessage.getUrl())
                .type(dataSourceMessage.getType())
                .html(doc.html())
                .unixTimestamp(dateTimeUtil.getCurrentTimeEpochMilli())
                .meta(dataSourceMessage.getMeta())
                .build();

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, scrapedDataMessage);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("ScrapedDataMessage successfully sent to Kafka with offset = {}",
                        result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("ScrapedDataMessage could not be sent to Kafka.", ex);
                // TODO: Read this exception in global level and public error to Prometheus.
                throw new KafkaRecordProducerException("ScrapedDataMessage could not be sent to Kafka.");
            }
        });
    }
}
