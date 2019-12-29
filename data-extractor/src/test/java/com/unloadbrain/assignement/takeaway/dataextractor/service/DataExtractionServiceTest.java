package com.unloadbrain.assignement.takeaway.dataextractor.service;

import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import com.unloadbrain.assignement.takeaway.dataextractor.domain.model.ExtractedData;
import com.unloadbrain.assignement.takeaway.dataextractor.domain.repository.ExtractedDataRepository;
import com.unloadbrain.assignement.takeaway.dataextractor.dto.message.ScrapedDataMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataExtractionServiceTest {

    private DataProcessor dataProcessor1;
    private DataProcessor dataProcessor2;
    private DataExtractionNotificationPublisherService dataExtractionNotificationPublisherService;
    private ExtractedDataRepository extractedDataRepository;
    private DateTimeUtil dateTimeUtil;

    private DataExtractionService dataExtractionService;

    @Before
    public void setUp() {

        this.dataProcessor1 = mock(DataProcessor.class);
        this.dataProcessor2 = mock(DataProcessor.class);
        this.dataExtractionNotificationPublisherService = mock(DataExtractionNotificationPublisherService.class);
        this.extractedDataRepository = mock(ExtractedDataRepository.class);
        this.dateTimeUtil = mock(DateTimeUtil.class);
        this.dataExtractionService = new DataExtractionService(Arrays.asList(dataProcessor1, dataProcessor2),
                dataExtractionNotificationPublisherService,
                extractedDataRepository, dateTimeUtil);
    }

    @Test
    public void shouldPersistExistingExtractedDataFromProcessors() {

        // Given

        ArgumentCaptor<ExtractedData> extractedDataArgumentCaptor = ArgumentCaptor.forClass(ExtractedData.class);

        ScrapedDataMessage scrapedDataMessage = ScrapedDataMessage.builder()
                .url("url")
                .type("type")
                .html("<html><body><p>Hello!</p></body></html>")
                .unixTimestamp(1563142796L)
                .meta(Collections.singletonMap("key1", "value1"))
                .build();

        when(dataProcessor1.processor(any(ScrapedDataMessage.class)))
                .thenReturn(Collections.singletonMap("key1", "value1"));
        when(dataProcessor2.processor(any(ScrapedDataMessage.class)))
                .thenReturn(Collections.singletonMap("key2", "value2"));

        ExtractedData extractedData = ExtractedData.builder()
                .url(scrapedDataMessage.getUrl())
                .extractedData(Collections.singletonMap("key0", "value0"))
                .build();
        when(extractedDataRepository.findByUrl(scrapedDataMessage.getUrl())).thenReturn(Optional.of(extractedData));

        when(dateTimeUtil.getCurrentTimeEpochMilli()).thenReturn(1563142796L);

        // When
        dataExtractionService.extract(scrapedDataMessage);

        // Then

        verify(extractedDataRepository, times(1)).save(extractedDataArgumentCaptor.capture());

        assertEquals("url", extractedDataArgumentCaptor.getValue().getUrl());
        assertEquals("value1", extractedDataArgumentCaptor.getValue().getExtractedData().get("key1"));
        assertEquals("value2", extractedDataArgumentCaptor.getValue().getExtractedData().get("key2"));
    }

    @Test
    public void shouldPersistNewExtractedDataFromProcessors() {

        // Given

        ArgumentCaptor<ExtractedData> extractedDataArgumentCaptor = ArgumentCaptor.forClass(ExtractedData.class);

        ScrapedDataMessage scrapedDataMessage = ScrapedDataMessage.builder()
                .url("url")
                .type("type")
                .html("<html><body><p>Hello!</p></body></html>")
                .unixTimestamp(1563142796L)
                .meta(Collections.singletonMap("key1", "value1"))
                .build();

        when(dataProcessor1.processor(any(ScrapedDataMessage.class)))
                .thenReturn(Collections.singletonMap("key1", "value1"));
        when(dataProcessor2.processor(any(ScrapedDataMessage.class)))
                .thenReturn(Collections.singletonMap("key2", "value2"));

        when(extractedDataRepository.findByUrl(scrapedDataMessage.getUrl())).thenReturn(Optional.empty());

        when(dateTimeUtil.getCurrentTimeEpochMilli()).thenReturn(1563142796L);

        // When
        dataExtractionService.extract(scrapedDataMessage);

        // Then

        verify(extractedDataRepository, times(1)).save(extractedDataArgumentCaptor.capture());

        assertEquals("url", extractedDataArgumentCaptor.getValue().getUrl());
        assertEquals("value1", extractedDataArgumentCaptor.getValue().getExtractedData().get("key1"));
        assertEquals("value2", extractedDataArgumentCaptor.getValue().getExtractedData().get("key2"));
    }
}