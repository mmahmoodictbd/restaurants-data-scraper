package com.unloadbrain.assignement.takeaway.kpipublisher.config;

import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides application related beans.
 */
@Configuration
@AllArgsConstructor
public class AppConfig {

    @Bean
    public DateTimeUtil dateTimeUtil() {
        return new DateTimeUtil();
    }

}
