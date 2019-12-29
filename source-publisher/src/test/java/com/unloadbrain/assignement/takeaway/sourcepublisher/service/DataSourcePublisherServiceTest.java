package com.unloadbrain.assignement.takeaway.sourcepublisher.service;

import com.unloadbrain.assignement.takeaway.common.exception.KafkaRecordProducerException;
import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import com.unloadbrain.assignement.takeaway.sourcepublisher.config.SourcesProperties;
import com.unloadbrain.assignement.takeaway.sourcepublisher.dto.message.DataSourceMessage;
import org.apache.kafka.clients.producer.RecordMetadata;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataSourcePublisherServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private KafkaTemplate kafkaTemplate;
    private SourcesProperties sourcesProperties;
    private DateTimeUtil dateTimeUtil;

    private DataSourcePublisherService dataSourcePublisherService;

    @Before
    public void setUp() {

        this.kafkaTemplate = mock(KafkaTemplate.class);
        this.sourcesProperties = mock(SourcesProperties.class);
        this.dateTimeUtil = mock(DateTimeUtil.class);
        this.dataSourcePublisherService
                = new DataSourcePublisherService(kafkaTemplate, sourcesProperties, dateTimeUtil);
    }

    @Test
    public void shouldSendKafkaMessage() {

        // Given

        ArgumentCaptor<DataSourceMessage> messageArgumentCaptor
                = ArgumentCaptor.forClass(DataSourceMessage.class);

        ArgumentCaptor<ListenableFutureCallback> callbackArgumentCaptor
                = ArgumentCaptor.forClass(ListenableFutureCallback.class);

        SourcesProperties.Source source = new SourcesProperties.Source();
        source.setUrl("url");
        source.setType("type");
        source.setMeta(Collections.singletonMap("metaKey1", "metaVal1"));
        when(sourcesProperties.getSources()).thenReturn(Collections.singletonList(source));

        ListenableFuture futureMock = mock(ListenableFuture.class);
        when(kafkaTemplate.send(anyString(), any(DataSourceMessage.class))).thenReturn(futureMock);

        when(dateTimeUtil.getCurrentTimeEpochMilli()).thenReturn(1563142796L);

        // When
        dataSourcePublisherService.publishSourcesToKafka();

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
        assertEquals("metaVal1", messageArgumentCaptor.getValue().getMeta().get("metaKey1"));
        assertEquals(1563142796L, messageArgumentCaptor.getValue().getUnixTimestamp());
    }

    @Test
    public void throwExceptionWhenMessageCouldNotBeSent() {

        // Given

        ArgumentCaptor<ListenableFutureCallback> callbackArgumentCaptor
                = ArgumentCaptor.forClass(ListenableFutureCallback.class);

        SourcesProperties.Source source = new SourcesProperties.Source();
        source.setUrl("url");
        source.setType("type");
        source.setMeta(Collections.singletonMap("metaKey1", "metaVal1"));
        when(sourcesProperties.getSources()).thenReturn(Collections.singletonList(source));

        ListenableFuture futureMock = mock(ListenableFuture.class);
        when(kafkaTemplate.send(anyString(), any(DataSourceMessage.class))).thenReturn(futureMock);

        thrown.expect(KafkaRecordProducerException.class);
        thrown.expectMessage("Source data could not be sent to Kafka.");

        // When
        dataSourcePublisherService.publishSourcesToKafka();

        // Then

        verify(futureMock).addCallback(callbackArgumentCaptor.capture());
        callbackArgumentCaptor.getValue().onFailure(mock(Throwable.class));
    }
}