package com.unloadbrain.assignement.takeaway.sourcepublisher;

import com.unloadbrain.assignement.takeaway.sourcepublisher.config.SourcesProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@ActiveProfiles("it")
public class ContextLoadIT {

    @Test
    public void contextLoads() {
    }

    @Configuration
    static class TestConfig {

        @Primary
        @Bean
        public SourcesProperties sourcesProperties() {
            return mock(SourcesProperties.class);
        }
    }
}
