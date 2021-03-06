package com.unloadbrain.assignement.takeaway.scraper.config;

import com.unloadbrain.assignement.takeaway.common.util.DateTimeUtil;
import com.unloadbrain.assignement.takeaway.scraper.util.JsoupUtil;
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

    @Bean
    public JsoupUtil jsoupUtil() {
        return new JsoupUtil();
    }
}
