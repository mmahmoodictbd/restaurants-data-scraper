package com.unloadbrain.assignement.takeaway.kpipublisher.service;

import com.unloadbrain.assignement.takeaway.kpipublisher.domain.model.ExtractedData;
import com.unloadbrain.assignement.takeaway.kpipublisher.domain.repository.ExtractedDataRepository;
import com.unloadbrain.assignement.takeaway.kpipublisher.dto.message.DataExtractionNotificationMessage;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KpiPublisherServiceTest {

    private ExtractedDataRepository extractedDataRepository;
    private CollectorRegistry collectorRegistry;

    private KpiPublisherService kpiPublisherService;

    @Before
    public void setUp() {

        this.extractedDataRepository = mock(ExtractedDataRepository.class);
        this.collectorRegistry = new CollectorRegistry();
        this.kpiPublisherService = new KpiPublisherService(extractedDataRepository, collectorRegistry);
        this.kpiPublisherService.init();
    }

    @Test
    public void shouldPublishKpi() {

        // Given

        List<Map<String, Object>> restaurants = new ArrayList<>();

        Map<String, Object> restaurant1 = new HashMap<>();
        restaurant1.put("name", "Hello World");
        restaurant1.put("deliveryCost", "1.5");
        restaurant1.put("averageDeliveryTimeInMinute", "30");
        restaurant1.put("address", "Senecastraat 12");
        restaurant1.put("cuisines", "Thai, Maxican");
        restaurant1.put("numberOfReviews", "25");
        restaurants.add(restaurant1);
        Map<String, Object> restaurant2 = new HashMap<>();
        restaurant2.put("name", "Hello Amsterdam");
        restaurant2.put("deliveryCost", "2.0");
        restaurant2.put("averageDeliveryTimeInMinute", "20");
        restaurant2.put("address", "Jocobstraat 12");
        restaurant2.put("cuisines", "Maxican, Indian");
        restaurant2.put("numberOfReviews", "40");
        restaurants.add(restaurant2);

        DataExtractionNotificationMessage dataExtractionNotificationMessage = DataExtractionNotificationMessage.builder()
                .url("url")
                .dataExtractionNotificationTopic("any")
                .unixTimestamp(1563142796L)
                .meta(Collections.singletonMap("key1", "value1"))
                .build();


        ExtractedData extractedData = ExtractedData.builder()
                .url(dataExtractionNotificationMessage.getUrl())
                .extractedData(Collections.singletonMap("restaurants", restaurants))
                .build();
        when(extractedDataRepository.findByUrl(dataExtractionNotificationMessage.getUrl()))
                .thenReturn(Optional.of(extractedData));


        // When
        kpiPublisherService.publishKpi(dataExtractionNotificationMessage);

        // Then

        List<Collector.MetricFamilySamples> mfs = Collections.list(collectorRegistry.metricFamilySamples());
        assertEquals(6, mfs.size());

        for (Collector.MetricFamilySamples metricFamilySamples : Collections.list(collectorRegistry.metricFamilySamples())) {
            System.out.println("metricFamilySamples = " + metricFamilySamples);
            if ("region1_minimum_delivery_time".equals(metricFamilySamples.name)) {
                assertEquals(20, metricFamilySamples.samples.get(0).value, 0.001);
            } else if ("region1_maximum_delivery_time".equals(metricFamilySamples.name)) {
                assertEquals(30, metricFamilySamples.samples.get(0).value, 0.001);
            } else if ("region1_average_delivery_time".equals(metricFamilySamples.name)) {
                assertEquals(25, metricFamilySamples.samples.get(0).value, 0.001);
            } else if ("region1_cuisine_with_maximum_review".equals(metricFamilySamples.name)) {
                assertEquals("Maxican", metricFamilySamples.samples.get(0).labelValues.get(0));
            } else if ("number_of_zip_codes".equals(metricFamilySamples.name)) {
                assertEquals(2, metricFamilySamples.samples.get(0).value, 0.001);
            } else if ("region1_delivery_cost_distribution".equals(metricFamilySamples.name)) {
                assertEquals("HelloWorld", metricFamilySamples.samples.get(0).labelNames.get(0));
                assertEquals("HelloAmsterdam", metricFamilySamples.samples.get(0).labelNames.get(1));
                assertEquals("1.5", metricFamilySamples.samples.get(0).labelValues.get(0));
                assertEquals("2.0", metricFamilySamples.samples.get(0).labelValues.get(1));
            }
        }
    }
}