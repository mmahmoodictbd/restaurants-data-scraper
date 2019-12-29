package com.unloadbrain.assignement.takeaway.dataextractor.service;

import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import com.unloadbrain.assignement.takeaway.dataextractor.domain.model.ExtractedData;
import com.unloadbrain.assignement.takeaway.dataextractor.domain.repository.ExtractedDataRepository;
import com.unloadbrain.assignement.takeaway.dataextractor.dto.message.DataExtractionNotificationMessage;
import com.unloadbrain.assignement.takeaway.dataextractor.dto.message.ScrapedDataMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class DataExtractionService {

    private final List<DataProcessor> dataProcessorList;
    private final DataExtractionNotificationPublisherService dataExtractionNotificationPublisherService;
    private final ExtractedDataRepository extractedDataRepository;
    private final DateTimeUtil dateTimeUtil;

    @KafkaListener(topics = "SCRAPED_DATA", groupId = "data-extractor")
    public void extract(ScrapedDataMessage scrapedDataMessage) {

        log.info("Received message {} SCRAPED_DATA topic for group 'data-extractor'", scrapedDataMessage.getUrl());

        Map<String, Object> extractedDataMap = new HashMap<>();
        for (DataProcessor<ScrapedDataMessage, Map<String, Object>> dataProcessor : dataProcessorList) {
            extractedDataMap.putAll(dataProcessor.processor(scrapedDataMessage));
        }

        ExtractedData extractedData;
        Optional<ExtractedData> extractedDataOptional = extractedDataRepository.findByUrl(scrapedDataMessage.getUrl());
        if (extractedDataOptional.isPresent()) {
            extractedData = extractedDataOptional.get();
        } else {
            extractedData = new ExtractedData();
            extractedData.setUrl(scrapedDataMessage.getUrl());
        }

        extractedData.setExtractedData(extractedDataMap);
        extractedDataRepository.save(extractedData);

        DataExtractionNotificationMessage dataExtractionNotificationMessage = DataExtractionNotificationMessage.builder()
                .url(scrapedDataMessage.getUrl())
                .dataExtractionNotificationTopic(scrapedDataMessage.getType())
                .unixTimestamp(dateTimeUtil.getCurrentTimeEpochMilli())
                .meta(scrapedDataMessage.getMeta())
                .build();
        dataExtractionNotificationPublisherService.publishDataExtractionNotificationToKafka(dataExtractionNotificationMessage);
    }
}
