package com.unloadbrain.assignement.takeaway.sourcepublisher.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
@Validated
@Component
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:sources.yml")
@ConfigurationProperties
@NoArgsConstructor
public class SourcesProperties {

    @NotEmpty
    private List<Source> sources;

    @Data
    @NoArgsConstructor
    public static class Source {

        @NotEmpty
        private String url;

        @NotEmpty
        private String type;

        private Map<String, String> meta;
    }
}