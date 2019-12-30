package com.unloadbrain.assignement.takeaway.kpipublisher.config;

import com.sun.net.httpserver.HttpServer;
import com.unloadbrain.assignement.takeaway.common.exception.IORuntimeException;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Provides prometheus related beans.
 */
@Configuration
@AllArgsConstructor
public class PrometheusClientConfig {

    @Bean
    public CollectorRegistry collectorRegistry() {
        return new CollectorRegistry();
    }

    @Bean
    public void prometheusClientHttpServer() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", 8084), 3);
            new HTTPServer(httpServer, collectorRegistry(), true);
        } catch (IOException ex) {
            throw new IORuntimeException("Could not start Prometheus HTTPServer.", ex);
        }
    }
}
