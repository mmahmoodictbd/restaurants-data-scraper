package com.unloadbrain.assignement.takeaway.scraper.service;

import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import com.unloadbrain.assignement.takeaway.scraper.dto.message.DataSourceMessage;
import com.unloadbrain.assignement.takeaway.scraper.dto.message.ScrapedDataMessage;
import com.unloadbrain.assignement.takeaway.scraper.util.JsoupUtil;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataScraperServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private KafkaTemplate kafkaTemplate;
    private DateTimeUtil dateTimeUtil;
    private JsoupUtil jsoupUtil;

    private DataScraperService dataScraperService;

    @Before
    public void setUp() {

        this.kafkaTemplate = mock(KafkaTemplate.class);
        this.dateTimeUtil = mock(DateTimeUtil.class);
        this.jsoupUtil = mock(JsoupUtil.class);
        this.dataScraperService = new DataScraperService(kafkaTemplate, dateTimeUtil, jsoupUtil);
    }

    @Test
    public void shouldScrapeAndPublishMessage() {

        // Given

        ArgumentCaptor<ScrapedDataMessage> messageArgumentCaptor
                = ArgumentCaptor.forClass(ScrapedDataMessage.class);

        ArgumentCaptor<ListenableFutureCallback> callbackArgumentCaptor
                = ArgumentCaptor.forClass(ListenableFutureCallback.class);

        Document document = mock(Document.class);
        when(document.html()).thenReturn("<html><body><p>Hello!</p></body></html>");
        when(jsoupUtil.fetch(any(), anyInt())).thenReturn(document);

        when(dateTimeUtil.getCurrentTimeEpochMilli()).thenReturn(1563142796L);

        ListenableFuture futureMock = mock(ListenableFuture.class);
        when(kafkaTemplate.send(anyString(), any(ScrapedDataMessage.class))).thenReturn(futureMock);

        DataSourceMessage dataSourceMessage = DataSourceMessage.builder()
                .url("url")
                .type("type")
                .meta(Collections.singletonMap("key1", "value1"))
                .build();

        // When
        dataScraperService.scrapeAndPublishToKafka(dataSourceMessage);

        // Then

        verify(futureMock).addCallback(callbackArgumentCaptor.capture());

        RecordMetadata recordMetadata = mock(RecordMetadata.class);
        when(recordMetadata.offset()).thenReturn(1L);
        SendResult sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        callbackArgumentCaptor.getValue().onSuccess(sendResult);

        verify(kafkaTemplate, times(1)).send(any(), messageArgumentCaptor.capture());

        assertEquals("url", messageArgumentCaptor.getValue().getUrl());
        assertEquals("type", messageArgumentCaptor.getValue().getType());
        assertEquals("value1", messageArgumentCaptor.getValue().getMeta().get("key1"));
        assertEquals("<html><body><p>Hello!</p></body></html>", messageArgumentCaptor.getValue().getHtml());
        assertEquals(1563142796L, messageArgumentCaptor.getValue().getUnixTimestamp());
    }
}