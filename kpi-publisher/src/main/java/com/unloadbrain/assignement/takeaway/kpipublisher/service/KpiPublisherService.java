package com.unloadbrain.assignement.takeaway.kpipublisher.service;

import com.unloadbrain.assignement.takeaway.kpipublisher.domain.model.ExtractedData;
import com.unloadbrain.assignement.takeaway.kpipublisher.domain.repository.ExtractedDataRepository;
import com.unloadbrain.assignement.takeaway.kpipublisher.dto.message.DataExtractionNotificationMessage;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KpiPublisherService {

    private final ExtractedDataRepository extractedDataRepository;
    private final CollectorRegistry collectorRegistry;

    private Histogram deliveryCostDistributionCollector;
    private Gauge minimumDeliveryTimeCollector;
    private Gauge maximumDeliveryTimeCollector;
    private Gauge averageDeliveryTimeCollector;
    private Gauge numberOfZipCodesCollector;
    private Gauge cuisineWithMaximumReviewCollector;

    public KpiPublisherService(ExtractedDataRepository extractedDataRepository, CollectorRegistry collectorRegistry) {
        this.extractedDataRepository = extractedDataRepository;
        this.collectorRegistry = collectorRegistry;
    }

    @PostConstruct
    void init() {
        deliveryCostDistributionCollector = buildDeliveryCostDistributionCollector();
        minimumDeliveryTimeCollector = buildMinimumDeliveryTimeCollector();
        maximumDeliveryTimeCollector = buildMaximumDeliveryTimeCollector();
        averageDeliveryTimeCollector = buildAverageDeliveryTimeCollector();
        numberOfZipCodesCollector = buildNumberOfZipCodesCollector();
        cuisineWithMaximumReviewCollector = buildCuisineWithMaxReviewCollector();
        DefaultExports.initialize();
    }

    @KafkaListener(topics = "REGION_DATA", groupId = "kpi-publisher")
    public void publishKpi(DataExtractionNotificationMessage dataExtractionNotificationMessage) {

        log.info("Received message {} DataExtractionNotificationMessage topic for group 'kpi-publisher'",
                dataExtractionNotificationMessage);

        Optional<ExtractedData> extractedDataOptional = extractedDataRepository.findByUrl(dataExtractionNotificationMessage.getUrl());
        if (!extractedDataOptional.isPresent()) {
            return;
        }

        ExtractedData extractedData = extractedDataOptional.get();
        List<Map<String, Object>> restaurants
                = (List<Map<String, Object>>) extractedData.getExtractedData().get("restaurants");

        List<BigDecimal> deliveryCostDistribution = restaurants.stream()
                .map(map -> (String) map.get("deliveryCost"))
                .map(cost -> "FREE".equalsIgnoreCase(cost) ? "0" : cost)
                .map(cost -> new BigDecimal(cost))
                .collect(Collectors.toList());
        log.debug("deliveryCostDistribution = {}", deliveryCostDistribution);

        collectorRegistry.unregister(deliveryCostDistributionCollector);
        deliveryCostDistributionCollector = Histogram.build()
                .namespace("region1")
                .name("delivery_cost_distribution")
                .help("Delivery cost distribution on region1")
                .labelNames(restaurants.stream()
                        .map(map -> (String) map.get("name"))
                        .map(name -> name.replaceAll("[^A-Za-z]", ""))
                        .toArray(String[]::new))
                .register(collectorRegistry);
        deliveryCostDistributionCollector.labels(deliveryCostDistribution.stream()
                .map(deliveryCost -> deliveryCost.toString())
                .toArray(String[]::new));

        IntSummaryStatistics deliveryTimeStats = restaurants.stream()
                .map(map -> (String) map.get("averageDeliveryTimeInMinute"))
                .mapToInt(deliveryTime -> Integer.parseInt(deliveryTime))
                .filter(deliveryTime -> deliveryTime > 0)
                .summaryStatistics();
        log.debug("deliveryTimeStats = {}", deliveryTimeStats);

        collectorRegistry.unregister(minimumDeliveryTimeCollector);
        minimumDeliveryTimeCollector = buildMinimumDeliveryTimeCollector();
        minimumDeliveryTimeCollector.set(deliveryTimeStats.getMin());

        collectorRegistry.unregister(maximumDeliveryTimeCollector);
        maximumDeliveryTimeCollector = buildMaximumDeliveryTimeCollector();
        maximumDeliveryTimeCollector.set(deliveryTimeStats.getMax());

        collectorRegistry.unregister(averageDeliveryTimeCollector);
        averageDeliveryTimeCollector = buildAverageDeliveryTimeCollector();
        averageDeliveryTimeCollector.set(deliveryTimeStats.getAverage());

        long numberOfZipCodes = restaurants.stream()
                .map(map -> (String) map.get("address"))
                .map(address -> address.substring(0, address.lastIndexOf(" ")).trim())
                .map(address -> address.toLowerCase())
                .distinct()
                .count();
        log.debug("numberOfZipCodes = {}", numberOfZipCodes);

        collectorRegistry.unregister(numberOfZipCodesCollector);
        numberOfZipCodesCollector = buildNumberOfZipCodesCollector();
        numberOfZipCodesCollector.set(numberOfZipCodes);

        Map<String, Integer> cuisineReviewCountMap = new HashMap<>();
        for (Map<String, Object> restaurant : restaurants) {
            List<String> cuisineList = Arrays.asList(((String) restaurant.get("cuisines")).split("\\s*,\\s*"));
            for (String cuisine : cuisineList) {
                int review = Integer.parseInt((String) restaurant.get("numberOfReviews"));
                if (cuisineReviewCountMap.containsKey(cuisine)) {
                    cuisineReviewCountMap.put(cuisine, cuisineReviewCountMap.get(cuisine) + review);
                } else {
                    cuisineReviewCountMap.put(cuisine, review);
                }
            }
        }

        Optional<Map.Entry<String, Integer>> maxReviewEntry = cuisineReviewCountMap.entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue));
        String cuisineWithMaxReview = maxReviewEntry.get().getKey();
        log.debug("cuisineWithMaxReview = {}", cuisineWithMaxReview);

        collectorRegistry.unregister(cuisineWithMaximumReviewCollector);
        cuisineWithMaximumReviewCollector = buildCuisineWithMaxReviewCollector();
        cuisineWithMaximumReviewCollector.labels(cuisineWithMaxReview);

    }

    private Gauge buildCuisineWithMaxReviewCollector() {
        return Gauge.build()
                .namespace("region1")
                .name("cuisine_with_maximum_review")
                .help("Cuisine with maximum review")
                .labelNames("cuisine_with_maximum_review")
                .register(collectorRegistry);
    }

    private Gauge buildNumberOfZipCodesCollector() {
        return Gauge.build()
                .namespace("region1")
                .name("number_of_zip_codes")
                .help("Number of zip codes")
                .register(collectorRegistry);
    }

    private Gauge buildAverageDeliveryTimeCollector() {
        return Gauge.build()
                .namespace("region1")
                .name("average_delivery_time")
                .help("Average delivery time")
                .register(collectorRegistry);
    }

    private Gauge buildMaximumDeliveryTimeCollector() {
        return Gauge.build()
                .namespace("region1")
                .name("maximum_delivery_time")
                .help("Maximum delivery time")
                .register(collectorRegistry);
    }

    private Gauge buildMinimumDeliveryTimeCollector() {
        return Gauge.build()
                .namespace("region1")
                .name("minimum_delivery_time")
                .help("Minimum delivery time")
                .register(collectorRegistry);
    }

    private Histogram buildDeliveryCostDistributionCollector() {
        return Histogram.build()
                .namespace("region1")
                .name("delivery_cost_distribution")
                .help("Delivery cost distribution on region1")
                .register(collectorRegistry);
    }
}
