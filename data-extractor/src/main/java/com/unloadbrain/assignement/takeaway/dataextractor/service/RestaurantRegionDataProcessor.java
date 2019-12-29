package com.unloadbrain.assignement.takeaway.dataextractor.service;

import com.unloadbrain.assignement.takeaway.dataextractor.dto.message.ScrapedDataMessage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RestaurantRegionDataProcessor extends DataProcessor<ScrapedDataMessage, Map<String, Object>> {

    public Map<String, Object> processor(ScrapedDataMessage scrapedDataMessage) {

        if (!"REGION_DATA".equals(scrapedDataMessage.getType())) {
            return Collections.emptyMap();
        }

        log.info("RestaurantRegionDataProcessor processing REGION_DATA data.");

        List<Map<String, String>> restaurantList = new ArrayList<>();

        Document doc = Jsoup.parse(scrapedDataMessage.getHtml());
        Elements restaurantMainBlock = doc.select(".restaurant[id^=irestaurant]");

        for (Element restaurantBlock : restaurantMainBlock) {

            Map<String, String> restaurantMap = buildRestaurantMap(scrapedDataMessage, restaurantBlock);

            if (restaurantMap.size() == 7) {
                restaurantList.add(restaurantMap);
            }
        }

        return Collections.singletonMap("restaurants", restaurantList);
    }

    private Map<String, String> buildRestaurantMap(ScrapedDataMessage scrapedDataMessage, Element restaurantBlock) {

        String baseUri = "https://www." + scrapedDataMessage.getMeta().get("domain");

        Map<String, String> restaurantMap = new HashMap<>();

        Element name = restaurantBlock.selectFirst(".detailswrapper a.restaurantname");
        Element link = restaurantBlock.selectFirst(".detailswrapper a.restaurantname[href]");
        Element deliveryCost = restaurantBlock.selectFirst(".detailswrapper .delivery .delivery-cost");
        Element averageDeliveryTimeInMinute = restaurantBlock.selectFirst(".detailswrapper .delivery .avgdeliverytime");
        Element address = restaurantBlock.selectFirst(".detailswrapper .pickup .address");
        Element cuisines = restaurantBlock.selectFirst(".detailswrapper .kitchens");
        Element numberOfReviews = restaurantBlock.selectFirst(".logowrapper .review-rating .rating-total");

        if (name != null) {
            restaurantMap.put("name", name.text());
        } else {
            publishErrorToPrometheus();
        }

        if (link != null) {
            link.setBaseUri(baseUri);
            String linkString = link.attr("abs:href");
            if (linkString != null && !linkString.isEmpty()) {
                restaurantMap.put("link", linkString);
            }
        } else {
            publishErrorToPrometheus();
        }

        if (deliveryCost != null) {
            restaurantMap.put("deliveryCost", deliveryCost
                    .text()
                    .replace("€", "")
                    .replace(",", ".")
                    .trim()
            );
        } else {
            publishErrorToPrometheus();
        }

        if (averageDeliveryTimeInMinute != null) {
            String deliveryTimeString = averageDeliveryTimeInMinute.text();
            if (!deliveryTimeString.contains("From")) {
                deliveryTimeString = deliveryTimeString.replaceAll("[^\\d]", "").trim();
            } else {
                deliveryTimeString = "-1";
            }
            restaurantMap.put("averageDeliveryTimeInMinute", deliveryTimeString.isEmpty() ? "-1" : deliveryTimeString);
        } else {
            publishErrorToPrometheus();
        }

        if (address != null) {
            restaurantMap.put("address", address.text());
        } else {
            publishErrorToPrometheus();
        }

        if (cuisines != null) {
            restaurantMap.put("cuisines", cuisines.text());
        } else {
            publishErrorToPrometheus();
        }

        if (numberOfReviews != null) {
            restaurantMap.put("numberOfReviews", numberOfReviews
                    .text()
                    .replace("(", "")
                    .replace(")", "")
            );
        } else {
            publishErrorToPrometheus();
        }

        return restaurantMap;
    }

    private void publishErrorToPrometheus() {
        // TODO: Handle error here.
    }

}
