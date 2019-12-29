package com.unloadbrain.assignement.takeaway.dataextractor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@ActiveProfiles("it")
public class ContextLoadIT {

    @Test
    public void contextLoads() {
    }
}
