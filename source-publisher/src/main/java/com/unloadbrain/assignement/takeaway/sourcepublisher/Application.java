package com.unloadbrain.assignement.takeaway.sourcepublisher;


import com.unloadbrain.assignement.takeaway.sourcepublisher.service.DataSourcePublisherService;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    /**
     * TODO: For test purpose, should be remove for production.
     */
    @RestController
    @AllArgsConstructor
    static class PublishDataSourceController {

        private DataSourcePublisherService dataSourcePublisherService;

        @RequestMapping(method = RequestMethod.GET, path = "/")
        public void publish() {
            dataSourcePublisherService.publishSourcesToKafka();
        }
    }
}

